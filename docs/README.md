# 📚 文档导航

本文档库已按功能模块整理，请根据需求查看对应目录。

---

## 🚀 快速开始（Start Here）

**适合**：首次使用者

| 文档 | 内容 | 阅读时间 |
|------|------|----------|
| [getting-started/QUICKSTART.md](getting-started/QUICKSTART.md) | 5分钟快速上手指南 | 5分钟 |
| [getting-started/QUICK_TEST.md](getting-started/QUICK_TEST.md) | 3分钟测试指南 | 3分钟 |
| [getting-started/DEEPSEEK_QUICKSTART.md](getting-started/DEEPSEEK_QUICKSTART.md) | DeepSeek 快速配置 | 5分钟 |

**推荐顺序**：
1. QUICKSTART.md - 了解整体流程
2. QUICK_TEST.md - 快速测试验证
3. 根据 API 选择 DeepSeek 或 OpenAI 指南

---

## ⚙️ 配置指南（Configuration）

**适合**：配置和设置

| 文档 | 内容 | 说明 |
|------|------|------|
| [configuration/CONFIGURATION.md](configuration/CONFIGURATION.md) | 完整配置指南 | 所有生成策略对比 |
| [configuration/DEEPSEEK_GUIDE.md](configuration/DEEPSEEK_GUIDE.md) | DeepSeek 详细配置 | 🇨🇳 中国用户推荐 |
| [configuration/SDK_COMPATIBILITY.md](configuration/SDK_COMPATIBILITY.md) | SDK 兼容性分析 | 技术方案对比 |

**推荐**：根据使用的 AI 服务查看对应指南

---

## ✨ 功能说明（Features）

### 模块化代码生成（Modular）

**适合**：了解模块化生成功能

| 文档 | 内容 |
|------|------|
| [features/modular/MODULAR_GUIDE.md](features/modular/MODULAR_GUIDE.md) | 模块化生成完整指南 |
| [features/modular/MODULAR_EXAMPLES.md](features/modular/MODULAR_EXAMPLES.md) | 示例 Issue 模板 |
| [features/modular/MODULAR_SUMMARY.md](features/modular/MODULAR_SUMMARY.md) | 功能总结 |
| [features/modular/MODULAR_FIX.md](features/modular/MODULAR_FIX.md) | 修复详情 |

### 上下文感知重构（Context-Aware）- 开发中

**适合**：进阶用户

| 文档 | 内容 | 状态 |
|------|------|------|
| [features/context-aware/CONTEXT_AWARE_DESIGN.md](features/context-aware/CONTEXT_AWARE_DESIGN.md) | 设计方案 | 🚧 规划中 |

---

## 🔧 故障排除（Troubleshooting）

**适合**：遇到问题

| 文档 | 问题类型 |
|------|----------|
| [troubleshooting/FIXES.md](troubleshooting/FIXES.md) | 原始问题修复说明 |
| [troubleshooting/FIX_COMPLETE.md](troubleshooting/FIX_COMPLETE.md) | 修复完成总结 |
| [troubleshooting/FIX_SINGLE_FILE_ISSUE.md](troubleshooting/FIX_SINGLE_FILE_ISSUE.md) | 单文件问题修复 |
| [troubleshooting/HOTFIX.md](troubleshooting/HOTFIX.md) | 紧急修复记录 |

**建议**：遇到问题时按时间顺序查看（从 HOTFIX 到 FIXES）

---

## 🛠️ 开发文档（Development）

**适合**：贡献者和高级用户

| 文档 | 内容 |
|------|------|
| [development/TEST_PLAN.md](development/TEST_PLAN.md) | 测试计划 |
| [development/STATUS.md](development/STATUS.md) | 项目状态 |
| [development/SUMMARY.md](development/SUMMARY.md) | 修改总结 |
| [development/COMMIT_CHECKLIST.md](development/COMMIT_CHECKLIST.md) | 提交清单 |

---

## 📁 文档结构

```
docs/
├── README.md                           # 本文件（导航入口）
├── getting-started/                    # 🚀 快速开始
│   ├── QUICKSTART.md
│   ├── QUICK_TEST.md
│   └── DEEPSEEK_QUICKSTART.md
├── configuration/                      # ⚙️ 配置指南
│   ├── CONFIGURATION.md
│   ├── DEEPSEEK_GUIDE.md
│   └── SDK_COMPATIBILITY.md
├── features/                           # ✨ 功能说明
│   ├── modular/                        # 模块化生成
│   │   ├── MODULAR_GUIDE.md
│   │   ├── MODULAR_EXAMPLES.md
│   │   ├── MODULAR_SUMMARY.md
│   │   └── MODULAR_FIX.md
│   └── context-aware/                  # 上下文感知（开发中）
│       └── CONTEXT_AWARE_DESIGN.md
├── troubleshooting/                    # 🔧 故障排除
│   ├── FIXES.md
│   ├── FIX_COMPLETE.md
│   ├── FIX_SINGLE_FILE_ISSUE.md
│   └── HOTFIX.md
└── development/                        # 🛠️ 开发文档
    ├── TEST_PLAN.md
    ├── STATUS.md
    ├── SUMMARY.md
    └── COMMIT_CHECKLIST.md
```

---

## 🎯 使用场景速查

| 场景 | 查看文档 |
|------|----------|
| 第一次使用 | `getting-started/QUICKSTART.md` |
| 配置 DeepSeek | `configuration/DEEPSEEK_GUIDE.md` |
| 了解模块化功能 | `features/modular/MODULAR_GUIDE.md` |
| 遇到问题 | `troubleshooting/HOTFIX.md` |
| 查看示例 | `features/modular/MODULAR_EXAMPLES.md` |
| 测试功能 | `getting-started/QUICK_TEST.md` |
| 开发贡献 | `development/TEST_PLAN.md` |

---

## 📝 更新日志

**2024-XX-XX**: 文档库重新组织，按功能模块分类存放

---

## 💡 建议

1. **新用户**：从 `getting-started/QUICKSTART.md` 开始
2. **遇到问题**：查看 `troubleshooting/` 目录
3. **想了解功能**：查看 `features/` 目录
4. **要配置**：查看 `configuration/` 目录

---

**找不到需要的文档？** 检查 `archive/` 目录或搜索文件名。
