# 🚨 紧急修复：添加缺失的参数

## 问题
```
issue_to_branch.py: error: unrecognized arguments: --strategy modular
```

## 原因
简化脚本时删除了 `--strategy` 参数，但 GitHub Actions 仍在传递它。

## 修复
添加了 `--strategy` 和 `--opencode-key` 参数（为兼容性保留，实际不使用）。

## 立即执行

```bash
git add issue_to_branch.py
git commit -m "fix: add missing --strategy argument for compatibility

- Add --strategy parameter (accepted but ignored)
- Add --opencode-key parameter (accepted but ignored)
- Always uses modular generation regardless of strategy"
git push origin main
```

## 重新测试

1. 推送修复后
2. 重新运行 Actions（或创建新 Issue）
3. 这次应该能正常工作！

## 预期输出
```
[Strategy] Using MODULAR code generation (multi-file package structure)
🚀 Generating modular code...
  ✅ src/main/java/.../user/UserController.java
  ✅ src/main/java/.../user/UserService.java
  ...
✅ Successfully generated X Java files
```

## 如果还有问题

告诉我新的错误信息！
