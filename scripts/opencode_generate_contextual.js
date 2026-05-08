#!/usr/bin/env node
// 基于现有代码的上下文感知重构
// 读取项目现有代码，分析结构，基于其生成增量代码
// Usage:
// CONTEXT_MODE=true node scripts/opencode_generate_contextual.js --issue-number 42 --issue-body "添加微信登录" --repo owner/repo --token xxx

const fs = require('fs');
const path = require('path');
const https = require('https');

// GitHub API 配置
const GITHUB_API = 'https://api.github.com';
const RAW_GITHUB = 'https://raw.githubusercontent.com';

// 语言配置
const LANGUAGE_CONFIG = {
  java: {
    ext: '.java',
    srcDir: 'src/main/java',
    indicators: ['.java', 'spring', 'pom.xml', 'build.gradle']
  },
  python: {
    ext: '.py',
    srcDir: '',
    indicators: ['.py', 'requirements.txt', 'setup.py']
  },
  typescript: {
    ext: '.ts',
    srcDir: 'src',
    indicators: ['.ts', 'package.json', 'tsconfig.json']
  }
};

// 调用 GitHub API
async function githubAPI(endpoint, token) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: 'api.github.com',
      path: endpoint,
      method: 'GET',
      headers: {
        'Authorization': `token ${token}`,
        'Accept': 'application/vnd.github.v3+json',
        'User-Agent': 'OpenCode-Context-Generator'
      },
      timeout: 30000
    };

    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => {
        try {
          const json = JSON.parse(data);
          if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(json);
          } else {
            reject(new Error(`GitHub API Error: ${json.message || res.statusCode}`));
          }
        } catch (e) {
          reject(new Error(`Parse error: ${e.message}`));
        }
      });
    });

    req.on('error', reject);
    req.on('timeout', () => { req.destroy(); reject(new Error('Timeout')); });
    req.end();
  });
}

// 获取仓库文件树
async function getRepoTree(owner, repo, token, branch = 'main') {
  try {
    // 获取最新 commit
    const ref = await githubAPI(`/repos/${owner}/${repo}/git/ref/heads/${branch}`, token);
    const treeSha = ref.object.sha;
    
    // 获取文件树
    const tree = await githubAPI(`/repos/${owner}/${repo}/git/trees/${treeSha}?recursive=1`, token);
    return tree.tree || [];
  } catch (error) {
    console.error(`❌ Failed to get repo tree: ${error.message}`);
    return [];
  }
}

// 读取文件内容
async function getFileContent(owner, repo, path, token) {
  try {
    const content = await githubAPI(`/repos/${owner}/${repo}/contents/${path}`, token);
    if (content.content) {
      return Buffer.from(content.content, 'base64').toString('utf8');
    }
    return null;
  } catch (error) {
    console.error(`❌ Failed to read ${path}: ${error.message}`);
    return null;
  }
}

// 检测项目语言
function detectProjectLanguage(files) {
  const filePaths = files.map(f => f.path.toLowerCase());
  
  // Java 检测
  if (filePaths.some(p => p.includes('.java') || p.includes('pom.xml') || p.includes('build.gradle'))) {
    return 'java';
  }
  
  // Python 检测
  if (filePaths.some(p => p.includes('.py') || p.includes('requirements.txt'))) {
    return 'python';
  }
  
  // TypeScript 检测
  if (filePaths.some(p => p.includes('.ts') && !p.includes('.d.ts'))) {
    return 'typescript';
  }
  
  return 'java';
}

// 关键词匹配找到相关文件
function findRelevantFiles(files, issueBody) {
  const keywords = extractKeywords(issueBody);
  console.log(`\n🔍 提取的关键词: ${keywords.join(', ')}`);
  
  const codeExtensions = ['.java', '.py', '.ts', '.js', '.go', '.rs'];
  
  // 评分匹配
  const scoredFiles = files
    .filter(f => codeExtensions.some(ext => f.path.endsWith(ext)))
    .map(f => {
      let score = 0;
      const path = f.path.toLowerCase();
      
      // 文件名匹配
      for (const kw of keywords) {
        if (path.includes(kw.toLowerCase())) {
          score += 10;
        }
      }
      
      // 路径匹配
      for (const kw of keywords) {
        if (f.path.toLowerCase().includes(kw.toLowerCase())) {
          score += 5;
        }
      }
      
      // 优先选择核心文件（Controller, Service 等）
      if (path.includes('controller') || path.includes('service')) {
        score += 3;
      }
      
      return { ...f, score };
    })
    .filter(f => f.score > 0)
    .sort((a, b) => b.score - a.score)
    .slice(0, 5); // 最多 5 个相关文件
  
  return scoredFiles;
}

// 提取关键词
function extractKeywords(issueBody) {
  const text = issueBody.toLowerCase();
  const keywords = [];
  
  // 常见业务关键词
  const patterns = [
    /user/g, /login/g, /auth/g, /register/g,
    /order/g, /product/g, /payment/g,
    /admin/g, /role/g, /permission/g,
    /wechat/g, /wx/g, /oauth/g, /sso/g,
    /profile/g, /account/g, /password/g,
    /email/g, /sms/g, /notification/g,
    /file/g, /upload/g, /image/g,
    /search/g, /filter/g, /sort/g
  ];
  
  for (const pattern of patterns) {
    const matches = text.match(pattern);
    if (matches) {
      keywords.push(matches[0]);
    }
  }
  
  // 去重
  return [...new Set(keywords)];
}

// 压缩代码内容（限制 token）
function compressCode(content, maxLines = 100) {
  const lines = content.split('\n');
  
  // 保留关键部分
  const imports = lines.filter(l => l.match(/^(import|package|from|using)/));
  const classDef = lines.find(l => l.match(/^(public\s+)?(class|interface|enum)/));
  const methods = [];
  let inMethod = false;
  let methodLines = [];
  
  for (const line of lines) {
    if (line.match(/^(public|private|protected)\s+.*\(/) && !line.includes('class')) {
      inMethod = true;
      methodLines = [line];
    } else if (inMethod) {
      methodLines.push(line);
      if (line === '    }' || line === '}') {
        methods.push(methodLines.join('\n'));
        inMethod = false;
        if (methods.length >= 5) break; // 最多 5 个方法
      }
    }
  }
  
  return [...imports, classDef || '', ...methods].join('\n');
}

// 调用 DeepSeek API
async function callDeepSeekAPI(messages, apiKey) {
  return new Promise((resolve, reject) => {
    const requestData = JSON.stringify({
      model: 'deepseek-coder',
      messages: messages,
      temperature: 0.3,
      max_tokens: 4000,
      stream: false
    });

    const options = {
      hostname: 'api.deepseek.com',
      port: 443,
      path: '/v1/chat/completions',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${apiKey}`,
        'Content-Length': Buffer.byteLength(requestData)
      },
      timeout: 180000
    };

    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => {
        try {
          const response = JSON.parse(data);
          if (response.error) {
            reject(new Error(`API Error: ${response.error.message}`));
          } else if (response.choices && response.choices[0]) {
            resolve(response.choices[0].message.content);
          } else {
            reject(new Error('Invalid response'));
          }
        } catch (e) {
          reject(new Error(`Parse error: ${e.message}`));
        }
      });
    });

    req.on('error', reject);
    req.on('timeout', () => { req.destroy(); reject(new Error('Timeout')); });
    req.write(requestData);
    req.end();
  });
}

// 基于上下文生成代码
async function generateContextualCode(issueBody, existingFiles, language, apiKey) {
  const langName = language.toUpperCase();
  
  // 构建上下文提示词
  let contextPrompt = `Based on the following existing ${langName} code:\n\n`;
  
  for (const file of existingFiles) {
    contextPrompt += `===== FILE: ${file.path} =====\n`;
    contextPrompt += file.content.substring(0, 3000); // 限制每个文件内容
    contextPrompt += '\n\n';
  }
  
  contextPrompt += `\n===== NEW REQUIREMENT =====\n${issueBody}\n\n`;
  contextPrompt += `Generate code that:\n`;
  contextPrompt += `1. Follows the existing code style and patterns\n`;
  contextPrompt += `2. Uses the same package/namespace structure\n`;
  contextPrompt += `3. Integrates with existing classes/interfaces\n`;
  contextPrompt += `4. Includes proper imports\n`;
  contextPrompt += `5. Is production-ready\n\n`;
  contextPrompt += `Output format:\n`;
  contextPrompt += `- If modifying existing file: "===== MODIFY: filepath =====" followed by complete new content\n`;
  contextPrompt += `- If creating new file: "===== CREATE: filepath =====" followed by content\n`;
  
  const messages = [
    {
      role: 'system',
      content: `You are an expert ${langName} developer specializing in incremental feature development.`
    },
    {
      role: 'user',
      content: contextPrompt
    }
  ];
  
  return await callDeepSeekAPI(messages, apiKey);
}

// 解析生成的代码变更
function parseCodeChanges(content) {
  const changes = {
    modify: [],
    create: []
  };
  
  const modifyPattern = /=====\s*MODIFY:\s*(.+?)\s*=====\n?([\s\S]*?)(?=====|$)/g;
  const createPattern = /=====\s*CREATE:\s*(.+?)\s*=====\n?([\s\S]*?)(?=====|$)/g;
  
  let match;
  while ((match = modifyPattern.exec(content)) !== null) {
    changes.modify.push({
      path: match[1].trim(),
      content: match[2].trim()
    });
  }
  
  while ((match = createPattern.exec(content)) !== null) {
    changes.create.push({
      path: match[1].trim(),
      content: match[2].trim()
    });
  }
  
  return changes;
}

// 主函数
(async () => {
  const args = {};
  for (let i = 2; i < process.argv.length; i++) {
    const a = process.argv[i];
    if (a.startsWith('--')) {
      const key = a.substring(2);
      const val = (i + 1) < process.argv.length && !process.argv[i + 1].startsWith('--') ? process.argv[i + 1] : 'true';
      args[key] = val;
    }
  }
  
  const issueNumber = parseInt(args['issue-number'], 10) || 0;
  const issueBody = args['issue-body'] || '';
  const repo = args['repo'] || '';
  const token = args['token'] || process.env.GITHUB_TOKEN || '';
  const outDir = args['outdir'] || path.resolve('auto_impl');
  const baseBranch = args['base'] || 'main';
  
  if (!issueNumber || !repo || !token) {
    console.error('❌ Error: Missing required arguments');
    process.exit(1);
  }
  
  const apiKey = process.env.DEEPSEEK_API_KEY;
  if (!apiKey) {
    console.error('❌ Error: DEEPSEEK_API_KEY not set');
    process.exit(1);
  }
  
  const [owner, repoName] = repo.split('/');
  
  console.log('\n========================================');
  console.log('🧠 基于上下文的代码重构');
  console.log(`📋 Issue #${issueNumber}`);
  console.log(`📁 仓库: ${repo}`);
  console.log('========================================\n');
  
  // 步骤 1: 获取仓库文件树
  console.log('[Step 1/4] 扫描仓库文件...');
  const files = await getRepoTree(owner, repoName, token, baseBranch);
  
  if (files.length === 0) {
    console.error('❌ 无法读取仓库文件，回退到普通模式');
    process.exit(1);
  }
  
  console.log(`✅ 发现 ${files.length} 个文件`);
  
  // 步骤 2: 检测语言和找相关文件
  console.log('\n[Step 2/4] 分析项目结构和相关文件...');
  const language = detectProjectLanguage(files);
  console.log(`🔤 项目语言: ${language.toUpperCase()}`);
  
  const relevantFiles = findRelevantFiles(files, issueBody);
  console.log(`✅ 找到 ${relevantFiles.length} 个相关文件:`);
  relevantFiles.forEach(f => console.log(`   - ${f.path} (相关度: ${f.score})`));
  
  // 步骤 3: 读取文件内容
  console.log('\n[Step 3/4] 读取相关文件内容...');
  const fileContents = [];
  
  for (const file of relevantFiles) {
    console.log(`   读取: ${file.path}...`);
    const content = await getFileContent(owner, repoName, file.path, token);
    if (content) {
      fileContents.push({
        path: file.path,
        content: compressCode(content, 80)
      });
    }
  }
  
  console.log(`✅ 成功读取 ${fileContents.length} 个文件`);
  
  // 步骤 4: 基于上下文生成代码
  console.log('\n[Step 4/4] 基于现有代码生成增量代码...');
  const generatedCode = await generateContextualCode(
    issueBody,
    fileContents,
    language,
    apiKey
  );
  
  // 解析变更
  const changes = parseCodeChanges(generatedCode);
  
  console.log(`\n📊 代码变更分析:`);
  console.log(`   修改文件: ${changes.modify.length} 个`);
  console.log(`   新增文件: ${changes.create.length} 个`);
  
  // 创建输出目录
  const issueDir = path.join(outDir, `issue-${issueNumber}`);
  fs.mkdirSync(issueDir, { recursive: true });
  
  // 保存修改的文件
  for (const change of changes.modify) {
    const filePath = path.join(issueDir, change.path);
    fs.mkdirSync(path.dirname(filePath), { recursive: true });
    fs.writeFileSync(filePath, change.content, 'utf8');
    console.log(`   ✏️  修改: ${change.path}`);
  }
  
  // 保存新增的文件
  for (const change of changes.create) {
    const filePath = path.join(issueDir, change.path);
    fs.mkdirSync(path.dirname(filePath), { recursive: true });
    fs.writeFileSync(filePath, change.content, 'utf8');
    console.log(`   ➕ 新增: ${change.path}`);
  }
  
  // 生成变更说明
  const changesDoc = `# Issue #${issueNumber} - 基于上下文的代码重构

## 分析过程
1. 扫描仓库发现 ${files.length} 个文件
2. 识别项目语言: ${language.toUpperCase()}
3. 找到 ${relevantFiles.length} 个相关文件
4. 读取并分析现有代码结构
5. 基于现有代码生成增量变更

## 相关文件
${relevantFiles.map(f => `- ${f.path} (相关度: ${f.score})`).join('\n')}

## 代码变更

### 修改的文件 (${changes.modify.length})
${changes.modify.map(c => `- ${c.path}`).join('\n') || '无'}

### 新增的文件 (${changes.create.length})
${changes.create.map(c => `- ${c.path}`).join('\n') || '无'}

## 变更说明
基于现有代码风格和架构生成的增量代码。
保持了原有项目的命名规范、包结构和设计模式。

---
*Generated by Context-Aware Code Generator*
`;
  
  fs.writeFileSync(path.join(issueDir, 'CHANGES.md'), changesDoc, 'utf8');
  
  const totalFiles = changes.modify.length + changes.create.length;
  
  console.log('\n========================================');
  console.log('✅ 上下文感知代码生成完成！');
  console.log(`📊 修改: ${changes.modify.length} 个文件`);
  console.log(`📊 新增: ${changes.create.length} 个文件`);
  console.log(`📁 总计: ${totalFiles} 个文件`);
  console.log(`📂 输出目录: ${issueDir}`);
  console.log('========================================\n');
  
  // 输出变更列表（供 Python 脚本使用）
  const output = {
    modify: changes.modify.map(c => c.path),
    create: changes.create.map(c => c.path),
    total: totalFiles,
    language: language
  };
  
  fs.writeFileSync(path.join(issueDir, '.context_output.json'), JSON.stringify(output, null, 2));
  
  process.exit(0);
  
})().catch(error => {
  console.error('\n❌ 错误:', error.message);
  process.exit(1);
});
