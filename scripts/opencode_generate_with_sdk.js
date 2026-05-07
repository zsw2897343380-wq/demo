#!/usr/bin/env node
// OpenCode SDK 版本 - 需要安装 @opencode-ai/sdk
// Usage:
// node scripts/opencode_generate_with_sdk.js --issue-number 42 --issue-body "<text>" --outdir ./auto_impl/issue-42

const fs = require('fs');
const path = require('path');

async function generateWithOpenCodeSDK(issueNumber, issueBody, outDir) {
  try {
    // 尝试导入 OpenCode SDK
    const { createOpencode } = require('@opencode-ai/sdk');
    
    console.log('[OpenCode SDK] Starting server and creating client...');
    
    // 启动 OpenCode 服务器和创建客户端
    const { client, server } = await createOpencode({
      hostname: '127.0.0.1',
      port: 4096,
      timeout: 30000, // 30秒超时
      config: {
        // 可以在这里覆盖配置，如指定模型
        // model: "anthropic/claude-3-5-sonnet-20241022",
      }
    });
    
    console.log('[OpenCode SDK] Server started, creating session...');
    
    // 创建会话
    const session = await client.session.create({
      body: { 
        title: `Issue #${issueNumber} - Auto Code Generation`,
      }
    });
    
    console.log(`[OpenCode SDK] Session created: ${session.id}`);
    console.log('[OpenCode SDK] Sending prompt to AI...');
    
    // 构建提示词
    const promptText = `Please generate implementation code for the following issue:

Issue #${issueNumber}:
${issueBody}

Please provide:
1. A clear implementation in the appropriate programming language
2. Comments explaining the key parts
3. Any necessary imports or dependencies
4. Example usage if applicable

Generate the complete, production-ready code.`;
    
    // 发送提示词到 AI
    const result = await client.session.prompt({
      path: { id: session.id },
      body: {
        parts: [{ 
          type: "text", 
          text: promptText 
        }]
      }
    });
    
    console.log('[OpenCode SDK] Received AI response');
    
    // 提取生成的代码
    let generatedCode = '';
    
    // 根据 SDK 返回结构提取文本
    if (result && result.data) {
      if (result.data.parts && Array.isArray(result.data.parts)) {
        // 从 parts 数组中提取文本
        generatedCode = result.data.parts
          .filter(part => part.type === 'text')
          .map(part => part.text)
          .join('\n\n');
      } else if (typeof result.data.text === 'string') {
        generatedCode = result.data.text;
      } else if (result.data.info && result.data.info.text) {
        generatedCode = result.data.info.text;
      }
    }
    
    // 关闭服务器
    if (server && typeof server.close === 'function') {
      server.close();
      console.log('[OpenCode SDK] Server closed');
    }
    
    // 写入文件
    const outPath = path.resolve(outDir);
    fs.mkdirSync(outPath, { recursive: true });
    
    const outFile = path.join(outPath, 'generated_by_sdk.txt');
    
    if (generatedCode && generatedCode.trim().length > 0) {
      fs.writeFileSync(outFile, generatedCode, 'utf8');
      console.log('[OpenCode SDK] AI-generated code written to', outFile);
      console.log('[OpenCode SDK] Code length:', generatedCode.length, 'characters');
      return { success: true, file: outFile };
    } else {
      throw new Error('AI returned empty response');
    }
    
  } catch (error) {
    console.error('[OpenCode SDK] Error:', error.message);
    
    // 如果 SDK 失败，返回错误信息
    if (error.message.includes("Cannot find module '@opencode-ai/sdk'")) {
      console.error('[OpenCode SDK] Package not installed. Please run: npm install @opencode-ai/sdk');
    }
    
    throw error;
  }
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

  try {
    await generateWithOpenCodeSDK(issueNumber, issueBody, outDir);
    process.exit(0);
  } catch (error) {
    console.error('[OpenCode SDK] Failed to generate code:', error.message);
    
    // 创建占位符文件
    const outPath = path.resolve(outDir);
    fs.mkdirSync(outPath, { recursive: true });
    const outFile = path.join(outPath, 'generated_by_sdk.txt');
    fs.writeFileSync(outFile, `# OpenCode SDK generation failed for issue #${issueNumber}\n# Error: ${error.message}\n`, 'utf8');
    
    process.exit(1);
  }
})();
