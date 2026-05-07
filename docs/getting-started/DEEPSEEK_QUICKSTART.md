# 🚀 DeepSeek 快速开始（5分钟配置）

## 你有 DeepSeek API Key？太好了！

只需要 **3 步**，就可以在 GitHub Actions 中使用 DeepSeek 自动生成代码。

---

## ⚡ 3 步配置

### 第 1 步：获取 API Key（如果还没有）

1. 访问 https://platform.deepseek.com/
2. 注册账号并完成实名认证
3. 进入 "API Keys" 页面
4. 点击 "创建 API Key"
5. 复制 Key（格式如：`sk-xxxxxxxxxxxxx`）

---

### 第 2 步：添加到 GitHub Secrets

1. 打开你的 GitHub 仓库
2. 点击顶部菜单 **Settings**
3. 左侧菜单选择 **Secrets and variables** → **Actions**
4. 点击绿色按钮 **New repository secret**
5. 填写：
   - **Name**: `DEEPSEEK_API_KEY`
   - **Value**: 粘贴你的 DeepSeek API Key
6. 点击 **Add secret**

完成！✅

---

### 第 3 步：测试

1. 在你的仓库创建一个新 issue，比如：
   ```markdown
   标题：创建一个计算阶乘的 Python 函数
   
   内容：
   要求：
   1. 支持递归和迭代两种方式
   2. 添加输入验证
   3. 处理大数情况
   ```

2. 等待几秒钟，GitHub Actions 会自动运行

3. 查看生成的 PR，你会看到 DeepSeek 生成的代码！🎉

---

## 📊 你将看到什么

### Actions 日志输出示例：

```
==========================================
Processing Issue #1
Base branch: main
Strategy: auto
==========================================
✅ DEEPSEEK_API_KEY is configured
[Strategy] Auto-selected: DEEPSEEK API (API key found)
[DeepSeek] Attempting to use DeepSeek API...
[DeepSeek] Using deepseek-coder model (optimized for code generation)
DeepSeek stdout: [DeepSeek] ✅ Code written to auto_impl/issue-1/generated_by_deepseek.txt
[DeepSeek] ✅ Total length: 1847 characters
[DeepSeek] ✅ Successfully generated code via DeepSeek API.
✅ Created PR #2: https://github.com/yourname/repo/pull/2
```

---

## 💡 提示

### 新用户有免费额度吗？
✅ **是的！** DeepSeek 新用户有免费额度（约 500M tokens），足够测试很多次了。

### 需要充值吗？
❌ **不需要马上充值**，先用免费额度测试。

如果额度用完了，充值也很便宜：
- deepseek-coder: ¥1/M tokens（输入）
- 单次代码生成约 500 tokens，成本约 ¥0.0015

### 可以和其他 API 一起用吗？
✅ **可以！** 你可以同时配置多个 API Keys：
```
DEEPSEEK_API_KEY: sk-xxx...    # 优先使用
OPENAI_API_KEY: sk-xxx...      # 备选
```

系统会优先使用 DeepSeek（推荐）。

---

## 🐛 遇到问题？

### 常见问题

**Q: 工作流显示 "DEEPSEEK_API_KEY not set"**
- 检查 Secrets 名称是否拼写正确（必须大写）
- 检查 Key 是否已保存

**Q: 显示 "Insufficient balance"**
- 免费额度用完了
- 访问 https://platform.deepseek.com/ 充值

**Q: 生成的代码不符合预期**
- 在 issue 中提供更详细的描述
- 指定编程语言
- 添加示例

**更多帮助**：[详细配置指南](./DEEPSEEK_GUIDE.md)

---

## ✅ 验证清单

配置完成后，请确认：

- [ ] 已在 DeepSeek 平台注册并获取 API Key
- [ ] 已将 `DEEPSEEK_API_KEY` 添加到 GitHub Secrets
- [ ] 已提交所有代码修改到仓库
- [ ] 已创建测试 issue
- [ ] 在 Actions 中看到 "DEEPSEEK_API_KEY is configured"

---

## 🎉 完成！

现在每当你创建 issue，DeepSeek 都会自动生成代码！

**下一步**：查看 [详细配置指南](./DEEPSEEK_GUIDE.md) 了解更多高级用法。

---

## 📞 需要帮助？

1. 查看完整日志：Actions → Harness → 点击运行记录 → 查看日志
2. 阅读 [CONFIGURATION.md](./CONFIGURATION.md)
3. 检查 DeepSeek 官方文档：https://platform.deepseek.com/docs

**祝你使用愉快！** 🚀
