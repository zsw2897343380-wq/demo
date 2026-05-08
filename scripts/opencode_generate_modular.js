#!/usr/bin/env node
// 智能模块化代码生成 - AI决定文件结构版
// AI根据需求分析决定：1. 需要哪些模块 2. 每个模块需要哪些文件 3. 文件命名
// Usage:
// DEEPSEEK_API_KEY=xxx node scripts/opencode_generate_modular.js --issue-number 42 --issue-body "<text>" --outdir ./auto_impl/issue-42

const fs = require('fs');
const path = require('path');
const https = require('https');

// 语言配置
const LANGUAGE_CONFIG = {
  java: {
    ext: '.java',
    srcDir: 'src/main/java',
    defaultPackage: 'com.example'
  },
  python: {
    ext: '.py',
    srcDir: 'src',
    defaultPackage: 'app'
  },
  typescript: {
    ext: '.ts',
    srcDir: 'src',
    defaultPackage: ''
  }
};

// 工具函数：清理包名（移除非ASCII字符）
function sanitizePackageName(name) {
  let sanitized = name.replace(/[^\x00-\x7F]/g, '');
  sanitized = sanitized.toLowerCase();
  sanitized = sanitized.replace(/[\s\-]+/g, '_');
  sanitized = sanitized.replace(/[^a-z0-9_]/g, '');
  if (!sanitized) sanitized = 'module';
  if (/^\d/.test(sanitized)) sanitized = 'm_' + sanitized;
  return sanitized;
}

// 检测语言
function detectLanguage(issueBody) {
  const body = issueBody.toLowerCase();
  if (body.includes('java') || body.includes('spring') || body.includes('springboot')) return 'java';
  if (body.includes('python') || body.includes('django') || body.includes('flask')) return 'python';
  if (body.includes('typescript') || body.includes('nestjs')) return 'typescript';
  return 'java';
}

// 调用 DeepSeek API
async function callDeepSeekAPI(messages, apiKey, maxTokens = 4000) {
  return new Promise((resolve, reject) => {
    const requestData = JSON.stringify({
      model: 'deepseek-coder',
      messages: messages,
      temperature: 0.3,
      max_tokens: maxTokens,
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
            reject(new Error('Invalid response format'));
          }
        } catch (e) {
          reject(new Error(`Parse error: ${e.message}`));
        }
      });
    });

    req.on('error', (e) => reject(e));
    req.on('timeout', () => { req.destroy(); reject(new Error('Timeout')); });
    req.write(requestData);
    req.end();
  });
}

// 第一阶段：分析需求，确定文件结构
async function analyzeRequirements(issueBody, language, apiKey) {
  const langName = language.toUpperCase();
  
  const messages = [
    {
      role: 'system',
      content: `You are an expert software architect. Analyze requirements and design the file structure.`
    },
    {
      role: 'user',
      content: `Analyze the following requirements and determine the optimal file structure for a ${langName} project.

Requirements:
${issueBody}

Please output in the following format:

## Project Analysis
Brief description of what needs to be built.

## Suggested Package/Module Structure
List 2-5 modules/packages needed, with English names only (no Chinese).

For each module, specify:
1. Module name (English, lowercase, like: user, order, payment)
2. Purpose (1 sentence)
3. Files needed in this module (AI decides based on requirements):
   - For Java: list files like UserController, UserService, UserRepository, UserEntity, etc.
   - For Python: list files like routes, service, models, utils, etc.
   - For TypeScript: list files like controller, service, repository, entity, dto, etc.
   
   Important: Only list files that are actually needed for this requirement. 
   Simple CRUD might only need 2-3 files.
   Complex business logic might need 4-6 files.
   Let the complexity of requirements determine the number of files.

## Example Output Format:
Module: user
- Purpose: Handle user registration and authentication
- Files:
  1. UserController - REST API endpoints
  2. UserService - Business logic
  3. UserRepository - Database access
  (Only 3 files because requirement is simple)

Module: payment  
- Purpose: Handle payment processing
- Files:
  1. PaymentController
  2. PaymentService
  3. PaymentGateway
  4. PaymentRepository
  5. PaymentDto
  (5 files because payment is complex)

Analyze carefully and suggest appropriate files based on actual requirements.`
    }
  ];

  return await callDeepSeekAPI(messages, apiKey, 3000);
}

// 解析AI返回的文件结构
function parseFileStructure(analysis, language) {
  const modules = [];
  const lines = analysis.split('\n');
  
  let currentModule = null;
  let currentFiles = [];
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    
    // 检测模块开始
    if (line.match(/^\s*module[\s:]\s*(.+)/i) || line.match(/^\s*[-*]\s*module[\s:]\s*(.+)/i)) {
      if (currentModule && currentFiles.length > 0) {
        modules.push({ ...currentModule, files: currentFiles });
      }
      
      const nameMatch = line.match(/module[\s:]\s*(\w+)/i);
      currentModule = {
        name: nameMatch ? sanitizePackageName(nameMatch[1]) : 'module',
        desc: ''
      };
      currentFiles = [];
    }
    // 检测文件列表
    else if (currentModule && line.match(/^\s*\d+\.\s*(\w+).*/)) {
      const fileMatch = line.match(/^\s*\d+\.\s*(\w+).*/);
      if (fileMatch) {
        currentFiles.push(fileMatch[1]);
      }
    }
    // 检测模块描述
    else if (currentModule && line.toLowerCase().includes('purpose')) {
      const descMatch = line.match(/purpose[:\s]+(.+)/i);
      if (descMatch) {
        currentModule.desc = descMatch[1].trim();
      }
    }
  }
  
  // 添加最后一个模块
  if (currentModule && currentFiles.length > 0) {
    modules.push({ ...currentModule, files: currentFiles });
  }
  
  return modules;
}

// 第二阶段：为每个文件生成代码
async function generateFileCode(fileName, moduleName, moduleDesc, language, packageName, issueBody, apiKey) {
  const langConfig = LANGUAGE_CONFIG[language];
  const ext = langConfig.ext;
  
  const messages = [
    {
      role: 'system',
      content: `You are an expert ${language} developer. Generate complete, production-ready code.`
    },
    {
      role: 'user',
      content: `Generate complete ${language} code for file: ${fileName}${ext}

Context:
- Module: ${moduleName}
- Module purpose: ${moduleDesc}
- Package/Namespace: ${packageName}
- Original requirements: ${issueBody}

Generate the complete ${fileName}${ext} file with:
1. All necessary imports
2. Complete class/function definition
3. Proper error handling
4. Clear comments
5. Follow ${language} best practices

Output only the code, no explanations.`
    }
  ];

  const content = await callDeepSeekAPI(messages, apiKey, 2000);
  
  // 清理 markdown 代码块
  return content.replace(/^```\w*\n?/gm, '').replace(/```\s*$/gm, '');
}

// 主函数
(async () => {
  // 解析参数
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
  
  if (!issueNumber) {
    console.error('❌ Error: --issue-number is required');
    process.exit(1);
  }
  
  const apiKey = process.env.DEEPSEEK_API_KEY;
  if (!apiKey) {
    console.error('❌ Error: DEEPSEEK_API_KEY not set');
    process.exit(1);
  }
  
  console.log('\n========================================');
  console.log('🔧 智能模块化代码生成');
  console.log(`📋 Issue #${issueNumber}`);
  console.log('========================================\n');
  
  // 检测语言
  const language = detectLanguage(issueBody);
  console.log(`🔤 检测语言: ${language.toUpperCase()}`);
  
  const langConfig = LANGUAGE_CONFIG[language];
  const issueDir = path.join(outDir, `issue-${issueNumber}`);
  fs.mkdirSync(issueDir, { recursive: true });
  
  // 第一阶段：分析需求，确定文件结构
  console.log('\n[Phase 1/2] 分析需求，确定文件结构...');
  const analysis = await analyzeRequirements(issueBody, language, apiKey);
  
  // 保存分析结果
  fs.writeFileSync(path.join(issueDir, 'ANALYSIS.md'), analysis, 'utf8');
  console.log('✅ 需求分析完成\n');
  
  // 解析文件结构
  const modules = parseFileStructure(analysis, language);
  
  if (modules.length === 0) {
    console.log('⚠️  未能解析模块结构，使用默认结构');
    modules.push({
      name: 'main',
      desc: 'Main module',
      files: language === 'java' ? ['Controller', 'Service'] : ['main']
    });
  }
  
  console.log(`📦 AI 建议的模块结构:`);
  modules.forEach((m, i) => {
    console.log(`\n  ${i + 1}. ${m.name}/`);
    console.log(`     描述: ${m.desc || 'N/A'}`);
    console.log(`     文件数: ${m.files.length}`);
    m.files.forEach((f, j) => {
      console.log(`       ${j + 1}. ${f}${langConfig.ext}`);
    });
  });
  console.log();
  
  // 第二阶段：生成代码文件
  console.log('[Phase 2/2] 生成代码文件...\n');
  
  const allFiles = [];
  
  for (let i = 0; i < modules.length; i++) {
    const module = modules[i];
    const modulePackage = sanitizePackageName(module.name);
    const fullPackage = language === 'java' 
      ? `${langConfig.defaultPackage}.issue${issueNumber}.${modulePackage}`
      : modulePackage;
    
    console.log(`[${i + 1}/${modules.length}] 模块: ${module.name}`);
    console.log(`   包名: ${fullPackage}`);
    console.log(`   生成 ${module.files.length} 个文件...`);
    
    // 创建包目录
    const packagePath = language === 'java'
      ? path.join(issueDir, langConfig.srcDir, ...fullPackage.split('.'))
      : path.join(issueDir, langConfig.srcDir, modulePackage);
    
    fs.mkdirSync(packagePath, { recursive: true });
    
    // 为每个文件生成代码
    for (let j = 0; j < module.files.length; j++) {
      const fileName = module.files[j];
      console.log(`   [${j + 1}/${module.files.length}] ${fileName}${langConfig.ext}...`);
      
      try {
        const code = await generateFileCode(
          fileName,
          module.name,
          module.desc,
          language,
          fullPackage,
          issueBody,
          apiKey
        );
        
        const filePath = path.join(packagePath, `${fileName}${langConfig.ext}`);
        fs.writeFileSync(filePath, code, 'utf8');
        allFiles.push(filePath);
        console.log(`       ✅ ${code.length} 字符`);
      } catch (error) {
        console.error(`       ❌ 生成失败: ${error.message}`);
      }
    }
    
    console.log();
  }
  
  // 生成 README
  const readmeContent = `# Issue #${issueNumber} - ${language.toUpperCase()} Code Generation

## 项目分析
AI 根据需求自动设计的模块结构。

## 生成的模块
${modules.map(m => `
### ${m.name}
- 描述: ${m.desc || 'N/A'}
- 文件数: ${m.files.length}
- 文件列表:
${m.files.map(f => `  - ${f}${langConfig.ext}`).join('\n')}
`).join('\n')}

## 文件统计
- 模块数: ${modules.length}
- 文件总数: ${allFiles.length}
- 语言: ${language.toUpperCase()}

## 目录结构
\`\`\`
${allFiles.map(f => path.relative(issueDir, f)).join('\n')}
\`\`\`

---
*Generated by AI-based Modular Code Generator*
`;
  
  fs.writeFileSync(path.join(issueDir, 'README.md'), readmeContent, 'utf8');
  
  // 汇总
  console.log('========================================');
  console.log('✅ 代码生成完成！');
  console.log(`📊 模块数: ${modules.length}`);
  console.log(`📄 文件总数: ${allFiles.length}`);
  console.log(`📁 输出目录: ${issueDir}`);
  console.log('========================================\n');
  
  process.exit(0);
  
})().catch(error => {
  console.error('\n❌ 错误:', error.message);
  process.exit(1);
});
