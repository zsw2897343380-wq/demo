# OpenCode SDK 兼容性分析报告

## 文档概览

根据 [OpenCode SDK 文档](https://opencode.ai/docs/zh-cn/sdk/)，OpenCode SDK 是一个用于与 OpenCode 服务器交互的 JavaScript/TypeScript 客户端。

## SDK 关键特性

### 1. 安装方式
```bash
npm install @opencode-ai/sdk
```

### 2. 核心 API
- `createOpencode()` - 启动服务器并创建客户端
- `client.session.create()` - 创建会话
- `client.session.prompt()` - 发送提示词并获取 AI 响应
- `client.find.text()` - 搜索文件内容
- `client.file.read()` - 读取文件

### 3. 服务器要求
SDK 需要连接到一个运行的 OpenCode 服务器：
- 默认地址：`http://127.0.0.1:4096`
- SDK 可以自动启动服务器（`createOpencode()`）
- 也可以连接已运行的服务器（`createOpencodeClient()`）

## 当前代码兼容性分析

### ✅ 方案 A：纯 Node.js 实现（当前采用）

**实现方式**（`scripts/opencode_generate.js`）：
```javascript
// 纯 Node.js，无需外部依赖
const fs = require('fs');
const path = require('path');

function generateCodeFromIssue(issueNumber, issueBody) {
  // 基于 issue 内容生成代码模板
  // 不依赖外部 AI 服务
}
```

**优点**：
- ✅ **无需安装 npm 包** - 直接使用 Node.js 内置模块
- ✅ **不依赖外部服务器** - 不需要启动 OpenCode 服务器
- ✅ **GitHub Actions 友好** - 执行速度快，无额外依赖
- ✅ **稳定可靠** - 不会因网络或服务问题失败
- ✅ **零配置** - 开箱即用

**缺点**：
- ❌ 生成的是模板代码，不是 AI 生成的智能实现
- ❌ 无法利用 OpenCode 的代码理解和生成能力

**适用场景**：
- CI/CD 自动化工作流
- 快速生成代码框架
- 无 OpenCode 环境的服务器

---

### 🔧 方案 B：使用 OpenCode SDK（可选增强）

**实现方式**：
```javascript
const { createOpencode } = require("@opencode-ai/sdk");

async function generateCodeWithAI(issueNumber, issueBody) {
  // 启动 OpenCode 服务器
  const { client, server } = await createOpencode({
    hostname: '127.0.0.1',
    port: 4096,
    config: {
      model: "anthropic/claude-3-5-sonnet-20241022",
    }
  });
  
  // 创建会话
  const session = await client.session.create({
    body: { title: `Issue ${issueNumber} Code Generation` }
  });
  
  // 发送提示词
  const result = await client.session.prompt({
    path: { id: session.id },
    body: {
      parts: [{ 
        type: "text", 
        text: `Generate implementation code for: ${issueBody}` 
      }]
    }
  });
  
  // 提取生成的代码
  const code = result.data?.parts?.[0]?.text || '';
  
  // 关闭服务器
  server.close();
  
  return code;
}
```

**优点**：
- ✅ **AI 智能生成** - 利用大模型理解需求并生成代码
- ✅ **上下文感知** - 可以读取现有代码库进行参考
- ✅ **高质量输出** - 生成的代码更符合实际需求

**缺点**：
- ❌ **需要安装 npm 包** - `@opencode-ai/sdk`
- ❌ **需要 OpenCode 环境** - 服务器启动需要配置
- ❌ **GitHub Actions 复杂** - 需要：
  - 预装 OpenCode CLI 或相关依赖
  - 配置 API Keys（Anthropic, OpenAI 等）
  - 可能还需要安装 opencode 主程序
- ❌ **执行时间较长** - 启动服务器 + AI 生成
- ❌ **成本** - 调用 AI API 可能产生费用

**适用场景**：
- 本地开发环境
- 有完整 OpenCode 配置的服务器
- 需要高质量 AI 生成代码的场景

---

## GitHub Actions 兼容性

### 当前方案 A 在 Actions 中的运行流程
```
GitHub Actions Runner
├── 检出代码
├── 安装 Python 依赖 (pip install requests)
├── 运行 Python 脚本
│   ├── 调用 Node.js 脚本（纯 JS，无依赖）
│   ├── 生成代码模板
│   └── 创建 PR
└── 完成
```

**执行时间**：约 10-30 秒

### 方案 B 在 Actions 中的运行流程
```
GitHub Actions Runner
├── 检出代码
├── 安装 Node.js 依赖
│   └── npm install @opencode-ai/sdk
├── 安装 OpenCode 环境（可能）
├── 配置 API Keys
├── 运行脚本
│   ├── 启动 OpenCode 服务器
│   ├── 创建会话并发送提示词
│   ├── 等待 AI 响应
│   ├── 提取生成代码
│   ├── 关闭服务器
│   └── 创建 PR
└── 完成
```

**执行时间**：约 30-120 秒（取决于 AI 响应时间）

---

## 推荐方案

对于 **GitHub Actions 自动化工作流**，推荐继续使用 **方案 A**，原因：

1. **可靠性**：不会因为 OpenCode 服务器启动失败或 AI API 限制而中断
2. **速度**：执行更快，不会阻塞 CI/CD 流程
3. **成本**：无需支付 AI API 调用费用
4. **维护性**：依赖更少，更容易维护

### 如何获得 AI 生成能力？

如果需要真正的 AI 代码生成，可以考虑：

1. **方案 A + OpenAI/Anthropic API 直接调用**
   - 直接使用 HTTP API，无需 OpenCode SDK
   - 更简单，更可控

2. **方案 B（本地运行）**
   - 在本地开发环境使用 OpenCode SDK
   - 将生成结果手动提交

3. **混合方案**
   - 本地使用 OpenCode SDK 生成高质量代码
   - CI/CD 使用方案 A 作为 fallback

---

## 如果你确实想使用 OpenCode SDK

我已经为你准备了支持 SDK 的脚本版本，请查看：
- `scripts/opencode_generate_with_sdk.js` - 使用 OpenCode SDK 的版本
- 需要配合 GitHub Secrets 配置 API Keys
- 需要修改 `package.json` 添加依赖

### 配置步骤：

1. **安装依赖**
   ```bash
   npm init -y
   npm install @opencode-ai/sdk
   ```

2. **配置 GitHub Secrets**
   - `ANTHROPIC_API_KEY` 或 `OPENAI_API_KEY`
   - 可能需要其他提供商的 Key

3. **修改 Actions 工作流**
   ```yaml
   - name: Install Node.js dependencies
     run: npm ci
   
   - name: Run Issue-to-Branch with SDK
     env:
       GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
       ANTHROPIC_API_KEY: ${{ secrets.ANTHROPIC_API_KEY }}
     run: python3 run_issue_to_branch.py ...
   ```

---

## 总结

| 特性 | 方案 A（纯 Node.js） | 方案 B（OpenCode SDK） |
|------|---------------------|----------------------|
| npm 依赖 | ❌ 无 | ✅ 需要 |
| 外部服务器 | ❌ 不需要 | ✅ 需要 |
| GitHub Actions 友好度 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 执行速度 | ⚡ 快 | 🐢 慢 |
| AI 生成质量 | 📝 模板 | 🧠 智能 |
| 成本 | 免费 | 可能收费 |
| 可靠性 | 高 | 中等 |

**结论**：当前采用的方案 A 是 GitHub Actions 环境的最佳选择。如果需要 AI 能力，建议使用直接 HTTP API 调用，而非完整的 OpenCode SDK。
