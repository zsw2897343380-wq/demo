#!/usr/bin/env node
// 使用 OpenAI API 直接调用版本 - 需要设置 OPENAI_API_KEY 环境变量
// Usage:
// OPENAI_API_KEY=xxx node scripts/opencode_generate_with_openai.js --issue-number 42 --issue-body "<text>" --outdir ./auto_impl/issue-42

const fs = require('fs');
const path = require('path');
const https = require('https');

// 使用 OpenAI API 生成代码
async function generateWithOpenAI(issueNumber, issueBody, apiKey) {
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
      model: 'gpt-3.5-turbo',  // 可以使用 gpt-4 获得更好的结果
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
      max_tokens: 2000
    });

    const options = {
      hostname: 'api.openai.com',
      port: 443,
      path: '/v1/chat/completions',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${apiKey}`,
        'Content-Length': Buffer.byteLength(requestData)
      },
      timeout: 60000  // 60秒超时
    };

    console.log('[OpenAI API] Sending request...');

    const req = https.request(options, (res) => {
      let data = '';

      res.on('data', (chunk) => {
        data += chunk;
      });

      res.on('end', () => {
        try {
          const response = JSON.parse(data);
          
          if (response.error) {
            reject(new Error(`OpenAI API Error: ${response.error.message}`));
            return;
          }

          if (response.choices && response.choices[0] && response.choices[0].message) {
            const generatedCode = response.choices[0].message.content;
            console.log('[OpenAI API] Successfully received response');
            resolve(generatedCode);
          } else {
            reject(new Error('Unexpected API response format'));
          }
        } catch (error) {
          reject(new Error(`Failed to parse API response: ${error.message}`));
        }
      });
    });

    req.on('error', (error) => {
      reject(new Error(`Request failed: ${error.message}`));
    });

    req.on('timeout', () => {
      req.destroy();
      reject(new Error('Request timeout'));
    });

    req.write(requestData);
    req.end();
  });
}

// 使用 Anthropic Claude API 生成代码（备选）
async function generateWithClaude(issueNumber, issueBody, apiKey) {
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
      model: 'claude-3-haiku-20240307',  // 可以使用 claude-3-sonnet 获得更好的结果
      max_tokens: 2000,
      messages: [
        {
          role: 'user',
          content: promptText
        }
      ]
    });

    const options = {
      hostname: 'api.anthropic.com',
      port: 443,
      path: '/v1/messages',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-api-key': apiKey,
        'anthropic-version': '2023-06-01',
        'Content-Length': Buffer.byteLength(requestData)
      },
      timeout: 60000
    };

    console.log('[Anthropic API] Sending request...');

    const req = https.request(options, (res) => {
      let data = '';

      res.on('data', (chunk) => {
        data += chunk;
      });

      res.on('end', () => {
        try {
          const response = JSON.parse(data);
          
          if (response.error) {
            reject(new Error(`Anthropic API Error: ${response.error.message}`));
            return;
          }

          if (response.content && response.content[0] && response.content[0].text) {
            const generatedCode = response.content[0].text;
            console.log('[Anthropic API] Successfully received response');
            resolve(generatedCode);
          } else {
            reject(new Error('Unexpected API response format'));
          }
        } catch (error) {
          reject(new Error(`Failed to parse API response: ${error.message}`));
        }
      });
    });

    req.on('error', (error) => {
      reject(new Error(`Request failed: ${error.message}`));
    });

    req.on('timeout', () => {
      req.destroy();
      reject(new Error('Request timeout'));
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

  if (!issueNumber) {
    console.error('Error: --issue-number is required');
    process.exit(1);
  }

  // 获取 API Key
  const openaiKey = process.env.OPENAI_API_KEY;
  const anthropicKey = process.env.ANTHROPIC_API_KEY;

  if (!openaiKey && !anthropicKey) {
    console.error('Error: Neither OPENAI_API_KEY nor ANTHROPIC_API_KEY environment variable is set');
    console.error('Please set one of them to use AI code generation');
    process.exit(1);
  }

  try {
    let generatedCode;

    // 优先使用 OpenAI，如果没有则使用 Anthropic
    if (openaiKey) {
      console.log('[AI Generation] Using OpenAI API...');
      generatedCode = await generateWithOpenAI(issueNumber, issueBody, openaiKey);
    } else {
      console.log('[AI Generation] Using Anthropic Claude API...');
      generatedCode = await generateWithClaude(issueNumber, issueBody, anthropicKey);
    }

    // 写入文件
    const outPath = path.resolve(outDir);
    fs.mkdirSync(outPath, { recursive: true });
    
    const outFile = path.join(outPath, 'generated_by_ai.txt');
    fs.writeFileSync(outFile, generatedCode, 'utf8');
    
    console.log('[AI Generation] Code written to', outFile);
    console.log('[AI Generation] Code length:', generatedCode.length, 'characters');
    
    process.exit(0);
  } catch (error) {
    console.error('[AI Generation] Failed:', error.message);
    
    // 创建错误占位符
    const outPath = path.resolve(outDir);
    fs.mkdirSync(outPath, { recursive: true });
    const outFile = path.join(outPath, 'generated_by_ai.txt');
    fs.writeFileSync(outFile, `# AI generation failed for issue #${issueNumber}\n# Error: ${error.message}\n`, 'utf8');
    
    process.exit(1);
  }
})();
