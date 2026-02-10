# 需求文档

## 简介

FlashSell 是一款面向跨境电商卖家的 AI 驱动爆品选品工具。本文档定义了 MVP 阶段的技术实现需求，基于已有的商业计划和 UI 设计，构建一个可行的全栈技术方案。

## 术语表

### 系统组件
- **FlashSell_System**: FlashSell 选品平台的整体系统
- **flashsell_web**: 基于 Vue 3 + Vite + Tailwind CSS 的前端 Web 应用
- **flashsell**: 基于 Java Spring Boot + Spring AI + COLA 5.x 架构的单体分模块后端服务（基于 github.com/978655242/demo-cola）

### 后端模块（COLA 5.x 架构）
- **flashsell_adapter**: 适配器层，处理外部请求（Web API、定时任务）
- **flashsell_app**: 应用层，业务编排和 DTO 转换
- **flashsell_client**: 客户端层，API 接口定义和 DTO
- **flashsell_domain**: 领域层，核心业务逻辑（包含 user、product、ai、payment 子领域）
- **flashsell_infrastructure**: 基础设施层，技术实现（PostgreSQL、Redis、智谱 API、支付宝 SDK、Bright Data MCP）

### 领域服务
- **flashsell_user**: 用户领域服务，处理认证、会话、用户管理
- **flashsell_core**: 核心业务服务，处理产品、收藏、看板等
- **flashsell_ai**: AI 领域服务，处理智谱 API 调用和 AI 分析
- **flashsell_data**: 数据领域服务，处理 Bright Data MCP 数据获取
- **flashsell_pay**: 支付领域服务，处理支付宝支付

### 基础设施
- **FlashSell_Database**: PostgreSQL 数据库存储层
- **FlashSell_Cache**: Redis 缓存层
- **Bright_Data_MCP**: Bright Data MCP Server，提供实时电商数据获取能力

### 业务概念
- **FlashSell_User**: 使用 FlashSell 平台的跨境电商卖家
- **Supported_Categories**: MVP 阶段支持的 45 个固定品类，分布在四大类目中
- **Category_Group_Industrial**: 工业用品类目组
- **Category_Group_Holiday**: 节日装饰类目组
- **Category_Group_Home**: 家居生活与百货类目组
- **Category_Group_Digital**: 数码配件与小家电类目组

## 需求

### 需求 1: 用户认证系统

**用户故事:** 作为用户，我希望能够注册和登录平台，以便访问个性化的选品功能。

#### Acceptance Criteria

1. WHEN 用户提交手机号和验证码进行注册, THE flashsell_user SHALL 创建新用户账户并返回 JWT token
2. WHEN 用户提交有效的登录凭证, THE flashsell_user SHALL 验证身份并返回 JWT token 和 refresh token
3. WHEN 用户的 JWT token 过期, THE flashsell_user SHALL 允许使用有效的 refresh token 刷新令牌
4. IF 用户提交无效凭证, THEN THE flashsell_user SHALL 返回适当的错误信息，不暴露敏感信息
5. WHEN 用户登出, THE flashsell_user SHALL 在 FlashSell_Cache 中使当前会话失效
6. THE flashsell_user SHALL 在 FlashSell_Cache 中存储用户会话，支持可配置的 TTL
7. WHEN 用户修改密码, THE flashsell_user SHALL 验证原密码后更新密码并使所有会话失效
8. WHEN 用户绑定手机号, THE flashsell_user SHALL 发送验证码并验证后绑定
9. WHEN 用户开启两步验证, THE flashsell_user SHALL 生成 TOTP 密钥并验证后启用
10. WHEN 用户注销账户, THE flashsell_user SHALL 软删除用户数据并使所有会话失效

### 需求 2: 个人中心

**用户故事:** 作为用户，我希望在个人中心管理我的账户信息和偏好设置，以便个性化我的使用体验。

#### Acceptance Criteria

1. WHEN 用户访问个人中心, THE flashsell_web SHALL 展示用户头像、昵称、邮箱、会员等级和到期时间
2. WHEN 用户查看使用情况, THE flashsell_adapter SHALL 返回本月搜索次数、收藏产品数、导出报告数及对应限额
3. WHEN 用户修改个人信息（昵称、头像）, THE flashsell_domain SHALL 更新用户资料并持久化
4. WHEN 用户切换消息通知开关, THE flashsell_domain SHALL 更新用户通知偏好设置
5. WHEN 用户切换深色/浅色模式, THE flashsell_web SHALL 保存主题偏好到本地存储
6. WHEN 用户切换邮件订阅开关, THE flashsell_domain SHALL 更新用户邮件订阅状态
7. WHEN 用户点击导出数据报告, THE flashsell_adapter SHALL 生成并下载用户的分析报告（PDF格式）
8. WHEN 用户点击邀请好友, THE flashsell_adapter SHALL 生成邀请链接和邀请码
9. THE flashsell_adapter SHALL 返回用户最后登录时间
10. WHEN 用户添加支付方式, THE flashsell_pay SHALL 验证并绑定支付宝账户

### 需求 3: AI 选品搜索

**用户故事:** 作为用户，我希望使用自然语言搜索产品，以便快速找到适合跨境电商的高潜力产品。

#### Acceptance Criteria

1. WHEN 用户提交搜索查询, THE flashsell_ai SHALL 调用智谱 API 处理查询并返回相关产品推荐
2. WHEN 展示搜索结果, THE flashsell_web SHALL 显示产品标题、图片、价格、BSR 排名和评论数
3. WHEN 用户应用筛选条件（价格区间、类目、评分）, THE flashsell_core SHALL 相应地过滤结果
4. IF flashsell_ai 收到空或无效查询, THEN THE flashsell_core SHALL 返回友好的错误提示
5. WHEN 返回搜索结果, THE flashsell_core SHALL 在 FlashSell_Cache 中缓存结果 15 分钟以降低 API 成本
6. THE flashsell_ai SHALL 将搜索请求序列化为 JSON 用于 API 通信
7. THE flashsell_ai SHALL 将 API 响应从 JSON 反序列化为领域对象
8. THE flashsell_core SHALL 限制搜索范围为预定义的 Supported_Categories（45 个品类），涵盖四大类目：工业用品、节日装饰、家居生活与百货、数码配件与小家电
9. WHEN 用户搜索超出 Supported_Categories 范围的产品, THE flashsell_core SHALL 返回提示信息，引导用户在支持的品类中搜索
10. THE flashsell_web SHALL 在搜索界面展示 Supported_Categories 列表，方便用户选择

### 需求 4: 实时电商数据获取

**用户故事:** 作为用户，我希望获取最新的电商平台商品数据，以便快速发现和抢占市场爆品机会。

#### Acceptance Criteria

1. THE flashsell_data SHALL 通过 Bright_Data_MCP 实时获取 Amazon 商品搜索结果
2. THE flashsell_data SHALL 通过 Bright_Data_MCP 实时获取 Amazon 商品详情（价格、评分、评论数、BSR 排名）
3. THE flashsell_data SHALL 通过 Bright_Data_MCP 实时获取 Amazon 商品评论数据
4. THE flashsell_data SHALL 通过 Bright_Data_MCP 爬取 1688/Alibaba 供应商商品数据
5. IF 获取实时数据失败, THEN THE flashsell_data SHALL 返回缓存数据并标注数据时效性
6. THE flashsell_data SHALL 对实时获取的数据进行标准化处理（价格转换、字段映射）
7. THE flashsell_core SHALL 在 FlashSell_Cache 中缓存实时数据，Amazon 商品详情 TTL 为 1 小时，搜索结果 TTL 为 15 分钟
8. THE flashsell_data SHALL 支持批量获取商品数据（最多 10 个商品/请求）
9. WHEN 调用 Bright_Data_MCP API 时, THE flashsell_data SHALL 记录请求日志用于成本监控
10. THE flashsell_data SHALL 遵守 Bright_Data_MCP 的请求频率限制，实现请求队列和重试机制

### 需求 5: 产品详情展示

**用户故事:** 作为用户，我希望查看详细的产品信息，以便做出明智的选品决策。

#### Acceptance Criteria

1. WHEN 用户点击产品, THE flashsell_web SHALL 展示完整的产品详情，包括价格历史、竞争分析和 AI 推荐
2. WHEN 展示价格趋势, THE flashsell_web SHALL 使用 ECharts 渲染交互式图表
3. THE flashsell_core SHALL 返回产品数据，包括：标题、图片、当前价格、历史价格、BSR 排名、评论统计和竞争评分
4. IF 产品未找到, THEN THE flashsell_core SHALL 返回 404 状态码和适当的提示信息

### 需求 6: 收藏与看板管理

**用户故事:** 作为用户，我希望将产品保存到收藏夹并在看板中组织它们，以便追踪和比较潜在产品。

#### Acceptance Criteria

1. WHEN 用户将产品添加到收藏, THE flashsell_core SHALL 将收藏关系持久化到 FlashSell_Database
2. WHEN 用户创建新看板, THE flashsell_core SHALL 创建看板并与用户关联
3. WHEN 用户将产品移动到看板, THE flashsell_core SHALL 更新产品-看板关系
4. WHEN 用户查看收藏列表, THE flashsell_core SHALL 返回分页的收藏产品列表
5. THE flashsell_core SHALL 限制免费用户最多 10 个看板，高级用户最多 50 个看板

### 需求 7: 订阅支付系统

**用户故事:** 作为用户，我希望使用支付宝订阅高级套餐，以便访问高级功能。

#### Acceptance Criteria

1. WHEN 用户选择订阅套餐, THE flashsell_pay SHALL 创建支付宝支付订单
2. WHEN 支付宝发送支付回调, THE flashsell_pay SHALL 验证签名并更新订阅状态
3. WHEN 订阅激活, THE flashsell_core SHALL 在 FlashSell_Database 中更新用户的权限级别
4. IF 支付失败, THEN THE flashsell_pay SHALL 记录失败日志并通知用户
5. WHEN 订阅过期, THE flashsell_core SHALL 将用户权限降级到免费层
6. THE flashsell_pay SHALL 将支付请求序列化为 JSON 用于支付宝 API
7. THE flashsell_pay SHALL 将支付宝回调响应从 JSON 反序列化

### 需求 8: 市场分析功能

**用户故事:** 作为用户，我希望查看产品类目的市场分析，以便了解市场趋势和竞争情况。

#### Acceptance Criteria

1. WHEN 用户查看市场分析, THE flashsell_adapter SHALL 返回类目销量分布和竞争强度评分
2. WHEN 展示市场数据, THE flashsell_web SHALL 渲染图表显示趋势数据，包含周环比和月环比
3. THE flashsell_domain SHALL 从缓存的产品信息中聚合市场数据
4. IF 某类目的市场数据不可用, THEN THE flashsell_adapter SHALL 返回空数据集和适当提示
5. WHEN 用户查看品类详细数据, THE flashsell_adapter SHALL 返回品类名称、市场规模、月增长率、竞争强度、进入壁垒和潜力评分
6. THE flashsell_web SHALL 支持按时间范围筛选市场数据（最近30天、90天、一年）
7. WHEN 用户点击导出报告, THE flashsell_adapter SHALL 生成市场分析报告（PDF格式）

### 需求 9: 仪表盘首页

**用户故事:** 作为用户，我希望在首页看到核心数据概览和快速入口，以便快速了解平台状态和开始选品工作。

#### Acceptance Criteria

1. WHEN 用户访问首页, THE flashsell_web SHALL 展示今日新品发现数、潜力爆品推荐数、收藏产品数和 AI 推荐准确率
2. WHEN 用户访问首页, THE flashsell_adapter SHALL 返回 AI 爆品推荐列表（Top 4）
3. WHEN 用户访问首页, THE flashsell_adapter SHALL 返回最近选品记录（最近浏览/搜索的产品）
4. WHEN 用户访问首页, THE flashsell_adapter SHALL 返回热门品类趋势数据
5. THE flashsell_web SHALL 展示快速搜索入口和热门搜索关键词
6. WHEN 用户点击产品卡片, THE flashsell_web SHALL 打开产品详情弹窗
7. THE flashsell_web SHALL 显示数据最后更新时间

### 需求 10: 搜索历史

**用户故事:** 作为用户，我希望查看我的搜索历史和最近浏览的产品，以便快速回顾和继续之前的选品工作。

#### Acceptance Criteria

1. WHEN 用户执行搜索, THE flashsell_domain SHALL 记录搜索查询和结果数量
2. WHEN 用户查看产品详情, THE flashsell_domain SHALL 记录浏览历史
3. WHEN 用户访问首页, THE flashsell_adapter SHALL 返回最近 10 条搜索历史
4. WHEN 用户访问首页, THE flashsell_adapter SHALL 返回最近浏览的 8 个产品
5. THE flashsell_domain SHALL 保留最近 30 天的搜索和浏览历史
6. WHEN 用户点击历史记录, THE flashsell_web SHALL 重新执行该搜索或打开产品详情

### 需求 11: AI 爆品推荐定时任务

**用户故事:** 作为用户，我希望系统每日自动发现和推荐爆品，以便我无需手动搜索就能获取最新的高潜力产品。

#### Acceptance Criteria

1. THE flashsell_core SHALL 每日定时执行 AI 爆品搜索任务，覆盖所有 Supported_Categories（45 个品类）
2. WHEN 定时任务执行, THE flashsell_data SHALL 通过 Bright_Data_MCP 实时获取各品类的热销商品数据
3. WHEN 获取到商品数据, THE flashsell_ai SHALL 调用智谱 API 分析爆品潜力并评分
4. THE flashsell_core SHALL 为每个品类维护爆品排行榜，包含 Top 20 推荐产品
5. WHEN 用户访问爆品推荐页面, THE flashsell_web SHALL 展示按品类分组的每日爆品列表
6. THE flashsell_core SHALL 支持配置定时任务执行时间，默认为每日凌晨 2:00
7. IF 定时任务执行失败, THEN THE flashsell_core SHALL 记录错误日志并触发告警通知
8. THE flashsell_core SHALL 保留最近 7 天的爆品历史数据，支持趋势对比
9. WHEN 展示爆品推荐, THE flashsell_web SHALL 显示产品的爆品评分、上榜天数和排名变化

### 需求 12: 前端应用架构

**用户故事:** 作为开发者，我希望有一个结构良好的 Vue 3 前端应用，以便代码库可维护和可扩展。

#### Acceptance Criteria

1. THE flashsell_web SHALL 使用 Vue 3 Composition API 配合 TypeScript 实现类型安全
2. THE flashsell_web SHALL 使用 Pinia 进行状态管理
3. THE flashsell_web SHALL 使用 Vue Router 进行导航，配合路由守卫实现认证
4. THE flashsell_web SHALL 实现响应式设计，支持桌面端和平板端视口
5. WHEN API 调用失败, THE flashsell_web SHALL 显示用户友好的错误信息和重试选项
6. THE flashsell_web SHALL 为所有异步操作实现加载状态

### 需求 13: 后端 API 架构

**用户故事:** 作为开发者，我希望有一个遵循 COLA 5.x 架构的 Spring Boot 后端，以便代码库遵循领域驱动设计原则。

#### Acceptance Criteria

1. THE flashsell SHALL 遵循 COLA 5.x 架构（基于 github.com/978655242/demo-cola），清晰分离 flashsell_adapter、flashsell_app、flashsell_client、flashsell_domain、flashsell_infrastructure 和 start 层
2. THE flashsell_adapter SHALL 暴露 RESTful API，使用一致的响应格式
3. THE flashsell SHALL 实现全局异常处理，返回适当的 HTTP 状态码
4. THE flashsell_infrastructure SHALL 使用 MyBatis-Plus 进行 PostgreSQL 数据库操作
5. THE flashsell SHALL 使用 Bean Validation 注解实现请求验证
6. WHEN 处理请求, THE flashsell_adapter SHALL 记录请求/响应日志用于调试
7. THE flashsell_infrastructure SHALL 使用 Spring AI 框架集成智谱模型
8. THE flashsell_domain SHALL 按业务领域划分为 user、product、ai、payment 四个子领域

### 需求 14: 数据持久化

**用户故事:** 作为开发者，我希望有可靠的数据存储，以便用户数据和产品信息安全持久化。

#### Acceptance Criteria

1. THE FlashSell_Database SHALL 使用 PostgreSQL 存储用户账户、订阅、收藏、看板和搜索历史
2. THE FlashSell_Database SHALL 为频繁查询的字段使用适当的索引
3. THE flashsell SHALL 使用 HikariCP 实现数据库连接池
4. WHEN 存储敏感数据, THE flashsell_user SHALL 使用 BCrypt 加密密码
5. THE flashsell_core SHALL 为用户生成的内容实现软删除

### 需求 15: 缓存策略

**用户故事:** 作为开发者，我希望有效的缓存策略，以便系统性能良好并降低外部 API 成本。

#### Acceptance Criteria

1. THE FlashSell_Cache SHALL 缓存 AI 搜索结果，TTL 为 15 分钟
2. THE FlashSell_Cache SHALL 缓存用户会话数据，支持可配置的 TTL
3. THE FlashSell_Cache SHALL 缓存频繁访问的产品数据，TTL 为 1 小时
4. WHEN 缓存未命中, THE flashsell_core SHALL 从源获取数据并填充缓存
5. THE flashsell_core SHALL 在底层数据变更时实现缓存失效
