#!/usr/bin/env node
// 智能需求拆解与模块化代码生成 - 支持多语言包结构
// 使用 DeepSeek API 进行需求分析和代码生成
// Usage:
// DEEPSEEK_API_KEY=xxx node scripts/opencode_generate_modular.js --issue-number 42 --issue-body "<text>" --outdir ./auto_impl/issue-42 --language java

const fs = require('fs');
const path = require('path');
const https = require('https');

// 语言配置 - 定义不同语言的包结构和文件扩展名
const LANGUAGE_CONFIG = {
  java: {
    ext: '.java',
    srcDir: 'src/main/java',
    testDir: 'src/test/java',
    packageSeparator: '.',
    commentStyle: 'block', // /* */
    defaultPackage: 'com.example.project',
    namingConvention: 'PascalCase' // 类名使用大驼峰
  },
  python: {
    ext: '.py',
    srcDir: 'src',
    testDir: 'tests',
    packageSeparator: '.',
    commentStyle: 'hash', // #
    defaultPackage: 'src',
    namingConvention: 'snake_case'
  },
  javascript: {
    ext: '.js',
    srcDir: 'src',
    testDir: 'tests',
    packageSeparator: '/',
    commentStyle: 'block', // /* */ or //
    defaultPackage: '',
    namingConvention: 'camelCase'
  },
  typescript: {
    ext: '.ts',
    srcDir: 'src',
    testDir: 'tests',
    packageSeparator: '/',
    commentStyle: 'block',
    defaultPackage: '',
    namingConvention: 'PascalCase'
  },
  go: {
    ext: '.go',
    srcDir: '',
    testDir: '',
    packageSeparator: '/',
    commentStyle: 'block',
    defaultPackage: 'main',
    namingConvention: 'PascalCase'
  },
  rust: {
    ext: '.rs',
    srcDir: 'src',
    testDir: 'tests',
    packageSeparator: '::',
    commentStyle: 'block', // /* */
    defaultPackage: 'crate',
    namingConvention: 'snake_case'
  }
};

// 第一步：分析需求，拆解为模块
async function analyzeRequirements(issueNumber, issueBody, apiKey) {
  return new Promise((resolve, reject) => {
    const promptText = `你是一个资深的软件架构师。请分析以下需求，将其拆解为清晰的功能模块。

Issue #${issueNumber}:
${issueBody}

请按以下格式输出分析结果：

## 项目概述
简要描述这个项目的核心目标和功能。

## 功能模块拆解
请列出 3-8 个核心功能模块，每个模块包含：
1. 模块名称
2. 模块职责
3. 主要类/函数（列出 2-5 个）
4. 与其他模块的关系

## 推荐技术栈
- 编程语言：
- 主要框架：
- 数据库（如需要）：

## 目录结构建议
建议使用什么样的包/目录结构来组织代码（给出示例路径）

请使用中文回答，保持专业和简洁。`;

    const requestData = JSON.stringify({
      model: 'deepseek-chat',
      messages: [
        {
          role: 'system',
          content: '你是一个资深的软件架构师，擅长需求分析和系统架构设计。请将复杂需求拆解为清晰的模块化结构。'
        },
        {
          role: 'user',
          content: promptText
        }
      ],
      temperature: 0.7,
      max_tokens: 3000,
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

    console.log('[Phase 1] 分析需求并拆解模块...');

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

// 第二步：为单个模块生成代码
async function generateModuleCode(moduleName, moduleDesc, issueBody, language, apiKey) {
  return new Promise((resolve, reject) => {
    const langConfig = LANGUAGE_CONFIG[language] || LANGUAGE_CONFIG.python;
    
    const promptText = `请为以下模块生成 ${language.toUpperCase()} 代码：

模块名称：${moduleName}
模块描述：${moduleDesc}
原始需求：${issueBody}

要求：
1. 生成完整、可运行的 ${language} 代码
2. 包含必要的导入/依赖
3. 添加清晰的注释
4. 包含示例用法
5. 遵循 ${language} 的最佳实践和命名规范

请直接输出代码，不要包含 markdown 代码块标记。`;

    const requestData = JSON.stringify({
      model: 'deepseek-coder',
      messages: [
        {
          role: 'system',
          content: `你是一个资深的 ${language} 开发工程师。请生成高质量、生产就绪的代码。`
        },
        {
          role: 'user',
          content: promptText
        }
      ],
      temperature: 0.5,
      max_tokens: 3000,
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

    console.log(`[Phase 2] 生成模块代码: ${moduleName}...`);

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

// 检测代码语言
function detectLanguage(code) {
  // 简单启发式检测
  if (code.includes('public class') || code.includes('private class') || code.includes('import java.')) {
    return 'java';
  }
  if (code.includes('def ') && (code.includes('import ') || code.includes('from ') || code.includes('print(') || code.includes('self.'))) {
    return 'python';
  }
  if (code.includes('interface ') && code.includes(': ') && code.includes('type ')) {
    return 'typescript';
  }
  if (code.includes('func ') || code.includes('package main')) {
    return 'go';
  }
  if (code.includes('fn ') || code.includes('impl ') || code.includes('use ')) {
    return 'rust';
  }
  if (code.includes('const ') || code.includes('let ') || code.includes('var ') || code.includes('function ') || code.includes('require(')) {
    return 'javascript';
  }
  return 'python'; // 默认
}

// 从代码中提取类名/模块名
function extractFileName(code, language) {
  const langConfig = LANGUAGE_CONFIG[language] || LANGUAGE_CONFIG.python;
  
  if (language === 'java') {
    const match = code.match(/public\s+class\s+(\w+)/);
    if (match) return match[1] + langConfig.ext;
    const match2 = code.match(/class\s+(\w+)/);
    if (match2) return match2[1] + langConfig.ext;
  }
  
  if (language === 'python') {
    // 尝试从注释或函数名推断
    const match = code.match(/def\s+main\s*\(\)/);
    if (match) return 'main.py';
    const match2 = code.match(/class\s+(\w+)/);
    if (match2) {
      const name = match2[1];
      // 转换为 snake_case
      return name.replace(/([A-Z])/g, '_$1').toLowerCase().replace(/^_/, '') + '.py';
    }
  }
  
  if (language === 'javascript' || language === 'typescript') {
    const match = code.match(/export\s+(?:default\s+)?(?:class|function|const)\s+(\w+)/);
    if (match) return match[1] + langConfig.ext;
    return 'index' + langConfig.ext;
  }
  
  return 'module' + langConfig.ext;
}

// 解析需求分析结果，提取模块列表
function parseModules(analysis) {
  const modules = [];
  
  // 尝试匹配常见的模块描述格式
  const lines = analysis.split('\n');
  let currentModule = null;
  
  for (const line of lines) {
    // 匹配模块名称（支持多种格式）
    const moduleMatch = line.match(/^(?:###?\s+)?(?:模块|Module)\s*[:：]\s*(.+)/i) ||
                       line.match(/^(?:###?\s+)?(\d+)\.\s*(.+)/);
    
    if (moduleMatch) {
      if (currentModule) {
        modules.push(currentModule);
      }
      currentModule = {
        name: moduleMatch[2] || moduleMatch[1],
        description: '',
        classes: []
      };
    } else if (currentModule && line.trim()) {
      // 收集模块描述
      if (line.includes('职责') || line.includes('描述') || line.includes('功能')) {
        currentModule.description += line.replace(/^.+?[:：]\s*/, '') + ' ';
      }
      // 收集类/函数名
      const classMatch = line.match(/(?:类|class|函数|function)\s*[:：]\s*(\w+)/);
      if (classMatch) {
        currentModule.classes.push(classMatch[1]);
      }
    }
  }
  
  if (currentModule) {
    modules.push(currentModule);
  }
  
  // 如果没有解析到模块，创建默认模块
  if (modules.length === 0) {
    modules.push({
      name: 'MainModule',
      description: '主要功能实现',
      classes: ['Main']
    });
  }
  
  return modules.slice(0, 6); // 最多6个模块
}

// 生成项目结构文件
function generateProjectStructure(language, modules, basePackage) {
  const langConfig = LANGUAGE_CONFIG[language] || LANGUAGE_CONFIG.python;
  const structure = [];
  
  // 生成 README
  let readme = `# 项目结构\n\n`;
  readme += `## 技术栈\n- 语言: ${language}\n\n`;
  readme += `## 模块说明\n\n`;
  
  modules.forEach((module, index) => {
    const packagePath = basePackage ? `${basePackage}.${module.name.toLowerCase()}` : module.name.toLowerCase();
    const filePath = langConfig.srcDir 
      ? `${langConfig.srcDir}/${packagePath.replace(/\./g, '/')}/${extractFileName('', language)}`
      : `${module.name.toLowerCase()}${langConfig.ext}`;
    
    readme += `### ${index + 1}. ${module.name}\n`;
    readme += `- 路径: \`${filePath}\`\n`;
    readme += `- 职责: ${module.description || '实现核心功能'}\n`;
    if (module.classes.length > 0) {
      readme += `- 主要类: ${module.classes.join(', ')}\n`;
    }
    readme += '\n';
    
    structure.push({
      module: module.name,
      package: packagePath,
      filePath: filePath
    });
  });
  
  return { readme, structure };
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
  let language = (args['language'] || 'auto').toLowerCase();
  
  if (!issueNumber) {
    console.error('Error: --issue-number is required');
    process.exit(1);
  }
  
  const apiKey = process.env.DEEPSEEK_API_KEY;
  if (!apiKey) {
    console.error('Error: DEEPSEEK_API_KEY environment variable is not set');
    process.exit(1);
  }
  
  try {
    console.log('='.repeat(60));
    console.log(`🔧 模块化代码生成 - Issue #${issueNumber}`);
    console.log(`📁 输出目录: ${outDir}`);
    console.log(`🔤 语言: ${language}`);
    console.log('='.repeat(60));
    
    // 阶段 1: 分析需求
    const analysis = await analyzeRequirements(issueNumber, issueBody, apiKey);
    
    // 保存分析结果
    const analysisPath = path.join(outDir, 'analysis.md');
    fs.mkdirSync(outDir, { recursive: true });
    fs.writeFileSync(analysisPath, analysis, 'utf8');
    console.log(`\n✅ 需求分析已保存: ${analysisPath}`);
    
    // 如果语言是 auto，从分析中检测
    if (language === 'auto') {
      if (analysis.includes('Java') || analysis.includes('java')) {
        language = 'java';
      } else if (analysis.includes('Python') || analysis.includes('python')) {
        language = 'python';
      } else if (analysis.includes('TypeScript') || analysis.includes('typescript')) {
        language = 'typescript';
      } else if (analysis.includes('Go') || analysis.includes('golang')) {
        language = 'go';
      } else if (analysis.includes('Rust') || analysis.includes('rust')) {
        language = 'rust';
      } else {
        language = 'python';
      }
      console.log(`🔤 自动检测到语言: ${language}`);
    }
    
    // 解析模块
    const modules = parseModules(analysis);
    console.log(`\n📦 识别到 ${modules.length} 个模块:`);
    modules.forEach((m, i) => console.log(`  ${i + 1}. ${m.name}`));
    
    // 生成项目结构
    const langConfig = LANGUAGE_CONFIG[language] || LANGUAGE_CONFIG.python;
    const basePackage = langConfig.defaultPackage + (issueNumber ? `.issue${issueNumber}` : '');
    const { readme, structure } = generateProjectStructure(language, modules, basePackage);
    
    // 保存 README
    const readmePath = path.join(outDir, 'README.md');
    fs.writeFileSync(readmePath, readme, 'utf8');
    console.log(`\n✅ 项目结构文档已保存: ${readmePath}`);
    
    // 阶段 2: 为每个模块生成代码
    console.log('\n' + '='.repeat(60));
    console.log('🚀 开始生成模块代码...');
    console.log('='.repeat(60));
    
    for (let i = 0; i < modules.length; i++) {
      const module = modules[i];
      console.log(`\n[${i + 1}/${modules.length}] 处理模块: ${module.name}`);
      
      try {
        // 生成代码
        const code = await generateModuleCode(
          module.name, 
          module.description || module.name, 
          issueBody, 
          language, 
          apiKey
        );
        
        // 确定文件路径
        const fileName = extractFileName(code, language);
        const packagePath = basePackage ? `${basePackage}.${module.name.toLowerCase()}` : module.name.toLowerCase();
        const dirPath = langConfig.srcDir 
          ? path.join(outDir, langConfig.srcDir, ...packagePath.split('.'))
          : outDir;
        
        fs.mkdirSync(dirPath, { recursive: true });
        
        const filePath = path.join(dirPath, fileName);
        fs.writeFileSync(filePath, code, 'utf8');
        
        console.log(`  ✅ 已生成: ${filePath} (${code.length} 字符)`);
        
      } catch (error) {
        console.error(`  ❌ 生成失败: ${error.message}`);
        // 创建占位符文件
        const placeholderPath = path.join(outDir, `${module.name.toLowerCase()}_placeholder${langConfig.ext}`);
        fs.writeFileSync(placeholderPath, `// Placeholder for ${module.name}\n// Error: ${error.message}\n`, 'utf8');
      }
    }
    
    // 创建汇总文件
    const summaryPath = path.join(outDir, 'auto_generated_code.txt');
    const summary = `# Issue #${issueNumber} - 模块化代码生成\n\n` +
                   `语言: ${language}\n` +
                   `模块数: ${modules.length}\n` +
                   `生成时间: ${new Date().toISOString()}\n\n` +
                   `## 生成的文件\n` +
                   structure.map(s => `- ${s.filePath}`).join('\n') + '\n';
    fs.writeFileSync(summaryPath, summary, 'utf8');
    
    console.log('\n' + '='.repeat(60));
    console.log('✅ 模块化代码生成完成！');
    console.log(`📊 生成了 ${modules.length} 个模块`);
    console.log(`📁 输出目录: ${outDir}`);
    console.log('='.repeat(60));
    
    process.exit(0);
    
  } catch (error) {
    console.error('\n❌ 生成失败:', error.message);
    
    // 创建错误日志
    const errorPath = path.join(outDir, 'generation_error.txt');
    fs.mkdirSync(outDir, { recursive: true });
    fs.writeFileSync(errorPath, `Error: ${error.message}\nTime: ${new Date().toISOString()}\n`, 'utf8');
    
    process.exit(1);
  }
})();
