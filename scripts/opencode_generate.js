#!/usr/bin/env node
// Lightweight bridge to OpenCode SDK for local code generation
// Usage:
// node scripts/opencode_generate.js --issue-number 42 --issue-body "<text>" --outdir ./auto_impl/issue-42

const fs = require('fs');
const path = require('path');
const { createOpencode } = require('@opencode-ai/sdk');

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

(async () => {
  const args = parseArgs(process.argv);
  const issueNumber = parseInt(args['issue-number'], 10) || 0;
  const issueBody = args['issue-body'] || '';
  const outDir = args['outdir'] || path.resolve('auto_impl');

  const outPath = path.resolve(outDir);
  fs.mkdirSync(outPath, { recursive: true });

  // Start a local OpenCode server and generate code via SDK
  try {
    const { client, server } = await createOpencode({ hostname: '127.0.0.1', port: 4096, config: {} });
    // Create a session and prompt
    let code = '';
    try {
      const session = await client.session.create({ body: { title: `Issue ${issueNumber} SDK Gen` } });
      const result = await client.session.prompt({ path: { id: session.id }, body: { parts: [{ type: 'text', text: issueBody }] } });
      if (result) {
        if (result.data && typeof result.data.code === 'string') code = result.data.code;
        if (!code && result.data && typeof result.data.text === 'string') code = result.data.text;
        if (!code && typeof result.text === 'string') code = result.text;
      }
    } catch (e) {
      // If SDK paths fail, we'll fallback to empty code
    }

    const outFile = path.join(outPath, 'generated_by_sdk.txt');
    if (code && code.length > 0) {
      fs.writeFileSync(outFile, code, 'utf8');
      console.log('SDK-generated code written to', outFile);
    } else {
      // Fallback placeholder to ensure downstream steps continue
      fs.writeFileSync(outFile, '# placeholder generated code (SDK produced nothing)\n', 'utf8');
      console.log('SDK did not return code; wrote placeholder to', outFile);
    }
    if (server && typeof server.close === 'function') server.close();
  } catch (err) {
    // If SDK path fails entirely, write a placeholder
    const outFile = path.join(outPath, 'generated_by_sdk.txt');
    fs.mkdirSync(outPath, { recursive: true });
    fs.writeFileSync(outFile, '# placeholder generated code (SDK bridge failed)\n', 'utf8');
    console.error('OpenCode SDK bridge failed:', err && err.message ? err.message : err);
    process.exit(2);
  }
})();
