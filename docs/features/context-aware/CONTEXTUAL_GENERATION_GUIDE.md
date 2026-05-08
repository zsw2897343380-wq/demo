# 🧠 基于上下文的代码重构（Context-Aware Code Generation）

## 概述

这是本项目的**高级功能**。不同于从零生成代码，此功能会：

1. **读取现有代码** - 扫描仓库文件结构
2. **理解当前架构** - 分析代码风格和模式
3. **生成增量代码** - 基于现有代码生成新功能或修改

## 🎯 适用场景

### ✅ 完美适用

- **添加新功能到现有模块**
  ```
  已有：UserController.java（基础登录）
  添加：微信第三方登录功能
  ```

- **优化现有功能**
  ```
  已有：订单查询功能
  优化：添加缓存和分页
  ```

- **扩展业务逻辑**
  ```
  已有：用户注册（邮箱）
  添加：手机验证码注册
  ```

- **重构代码**
  ```
  已有：单体类（500行）
  重构：拆分为 Service + Repository
  ```

### ❌ 不适用

- 全新项目（无现有代码）→ 使用模块化生成
- 完全不同的技术栈 → 使用模块化生成
- 简单的代码片段 → 使用模块化生成

---

## 🚀 使用方法

### 自动检测

系统会自动检测上下文模式的关键词：

**中文关键词**：`优化`、`重构`、`修改`、`添加`、`更新`、`改进`、`基于`、`现有`、`已有`

**英文关键词**：`optimize`, `refactor`, `modify`, `update`, `improve`, `existing`, `current`, `based on`

### Issue 示例

**示例 1：添加功能**
```markdown
在用户登录基础上，添加微信第三方登录支持。

当前已有：
- UserController（基础登录）
- UserService
- UserRepository

需要添加：
- 微信 OAuth2 认证
- 绑定微信账号
- 微信登录回调处理
```

**示例 2：优化功能**
```markdown
优化现有的订单查询功能，添加以下改进：

1. 添加 Redis 缓存（缓存 5 分钟）
2. 分页查询（每页 20 条）
3. 按时间范围筛选
4. 添加索引优化
```

**示例 3：重构**
```markdown
重构用户模块，将现有的 UserService 拆分为：
- UserAuthService（认证相关）
- UserProfileService（资料管理）
- UserAdminService（后台管理）

保持现有接口不变，内部重新组织。
```

---

## 📊 工作流程

```mermaid
Issue 创建（包含上下文关键词）
        ↓
[Step 1] 扫描仓库
    - 获取文件树（GitHub API）
    - 识别项目语言
        ↓
[Step 2] 关键词匹配
    - 提取 Issue 关键词
    - 匹配相关文件（评分算法）
    - 选择 Top 5 相关文件
        ↓
[Step 3] 读取代码
    - 读取相关文件内容
    - 压缩处理（限制 token）
        ↓
[Step 4] AI 分析
    - 理解现有代码结构
    - 分析设计模式
    - 识别扩展点
        ↓
[Step 5] 生成代码
    - 生成修改的文件（MODIFY）
    - 生成新增的文件（CREATE）
    - 保持风格一致
        ↓
提交 PR（包含变更说明）
```

---

## 📁 输出结构

### 修改现有文件
```
auto_impl/issue-42/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── example/
│                   └── user/
│                       ├── UserController.java      ← 修改（添加微信登录端点）
│                       └── UserService.java         ← 修改（添加微信登录逻辑）
├── WechatAuthService.java                          ← 新增
├── CHANGES.md                                      ← 变更说明
└── .context_output.json                            ← 元数据
```

### 文件标记

生成的文件使用特殊标记区分：

```
===== MODIFY: src/main/java/com/example/user/UserController.java =====
[完整的修改后代码]

===== CREATE: src/main/java/com/example/auth/WechatAuthService.java =====
[完整的新代码]
```

---

## 🔍 技术实现

### 1. 文件检索算法

```python
# 评分规则
score = 0

# 文件名匹配 +10
if keyword in filename:
    score += 10

# 路径匹配 +5
if keyword in filepath:
    score += 5

# 核心文件加成 +3
if 'controller' in filepath or 'service' in filepath:
    score += 3

# 按分数排序，取 Top 5
```

### 2. Token 优化

```python
# 保留关键部分
- package/import 语句
- 类/接口定义
- 前 5 个 public 方法签名

# 压缩策略
- 删除方法体实现（保留签名）
- 删除注释（保留关键注释）
- 删除空行

# 限制
- 每个文件最多 3000 tokens
- 最多读取 5 个相关文件
```

### 3. 上下文提示词

```
基于以下现有代码：
===== FILE: UserController.java =====
[压缩后的代码]

===== FILE: UserService.java =====
[压缩后的代码]

===== NEW REQUIREMENT =====
添加微信第三方登录

要求：
1. 遵循现有代码风格
2. 复用现有接口
3. 保持包结构一致
4. 生成修改和新增文件

输出格式：
===== MODIFY: filepath =====
[完整修改后代码]

===== CREATE: filepath =====
[完整新代码]
```

---

## 💡 最佳实践

### 1. 明确引用现有功能

✅ **好的示例**：
```markdown
在现有的 UserController.login 方法基础上，
添加微信登录支持。

当前使用 JWT token，
需要兼容微信的 access_token。
```

❌ **不好的示例**：
```markdown
添加微信登录。
（没有提及现有代码）
```

### 2. 描述当前状态

✅ **好的示例**：
```markdown
当前订单查询直接查数据库，
没有缓存，响应较慢。

需要：
1. 添加 Redis 缓存
2. 优化 SQL 查询
```

### 3. 指定技术栈

✅ **好的示例**：
```markdown
项目使用 Spring Boot + MyBatis + Redis

基于现有的 OrderMapper.xml，
添加缓存配置。
```

---

## 🐛 故障排除

### 问题 1：没有找到相关文件

**症状**：系统提示"没有找到相关文件"

**原因**：
- 关键词不匹配
- 仓库为空或没有代码文件

**解决**：
- 在 Issue 中明确提及现有文件名
- 使用更通用的关键词（如 user, order）

### 问题 2：生成的代码风格不一致

**症状**：新代码和现有代码风格差异大

**原因**：
- 读取的上下文文件太少
- AI 理解偏差

**解决**：
- 提供更多上下文信息
- 明确指定代码规范

### 问题 3：Token 超限

**症状**：API 报错或返回不完整代码

**原因**：
- 现有代码文件太大
- 读取了太多文件

**解决**：
- 系统已自动压缩，但复杂项目可能仍超限
- 拆分为多个小 Issue

---

## 📊 与模块化生成的对比

| 特性 | 模块化生成 | 上下文感知 |
|------|-----------|-----------|
| **适用场景** | 全新项目/模块 | 现有项目扩展 |
| **输入** | 需求描述 | 需求 + 现有代码 |
| **输出** | 全新代码 | 修改 + 新增 |
| **代码一致性** | 标准架构 | 与现有一致 |
| **成本** | 较低 | 较高（需要读取代码）|
| **速度** | 快 | 较慢 |

---

## 🎯 成功标志

当看到以下输出，说明上下文模式工作正常：

```
[Strategy] Using CONTEXTUAL code generation (based on existing codebase)

[Step 1/4] 扫描仓库文件...
✅ 发现 45 个文件

[Step 2/4] 分析项目结构和相关文件...
🔤 项目语言: JAVA
✅ 找到 3 个相关文件:
   - UserController.java (相关度: 25)
   - UserService.java (相关度: 20)
   - UserRepository.java (相关度: 15)

[Step 3/4] 读取相关文件内容...
✅ 成功读取 3 个文件

[Step 4/4] 基于现有代码生成增量代码...
✅ 上下文感知代码生成完成！
📊 修改: 2 个文件
📊 新增: 1 个文件
```

---

## 📚 相关文档

- [模块化生成指南](../features/modular/MODULAR_GUIDE.md)
- [架构标准](../configuration/ARCHITECTURE_STANDARDS.md)
- [配置指南](../configuration/CONFIGURATION.md)

---

**开始体验上下文感知重构吧！** 🧠✨
