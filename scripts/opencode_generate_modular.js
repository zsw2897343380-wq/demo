#!/usr/bin/env node
// 智能模块化代码生成 - 按语言标准架构版
// Java = 三层架构, Python = 标准包结构, TypeScript = 模块化架构
// Usage:
// DEEPSEEK_API_KEY=xxx node scripts/opencode_generate_modular.js --issue-number 42 --issue-body "<text>" --outdir ./auto_impl/issue-42

const fs = require('fs');
const path = require('path');
const https = require('https');

// 语言架构标准配置
const LANGUAGE_ARCHITECTURE = {
  java: {
    ext: '.java',
    srcDir: 'src/main/java',
    defaultPackage: 'com.example',
    description: 'Spring Boot 三层架构',
    layers: [
      {
        name: 'controller',
        suffix: 'Controller',
        description: '控制层 - REST API 入口，处理 HTTP 请求响应',
        annotations: ['@RestController', '@RequestMapping'],
        imports: ['org.springframework.web.bind.annotation.*', 'org.springframework.beans.factory.annotation.Autowired']
      },
      {
        name: 'service',
        suffix: 'Service',
        description: '业务层 - 业务逻辑处理，事务管理',
        annotations: ['@Service', '@Transactional'],
        imports: ['org.springframework.stereotype.Service', 'org.springframework.transaction.annotation.Transactional']
      },
      {
        name: 'repository',
        suffix: 'Repository',
        description: '数据层 - 数据库访问，JPA 操作',
        annotations: ['@Repository'],
        imports: ['org.springframework.stereotype.Repository', 'org.springframework.data.jpa.repository.JpaRepository']
      },
      {
        name: 'entity',
        suffix: '',  // Entity 直接使用名称，如 User
        description: '实体层 - 数据模型，对应数据库表',
        annotations: ['@Entity', '@Table'],
        imports: ['javax.persistence.*']
      },
      {
        name: 'dto',
        suffix: 'DTO',
        description: '传输层 - 数据传输对象，用于 API 交互',
        annotations: [],
        imports: []
      }
    ]
  },
  
  python: {
    ext: '.py',
    srcDir: 'src',
    defaultPackage: 'app',
    description: 'Python 标准项目结构',
    layers: [
      {
        name: '__init__',
        suffix: '',
        description: '包初始化文件',
        isPackageInit: true
      },
      {
        name: 'routes',
        suffix: '_routes',
        description: '路由层 - API 端点定义',
        imports: ['from flask import Blueprint', 'from . import service']
      },
      {
        name: 'service',
        suffix: '_service',
        description: '业务层 - 核心业务逻辑',
        imports: ['from . import models']
      },
      {
        name: 'models',
        suffix: '_models',
        description: '模型层 - 数据模型定义',
        imports: ['from sqlalchemy import Column', 'from sqlalchemy.ext.declarative import declarative_base']
      },
      {
        name: 'schemas',
        suffix: '_schemas',
        description: '模式层 - 数据验证和序列化',
        imports: ['from marshmallow import Schema', 'from marshmallow import fields']
      },
      {
        name: 'utils',
        suffix: '_utils',
        description: '工具层 - 辅助函数',
        imports: []
      }
    ]
  },
  
  typescript: {
    ext: '.ts',
    srcDir: 'src',
    defaultPackage: '',
    description: 'TypeScript/NestJS 模块化架构',
    layers: [
      {
        name: 'controller',
        suffix: '.controller',
        description: '控制器 - 处理 HTTP 请求',
        decorators: ['@Controller()', '@Get()', '@Post()'],
        imports: ['@nestjs/common']
      },
      {
        name: 'service',
        suffix: '.service',
        description: '服务层 - 业务逻辑',
        decorators: ['@Injectable()'],
        imports: ['@nestjs/common']
      },
      {
        name: 'module',
        suffix: '.module',
        description: '模块定义 - NestJS 模块',
        decorators: ['@Module()'],
        imports: ['@nestjs/common']
      },
      {
        name: 'entity',
        suffix: '.entity',
        description: '实体 - 数据库模型',
        decorators: ['@Entity()'],
        imports: ['typeorm']
      },
      {
        name: 'dto',
        suffix: '.dto',
        description: 'DTO - 数据传输对象',
        decorators: [],
        imports: ['class-validator', 'class-transformer']
      },
      {
        name: 'interface',
        suffix: '.interface',
        description: '接口定义 - TypeScript 类型',
        decorators: [],
        imports: []
      }
    ]
  },
  
  go: {
    ext: '.go',
    srcDir: '',
    defaultPackage: 'main',
    description: 'Go 标准项目结构',
    layers: [
      {
        name: 'handler',
        suffix: '_handler',
        description: '处理器 - HTTP 处理函数',
        imports: ['net/http', 'github.com/gin-gonic/gin']
      },
      {
        name: 'service',
        suffix: '_service',
        description: '服务层 - 业务逻辑',
        imports: []
      },
      {
        name: 'repository',
        suffix: '_repository',
        description: '仓储层 - 数据访问',
        imports: ['database/sql', 'gorm.io/gorm']
      },
      {
        name: 'model',
        suffix: '_model',
        description: '模型层 - 数据结构定义',
        imports: ['gorm.io/gorm']
      },
      {
        name: 'dto',
        suffix: '_dto',
        description: 'DTO - 数据传输结构',
        imports: []
      }
    ]
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

// 工具函数：转换为类名（大驼峰）
function toClassName(name) {
  let sanitized = name.replace(/[^\x00-\x7F\s]/g, '');
  return sanitized
    .split(/[\s_-]+/)
    .filter(word => word.length > 0)
    .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join('');
}

// 检测语言
function detectLanguage(issueBody) {
  const body = issueBody.toLowerCase();
  if (body.includes('java') || body.includes('spring') || body.includes('springboot') ||
      body.includes('maven') || body.includes('gradle') || body.includes('jpa')) {
    return 'java';
  }
  if (body.includes('python') || body.includes('django') || body.includes('flask') ||
      body.includes('fastapi') || body.includes('sqlalchemy')) {
    return 'python';
  }
  if (body.includes('typescript') || body.includes('nestjs') || body.includes('ts') ||
      body.includes('typeorm') || body.includes('angular')) {
    return 'typescript';
  }
  if (body.includes('go') || body.includes('golang') || body.includes('gin') ||
      body.includes('beego')) {
    return 'go';
  }
  return 'java'; // 默认 Java
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

// 分析需求，确定需要哪些模块
async function analyzeModules(issueBody, language, archConfig, apiKey) {
  const messages = [
    {
      role: 'system',
      content: `You are a software architect specializing in ${archConfig.description}. Analyze requirements and identify modules.`
    },
    {
      role: 'user',
      content: `Analyze the following requirements and identify the modules needed for a ${archConfig.description} project.

Requirements:
${issueBody}

The project uses ${language.toUpperCase()} with the following standard layers:
${archConfig.layers.map(l => `- ${l.name}: ${l.description}`).join('\n')}

Output format:
List 2-5 business modules (e.g., user, order, product, payment).
For each module:
1. Module name (English, lowercase, like: user, order, product)
2. Brief purpose (1 sentence)
3. Which layers are needed from the standard architecture above

Example for Java:
Module: user
- Purpose: Handle user registration and authentication
- Layers: controller, service, repository, entity (standard CRUD)

Module: payment
- Purpose: Process payments
- Layers: controller, service, repository, entity, dto (complex, needs DTO)

Be practical - simple modules may not need all layers.`
    }
  ];

  return await callDeepSeekAPI(messages, apiKey, 2000);
}

// 解析模块分析结果
function parseModules(analysis) {
  const modules = [];
  const lines = analysis.split('\n');
  
  let currentModule = null;
  
  for (const line of lines) {
    if (line.match(/^\s*module[\s:]\s*(\w+)/i)) {
      if (currentModule) {
        modules.push(currentModule);
      }
      const match = line.match(/module[\s:]\s*(\w+)/i);
      currentModule = {
        name: sanitizePackageName(match[1]),
        desc: '',
        layers: []
      };
    } else if (currentModule && line.toLowerCase().includes('purpose')) {
      const match = line.match(/purpose[:\s]+(.+)/i);
      if (match) currentModule.desc = match[1].trim();
    } else if (currentModule && line.toLowerCase().includes('layers')) {
      const match = line.match(/layers[:\s]+(.+)/i);
      if (match) {
        currentModule.layers = match[1].split(/[,\s]+/).filter(l => l.length > 0);
      }
    }
  }
  
  if (currentModule) {
    modules.push(currentModule);
  }
  
  return modules.length > 0 ? modules : [{ name: 'main', desc: 'Main module', layers: [] }];
}

// 生成特定文件的代码
async function generateLayerFile(moduleName, layerConfig, language, packageName, issueBody, apiKey) {
  const className = toClassName(moduleName) + layerConfig.suffix;
  const fileName = className + LANGUAGE_ARCHITECTURE[language].ext;
  
  const messages = [
    {
      role: 'system',
      content: `You are an expert ${language} developer. Generate production-ready code following ${LANGUAGE_ARCHITECTURE[language].description}.`
    },
    {
      role: 'user',
      content: `Generate ${language} code for: ${fileName}

Context:
- Module: ${moduleName}
- Layer: ${layerConfig.name}
- Purpose: ${layerConfig.description}
- Package/Namespace: ${packageName}
- Requirements: ${issueBody}

Requirements:
1. Include all necessary imports
2. Use standard ${language} conventions
${layerConfig.annotations ? `3. Use annotations: ${layerConfig.annotations.join(', ')}` : ''}
${layerConfig.decorators ? `3. Use decorators: ${layerConfig.decorators.join(', ')}` : ''}
4. Add clear comments
5. Follow ${LANGUAGE_ARCHITECTURE[language].description} best practices

Generate complete, compilable code.`
    }
  ];

  const content = await callDeepSeekAPI(messages, apiKey, 2500);
  
  // 清理 markdown
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
  console.log('🔧 模块化代码生成 - 标准架构版');
  console.log(`📋 Issue #${issueNumber}`);
  console.log('========================================\n');
  
  // 检测语言
  const language = detectLanguage(issueBody);
  const archConfig = LANGUAGE_ARCHITECTURE[language];
  
  console.log(`🔤 检测语言: ${language.toUpperCase()}`);
  console.log(`📐 架构标准: ${archConfig.description}`);
  console.log(`📚 标准层级: ${archConfig.layers.map(l => l.name).join(', ')}\n`);
  
  const issueDir = path.join(outDir, `issue-${issueNumber}`);
  fs.mkdirSync(issueDir, { recursive: true });
  
  // 第一阶段：分析模块
  console.log('[Phase 1/2] 分析业务模块...');
  const analysis = await analyzeModules(issueBody, language, archConfig, apiKey);
  fs.writeFileSync(path.join(issueDir, 'ARCHITECTURE.md'), analysis, 'utf8');
  
  const modules = parseModules(analysis);
  
  // 为每个模块确定需要的层级
  modules.forEach(m => {
    if (m.layers.length === 0) {
      // 默认使用所有层级
      m.layers = archConfig.layers.map(l => l.name);
    }
  });
  
  console.log(`✅ 识别到 ${modules.length} 个业务模块:\n`);
  modules.forEach((m, i) => {
    console.log(`  ${i + 1}. ${m.name}/`);
    console.log(`     描述: ${m.desc || 'N/A'}`);
    console.log(`     层级: ${m.layers.join(', ')}`);
  });
  console.log();
  
  // 第二阶段：生成代码
  console.log('[Phase 2/2] 按标准架构生成代码...\n');
  
  const allFiles = [];
  
  for (let i = 0; i < modules.length; i++) {
    const module = modules[i];
    const modulePackage = sanitizePackageName(module.name);
    
    console.log(`[${i + 1}/${modules.length}] 模块: ${module.name}`);
    
    // 创建包目录
    let packagePath;
    if (language === 'java') {
      const fullPackage = `${archConfig.defaultPackage}.issue${issueNumber}.${modulePackage}`;
      packagePath = path.join(issueDir, archConfig.srcDir, ...fullPackage.split('.'));
      console.log(`   包名: ${fullPackage}`);
    } else {
      packagePath = path.join(issueDir, archConfig.srcDir, modulePackage);
      console.log(`   目录: ${archConfig.srcDir}/${modulePackage}`);
    }
    
    fs.mkdirSync(packagePath, { recursive: true });
    
    // 为每个层级生成文件
    for (const layerName of module.layers) {
      const layerConfig = archConfig.layers.find(l => l.name === layerName);
      if (!layerConfig) continue;
      
      // 跳过 __init__ 文件生成（Python）
      if (layerConfig.isPackageInit) {
        const initPath = path.join(packagePath, '__init__.py');
        fs.writeFileSync(initPath, `# ${module.name} package\n`, 'utf8');
        allFiles.push(initPath);
        console.log(`   ✅ __init__.py`);
        continue;
      }
      
      const fileName = toClassName(module.name) + layerConfig.suffix + archConfig.ext;
      console.log(`   生成: ${fileName} (${layerConfig.name})...`);
      
      try {
        const fullPackageName = language === 'java' 
          ? `${archConfig.defaultPackage}.issue${issueNumber}.${modulePackage}`
          : modulePackage;
          
        const code = await generateLayerFile(
          module.name,
          layerConfig,
          language,
          fullPackageName,
          issueBody,
          apiKey
        );
        
        const filePath = path.join(packagePath, fileName);
        fs.writeFileSync(filePath, code, 'utf8');
        allFiles.push(filePath);
        console.log(`      ✅ ${code.length} 字符`);
      } catch (error) {
        console.error(`      ❌ 失败: ${error.message}`);
      }
    }
    
    console.log();
  }
  
  // 生成 README
  const readmeContent = `# Issue #${issueNumber} - ${language.toUpperCase()} Code Generation

## 架构标准
${archConfig.description}

## 标准层级
${archConfig.layers.map(l => `- **${l.name}**: ${l.description}`).join('\n')}

## 生成的模块
${modules.map(m => `
### ${m.name}
- 描述: ${m.desc || 'N/A'}
- 包含层级: ${m.layers.join(', ')}
`).join('\n')}

## 文件统计
- 模块数: ${modules.length}
- 文件总数: ${allFiles.length}
- 语言: ${language.toUpperCase()}
- 架构: ${archConfig.description}

## 目录结构
\`\`\`
${allFiles.map(f => path.relative(issueDir, f)).join('\n')}
\`\`\`

---
*Generated following ${archConfig.description} standards*
`;
  
  fs.writeFileSync(path.join(issueDir, 'README.md'), readmeContent, 'utf8');
  
  console.log('========================================');
  console.log('✅ 代码生成完成！');
  console.log(`📊 模块数: ${modules.length}`);
  console.log(`📄 文件总数: ${allFiles.length}`);
  console.log(`🔤 语言: ${language.toUpperCase()}`);
  console.log(`📐 架构: ${archConfig.description}`);
  console.log('========================================\n');
  
  process.exit(0);
  
})().catch(error => {
  console.error('\n❌ 错误:', error.message);
  process.exit(1);
});
