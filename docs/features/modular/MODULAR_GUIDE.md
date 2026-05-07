# 🏗️ 模块化代码生成指南

## 什么是模块化生成？

模块化生成功能将**大型需求自动拆解**为多个小模块，并为每个模块生成独立的代码文件，组织成清晰的包结构。

### 适合场景

✅ **大型需求** - 包含多个功能点的复杂需求  
✅ **架构设计** - 需要清晰的模块划分  
✅ **团队协作** - 生成的代码结构规范，易于分工  
✅ **Java/Python 项目** - 特别适合需要包结构的语言

---

## 📋 示例演示

### 输入：复杂的需求描述

```markdown
## 需求：电商订单管理系统

### 功能模块：
1. **用户管理模块**
   - 用户注册/登录
   - 用户信息管理
   - 权限控制

2. **商品管理模块**
   - 商品CRUD
   - 库存管理
   - 分类管理

3. **订单管理模块**
   - 创建订单
   - 订单状态流转
   - 支付集成

4. **通知模块**
   - 邮件通知
   - 短信通知
   - 站内信

技术栈：Java + Spring Boot + MySQL
```

### 输出：生成的项目结构

```
auto_impl/issue-1/
├── README.md                      # 项目结构说明
├── analysis.md                    # AI需求分析报告
├── auto_generated_code.txt        # 汇总文档
└── src/
    └── main/
        └── java/
            └── com/
                └── example/
                    └── project/
                        └── issue1/
                            ├── user/
                            │   ├── UserController.java
                            │   ├── UserService.java
                            │   ├── UserRepository.java
                            │   └── User.java
                            ├── product/
                            │   ├── ProductController.java
                            │   ├── ProductService.java
                            │   └── Product.java
                            ├── order/
                            │   ├── OrderController.java
                            │   ├── OrderService.java
                            │   ├── OrderRepository.java
                            │   └── Order.java
                            └── notification/
                                ├── EmailService.java
                                ├── SmsService.java
                                └── NotificationService.java
```

---

## 🚀 如何使用

### 方式 1：自动检测（推荐）

当系统检测到复杂需求时，会自动使用模块化生成：

**触发条件**（满足任一）：
- Issue 长度 > 200 字符
- 包含关键词：module、service、component、包、模块、服务

**示例输出**：
```
[Strategy] Auto-selected: MODULAR (complex requirement detected, DeepSeek API found)
[Modular] ✅ Successfully generated 15 files via modular approach.
```

### 方式 2：手动指定

在 GitHub Actions 中手动触发：

```
Actions → Harness → Run workflow
  Issue number: 1
  Base branch: main
  Strategy: modular  ← 选择这个
```

### 方式 3：本地运行

```bash
export DEEPSEEK_API_KEY="sk-xxxxxxxxxxxxx"

node scripts/opencode_generate_modular.js \
  --issue-number 1 \
  --issue-body "创建一个电商系统，包含用户管理、商品管理、订单管理模块" \
  --outdir ./auto_impl/issue-1 \
  --language java
```

---

## 📝 各语言的项目结构

### Java 项目

```
src/main/java/com/example/project/issue{N}/
├── user/
│   ├── UserController.java
│   ├── UserService.java
│   ├── UserRepository.java
│   └── User.java
├── product/
│   ├── ProductController.java
│   ├── ProductService.java
│   └── Product.java
└── ...
```

### Python 项目

```
src/
├── user/
│   ├── __init__.py
│   ├── controller.py
│   ├── service.py
│   └── model.py
├── product/
│   ├── __init__.py
│   ├── controller.py
│   └── model.py
└── ...
```

### TypeScript/JavaScript 项目

```
src/
├── user/
│   ├── index.ts
│   ├── controller.ts
│   ├── service.ts
│   └── types.ts
├── product/
│   ├── index.ts
│   └── ...
└── ...
```

### Go 项目

```
├── user/
│   ├── user.go
│   ├── service.go
│   └── repository.go
├── product/
│   ├── product.go
│   └── ...
└── ...
```

---

## 🎯 工作流程

### Phase 1: 需求分析

AI 分析 issue 内容，输出：
- 项目概述
- 功能模块列表（3-8个模块）
- 每个模块的职责
- 推荐技术栈
- 目录结构建议

**保存为**: `analysis.md`

### Phase 2: 逐个生成模块

对每个模块：
1. 生成 Controller/Handler（入口）
2. 生成 Service（业务逻辑）
3. 生成 Repository/DAO（数据访问）
4. 生成 Model/Entity（数据模型）

**文件命名**: 根据代码内容自动提取类名

### Phase 3: 创建项目文档

生成：
- `README.md` - 项目结构说明
- `auto_generated_code.txt` - 文件清单

---

## 💡 最佳实践

### 1. 如何写好模块化需求的 Issue

**✅ 好的示例**：
```markdown
## 项目：在线图书管理系统

### 核心模块：
1. **用户模块**: 注册、登录、权限管理
2. **图书模块**: 图书CRUD、分类、搜索
3. **借阅模块**: 借书、还书、逾期提醒
4. **统计模块**: 借阅统计、热门图书

技术栈：Python + FastAPI + PostgreSQL
```

**❌ 避免过于简单**：
```markdown
创建一个登录功能  # 太简单，不需要模块化
```

### 2. 指定编程语言

AI 会自动检测语言，但你可以在 issue 中明确说明：

```markdown
使用 Java 实现...
# 或
技术栈：Java + Spring Boot
```

支持的语言：
- `java` - Java + Maven/Gradle 结构
- `python` - Python 包结构
- `typescript` - TypeScript 项目
- `javascript` - JavaScript 项目
- `go` - Go 项目结构
- `rust` - Rust 项目结构

### 3. 后续开发建议

模块化生成后，你可以：

1. **逐个完善模块**
   ```bash
   # 查看生成的文件
   ls -R auto_impl/issue-1/src/
   
   # 逐个实现 TODO 部分
   ```

2. **添加测试**
   ```bash
   # 为每个模块添加单元测试
   touch src/test/java/.../UserServiceTest.java
   ```

3. **添加配置文件**
   ```bash
   # 添加 application.yml / config.py 等
   ```

---

## 📊 模块化 vs 单文件生成

| 特性 | 模块化生成 | 单文件生成 |
|------|-----------|-----------|
| **适用场景** | 复杂需求、多模块 | 简单需求、单一功能 |
| **代码组织** | 清晰的包结构 | 单一大文件 |
| **可维护性** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| **生成时间** | 较长（多API调用） | 较短（单次调用） |
| **适合语言** | Java、Python、Go | 任何语言 |

---

## 🐛 故障排除

### 问题 1：模块划分不合理

**症状**：生成的模块不符合预期

**解决**：
- 在 issue 中更明确地描述模块划分
- 使用 `## 模块 1: XXX` 格式列出模块

### 问题 2：生成的文件不完整

**症状**：某些模块缺少文件

**原因**：API 超时或 token 限制

**解决**：
- 简化需求描述
- 手动触发时逐个生成模块

### 问题 3：包结构不符合团队规范

**解决**：
- 修改 `scripts/opencode_generate_modular.js` 中的 `LANGUAGE_CONFIG`
- 调整 `defaultPackage` 为你团队的规范

---

## 🎓 示例项目

### 示例 1：Java 微服务

**Issue**：
```markdown
创建一个用户微服务，包含：
- 用户注册/登录（JWT认证）
- 用户资料管理
- 权限管理（RBAC）
- 操作日志记录

使用 Spring Boot + MyBatis + MySQL
```

**生成结构**：
```
com/example/userservice/
├── auth/
│   ├── JwtAuthFilter.java
│   ├── JwtTokenProvider.java
│   └── AuthController.java
├── user/
│   ├── UserController.java
│   ├── UserService.java
│   ├── UserRepository.java
│   └── User.java
├── rbac/
│   ├── RoleController.java
│   ├── PermissionService.java
│   └── Role.java
└── log/
    ├── OperationLogAspect.java
    └── OperationLogService.java
```

### 示例 2：Python 数据分析工具

**Issue**：
```markdown
创建一个数据清洗和分析工具，包含：
- 数据加载模块（支持CSV、Excel、数据库）
- 数据清洗模块（处理缺失值、异常值）
- 数据分析模块（统计、可视化）
- 报告生成模块（PDF、HTML）

使用 pandas + matplotlib + jinja2
```

**生成结构**：
```
src/
├── data_loader/
│   ├── __init__.py
│   ├── csv_loader.py
│   ├── excel_loader.py
│   └── db_loader.py
├── data_cleaner/
│   ├── __init__.py
│   ├── missing_value_handler.py
│   └── outlier_detector.py
├── analyzer/
│   ├── __init__.py
│   ├── statistics.py
│   └── visualizer.py
└── reporter/
    ├── __init__.py
    ├── pdf_generator.py
    └── html_generator.py
```

---

## 🔗 相关文档

- [DeepSeek 配置指南](./DEEPSEEK_GUIDE.md)
- [快速上手指南](./QUICKSTART.md)
- [完整配置说明](./CONFIGURATION.md)

---

## ✅ 总结

模块化生成功能帮助你：
- ✅ 将复杂需求拆解为清晰模块
- ✅ 自动生成规范的包结构
- ✅ 每个模块独立、完整
- ✅ 支持 Java、Python、TypeScript 等多种语言

**适合**：大型项目、架构设计、团队协作

**开始使用**：创建包含多个功能点的 issue，系统自动触发模块化生成！
