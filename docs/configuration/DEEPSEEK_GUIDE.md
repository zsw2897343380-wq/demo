# 🚀 DeepSeek API 配置指南

## 简介

DeepSeek（深度求索）是一家中国 AI 公司，提供高性能、低成本的代码生成模型。特别适合中国用户使用，具有：

- ✅ **中文理解能力强** - 对中文 issue 描述理解更好
- ✅ **性价比高** - 成本远低于 OpenAI/Anthropic
- ✅ **代码生成优秀** - deepseek-coder 专门针对代码优化
- ✅ **国内访问稳定** - 国内网络环境友好

---

## 🔑 获取 API Key

### 步骤 1：注册账号
1. 访问 https://platform.deepseek.com/
2. 点击注册，使用邮箱或手机号注册
3. 完成实名认证（需要身份证）

### 步骤 2：获取 API Key
1. 登录后进入控制台
2. 点击左侧菜单 "API Keys"
3. 点击 "创建 API Key"
4. 复制生成的 Key（格式：`sk-xxxxxxxxxxxxx`）

### 步骤 3：充值（可选）
- 新用户有免费额度（约 500M tokens）
- 点击 "充值" 添加余额
- 支持支付宝、微信支付

---

## ⚙️ 配置到 GitHub

### 方法：使用 GitHub Secrets

1. 进入你的 GitHub 仓库
2. 点击 **Settings** → **Secrets and variables** → **Actions**
3. 点击 **New repository secret**
4. 填写：
   - **Name**: `DEEPSEEK_API_KEY`
   - **Value**: 你的 DeepSeek API Key（如 `sk-xxxxxxxxxxxxx`）
5. 点击 **Add secret**

完成！✅ 下次工作流运行时将自动使用 DeepSeek。

---

## 💰 费用说明

### DeepSeek 模型价格

| 模型 | 输入价格 | 输出价格 | 特点 |
|------|----------|----------|------|
| deepseek-chat | ¥1/M tokens | ¥2/M tokens | 通用对话 |
| deepseek-coder | ¥1/M tokens | ¥2/M tokens | 代码专用 ⭐ |

### 成本对比（单次代码生成约 500 tokens）

| 服务商 | 单次成本 | 100次/月 |
|--------|----------|----------|
| **DeepSeek** | ¥0.0015 | ¥0.15 |
| OpenAI GPT-3.5 | $0.001 (~¥0.007) | $0.1 (~¥0.7) |
| Anthropic Claude | $0.00125 (~¥0.009) | $0.125 (~¥0.9) |

**结论**：DeepSeek 是最经济的选择，特别适合中国用户！💰

---

## 🎯 使用方式

### 方式 1：自动检测（推荐）

只需配置 `DEEPSEEK_API_KEY`，工作流会自动选择 DeepSeek：

```
[Strategy] Auto-selected: DEEPSEEK API (API key found)
[DeepSeek] Using deepseek-coder model
[DeepSeek] ✅ Successfully generated code
```

### 方式 2：手动指定

在手动触发工作流时选择策略：

1. 进入 **Actions** → **Harness** → **Run workflow**
2. 填写 Issue number
3. **Strategy** 选择 `deepseek`
4. 点击 **Run workflow**

---

## 📝 示例

### 输入 Issue

```markdown
创建一个 Python 函数，用于计算斐波那契数列
要求：
1. 支持递归和迭代两种方式
2. 添加输入验证
3. 包含时间复杂度分析
```

### DeepSeek 生成代码示例

```python
"""
斐波那契数列计算模块
包含递归和迭代两种实现方式
"""

def fibonacci_recursive(n):
    """
    递归方式计算斐波那契数列
    时间复杂度：O(2^n)
    空间复杂度：O(n) - 调用栈深度
    """
    if not isinstance(n, int):
        raise TypeError("输入必须是整数")
    if n < 0:
        raise ValueError("输入必须是非负整数")
    if n <= 1:
        return n
    return fibonacci_recursive(n - 1) + fibonacci_recursive(n - 2)


def fibonacci_iterative(n):
    """
    迭代方式计算斐波那契数列
    时间复杂度：O(n)
    空间复杂度：O(1)
    """
    if not isinstance(n, int):
        raise TypeError("输入必须是整数")
    if n < 0:
        raise ValueError("输入必须是非负整数")
    if n <= 1:
        return n
    
    a, b = 0, 1
    for _ in range(2, n + 1):
        a, b = b, a + b
    return b


# 示例用法
if __name__ == "__main__":
    # 测试递归方式
    print("递归方式:")
    for i in range(10):
        print(f"F({i}) = {fibonacci_recursive(i)}")
    
    # 测试迭代方式
    print("\n迭代方式:")
    for i in range(10):
        print(f"F({i}) = {fibonacci_iterative(i)}")
```

---

## 🔧 高级配置

### 切换模型

工作流默认使用 `deepseek-coder` 模型（最适合代码生成）。

如需使用 `deepseek-chat` 模型，可以修改脚本参数：

```javascript
// 在 opencode_generate_with_deepseek.js 中
node scripts/opencode_generate_with_deepseek.js --model chat
```

### 调整生成参数

在脚本中可以调整：

```javascript
{
  model: 'deepseek-coder',
  temperature: 0.5,  // 创造性程度（0-1）
  max_tokens: 3000   // 最大生成长度
}
```

---

## 🐛 故障排除

### 问题 1：API Key 无效

**症状**：
```
[DeepSeek] ❌ Failed: DeepSeek API Error: Invalid API key
```

**解决**：
1. 检查 Key 是否复制完整（包含 `sk-` 前缀）
2. 在 DeepSeek 控制台确认 Key 状态为 "有效"
3. 重新创建 Key 并更新 GitHub Secrets

### 问题 2：余额不足

**症状**：
```
[DeepSeek] ❌ Failed: Insufficient balance
```

**解决**：
1. 登录 https://platform.deepseek.com/
2. 进入 "充值" 页面
3. 添加余额（支持支付宝、微信）

### 问题 3：网络超时

**症状**：
```
[DeepSeek] ❌ Failed: Request timeout (120s)
```

**解决**：
- 正常现象，复杂请求可能需要 60-120 秒
- 已设置 120 秒超时，如仍超时请重试
- 或简化 issue 描述

### 问题 4：生成的代码不符合预期

**解决**：
1. 提供更详细的 issue 描述
2. 指定编程语言
3. 添加示例输入/输出

---

## 🆚 与其他方案对比

| 特性 | DeepSeek | OpenAI | Anthropic | Template |
|------|----------|--------|-----------|----------|
| **中文理解** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | N/A |
| **代码质量** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ |
| **成本** | 💰 最低 | 💰💰 中等 | 💰💰 中等 | 免费 |
| **国内访问** | ✅ 稳定 | ⚠️ 可能不稳定 | ⚠️ 可能不稳定 | ✅ |
| **配置难度** | 简单 | 简单 | 简单 | 无需配置 |

**推荐**：如果你在国内且主要是中文需求，DeepSeek 是最佳选择！🇨🇳

---

## 📚 相关链接

- [DeepSeek 官方文档](https://platform.deepseek.com/docs)
- [DeepSeek API 定价](https://platform.deepseek.com/pricing)
- [通用配置指南](./CONFIGURATION.md)

---

## ✅ 快速检查清单

配置 DeepSeek 前，请确认：

- [ ] 已注册 DeepSeek 账号
- [ ] 已完成实名认证
- [ ] 已创建 API Key
- [ ] 已将 Key 添加到 GitHub Secrets（名称：`DEEPSEEK_API_KEY`）
- [ ] （可选）已充值或还有免费额度

完成后，创建一个新 issue 测试吧！🎉
