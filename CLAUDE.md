# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

FlashSell 是一个 AI 驱动的跨境电商爆品选品工具，采用前后端分离的 Clean Architecture 架构。

- **后端**: Spring Boot 3.2.1 + Java 17，端口 8080
- **前端**: Vue 3 + TypeScript + Vite，开发端口 3000
- **数据库**: PostgreSQL 15 (flashsell/flashsell123@localhost:5432/flashsell)
- **缓存**: Redis (localhost:6379)
- **AI服务**: 智谱AI GLM-4

## 常用命令

### 后端开发
```bash
cd flashsell/start
mvn clean package spring-boot:run    # 构建并运行
mvn test                             # 运行测试
```

### 前端开发
```bash
cd flashsell-web
npm install                          # 安装依赖
npm run dev                          # 开发模式 (端口 3000)
npm run build                        # 生产构建
npm run lint                         # 代码检查
npm run format                       # 代码格式化
```

### 测试脚本
项目根目录包含多个测试脚本：
- `test-auth-system.sh` - 认证系统测试
- `test-hot-products.sh` - 爆品推荐功能测试
- `test-market-analysis.sh` - 市场分析测试

## 架构设计

### Clean Architecture 分层

后端采用严格的 DDD 分层架构，模块依赖关系：adapter → app → domain ← infrastructure

```
flashsell/
├── flashsell-client/        # DTOs 和 API 契约
├── flashsell-domain/        # 领域层 (核心业务逻辑)
│   ├── ai/                 # AI 服务领域
│   ├── board/              # 看板管理
│   ├── category/           # 品类管理
│   ├── data/               # 数据聚合
│   ├── favorite/           # 收藏功能
│   ├── history/            # 历史记录
│   ├── market/             # 市场分析
│   ├── product/            # 产品管理
│   └── user/               # 用户管理
├── flashsell-app/          # 应用服务层 (Use Cases)
├── flashsell-adapter/      # 控制器层 (REST API)
│   └── web/               # Spring MVC 控制器
├── flashsell-infrastructure/ # 基础设施层 (实现)
│   ├── data/              # MyBatis-Plus 数据访问
│   └── external/          # 外部服务集成
└── start/                 # Spring Boot 启动模块
```

### 前端架构

```
flashsell-web/src/
├── views/                 # 页面组件
├── components/            # 可复用组件
├── stores/               # Pinia 状态管理
│   ├── user.ts           # 用户状态
│   ├── search.ts         # 搜索状态
│   └── favorites.ts      # 收藏状态
├── api/                  # API 客户端
├── types/                # TypeScript 类型定义
└── router/               # Vue Router 配置
```

## 核心功能模块

### 产品分析
- **爆品推荐** (`HotProductController`): 每日 AI 驱动的热门产品推荐
- **智能搜索** (`SearchController`): 多维度筛选和 AI 总结
- **市场分析** (`MarketController`): 竞争分数和 BSR 排名计算

### 用户系统
- **认证** (`AuthController`): JWT 认证 + BCrypt 密码加密
- **收藏看板** (`BoardController`, `FavoriteController`): 用户产品收藏管理
- **历史记录** (`HistoryController`): 搜索和浏览历史追踪

### 数据同步
- 定时任务每日凌晨 2:00 更新爆品推荐
- Redis 缓存热门数据，提高响应速度

## 开发注意事项

### 代码规范
- 遵循 DDD 分层原则，不要跨层调用
- 使用 Lombok 减少样板代码
- MapStruct 进行对象映射
- MyBatis-Plus 逻辑删除字段: `deletedAt`

### 安全
- JWT Token 认证机制
- 敏感配置使用环境变量: `${ZHIPU_API_KEY}`, `${BRIGHTDATA_API_TOKEN}`
- BCrypt 密码加密

### 数据库
- Flyway 管理数据库迁移
- 支持软删除（`deleted_at` 字段）
- 45 个固定品类，不可随意修改

### API 代理
前端开发时通过 Vite 代理转发 `/api` 到后端 8080 端口
