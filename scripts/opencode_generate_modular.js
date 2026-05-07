#!/usr/bin/env node
// 智能模块化代码生成 - 修复版
// 修复：1. 包名只能是英文 2. 生成完整文件（Controller/Service/Repository/Entity）
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
    packageSeparator: '.',
    defaultPackage: 'com.example',
    standardFiles: ['Controller', 'Service', 'Repository', 'Entity']
  },
  python: {
    ext: '.py',
    srcDir: 'src',
    packageSeparator: '.',
    defaultPackage: 'app',
    standardFiles: ['routes', 'service', 'models']
  },
  typescript: {
    ext: '.ts',
    srcDir: 'src',
    packageSeparator: '/',
    defaultPackage: '',
    standardFiles: ['controller', 'service', 'repository', 'entity']
  }
};

// 工具函数：转换为合法的包名（只能是英文、数字、下划线）
function sanitizePackageName(name) {
  // 移除所有非 ASCII 字符（中文等）
  let sanitized = name.replace(/[^\x00-\x7F]/g, '');
  
  // 转换为小写
  sanitized = sanitized.toLowerCase();
  
  // 替换空格和特殊字符为下划线
  sanitized = sanitized.replace(/[\s\-]+/g, '_');
  
  // 移除非字母数字下划线的字符
  sanitized = sanitized.replace(/[^a-z0-9_]/g, '');
  
  // 如果为空，使用默认值
  if (!sanitized) {
    sanitized = 'module';
  }
  
  // 确保以字母开头
  if (/^\d/.test(sanitized)) {
    sanitized = 'm_' + sanitized;
  }
  
  return sanitized;
}

// 工具函数：转换为类名（大驼峰）
function toClassName(name) {
  // 移除中文和特殊字符
  let sanitized = name.replace(/[^\x00-\x7F\s]/g, '');
  
  // 按空格、下划线、连字符分割
  return sanitized
    .split(/[\s_-]+/)
    .filter(word => word.length > 0)
    .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join('');
}

// 工具函数：检测语言
function detectLanguage(issueBody) {
  const body = issueBody.toLowerCase();
  
  // Java 检测
  if (body.includes('java') || body.includes('spring') || body.includes('springboot') || 
      body.includes('maven') || body.includes('gradle') || body.includes('jdbc') ||
      body.includes('jpa') || body.includes('mybatis')) {
    return 'java';
  }
  
  // Python 检测
  if (body.includes('python') || body.includes('django') || body.includes('flask') || 
      body.includes('fastapi')) {
    return 'python';
  }
  
  // TypeScript 检测
  if (body.includes('typescript') || body.includes('nestjs') || body.includes('typeorm')) {
    return 'typescript';
  }
  
  // 默认 Java（适合企业级应用）
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

// 生成模块代码
async function generateModuleCode(moduleName, moduleDesc, language, packageName, issueBody, apiKey) {
  const langConfig = LANGUAGE_CONFIG[language];
  const className = toClassName(moduleName);
  
  const messages = [
    {
      role: 'system',
      content: `You are an expert ${language} developer. Generate complete, production-ready code.`
    },
    {
      role: 'user',
      content: `Generate a complete ${language} module for: ${moduleName}

Package: ${packageName}
Description: ${moduleDesc}
Original requirements: ${issueBody}

Generate EXACTLY these ${language === 'java' ? 4 : 3} files:
${language === 'java' ? `
1. ${className}Controller.java - REST API endpoints with @RestController
2. ${className}Service.java - Business logic with @Service
3. ${className}Repository.java - Data access with @Repository
4. ${className}Entity.java - JPA entity with @Entity
` : `
1. ${moduleName}_routes.py - API routes
2. ${moduleName}_service.py - Business logic  
3. ${moduleName}_models.py - Data models
`}

Requirements:
- Use package: ${packageName}
- Include all necessary imports
- Add comprehensive comments
- Follow ${language} best practices
- Code must be complete and compilable

Format: Start each file with "===== FILE: Filename.java =====" followed by the code.`
    }
  ];

  return await callDeepSeekAPI(messages, apiKey, 4000);
}

// 解析生成的代码文件
function parseGeneratedFiles(content, language) {
  const files = [];
  const langConfig = LANGUAGE_CONFIG[language];
  
  // 匹配文件分隔符
  const filePattern = /=====\s*FILE:\s*(.+?)\s*=====\n?([\s\S]*?)(?======\s*FILE:|$)/g;
  let match;
  
  while ((match = filePattern.exec(content)) !== null) {
    let fileName = match[1].trim();
    let fileContent = match[2].trim();
    
    // 确保有正确的扩展名
    if (!fileName.endsWith(langConfig.ext)) {
      fileName += langConfig.ext;
    }
    
    // 清理 markdown 代码块
    fileContent = fileContent.replace(/^```\w*\n?/gm, '').replace(/```\s*$/gm, '');
    
    if (fileContent.length > 50) {
      files.push({ name: fileName, content: fileContent });
    }
  }
  
  return files;
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
  console.log('🔧 模块化代码生成（修复版）');
  console.log(`📋 Issue #${issueNumber}`);
  console.log('========================================\n');
  
  // 检测语言
  const language = detectLanguage(issueBody);
  console.log(`🔤 检测语言: ${language.toUpperCase()}`);
  
  const langConfig = LANGUAGE_CONFIG[language];
  
  // 创建输出目录
  const issueDir = path.join(outDir, `issue-${issueNumber}`);
  fs.mkdirSync(issueDir, { recursive: true });
  
  // 提取模块
  const modules = [];
  
  // 从 issue body 提取模块（简单实现）
  const lines = issueBody.split('\n');
  let currentModule = null;
  
  for (const line of lines) {
    // 匹配 "模块 X:" 或 "X. 模块名"
    const moduleMatch = line.match(/^(?:模块|\d+\.)\s*[：:]?\s*(.+)/) ||
                       line.match(/^###?\s*(.+模块.+)/);
    
    if (moduleMatch) {
      if (currentModule) {
        modules.push(currentModule);
      }
      currentModule = {
        name: moduleMatch[1].trim(),
        desc: ''
      };
    } else if (currentModule && line.trim().startsWith('-')) {
      currentModule.desc += line.trim() + ' ';
    }
  }
  
  if (currentModule) {
    modules.push(currentModule);
  }
  
  // 如果没提取到模块，创建默认模块
  if (modules.length === 0) {
    modules.push(
      { name: 'User', desc: 'User management' },
      { name: 'Order', desc: 'Order management' }
    );
  }
  
  // 限制模块数量
  const selectedModules = modules.slice(0, 4);
  
  console.log(`📦 识别到 ${selectedModules.length} 个模块:`);
  selectedModules.forEach((m, i) => {
    console.log(`  ${i + 1}. ${m.name} - ${m.desc || 'Core module'}`);
  });
  console.log();
  
  // 为每个模块生成代码
  const allFiles = [];
  
  for (let i = 0; i < selectedModules.length; i++) {
    const module = selectedModules[i];
    const modulePackage = sanitizePackageName(module.name);
    const fullPackage = language === 'java' 
      ? `${langConfig.defaultPackage}.issue${issueNumber}.${modulePackage}`
      : modulePackage;
    
    console.log(`[${i + 1}/${selectedModules.length}] 生成模块: ${module.name}`);
    console.log(`   包名: ${fullPackage}`);
    
    try {
      const generatedCode = await generateModuleCode(
        module.name,
        module.desc,
        language,
        fullPackage,
        issueBody,
        apiKey
      );
      
      const files = parseGeneratedFiles(generatedCode, language);
      
      // 创建包目录
      const packagePath = language === 'java'
        ? path.join(issueDir, langConfig.srcDir, ...fullPackage.split('.'))
        : path.join(issueDir, langConfig.srcDir, modulePackage);
      
      fs.mkdirSync(packagePath, { recursive: true });
      
      // 保存文件
      for (const file of files) {
        const filePath = path.join(packagePath, file.name);
        fs.writeFileSync(filePath, file.content, 'utf8');
        allFiles.push(filePath);
        console.log(`   ✅ ${file.name} (${file.content.length} 字符)`);
      }
      
      if (files.length === 0) {
        console.log(`   ⚠️  未能解析生成文件`);
      }
      
    } catch (error) {
      console.error(`   ❌ 生成失败: ${error.message}`);
    }
    
    console.log();
  }
  
  // 生成 README
  const readmeContent = `# Issue #${issueNumber} - ${language.toUpperCase()} Code Generation

## Generated Modules
${selectedModules.map(m => `- ${m.name}`).join('\n')}

## Files
${allFiles.map(f => `- ${path.relative(issueDir, f)}`).join('\n')}

## Package Structure
\`\`\`
${langConfig.srcDir}/
└── ${langConfig.defaultPackage}/
    └── issue${issueNumber}/
${selectedModules.map(m => `        └── ${sanitizePackageName(m.name)}/`).join('\n')}
\`\`\`

---
*Generated by Modular Code Generator*
`;
  
  fs.writeFileSync(path.join(issueDir, 'README.md'), readmeContent, 'utf8');
  
  // 汇总
  console.log('========================================');
  console.log('✅ 代码生成完成！');
  console.log(`📊 模块数: ${selectedModules.length}`);
  console.log(`📄 文件数: ${allFiles.length}`);
  console.log(`📁 输出目录: ${issueDir}`);
  console.log('========================================\n');
  
  process.exit(0);
  
})().catch(error => {
  console.error('\n❌ 错误:', error.message);
  process.exit(1);
});
