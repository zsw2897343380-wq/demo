#!/usr/bin/env node
// 使用 DeepSeek API 生成代码 - 需要设置 DEEPSEEK_API_KEY 环境变量
// Usage:
// DEEPSEEK_API_KEY=xxx node scripts/opencode_generate_with_deepseek.js --issue-number 42 --issue-body "<text>" --outdir ./auto_impl/issue-42

const fs = require('fs');
const path = require('path');
const https = require('https');

// 使用 DeepSeek API 生成代码
async function generateWithDeepSeek(issueNumber, issueBody, apiKey) {
  return new Promise((resolve, reject) => {
    const promptText = `Please generate implementation code for the following issue:

Issue #${issueNumber}:
${issueBody}

Please provide:
1. A clear implementation in the appropriate programming language
2. Comments explaining the key parts
3. Any necessary imports or dependencies
4. Example usage if applicable

Generate the complete, production-ready code.`;

    const requestData = JSON.stringify({
      model: 'deepseek-chat',  // 可选: deepseek-coder 更适合代码
      messages: [
        {
          role: 'system',
          content: 'You are a senior software engineer. Generate high-quality, production-ready code based on the requirements.'
        },
        {
          role: 'user',
          content: promptText
        }
      ],
      temperature: 0.7,
      max_tokens: 2000,
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
      timeout: 120000  // 120秒超时（DeepSeek 可能需要更长时间）
    };

    console.log('[DeepSeek API] Sending request...');
    console.log('[DeepSeek API] Model: deepseek-chat');

    const req = https.request(options, (res) => {
      let data = '';

      res.on('data', (chunk) => {
        data += chunk;
      });

      res.on('end', () => {
        try {
          const response = JSON.parse(data);
          
          if (response.error) {
            reject(new Error(`DeepSeek API Error: ${response.error.message}`));
            return;
          }

          if (response.choices && response.choices[0] && response.choices[0].message) {
            const generatedCode = response.choices[0].message.content;
            console.log('[DeepSeek API] Successfully received response');
            console.log(`[DeepSeek API] Generated ${generatedCode.length} characters`);
            resolve(generatedCode);
          } else {
            console.error('[DeepSeek API] Unexpected response:', JSON.stringify(response, null, 2));
            reject(new Error('Unexpected API response format'));
          }
        } catch (error) {
          console.error('[DeepSeek API] Failed to parse response:', data);
          reject(new Error(`Failed to parse API response: ${error.message}`));
        }
      });
    });

    req.on('error', (error) => {
      reject(new Error(`Request failed: ${error.message}`));
    });

    req.on('timeout', () => {
      req.destroy();
      reject(new Error('Request timeout (120s)'));
    });

    req.write(requestData);
    req.end();
  });
}

// 使用 DeepSeek Coder 模型（更适合代码生成）
async function generateWithDeepSeekCoder(issueNumber, issueBody, apiKey) {
  return new Promise((resolve, reject) => {
    const promptText = `You are an expert programmer. Please generate implementation code for the following issue:

Issue #${issueNumber}:
${issueBody}

Requirements:
1. Write clean, well-documented code
2. Include necessary imports/dependencies
3. Add example usage
4. Follow best practices for the programming language

Please generate the complete implementation.`;

    const requestData = JSON.stringify({
      model: 'deepseek-coder',  // 专门用于代码生成的模型
      messages: [
        {
          role: 'system',
          content: 'You are an expert software engineer specialized in writing high-quality, production-ready code.'
        },
        {
          role: 'user',
          content: promptText
        }
      ],
      temperature: 0.5,  // 更低的温度使输出更确定
      max_tokens: 3000,  // coder 模型支持更长的输出
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

    console.log('[DeepSeek API] Sending request (Coder model)...');
    console.log('[DeepSeek API] Model: deepseek-coder');

    const req = https.request(options, (res) => {
      let data = '';

      res.on('data', (chunk) => {
        data += chunk;
      });

      res.on('end', () => {
        try {
          const response = JSON.parse(data);
          
          if (response.error) {
            reject(new Error(`DeepSeek API Error: ${response.error.message}`));
            return;
          }

          if (response.choices && response.choices[0] && response.choices[0].message) {
            const generatedCode = response.choices[0].message.content;
            console.log('[DeepSeek API] Successfully received response');
            console.log(`[DeepSeek API] Generated ${generatedCode.length} characters`);
            resolve(generatedCode);
          } else {
            console.error('[DeepSeek API] Unexpected response:', JSON.stringify(response, null, 2));
            reject(new Error('Unexpected API response format'));
          }
        } catch (error) {
          console.error('[DeepSeek API] Failed to parse response:', data);
          reject(new Error(`Failed to parse API response: ${error.message}`));
        }
      });
    });

    req.on('error', (error) => {
      reject(new Error(`Request failed: ${error.message}`));
    });

    req.on('timeout', () => {
      req.destroy();
      reject(new Error('Request timeout (120s)'));
    });

    req.write(requestData);
    req.end();
  });
}

function parseArgs(argv) {
  const args = {};
  for (let i = 2; i < argv.length; i++) {
    const a = argv[i];
    if (a.startsWith('--')) {
      const key = a.substring(2);
      const val = (i + 1) < argv.length && !argv[i + 1].startsWith('--') ? argv[i + 1] : 'true';
      args[key] = val;
    }
  }
  return args;
}

// 主函数
(async () => {
  const args = parseArgs(process.argv);
  const issueNumber = parseInt(args['issue-number'], 10) || 0;
  const issueBody = args['issue-body'] || '';
  const outDir = args['outdir'] || path.resolve('auto_impl');
  const model = args['model'] || 'coder';  // 'coder' 或 'chat'

  if (!issueNumber) {
    console.error('Error: --issue-number is required');
    process.exit(1);
  }

  // 获取 API Key
  const apiKey = process.env.DEEPSEEK_API_KEY;

  if (!apiKey) {
    console.error('Error: DEEPSEEK_API_KEY environment variable is not set');
    console.error('Please set it to use DeepSeek code generation');
    console.error('Get your API key from: https://platform.deepseek.com/');
    process.exit(1);
  }

  try {
    let generatedCode;

    // 根据模型选择生成方式
    if (model === 'coder') {
      console.log('[DeepSeek] Using deepseek-coder model (optimized for code generation)');
      generatedCode = await generateWithDeepSeekCoder(issueNumber, issueBody, apiKey);
    } else {
      console.log('[DeepSeek] Using deepseek-chat model');
      generatedCode = await generateWithDeepSeek(issueNumber, issueBody, apiKey);
    }

    // 写入文件
    const outPath = path.resolve(outDir);
    fs.mkdirSync(outPath, { recursive: true });
    
    const outFile = path.join(outPath, 'generated_by_deepseek.txt');
    fs.writeFileSync(outFile, generatedCode, 'utf8');
    
    console.log('[DeepSeek] ✅ Code written to', outFile);
    console.log('[DeepSeek] ✅ Total length:', generatedCode.length, 'characters');
    
    process.exit(0);
  } catch (error) {
    console.error('[DeepSeek] ❌ Failed:', error.message);
    
    // 创建错误占位符
    const outPath = path.resolve(outDir);
    fs.mkdirSync(outPath, { recursive: true });
    const outFile = path.join(outPath, 'generated_by_deepseek.txt');
    fs.writeFileSync(outFile, `# DeepSeek generation failed for issue #${issueNumber}\n# Error: ${error.message}\n# Time: ${new Date().toISOString()}\n`, 'utf8');
    
    process.exit(1);
  }
})();
