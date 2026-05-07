# 🚀 Issue-to-Branch 自动化配置指南

## 快速开始

### 1. 基础配置（无需 API Keys）

**默认工作方式**：使用纯 Node.js 模板生成
- ✅ 无需任何 API Keys
- ✅ 稳定可靠，永不失败
- ✅ 生成代码框架和占位符

**配置步骤**：
1. 将工作流文件提交到仓库
2. 确保 GitHub Actions 有写入权限：
   - Settings → Actions → General → "Read and write permissions"
3. 完成！🎉

---

## 🧠 AI 增强配置（推荐）

### 方案 A：OpenAI GPT（推荐）

**特点**：
- 🤖 使用 GPT-3.5/4 生成代码
- 💰 成本较低（GPT-3.5 每 1K tokens 约 $0.002）
- ⚡ 响应快速
- 📝 代码质量高

**配置步骤**：

1. **获取 OpenAI API Key**
   - 访问 https://platform.openai.com/api-keys
   - 创建新的 API Key
   - 复制 Key 值

2. **添加到 GitHub Secrets**
   ```
   Settings → Secrets and variables → Actions → New repository secret
   Name: OPENAI_API_KEY
   Value: sk-xxxxxxxxxxxxxxxxx
   ```

3. **测试**
   - 创建一个新 issue
   - 工作流会自动使用 OpenAI 生成代码

---

### 方案 B：Anthropic Claude

**特点**：
- 🤖 使用 Claude 3 生成代码
- 🧠 代码理解能力更强
- 💰 成本适中
- 📚 擅长处理复杂需求

**配置步骤**：

1. **获取 Anthropic API Key**
   - 访问 https://console.anthropic.com/
   - 创建 API Key
   - 复制 Key 值

2. **添加到 GitHub Secrets**
   ```
   Settings → Secrets and variables → Actions → New repository secret
   Name: ANTHROPIC_API_KEY
   Value: sk-ant-api03-xxxxxxxxxxxxxxxxx
   ```

3. **测试**
   - 创建一个新 issue
   - 工作流会自动使用 Claude 生成代码

---

### 方案 C：同时使用多个 API

如果你有多个 API Keys，系统会按以下优先级选择：

1. **OpenAI**（如果有 OPENAI_API_KEY）
2. **Anthropic**（如果有 ANTHROPIC_API_KEY）
3. **Template**（如果没有 API Keys）

**建议配置**：
```yaml
# GitHub Secrets 配置
OPENAI_API_KEY: sk-xxx...    # 主要使用
ANTHROPIC_API_KEY: sk-ant... # 备选
```

---

## 📊 生成策略对比

| 策略 | 质量 | 速度 | 成本 | 稳定性 | 适用场景 |
|------|------|------|------|--------|----------|
| **OpenAI** ⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 💰 低 | ⭐⭐⭐⭐ | 日常开发 |
| **Anthropic** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | 💰 中 | ⭐⭐⭐⭐ | 复杂需求 |
| **Template** | ⭐⭐ | ⭐⭐⭐⭐⭐ | 免费 | ⭐⭐⭐⭐⭐ | CI/CD、基础框架 |
| **OpenCode SDK** | ⭐⭐⭐⭐⭐ | ⭐⭐ | 免费* | ⭐⭐⭐ | 本地开发 |

*OpenCode SDK 免费但需要本地配置

---

## 🔧 高级配置

### 手动选择生成策略

在手动触发工作流时，可以选择特定策略：

1. 进入 Actions → Harness
2. 点击 "Run workflow"
3. 选择 **Strategy**：
   - `auto` - 自动选择（推荐）
   - `openai` - 强制使用 OpenAI
   - `anthropic` - 强制使用 Anthropic
   - `opencode` - 使用 OpenCode SDK（需要本地配置）
   - `template` - 强制使用模板

### 环境变量配置

| 变量名 | 用途 | 必需 |
|--------|------|------|
| `GITHUB_TOKEN` | GitHub API 认证 | ✅ 自动提供 |
| `OPENAI_API_KEY` | OpenAI API 认证 | ❌ 可选 |
| `ANTHROPIC_API_KEY` | Anthropic API 认证 | ❌ 可选 |
| `OPENCODE_API_KEY` | 兼容性保留 | ❌ 可选 |

---

## 📝 使用示例

### 示例 1：Issue 触发（自动）

当有人创建或编辑 issue 时，工作流自动运行：

```yaml
# 工作流会自动：
# 1. 读取 issue 内容
# 2. 使用最佳可用策略生成代码
# 3. 创建分支并提交代码
# 4. 创建 PR
```

### 示例 2：手动触发（带参数）

```bash
# 在 GitHub Actions UI 中：
# Issue number: 42
# Base branch: develop
# Strategy: openai
```

### 示例 3：代码生成效果

**输入 Issue**：
```markdown
创建一个用户认证系统
- 支持邮箱和密码登录
- 包含密码重置功能
- 使用 JWT token
```

**OpenAI/Anthropic 生成**：
```python
import jwt
from datetime import datetime, timedelta
from typing import Optional

class AuthSystem:
    def __init__(self, secret_key: str):
        self.secret_key = secret_key
    
    def login(self, email: str, password: str) -> Optional[str]:
        \"\"\"验证用户并返回 JWT token\"\"\"
        # Implementation...
    
    def reset_password(self, email: str) -> bool:
        \"\"\"发送密码重置邮件\"\"\"
        # Implementation...
```

**Template 生成**：
```python
# Placeholder implementation
def implement_issue_42():
    \"\"\"Implementation for issue #42: 创建一个用户认证系统\"\"\"
    raise NotImplementedError("Please implement based on issue requirements")
```

---

## 💡 最佳实践

### 1. 渐进式采用

**阶段 1**：使用 Template（无需配置）
- 快速建立自动化流程
- 生成代码框架

**阶段 2**：添加 OpenAI API Key
- 获得 AI 生成能力
- 显著提升代码质量

**阶段 3**：添加 Anthropic API Key
- 备选方案
- 处理复杂需求

### 2. 成本控制

**监控使用量**：
- OpenAI: https://platform.openai.com/usage
- Anthropic: https://console.anthropic.com/

**节省成本技巧**：
- 使用 GPT-3.5 代替 GPT-4（质量足够，成本更低）
- 为复杂 issue 才使用 Anthropic
- 设置用量警报

### 3. 安全建议

- ✅ 使用 GitHub Secrets 存储 API Keys
- ✅ 定期轮换 API Keys
- ✅ 设置 API Key 使用限制
- ❌ 不要将 API Keys 提交到代码仓库

---

## 🐛 故障排除

### 问题 1：代码生成失败

**症状**：生成了占位符而不是实际代码

**解决方案**：
```bash
# 检查 API Key 是否正确设置
echo $OPENAI_API_KEY  # 应该显示你的 key

# 检查 API Key 是否有额度
# 访问 OpenAI/Anthropic 控制台查看余额
```

### 问题 2：PR 创建失败

**症状**：`Validation Failed` 或 `No commits`

**解决方案**：
1. 检查分支是否正确推送
2. 确保 GitHub Actions 有写入权限
3. 检查 base 分支是否存在

### 问题 3：API 超时

**症状**：`Request timeout`

**解决方案**：
- 这是正常的，复杂请求可能需要 30-60 秒
- 如果频繁超时，考虑使用 Template 策略

---

## 🔗 相关文档

- [OpenAI API 文档](https://platform.openai.com/docs)
- [Anthropic API 文档](https://docs.anthropic.com/)
- [SDK 兼容性分析](./SDK_COMPATIBILITY.md)
- [原始修复说明](./FIXES.md)

---

## 🤝 贡献

欢迎提交 issue 和 PR 来改进这个工作流！

---

## 📄 许可

MIT License
