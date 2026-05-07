# 🤖 Issue-to-Branch 自动化

基于 AI 的代码自动生成工具，支持模块化代码生成和智能重构。

---

## 🚀 快速开始

**5分钟上手指南**: [docs/getting-started/QUICKSTART.md](docs/getting-started/QUICKSTART.md)

**3分钟测试**: [docs/getting-started/QUICK_TEST.md](docs/getting-started/QUICK_TEST.md)

**DeepSeek 配置**（推荐）: [docs/configuration/DEEPSEEK_GUIDE.md](docs/configuration/DEEPSEEK_GUIDE.md)

---

## 📚 文档导航

文档已按功能模块整理：

| 目录 | 内容 |
|------|------|
| [docs/getting-started/](docs/getting-started/) | 🚀 快速开始指南 |
| [docs/configuration/](docs/configuration/) | ⚙️ 配置说明 |
| [docs/features/modular/](docs/features/modular/) | ✨ 模块化代码生成 |
| [docs/features/context-aware/](docs/features/context-aware/) | 🧠 上下文感知重构（开发中） |
| [docs/troubleshooting/](docs/troubleshooting/) | 🔧 故障排除 |
| [docs/development/](docs/development/) | 🛠️ 开发文档 |

**完整导航**: [docs/README.md](docs/README.md)

---

## ✨ 核心功能

### 1. 模块化代码生成
- 自动拆解需求为多个模块
- 生成完整的包结构（Java/Python/TypeScript/Go）
- 每个模块包含 Controller/Service/Repository

### 2. 智能语言检测
- 根据需求自动选择最合适的语言
- 支持 Java、Python、TypeScript、Go、Rust

### 3. 上下文感知重构（开发中）
- 读取现有项目代码
- 基于现有代码生成增量更新
- 保持代码风格一致性

---

## 🎯 使用方式

### 自动触发
创建 Issue，系统自动生成代码并提交 PR

### 手动触发
```
Actions → Harness → Run workflow
  Issue number: {number}
  Strategy: auto
```

---

## 💡 示例

**输入 Issue**:
```markdown
构建电商订单系统：
- 用户管理（JWT 认证）
- 订单管理
- 支付功能

使用 Java + Spring Boot
```

**输出**:
```
src/main/java/com/example/project/
├── user/
│   ├── UserController.java
│   ├── UserService.java
│   └── UserRepository.java
└── order/
    ├── OrderController.java
    ├── OrderService.java
    └── OrderRepository.java
```

---

## 🔧 支持的 AI 服务

- **DeepSeek**（推荐）: 中国用户首选，性价比高
- **OpenAI**: GPT-3.5/4
- **Anthropic**: Claude

---

## 📖 详细文档

根据需求查看对应文档：

- **首次使用**: [docs/getting-started/QUICKSTART.md](docs/getting-started/QUICKSTART.md)
- **配置指南**: [docs/configuration/CONFIGURATION.md](docs/configuration/CONFIGURATION.md)
- **模块化功能**: [docs/features/modular/MODULAR_GUIDE.md](docs/features/modular/MODULAR_GUIDE.md)
- **遇到问题**: [docs/troubleshooting/](docs/troubleshooting/)

---

## 🤝 贡献

欢迎提交 Issue 和 PR！

**开发文档**: [docs/development/](docs/development/)

---

## 📄 许可

MIT License
