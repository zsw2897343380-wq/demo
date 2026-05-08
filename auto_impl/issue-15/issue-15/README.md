# Issue #15 - JAVA Code Generation

## 架构标准
Spring Boot 三层架构

## 标准层级
- **controller**: 控制层 - REST API 入口，处理 HTTP 请求响应
- **service**: 业务层 - 业务逻辑处理，事务管理
- **repository**: 数据层 - 数据库访问，JPA 操作
- **entity**: 实体层 - 数据模型，对应数据库表
- **dto**: 传输层 - 数据传输对象，用于 API 交互

## 生成的模块

### user
- 描述: Handle user registration, login, and JWT token authentication
- 包含层级: controller, service, repository, entity, dto, (needs, DTO, for, login/register, request/response)


### profile
- 描述: Manage user profile information (view, update)
- 包含层级: controller, service, repository, entity, dto, (needs, DTO, for, profile, update, request/response)


## 文件统计
- 模块数: 2
- 文件总数: 10
- 语言: JAVA
- 架构: Spring Boot 三层架构

## 目录结构
```
src/main/java/com/example/issue15/user/UserController.java
src/main/java/com/example/issue15/user/UserService.java
src/main/java/com/example/issue15/user/UserRepository.java
src/main/java/com/example/issue15/user/User.java
src/main/java/com/example/issue15/user/UserDTO.java
src/main/java/com/example/issue15/profile/ProfileController.java
src/main/java/com/example/issue15/profile/ProfileService.java
src/main/java/com/example/issue15/profile/ProfileRepository.java
src/main/java/com/example/issue15/profile/Profile.java
src/main/java/com/example/issue15/profile/ProfileDTO.java
```

---
*Generated following Spring Boot 三层架构 standards*
