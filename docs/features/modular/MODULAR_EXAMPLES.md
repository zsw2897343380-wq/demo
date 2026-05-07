# 📝 模块化生成 Issue 示例

## 如何使用

复制以下内容创建 Issue，系统会自动使用模块化生成功能。

---

## 示例 1：Java 电商系统

**标题**：电商订单管理系统

**内容**：
```markdown
## 项目概述
构建一个完整的电商订单管理系统，支持商品管理、用户管理、订单处理和支付功能。

## 功能模块

### 1. 用户管理模块
- 用户注册/登录（支持手机号、邮箱）
- 用户信息管理（头像、地址、密码）
- 权限控制（普通用户、VIP用户、管理员）
- JWT Token 认证

### 2. 商品管理模块
- 商品CRUD操作
- 商品分类管理（多级分类）
- 库存管理（库存预警、库存日志）
- 商品搜索（支持全文搜索）

### 3. 订单管理模块
- 购物车功能
- 订单创建和取消
- 订单状态流转（待支付→已支付→已发货→已完成）
- 订单历史查询

### 4. 支付模块
- 支持多种支付方式（支付宝、微信、银行卡）
- 支付状态回调处理
- 退款功能
- 支付日志记录

### 5. 通知模块
- 短信通知（订单状态变更）
- 邮件通知（注册确认、订单确认）
- 站内消息

## 技术要求
- 后端：Java 17 + Spring Boot 3.x
- 数据库：MySQL 8.0 + Redis
- ORM：MyBatis-Plus
- 安全：Spring Security + JWT
- 文档：Swagger/OpenAPI
```

**预期生成**：
```
src/main/java/com/example/project/issue1/
├── user/
│   ├── UserController.java
│   ├── UserService.java
│   ├── UserRepository.java
│   ├── User.java
│   ├── UserRegisterRequest.java
│   └── JwtTokenProvider.java
├── product/
│   ├── ProductController.java
│   ├── ProductService.java
│   ├── ProductRepository.java
│   └── Product.java
├── order/
│   ├── OrderController.java
│   ├── OrderService.java
│   ├── OrderRepository.java
│   ├── Order.java
│   └── OrderStatus.java
├── payment/
│   ├── PaymentController.java
│   ├── PaymentService.java
│   └── PaymentCallbackHandler.java
└── notification/
    ├── NotificationService.java
    ├── SmsService.java
    └── EmailService.java
```

---

## 示例 2：Python 数据分析平台

**标题**：数据分析与可视化平台

**内容**：
```markdown
## 项目概述
构建一个数据分析平台，支持多种数据源接入、数据清洗、分析和可视化。

## 功能模块

### 1. 数据接入模块
- 支持 CSV、Excel、JSON 文件导入
- 支持数据库连接（MySQL、PostgreSQL、MongoDB）
- 支持 API 数据源
- 数据格式自动识别

### 2. 数据清洗模块
- 缺失值处理（删除、填充、插值）
- 异常值检测和处理
- 数据类型转换
- 重复数据处理

### 3. 数据分析模块
- 描述性统计（均值、中位数、标准差等）
- 相关性分析
- 趋势分析
- 分组聚合

### 4. 可视化模块
- 图表生成（折线图、柱状图、饼图、散点图）
- 交互式图表
- 图表导出（PNG、PDF、HTML）
- 仪表板生成

### 5. 报告模块
- 自动生成分析报告
- 支持 PDF 和 HTML 格式
- 模板自定义
- 定时报告

## 技术要求
- Python 3.11+
- FastAPI + uvicorn
- pandas + numpy
- matplotlib + plotly
- jinja2（模板）
- reportlab（PDF生成）
```

**预期生成**：
```
src/
├── data_loader/
│   ├── __init__.py
│   ├── base_loader.py
│   ├── csv_loader.py
│   ├── excel_loader.py
│   ├── db_loader.py
│   └── api_loader.py
├── data_cleaner/
│   ├── __init__.py
│   ├── missing_value_handler.py
│   ├── outlier_detector.py
│   └── data_transformer.py
├── analyzer/
│   ├── __init__.py
│   ├── statistics.py
│   ├── correlation_analyzer.py
│   └── trend_analyzer.py
├── visualizer/
│   ├── __init__.py
│   ├── chart_generator.py
│   ├── interactive_plot.py
│   └── dashboard.py
└── reporter/
    ├── __init__.py
    ├── report_generator.py
    ├── pdf_exporter.py
    └── html_exporter.py
```

---

## 示例 3：TypeScript 微前端框架

**标题**：微前端应用框架

**内容**：
```markdown
## 项目概述
构建一个微前端框架，支持多个独立应用的集成、通信和共享状态管理。

## 功能模块

### 1. 应用加载模块
- 动态加载子应用
- 应用生命周期管理
- 路由集成
- 沙箱隔离

### 2. 通信模块
- 应用间事件通信
- 全局状态同步
- 广播机制
- 点对点通信

### 3. 状态管理模块
- 全局 Store
- 状态订阅/发布
- 状态持久化
- 状态恢复

### 4. 共享模块
- 共享组件库
- 共享工具函数
- 共享样式
- 版本管理

### 5. 监控模块
- 性能监控
- 错误捕获
- 日志收集
- 用户行为追踪

## 技术要求
- TypeScript 5.x
- Webpack 5 Module Federation
- React/Vue 支持
- RxJS（事件通信）
```

---

## 💡 写作技巧

### 好的 Issue 应该包含：

1. **清晰的项目概述**（1-2句话）
2. **明确的功能模块**（3-8个模块）
3. **每个模块的具体功能点**（2-5个）
4. **技术栈说明**（编程语言、框架、数据库）

### 触发模块化生成的关键词：

- "模块"、"module"
- "服务"、"service"
- "组件"、"component"
- "包"、"package"
- 多个功能点（使用数字列表）

### 指定编程语言：

在 Issue 中明确说明技术栈：
```markdown
技术栈：Java + Spring Boot
# 或
使用 Python 实现
# 或
后端：Go + Gin 框架
```

---

## ✅ 检查清单

创建 Issue 前检查：

- [ ] 需求是否包含多个功能模块？
- [ ] 是否明确了编程语言？
- [ ] 每个模块是否有清晰的功能描述？
- [ ] 是否说明了技术栈（数据库、框架等）？
- [ ] 长度是否超过 200 字符？（帮助触发模块化生成）

---

## 🎉 效果预览

当你创建这样的 Issue 后，Actions 会显示：

```
[Strategy] Auto-selected: MODULAR (complex requirement detected, DeepSeek API found)
[Phase 1] 分析需求并拆解模块...
📦 识别到 5 个模块:
  1. 用户管理模块
  2. 商品管理模块
  3. 订单管理模块
  4. 支付模块
  5. 通知模块
============================================================
🚀 开始生成模块代码...
============================================================

[1/5] 处理模块: 用户管理模块
  ✅ 已生成: src/main/java/.../user/UserController.java (2847 字符)
  ✅ 已生成: src/main/java/.../user/UserService.java (3421 字符)
  ...

✅ 模块化代码生成完成！
📊 生成了 15 个文件
```

然后会自动创建 PR，包含完整的项目结构！

---

**开始尝试**：复制上面的示例创建 Issue，体验模块化生成！🚀
