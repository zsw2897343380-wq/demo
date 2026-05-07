#!/usr/bin/env node
// 智能模块化代码生成 - 强制多文件包结构
// 根据需求自动选择最合适的编程语言
// Usage:
// DEEPSEEK_API_KEY=xxx node scripts/opencode_generate_modular.js --issue-number 42 --issue-body "<text>" --outdir ./auto_impl/issue-42

const fs = require('fs');
const path = require('path');
const https = require('https');

// 语言配置 - 更详细的配置
const LANGUAGE_CONFIG = {
  java: {
    ext: '.java',
    srcDir: 'src/main/java',
    testDir: 'src/test/java',
    packageSeparator: '.',
    commentStyle: 'block',
    defaultPackage: 'com.example.project',
    namingConvention: 'PascalCase',
    indicators: ['java', 'spring', 'springboot', 'maven', 'gradle', 'jdbc', 'mybatis', 'jpa', 'servlet', 'jsp'],
    fileNaming: (className) => `${className}.java`
  },
  python: {
    ext: '.py',
    srcDir: 'src',
    testDir: 'tests',
    packageSeparator: '.',
    commentStyle: 'hash',
    defaultPackage: 'src',
    namingConvention: 'snake_case',
    indicators: ['python', 'django', 'flask', 'fastapi', 'pandas', 'numpy', 'py'],
    fileNaming: (moduleName) => `${toSnakeCase(moduleName)}.py`
  },
  typescript: {
    ext: '.ts',
    srcDir: 'src',
    testDir: 'tests',
    packageSeparator: '/',
    commentStyle: 'block',
    defaultPackage: '',
    namingConvention: 'PascalCase',
    indicators: ['typescript', 'ts', 'angular', 'react', 'vue', 'nestjs', 'typeorm', 'deno'],
    fileNaming: (name) => `${toKebabCase(name)}.ts`
  },
  javascript: {
    ext: '.js',
    srcDir: 'src',
    testDir: 'tests',
    packageSeparator: '/',
    commentStyle: 'block',
    defaultPackage: '',
    namingConvention: 'camelCase',
    indicators: ['javascript', 'js', 'node', 'nodejs', 'express', 'vue', 'react'],
    fileNaming: (name) => `${toKebabCase(name)}.js`
  },
  go: {
    ext: '.go',
    srcDir: '',
    testDir: '',
    packageSeparator: '/',
    commentStyle: 'block',
    defaultPackage: 'main',
    namingConvention: 'PascalCase',
    indicators: ['go', 'golang', 'gin', 'beego', 'echo'],
    fileNaming: (name) => `${toSnakeCase(name)}.go`
  },
  rust: {
    ext: '.rs',
    srcDir: 'src',
    testDir: 'tests',
    packageSeparator: '::',
    commentStyle: 'block',
    defaultPackage: 'crate',
    namingConvention: 'snake_case',
    indicators: ['rust', 'cargo', 'actix', 'rocket'],
    fileNaming: (name) => `${toSnakeCase(name)}.rs`
  }
};

// 字符串转换工具
function toSnakeCase(str) {
  return str.replace(/[A-Z]/g, letter => `_${letter.toLowerCase()}`)
            .replace(/^_/, '')
            .replace(/\s+/g, '_')
            .toLowerCase();
}

function toKebabCase(str) {
  return str.replace(/[A-Z]/g, letter => `-${letter.toLowerCase()}`)
            .replace(/^-/, '')
            .replace(/\s+/g, '-')
            .toLowerCase();
}

function toPascalCase(str) {
  return str.replace(/(?:^\w|[A-Z]|\b\w)/g, (word, index) => 
    word.toUpperCase()
  ).replace(/\s+/g, '');
}

// 智能语言检测
function detectLanguageSmart(issueBody) {
  const body = issueBody.toLowerCase();
  
  // 定义语言权重
  const scores = {};
  
  for (const [lang, config] of Object.entries(LANGUAGE_CONFIG)) {
    scores[lang] = 0;
    for (const indicator of config.indicators) {
      if (body.includes(indicator.toLowerCase())) {
        scores[lang] += 1;
      }
    }
  }
  
  // 特定关键词加分
  if (body.includes('jwt') && body.includes('spring')) scores['java'] += 3;
  if (body.includes('spring boot')) scores['java'] += 5;
  if (body.includes('django') || body.includes('flask')) scores['python'] += 5;
  if (body.includes('fastapi')) scores['python'] += 3;
  if (body.includes('nestjs')) scores['typescript'] += 5;
  if (body.includes('gin') || body.includes('beego')) scores['go'] += 5;
  if (body.includes('actix') || body.includes('rocket')) scores['rust'] += 5;
  
  // 电商系统默认推荐 Java
  if (body.includes('电商') || body.includes('订单') || body.includes('支付')) {
    if (scores['java'] >= 0) scores['java'] += 2; // 如果没有明确指定其他语言，优先Java
  }
  
  // 找到最高分
  let bestLang = 'java'; // 默认Java
  let maxScore = -1;
  
  for (const [lang, score] of Object.entries(scores)) {
    if (score > maxScore) {
      maxScore = score;
      bestLang = lang;
    }
  }
  
  console.log(`[Language Detection] Scores:`, scores);
  console.log(`[Language Detection] Selected: ${bestLang} (score: ${maxScore})`);
  
  return bestLang;
}

// 第一阶段：分析需求并确定架构
async function analyzeArchitecture(issueNumber, issueBody, apiKey) {
  return new Promise((resolve, reject) => {
    const promptText = `作为资深软件架构师，请分析以下需求并设计系统架构。

Issue #${issueNumber}:
${issueBody}

请输出以下格式的架构设计：

## 推荐技术栈
- 编程语言：（根据需求选择最合适的，考虑：Java适合企业级、Python适合快速开发、Go适合高性能、TypeScript适合全栈）
- 框架/库：
- 数据库：

## 模块划分
列出 3-6 个核心模块，每个模块包含：
1. 模块名称
2. 模块职责（简短描述）
3. 包含的类/文件（列出 2-4 个具体文件名，使用驼峰命名）
4. 依赖的其他模块

## 项目结构
建议使用什么样的目录/包结构来组织代码（给出具体的包名示例，如 com.example.ecommerce.user）

请使用中文回答，保持专业和简洁。`;

    const requestData = JSON.stringify({
      model: 'deepseek-chat',
      messages: [
        {
          role: 'system',
          content: '你是资深软件架构师，擅长系统架构设计和模块化分解。请根据需求选择最合适的技术栈，并设计清晰的项目结构。'
        },
        {
          role: 'user',
          content: promptText
        }
      ],
      temperature: 0.7,
      max_tokens: 2500,
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
      timeout: 120000
    };

    console.log('[Phase 1/3] 分析架构并确定技术栈...');

    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => {
        try {
          const response = JSON.parse(data);
          if (response.error) {
            reject(new Error(`DeepSeek API Error: ${response.error.message}`));
            return;
          }
          if (response.choices && response.choices[0] && response.choices[0].message) {
            resolve(response.choices[0].message.content);
          } else {
            reject(new Error('Unexpected API response format'));
          }
        } catch (error) {
          reject(new Error(`Failed to parse API response: ${error.message}`));
        }
      });
    });

    req.on('error', (error) => reject(new Error(`Request failed: ${error.message}`)));
    req.on('timeout', () => { req.destroy(); reject(new Error('Request timeout')); });
    req.write(requestData);
    req.end();
  });
}

// 第二阶段：为每个模块生成完整的代码文件
async function generateModuleFiles(moduleName, moduleDesc, architecture, issueBody, language, apiKey) {
  return new Promise((resolve, reject) => {
    const langConfig = LANGUAGE_CONFIG[language] || LANGUAGE_CONFIG.java;
    
    const promptText = `请为以下模块生成完整的 ${language.toUpperCase()} 代码。

项目架构：
${architecture}

当前模块：${moduleName}
模块描述：${moduleDesc}

要求：
1. 生成 2-4 个完整的代码文件（如 Controller/Service/Repository/Model）
2. 每个文件都必须包含完整的类/函数定义
3. 包含必要的导入语句
4. 添加清晰的注释
5. 文件之间要有合理的依赖关系
6. 遵循 ${language} 的命名规范和最佳实践
7. 代码必须是生产级别的，包含错误处理

请按以下格式输出，每个文件之间用 "---FILE: 文件名---" 分隔：

---FILE: UserController.java---
[代码内容]

---FILE: UserService.java---
[代码内容]

直接输出代码，不要包含 markdown 代码块标记。`;

    const requestData = JSON.stringify({
      model: 'deepseek-coder',
      messages: [
        {
          role: 'system',
          content: `你是资深的 ${language} 开发专家。请生成多个完整的、可直接运行的代码文件，每个文件之间用特定分隔符分隔。`
        },
        {
          role: 'user',
          content: promptText
        }
      ],
      temperature: 0.5,
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
      timeout: 180000 // 3分钟超时
    };

    console.log(`  生成模块 "${moduleName}" 的代码文件...`);

    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => {
        try {
          const response = JSON.parse(data);
          if (response.error) {
            reject(new Error(`DeepSeek API Error: ${response.error.message}`));
            return;
          }
          if (response.choices && response.choices[0] && response.choices[0].message) {
            resolve(response.choices[0].message.content);
          } else {
            reject(new Error('Unexpected API response format'));
          }
        } catch (error) {
          reject(new Error(`Failed to parse API response: ${error.message}`));
        }
      });
    });

    req.on('error', (error) => reject(new Error(`Request failed: ${error.message}`)));
    req.on('timeout', () => { req.destroy(); reject(new Error('Request timeout')); });
    req.write(requestData);
    req.end();
  });
}

// 解析生成的代码，分离多个文件
function parseGeneratedFiles(content, language) {
  const files = [];
  const langConfig = LANGUAGE_CONFIG[language] || LANGUAGE_CONFIG.java;
  
  // 按分隔符分割
  const filePattern = /---FILE:\s*(.+?)---\n?([\s\S]*?)(?=---FILE:|$)/g;
  let match;
  
  while ((match = filePattern.exec(content)) !== null) {
    let fileName = match[1].trim();
    let fileContent = match[2].trim();
    
    // 确保文件名有正确的扩展名
    if (!fileName.endsWith(langConfig.ext)) {
      fileName += langConfig.ext;
    }
    
    // 清理 markdown 代码块标记
    fileContent = fileContent.replace(/^```\w*\n?/, '').replace(/```\s*$/, '');
    
    files.push({
      name: fileName,
      content: fileContent
    });
  }
  
  // 如果没有解析到文件，尝试其他方式
  if (files.length === 0) {
    // 尝试按类/接口定义分割
    const classPattern = new RegExp(
      `(public\s+)?(class|interface|enum)\s+(\\w+)\\s*[^{]*{`,
      'g'
    );
    
    let lastIndex = 0;
    let lastClassName = '';
    
    while ((match = classPattern.exec(content)) !== null) {
      if (lastClassName) {
        const classContent = content.substring(lastIndex, match.index).trim();
        files.push({
          name: langConfig.fileNaming(lastClassName),
          content: classContent
        });
      }
      lastClassName = match[3];
      lastIndex = match.index;
    }
    
    // 添加最后一个类
    if (lastClassName && lastIndex < content.length) {
      const classContent = content.substring(lastIndex).trim();
      files.push({
        name: langConfig.fileNaming(lastClassName),
        content: classContent
      });
    }
  }
  
  // 如果还是没解析到，将整个内容作为一个文件
  if (files.length === 0 && content.trim()) {
    // 尝试提取类名
    const classMatch = content.match(/(?:public\s+)?class\s+(\w+)/);
    const fileName = classMatch 
      ? langConfig.fileNaming(classMatch[1])
      : `Main${langConfig.ext}`;
    
    files.push({
      name: fileName,
      content: content.replace(/^```\w*\n?/, '').replace(/```\s*$/, '')
    });
  }
  
  return files;
}

// 从架构分析中提取模块列表
function extractModules(architecture) {
  const modules = [];
  const lines = architecture.split('\n');
  
  let currentModule = null;
  let inModuleSection = false;
  
  for (const line of lines) {
    // 检测模块开始
    if (line.match(/^##?\s*(模块|Module)/i)) {
      inModuleSection = true;
      continue;
    }
    
    // 检测模块名称
    const moduleMatch = line.match(/^\d+\.\s*(.+)/) || 
                       line.match(/^-\s*(.+)/) ||
                       line.match(/^###?\s*(.+)/);
    
    if (inModuleSection && moduleMatch && !line.includes('：') && !line.includes(':')) {
      if (currentModule) {
        modules.push(currentModule);
      }
      currentModule = {
        name: moduleMatch[1].replace(/[（(].*?[)）]/g, '').trim(),
        description: ''
      };
    } else if (currentModule && line.includes('职责')) {
      currentModule.description = line.replace(/.*[：:]\s*/, '').trim();
    } else if (currentModule && line.trim() && !line.startsWith('#')) {
      currentModule.description += ' ' + line.trim();
    }
  }
  
  if (currentModule) {
    modules.push(currentModule);
  }
  
  // 如果没解析到模块，创建默认模块
  if (modules.length === 0) {
    modules.push(
      { name: 'Controller', description: '控制器层' },
      { name: 'Service', description: '业务逻辑层' },
      { name: 'Repository', description: '数据访问层' }
    );
  }
  
  return modules.slice(0, 6); // 最多6个模块
}

// 提取推荐的语言
function extractRecommendedLanguage(architecture) {
  const lowerArch = architecture.toLowerCase();
  
  if (lowerArch.includes('java')) return 'java';
  if (lowerArch.includes('python')) return 'python';
  if (lowerArch.includes('typescript') || lowerArch.includes('ts')) return 'typescript';
  if (lowerArch.includes('javascript') || lowerArch.includes('js')) return 'javascript';
  if (lowerArch.includes('go') || lowerArch.includes('golang')) return 'go';
  if (lowerArch.includes('rust')) return 'rust';
  
  return null;
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
  const outDir = args['outdir'] || path.resolve('auto_impl');
  const forcedLanguage = args['language'] || 'auto';
  
  if (!issueNumber) {
    console.error('❌ Error: --issue-number is required');
    process.exit(1);
  }
  
  const apiKey = process.env.DEEPSEEK_API_KEY;
  if (!apiKey) {
    console.error('❌ Error: DEEPSEEK_API_KEY environment variable is not set');
    process.exit(1);
  }
  
  try {
    console.log('\n' + '='.repeat(70));
    console.log('🏗️  模块化代码生成');
    console.log(`📋 Issue #${issueNumber}`);
    console.log(`📁 输出目录: ${outDir}`);
    console.log('='.repeat(70) + '\n');
    
    // 步骤 1: 检测语言
    let language;
    if (forcedLanguage !== 'auto') {
      language = forcedLanguage;
      console.log(`🔤 强制使用语言: ${language.toUpperCase()}`);
    } else {
      language = detectLanguageSmart(issueBody);
      console.log(`🔤 智能检测语言: ${language.toUpperCase()}`);
    }
    
    const langConfig = LANGUAGE_CONFIG[language];
    console.log(`   扩展名: ${langConfig.ext}`);
    console.log(`   源目录: ${langConfig.srcDir || '项目根目录'}`);
    console.log();
    
    // 步骤 2: 架构分析
    console.log('[Phase 1/3] 分析架构...');
    const architecture = await analyzeArchitecture(issueNumber, issueBody, apiKey);
    
    // 保存架构设计
    fs.mkdirSync(outDir, { recursive: true });
    fs.writeFileSync(path.join(outDir, 'ARCHITECTURE.md'), architecture, 'utf8');
    console.log('✅ 架构设计已保存\n');
    
    // 检查架构中推荐的语言
    const archLanguage = extractRecommendedLanguage(architecture);
    if (archLanguage && archLanguage !== language && forcedLanguage === 'auto') {
      console.log(`📝 架构推荐使用: ${archLanguage.toUpperCase()}，切换中...\n`);
      language = archLanguage;
    }
    
    // 步骤 3: 提取模块
    const modules = extractModules(architecture);
    console.log(`[Phase 2/3] 识别到 ${modules.length} 个模块:`);
    modules.forEach((m, i) => {
      console.log(`  ${i + 1}. ${m.name}`);
      if (m.description) console.log(`     ${m.description}`);
    });
    console.log();
    
    // 步骤 4: 为每个模块生成代码文件
    console.log('[Phase 3/3] 生成代码文件...\n');
    
    const generatedFiles = [];
    const basePackage = `${langConfig.defaultPackage}.issue${issueNumber}`;
    
    for (let i = 0; i < modules.length; i++) {
      const module = modules[i];
      console.log(`[${i + 1}/${modules.length}] ${module.name}`);
      
      try {
        const generatedContent = await generateModuleFiles(
          module.name,
          module.description || module.name,
          architecture,
          issueBody,
          language,
          apiKey
        );
        
        // 解析生成的多个文件
        const files = parseGeneratedFiles(generatedContent, language);
        
        // 创建模块目录
        const modulePackage = module.name.toLowerCase().replace(/\s+/g, '');
        const packagePath = langConfig.srcDir
          ? path.join(outDir, langConfig.srcDir, ...basePackage.split('.'), modulePackage)
          : path.join(outDir, modulePackage);
        
        fs.mkdirSync(packagePath, { recursive: true });
        
        // 保存每个文件
        for (const file of files) {
          const filePath = path.join(packagePath, file.name);
          fs.writeFileSync(filePath, file.content, 'utf8');
          generatedFiles.push(filePath);
          console.log(`   ✅ ${file.name} (${file.content.length} 字符)`);
        }
        
      } catch (error) {
        console.error(`   ❌ 错误: ${error.message}`);
      }
      
      console.log();
    }
    
    // 步骤 5: 创建项目说明
    const readmeContent = `# Issue #${issueNumber} - 模块化代码生成

## 项目信息
- **语言**: ${language.toUpperCase()}
- **模块数**: ${modules.length}
- **文件数**: ${generatedFiles.length}
- **生成时间**: ${new Date().toLocaleString()}

## 目录结构

${generatedFiles.map(f => {
  const relPath = path.relative(outDir, f);
  return `- \`${relPath}\``;
}).join('\n')}

## 模块说明

${modules.map((m, i) => `${i + 1}. **${m.name}** - ${m.description || '核心业务模块'}`).join('\n')}

## 架构设计

详见 [ARCHITECTURE.md](./ARCHITECTURE.md)

---
*Generated by OpenCode Modular Generator*
`;
    
    fs.writeFileSync(path.join(outDir, 'README.md'), readmeContent, 'utf8');
    
    // 创建汇总文件（用于兼容性）
    const summaryContent = `Issue #${issueNumber} Modular Generation Summary
Language: ${language}
Modules: ${modules.length}
Files: ${generatedFiles.length}
Generated: ${new Date().toISOString()}

See README.md for details.
`;
    fs.writeFileSync(path.join(outDir, 'auto_generated_code.txt'), summaryContent, 'utf8');
    
    // 完成
    console.log('='.repeat(70));
    console.log('✅ 模块化代码生成完成！');
    console.log(`📊 统计:`);
    console.log(`   语言: ${language.toUpperCase()}`);
    console.log(`   模块: ${modules.length} 个`);
    console.log(`   文件: ${generatedFiles.length} 个`);
    console.log(`   目录: ${outDir}`);
    console.log('='.repeat(70) + '\n');
    
    process.exit(0);
    
  } catch (error) {
    console.error('\n❌ 生成失败:', error.message);
    
    // 保存错误信息
    fs.mkdirSync(outDir, { recursive: true });
    fs.writeFileSync(
      path.join(outDir, 'ERROR.txt'),
      `Error: ${error.message}\nTime: ${new Date().toISOString()}\n`,
      'utf8'
    );
    
    process.exit(1);
  }
})();
