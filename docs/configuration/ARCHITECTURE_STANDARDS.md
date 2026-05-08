# 🏗️ 各语言标准架构规范

## 概述

代码生成器根据语言自动选择对应的标准架构，确保生成的代码符合各语言的最佳实践。

---

## ☕ Java - 三层架构（Spring Boot）

### 架构层级

```
com.example.project/
├── controller/     # 控制层
│   └── UserController.java
├── service/        # 业务层
│   └── UserService.java
├── repository/     # 数据层
│   └── UserRepository.java
├── entity/         # 实体层
│   └── User.java
└── dto/            # 传输层（可选）
    └── UserDTO.java
```

### 各层职责

| 层级 | 后缀 | 注解 | 职责 |
|------|------|------|------|
| **Controller** | Controller | @RestController | REST API 入口，处理 HTTP 请求响应 |
| **Service** | Service | @Service, @Transactional | 业务逻辑处理，事务管理 |
| **Repository** | Repository | @Repository | 数据库访问，JPA 操作 |
| **Entity** | 无 | @Entity, @Table | 数据模型，对应数据库表 |
| **DTO** | DTO | 无 | 数据传输对象，API 交互 |

### 示例代码结构

```java
// UserController.java
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    
    @GetMapping
    public List<User> getAllUsers() { }
}

// UserService.java
@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    public User createUser(User user) { }
}

// UserRepository.java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}

// User.java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
}
```

---

## 🐍 Python - 标准包结构（Flask/FastAPI）

### 架构层级

```
app/
├── __init__.py      # 包初始化
├── routes.py        # 路由层
├── service.py       # 业务层
├── models.py        # 模型层
├── schemas.py       # 模式层
└── utils.py         # 工具层
```

### 各层职责

| 层级 | 后缀 | 职责 |
|------|------|------|
| **routes** | _routes | API 端点定义，路由注册 |
| **service** | _service | 核心业务逻辑 |
| **models** | _models | 数据模型，SQLAlchemy 定义 |
| **schemas** | _schemas | 数据验证，Marshmallow 模式 |
| **utils** | _utils | 辅助函数，工具方法 |

### 示例代码结构

```python
# user_routes.py
from flask import Blueprint
from . import user_service

user_bp = Blueprint('user', __name__)

@user_bp.route('/users', methods=['GET'])
def get_users():
    return user_service.get_all_users()

# user_service.py
from . import user_models

def create_user(data):
    user = user_models.User(**data)
    # 业务逻辑
    return user

# user_models.py
from sqlalchemy import Column, Integer, String
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class User(Base):
    __tablename__ = 'users'
    id = Column(Integer, primary_key=True)
    username = Column(String(80), unique=True)
```

---

## 📘 TypeScript - 模块化架构（NestJS）

### 架构层级

```
src/
├── user/
│   ├── user.controller.ts    # 控制器
│   ├── user.service.ts       # 服务
│   ├── user.module.ts        # 模块定义
│   ├── user.entity.ts        # 实体
│   ├── user.dto.ts           # DTO
│   └── user.interface.ts     # 接口
```

### 各层职责

| 层级 | 后缀 | 装饰器 | 职责 |
|------|------|--------|------|
| **Controller** | .controller | @Controller(), @Get(), @Post() | HTTP 请求处理 |
| **Service** | .service | @Injectable() | 业务逻辑 |
| **Module** | .module | @Module() | NestJS 模块组织 |
| **Entity** | .entity | @Entity() | TypeORM 数据库实体 |
| **DTO** | .dto | 无 | 数据传输对象验证 |
| **Interface** | .interface | 无 | TypeScript 类型定义 |

### 示例代码结构

```typescript
// user.controller.ts
@Controller('users')
export class UserController {
  constructor(private readonly userService: UserService) {}
  
  @Get()
  findAll(): Promise<User[]> {
    return this.userService.findAll();
  }
}

// user.service.ts
@Injectable()
export class UserService {
  constructor(@InjectRepository(User) private userRepo: Repository<User>) {}
  
  async findAll(): Promise<User[]> {
    return this.userRepo.find();
  }
}

// user.module.ts
@Module({
  imports: [TypeOrmModule.forFeature([User])],
  controllers: [UserController],
  providers: [UserService],
})
export class UserModule {}

// user.entity.ts
@Entity()
export class User {
  @PrimaryGeneratedColumn()
  id: number;
  
  @Column()
  username: string;
}

// user.dto.ts
export class CreateUserDto {
  @IsString()
  username: string;
}
```

---

## 🔵 Go - 标准项目结构

### 架构层级

```
project/
├── user/
│   ├── user_handler.go       # 处理器
│   ├── user_service.go       # 服务
│   ├── user_repository.go    # 仓储
│   ├── user_model.go         # 模型
│   └── user_dto.go           # DTO
```

### 各层职责

| 层级 | 后缀 | 职责 |
|------|------|------|
| **Handler** | _handler | HTTP 处理函数，Gin/Echo 路由 |
| **Service** | _service | 业务逻辑 |
| **Repository** | _repository | 数据访问，数据库操作 |
| **Model** | _model | 数据结构定义，GORM 模型 |
| **DTO** | _dto | 数据传输结构 |

### 示例代码结构

```go
// user_handler.go
package user

import (
    "github.com/gin-gonic/gin"
)

type UserHandler struct {
    service UserService
}

func (h *UserHandler) GetUsers(c *gin.Context) {
    users := h.service.GetAll()
    c.JSON(200, users)
}

// user_service.go
package user

type UserService struct {
    repo UserRepository
}

func (s *UserService) GetAll() []User {
    return s.repo.FindAll()
}

// user_repository.go
package user

import "gorm.io/gorm"

type UserRepository struct {
    db *gorm.DB
}

func (r *UserRepository) FindAll() []User {
    var users []User
    r.db.Find(&users)
    return users
}

// user_model.go
package user

import "gorm.io/gorm"

type User struct {
    gorm.Model
    Username string
    Email    string
}
```

---

## 🎯 自动检测规则

系统根据关键词自动选择语言和架构：

| 语言 | 检测关键词 |
|------|-----------|
| **Java** | java, spring, springboot, maven, gradle, jpa, mybatis |
| **Python** | python, django, flask, fastapi, sqlalchemy |
| **TypeScript** | typescript, nestjs, ts, typeorm, angular |
| **Go** | go, golang, gin, beego, echo |

**默认**: Java (Spring Boot 三层架构)

---

## 💡 使用建议

### 在 Issue 中明确指定

如果不确定语言，可以在 Issue 中明确说明：

```markdown
使用 Java + Spring Boot 构建电商系统...

技术栈：
- 后端：Python + FastAPI
- 前端：TypeScript + NestJS

使用 Go + Gin 开发微服务...
```

### 查看生成的 ARCHITECTURE.md

每次生成后，会创建 `ARCHITECTURE.md` 文件，说明：
- 使用的架构标准
- 各层级定义
- 生成的文件列表

---

## ✅ 验证方法

### Java 项目
```bash
# 检查三层架构
ls -R src/main/java/com/example/
# 应看到：controller/, service/, repository/, entity/
```

### Python 项目
```bash
# 检查包结构
ls src/user/
# 应看到：__init__.py, routes.py, service.py, models.py
```

### TypeScript 项目
```bash
# 检查 NestJS 模块
ls src/user/
# 应看到：*.controller.ts, *.service.ts, *.module.ts
```

---

## 📝 注意事项

1. **层级可选**: AI 会根据需求复杂度决定使用哪些层级
   - 简单 CRUD → 可能只生成 controller, service, repository
   - 复杂业务 → 会生成全部层级包括 dto, interface

2. **包名清理**: 所有包名/目录名会自动转换为英文
   - `"用户管理"` → `"user"`
   - `"订单模块"` → `"order"`

3. **代码风格**: 遵循各语言的标准规范
   - Java: PascalCase 类名
   - Python: snake_case 文件名
   - TypeScript: PascalCase + 类型定义
   - Go: PascalCase 导出，小写私有

---

**生成的代码将严格遵循上述架构标准！** 🏗️
