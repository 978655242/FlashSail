# FlashSell 项目编码规范

## 命名规范

### DTO 命名
- Request 类型统一简写为 `Req`，例如：`LoginReq`、`SearchReq`
- Response 类型统一简写为 `Res`，例如：`LoginRes`、`SearchRes`

### 数据库规范
- 所有数据库表必须添加表注释（COMMENT ON TABLE）
- 所有字段必须添加字段注释（COMMENT ON COLUMN）
- 注释使用中文，清晰描述表/字段的用途

### 示例

```java
// DTO 命名示例
public class LoginReq {
    private String phone;
    private String verifyCode;
}

public class LoginRes {
    private Long userId;
    private String token;
}
```

```sql
-- 数据库注释示例
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(20) UNIQUE NOT NULL
);

COMMENT ON TABLE users IS '用户表';
COMMENT ON COLUMN users.id IS '用户ID，主键自增';
COMMENT ON COLUMN users.phone IS '手机号，唯一';
```

---

## COLA 5.x 架构规范

基于 [github.com/978655242/demo-cola](https://github.com/978655242/demo-cola) 的 COLA 5.x 架构规范。

### 模块结构

```
flashsell/
├── flashsell-adapter/        # 适配器层
├── flashsell-app/            # 应用层
├── flashsell-client/         # 客户端层
├── flashsell-domain/         # 领域层
├── flashsell-infrastructure/ # 基础设施层
└── start/                    # 启动模块
```

### 层级职责与依赖

| 层级 | 职责 | 依赖方向 | 包含内容 |
|-----|------|---------|---------|
| adapter | 处理外部请求 | → app | Controller, Scheduler, Consumer |
| app | 业务编排 | → domain, client | AppService, Assembler |
| client | API 定义 | 无依赖 | API接口, DTO, Query |
| domain | 核心业务 | → client | Entity, DomainService, Gateway |
| infrastructure | 技术实现 | → domain | GatewayImpl, Mapper, Config |
| start | 应用启动 | → 所有模块 | Application, 配置文件 |

### 各层详细规范

#### 1. adapter 层（适配器层）

**职责**：处理外部请求，包括 Web API、定时任务、消息消费等

**目录结构**：
```
flashsell-adapter/
├── web/                      # Web 控制器
│   ├── AuthController.java
│   └── SearchController.java
├── scheduler/                # 定时任务
│   └── HotProductScheduler.java
└── consumer/                 # 消息消费者（如有）
```

**命名规范**：
- Controller 类：`XxxController`
- Scheduler 类：`XxxScheduler`
- Consumer 类：`XxxConsumer`

**代码规范**：
```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthAppService authAppService;
    
    @PostMapping("/login")
    public ApiResponse<LoginRes> login(@Valid @RequestBody LoginReq req) {
        return ApiResponse.success(authAppService.login(req));
    }
}
```

#### 2. app 层（应用层）

**职责**：业务编排、事务管理、DTO 转换

**目录结构**：
```
flashsell-app/
├── service/                  # 应用服务
│   ├── AuthAppService.java
│   └── SearchAppService.java
└── assembler/                # DTO 转换器
    ├── UserAssembler.java
    └── ProductAssembler.java
```

**命名规范**：
- 应用服务：`XxxAppService`
- 转换器：`XxxAssembler`

**代码规范**：
```java
@Service
@RequiredArgsConstructor
public class AuthAppService {
    
    private final UserDomainService userDomainService;
    private final UserAssembler userAssembler;
    
    @Transactional
    public LoginRes login(LoginReq req) {
        User user = userDomainService.authenticate(req.getPhone(), req.getVerifyCode());
        return userAssembler.toLoginRes(user);
    }
}
```

#### 3. client 层（客户端层）

**职责**：定义 API 接口、DTO、Query 对象

**目录结构**：
```
flashsell-client/
├── api/                      # API 接口定义
│   └── IAuthApi.java
├── dto/                      # 数据传输对象
│   ├── req/                  # 请求 DTO
│   │   └── LoginReq.java
│   └── res/                  # 响应 DTO
│       └── LoginRes.java
└── query/                    # 查询对象
    └── ProductQuery.java
```

**命名规范**：
- API 接口：`IXxxApi`
- 请求 DTO：`XxxReq`
- 响应 DTO：`XxxRes`
- 查询对象：`XxxQuery`

**代码规范**：
```java
// 请求 DTO
@Data
public class LoginReq {
    @NotBlank(message = "手机号不能为空")
    private String phone;
    
    @NotBlank(message = "验证码不能为空")
    private String verifyCode;
}

// 响应 DTO
@Data
@Builder
public class LoginRes {
    private Long userId;
    private String token;
    private String refreshToken;
}
```

#### 4. domain 层（领域层）

**职责**：核心业务逻辑、领域实体、领域服务

**目录结构**：
```
flashsell-domain/
├── user/                     # 用户领域
│   ├── entity/
│   │   └── User.java
│   ├── service/
│   │   └── UserDomainService.java
│   └── gateway/
│       └── UserGateway.java
├── product/                  # 产品领域
├── ai/                       # AI 领域
└── payment/                  # 支付领域
```

**命名规范**：
- 领域实体：`Xxx`（无后缀）
- 领域服务：`XxxDomainService`
- 网关接口：`XxxGateway`

**代码规范**：
```java
// 领域实体
@Data
public class User {
    private Long id;
    private String phone;
    private String nickname;
    private SubscriptionLevel subscriptionLevel;
    
    // 领域行为
    public boolean canCreateBoard(int currentBoardCount) {
        return currentBoardCount < subscriptionLevel.getMaxBoards();
    }
}

// 领域服务
@Service
@RequiredArgsConstructor
public class UserDomainService {
    
    private final UserGateway userGateway;
    
    public User authenticate(String phone, String verifyCode) {
        // 业务逻辑
    }
}

// 网关接口（由 infrastructure 层实现）
public interface UserGateway {
    User findByPhone(String phone);
    void save(User user);
}
```

#### 5. infrastructure 层（基础设施层）

**职责**：技术实现，包括数据库、缓存、外部 API 等

**目录结构**：
```
flashsell-infrastructure/
├── gatewayimpl/              # 网关实现
│   ├── UserGatewayImpl.java
│   └── AiGatewayImpl.java
├── mapper/                   # MyBatis-Plus Mapper
│   └── UserMapper.java
├── dataobject/               # 数据库对象
│   └── UserDO.java
├── convertor/                # DO/Entity 转换器
│   └── UserConvertor.java
└── config/                   # 配置类
    ├── RedisConfig.java
    └── SecurityConfig.java
```

**命名规范**：
- 网关实现：`XxxGatewayImpl`
- Mapper：`XxxMapper`
- 数据对象：`XxxDO`
- 转换器：`XxxConvertor`
- 配置类：`XxxConfig`

**代码规范**：
```java
// 网关实现
@Repository
@RequiredArgsConstructor
public class UserGatewayImpl implements UserGateway {
    
    private final UserMapper userMapper;
    private final UserConvertor userConvertor;
    
    @Override
    public User findByPhone(String phone) {
        UserDO userDO = userMapper.selectByPhone(phone);
        return userConvertor.toEntity(userDO);
    }
}

// 数据对象
@Data
@TableName("users")
public class UserDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String phone;
    private String nickname;
    // ... 其他字段
}
```

#### 6. start 模块（启动模块）

**职责**：应用启动、配置文件

**目录结构**：
```
start/
├── FlashSellApplication.java
└── resources/
    ├── application.yml
    ├── application-dev.yml
    └── application-prod.yml
```

### 依赖规则（重要）

1. **adapter** 只能依赖 **app** 和 **client**
2. **app** 只能依赖 **domain** 和 **client**
3. **domain** 只能依赖 **client**（定义 Gateway 接口）
4. **infrastructure** 只能依赖 **domain**（实现 Gateway 接口）
5. **client** 不依赖任何其他模块
6. **start** 依赖所有模块

### 禁止事项

- ❌ adapter 层直接调用 domain 层
- ❌ domain 层依赖 infrastructure 层
- ❌ 在 domain 层使用 Spring 注解（除 @Service）
- ❌ 在 client 层包含业务逻辑
- ❌ 在 infrastructure 层包含业务逻辑

### 推荐实践

- ✅ 使用 Lombok 简化代码
- ✅ 使用 MapStruct 进行对象转换
- ✅ 在 app 层管理事务
- ✅ 在 domain 层定义领域行为
- ✅ 使用 Gateway 模式隔离技术实现

---

## 基础设施配置

### PostgreSQL 配置

**Docker 容器信息**：
- 容器 ID: `b3634838bc1e`
- 镜像: `postgres:16-alpine`
- 端口映射: `0.0.0.0:5432->5432/tcp`
- 容器名称: `postgres`
- 状态: Up (healthy)

**连接信息**：
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/flashsell
    username: flashsell
    password: flashsell123
    driver-class-name: org.postgresql.Driver
```

**数据库名称**: `flashsell`

### Redis 配置

**Docker 容器信息**：
- 容器 ID: `b3634838bc1e`
- 镜像: `redis:7-alpine`
- 端口映射: `0.0.0.0:6379->6379/tcp`
- 容器名称: `redis`
- 状态: Up (healthy)

**连接信息**：
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: # 无密码
      database: 0
```

### 使用说明

1. **启动容器**：
   ```bash
   docker start postgres redis
   ```

2. **停止容器**：
   ```bash
   docker stop postgres redis
   ```

3. **查看容器状态**：
   ```bash
   docker ps | grep -E "postgres|redis"
   ```

4. **连接 PostgreSQL**：
   ```bash
   docker exec -it postgres psql -U flashsell -d flashsell
   ```

5. **连接 Redis**：
   ```bash
   docker exec -it redis redis-cli
   ```
