# 🚀 立即开始测试（3分钟上手）

## 第一步：提交修复（1分钟）

```bash
# 1. 提交当前修复
git add issue_to_branch.py
git commit -m "fix: force modular generation, never generate single .txt file

- Simplify strategy selection to modular only
- Remove all single-file .txt generation logic
- Force multi-file package structure
- Only count .java/.py/.ts/.go/.rs as valid code files"

git push origin main
```

## 第二步：创建测试 Issue（1分钟）

访问你的 GitHub 仓库，创建新 Issue：

**标题**：测试 - 电商用户管理系统

**内容**：
```markdown
构建用户管理系统：
- 用户注册/登录
- JWT Token 认证
- 用户信息管理

使用 Java + Spring Boot + MySQL
```

## 第三步：查看结果（1分钟）

等待 1-2 分钟后：

### 查看 Actions
1. 进入仓库 → Actions 标签
2. 点击正在运行的工作流
3. 查看日志输出

### 期望看到的输出
```
[Strategy] Using MODULAR code generation (multi-file package structure)
🚀 Generating modular code...
  ✅ src/main/java/.../user/UserController.java
  ✅ src/main/java/.../user/UserService.java
  ✅ src/main/java/.../user/UserRepository.java

✅ Successfully generated 6 Java files
📦 Total files: 6
✅ Created PR #123: https://github.com/.../pull/123
```

### 检查生成的 PR
1. 查看 Pull Requests
2. 应该看到新创建的 PR
3. Files changed 中应该看到多个 .java 文件
4. **不应该**看到 `auto_generated_code.txt`（或里面只有说明文字）

---

## ✅ 成功标志

| 检查项 | 期望 | 实际 |
|--------|------|------|
| 生成 .java 文件 | ✅ 有 | ? |
| 文件在 src/ 目录下 | ✅ 是 | ? |
| 没有代码写入 .txt | ✅ 无 | ? |
| PR 成功创建 | ✅ 是 | ? |

如果全部通过，✅ **测试成功！**

---

## ❌ 如果失败

### 情况 1：Actions 显示 "No API Key"
**解决**：
```
GitHub → Settings → Secrets → DEEPSEEK_API_KEY
```

### 情况 2：生成了 .txt 文件
**检查**：
1. 是否推送了最新代码？
2. 查看 Actions 日志中的 strategy 是什么？
3. 脚本是否有执行权限？

### 情况 3：Actions 失败
**查看日志**：
1. Actions → 失败的运行 → 查看完整日志
2. 找到错误信息
3. 截图发给我

---

## 📊 记录结果

测试完成后，告诉我：

1. **生成了什么语言？** （Java/Python/其他）
2. **生成了多少文件？** （N 个）
3. **有 .txt 文件吗？** （有/没有）
4. **PR 创建成功了吗？** （成功/失败）

---

## 🎯 下一步

根据测试结果：

- ✅ **全部通过** → 继续测试其他语言（Python、TypeScript）
- ⚠️ **部分问题** → 修复后再测试
- ❌ **完全失败** → 立即查看日志，找出原因

---

**开始测试吧！完成后告诉我结果！** 🧪
