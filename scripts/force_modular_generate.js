#!/usr/bin/env node
// 强制模块化代码生成 - 只生成多文件包结构，永不生成单文件
// Usage:
// node scripts/force_modular_generate.js --issue-number 42 --issue-body "<text>" --outdir ./auto_impl/issue-42

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

// 检查 API Key
const apiKey = process.env.DEEPSEEK_API_KEY;
if (!apiKey) {
  console.error('❌ Error: DEEPSEEK_API_KEY environment variable is not set');
  process.exit(1);
}

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

console.log('\n========================================');
console.log('🔧 强制模块化代码生成');
console.log(`📋 Issue #${issueNumber}`);
console.log('📁 只生成多文件包结构（禁止单文件.txt）');
console.log('========================================\n');

// 直接调用模块化生成器
const modularScript = path.join(__dirname, 'opencode_generate_modular.js');

if (!fs.existsSync(modularScript)) {
  console.error(`❌ Error: Modular generator not found at ${modularScript}`);
  process.exit(1);
}

const cmd = `DEEPSEEK_API_KEY=${apiKey} node "${modularScript}" --issue-number ${issueNumber} --issue-body ${JSON.stringify(issueBody)} --outdir "${outDir}"`;

try {
  console.log('🚀 启动模块化代码生成器...\n');
  execSync(cmd, { stdio: 'inherit' });
  console.log('\n✅ 模块化代码生成完成');
  process.exit(0);
} catch (error) {
  console.error('\n❌ 模块化生成失败:', error.message);
  process.exit(1);
}
