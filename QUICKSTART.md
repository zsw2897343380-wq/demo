# 🚀 快速参考卡片

## 第一次使用？

```bash
# 1. 提交所有修改到仓库
git add .
git commit -m "feat: add issue-to-branch automation with multi-strategy code generation"
git push origin main

# 2. 配置 GitHub Actions 权限
# Settings → Actions → General → "Read and write permissions" → Save

# 3. 测试（创建新 issue）
# 工作流会自动运行

# 4. （可选）添加 AI 能力
# Settings → Secrets → OPENAI_API_KEY
```

---

## 🎯 四种使用模式

### 模式 1：基础模式（推荐新手）

**配置**：无需任何配置

**效果**：生成代码模板和占位符

**触发方式**：
- 创建 issue 自动触发
- 或 Actions → Harness → Run workflow

---

### 模式 2：DeepSeek AI 模式（🇨🇳 中国用户强烈推荐！）

**配置**：
```
GitHub Secrets:
  DEEPSEEK_API_KEY=sk-xxxxxxxxxxxxx
```

**效果**：使用 DeepSeek 智能生成代码
- ✅ 中文理解能力最强
- ✅ 成本最低（约 ¥1-2/M tokens）
- ✅ 国内访问稳定

**获取 API Key**：https://platform.deepseek.com/

**详细文档**：[DEEPSEEK_GUIDE.md](./DEEPSEEK_GUIDE.md)

---

### 模式 3：OpenAI/Anthropic AI 模式

**配置**：
```
GitHub Secrets:
  OPENAI_API_KEY=sk-xxxxxxxxxxxxx
  # 或
  ANTHROPIC_API_KEY=sk-ant-xxxxxxxxxxxxx
```

**效果**：使用 GPT/Claude 智能生成代码

**触发方式**：
- 创建 issue 自动使用 AI
- 自动检测 API Key 并选择最佳策略

---

### 模式 4：精确控制

**触发方式**：Actions → Harness → Run workflow

**参数**：
- Issue number: `42`
- Base branch: `main` (或 `develop`)
- Strategy: 
  - `auto` - 自动选择（推荐）
  - `deepseek` - 强制 DeepSeek
  - `openai` - 强制 OpenAI
  - `anthropic` - 强制 Claude
  - `template` - 强制模板

---

## 📁 文件结构

```
.
├── .github/workflows/main.yml          # GitHub Actions 工作流
├── scripts/
│   ├── opencode_generate.js            # 模板生成器（默认）
│   ├── opencode_generate_with_ai_api.js # OpenAI/Anthropic API
│   └── opencode_generate_with_sdk.js   # OpenCode SDK
├── issue_to_branch.py                  # 主自动化脚本
├── run_issue_to_branch.py              # 入口包装器
├── SDK_COMPATIBILITY.md                # SDK 兼容性分析
├── CONFIGURATION.md                    # 详细配置指南
├── FIXES.md                            # 原始问题修复说明
└── SUMMARY.md                          # 修改总结
```

---

## 🔧 常见问题速查

| 问题 | 快速解决 |
|------|----------|
| **工作流失败** | 检查 Actions 日志，通常是权限问题 |
| **生成占位符** | 正常！添加 `OPENAI_API_KEY` 获得 AI 生成 |
| **PR 创建失败** | Settings → Actions → 开启读写权限 |
| **API 超时** | 正常现象，复杂请求需要 30-60 秒 |
| **选择策略** | 手动触发时可选择 `strategy` 参数 |

---

## 💡 技巧

### 技巧 1：快速测试
```bash
# 本地测试 Python 脚本
export GITHUB_TOKEN="ghp_xxx"
export OPENAI_API_KEY="sk-xxx"
python3 issue_to_branch.py \
  --issue-number 1 \
  --repo "username/repo" \
  --base main \
  --token "$GITHUB_TOKEN"
```

### 技巧 2：查看生成的代码
```bash
# 工作流会在 PR 中显示生成的文件
# 或直接查看 auto_impl/issue-{number}/ 目录
cat auto_impl/issue-1/auto_generated_code.txt
```

### 技巧 3：强制使用特定策略
```bash
# 在 Python 脚本中指定策略
python3 issue_to_branch.py ... --strategy openai

# 或在 GitHub Actions 手动触发时选择
```

---

## 📊 监控

### 查看 API 用量

**OpenAI**：https://platform.openai.com/usage

**Anthropic**：https://console.anthropic.com/

### 查看 Actions 历史

GitHub → Repository → Actions → Harness

---

## 🎓 学习路径

```
第 1 天：基础模式
├── 提交代码
├── 创建 issue 测试
└── 查看生成的 PR

第 2-3 天：AI 增强
├── 申请 OpenAI API Key
├── 添加到 GitHub Secrets
└── 测试 AI 生成质量

第 1 周：优化调整
├── 根据反馈调整 issue 描述格式
├── 测试不同策略的效果
└── 决定是否添加 Anthropic 备选

持续：日常使用
├── 创建 issue 自动获得代码
├── 审查和修改 AI 生成的代码
└── 合并 PR
```

---

## 🔗 有用链接

- [详细配置指南](./CONFIGURATION.md)
- [SDK 兼容性分析](./SDK_COMPATIBILITY.md)
- [OpenAI API 文档](https://platform.openai.com/docs)
- [Anthropic API 文档](https://docs.anthropic.com/)

---

## 📞 需要帮助？

1. 查看 [CONFIGURATION.md](./CONFIGURATION.md) 的故障排除章节
2. 检查 Actions 日志获取详细错误信息
3. 确认 API Keys 正确配置

---

**准备好了吗？** 现在就去提交代码并测试吧！🎉
