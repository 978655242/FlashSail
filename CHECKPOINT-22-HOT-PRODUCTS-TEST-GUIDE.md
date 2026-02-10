# Checkpoint 22: 爆品推荐功能测试指南

## 测试目标

验证爆品推荐功能是否正常工作，包括：
1. 爆品推荐 API 接口测试
2. 定时任务手动触发测试
3. 缓存功能验证
4. 数据完整性检查

## 前置条件

### 1. 环境准备

确保以下服务正常运行：

```bash
# PostgreSQL 数据库
psql -h localhost -U flashsell -d flashsell -c "SELECT 1;"

# Redis 缓存
redis-cli ping

# 检查数据库表是否存在
psql -h localhost -U flashsell -d flashsell -c "\dt"
```

### 2. 配置检查

检查 `flashsell/start/src/main/resources/application.yml` 配置：

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/flashsell
    username: flashsell
    password: flashsell123

# Redis 配置
  data:
    redis:
      host: localhost
      port: 6379

# AI 配置（可选，用于定时任务）
  ai:
    zhipuai:
      api-key: ${ZHIPU_API_KEY:test-api-key-placeholder}

# Bright Data 配置（可选，用于定时任务）
brightdata:
  api-token: ${BRIGHTDATA_API_TOKEN:}

# 定时任务配置
flashsell:
  scheduler:
    hot-product-cron: "0 0 2 * * ?"  # 每日凌晨 2:00
```

### 3. 启动应用

```bash
cd flashsell
mvn clean install -DskipTests
cd start
mvn spring-boot:run
```

等待应用启动完成，看到以下日志：
```
Started FlashSellApplication in X.XXX seconds
```

## 测试步骤

### 方式一：使用自动化测试脚本（推荐）

```bash
# 在项目根目录执行
./test-hot-products.sh
```

脚本会自动执行以下测试：
1. 检查服务健康状态
2. 测试爆品推荐列表 API
3. 测试指定日期的爆品推荐
4. 测试指定品类组的爆品推荐
5. 测试 Top 4 爆品推荐（首页展示）
6. 测试产品爆品历史趋势
7. 验证缓存功能
8. 可选：手动触发定时任务

### 方式二：手动测试 API

#### 1. 测试爆品推荐列表 API

```bash
# 获取今日爆品推荐列表
curl -X GET "http://localhost:8080/api/hot-products" \
  -H "Content-Type: application/json" | jq

# 预期响应
{
  "code": 200,
  "message": "success",
  "data": {
    "date": "2025-01-20",
    "groups": [
      {
        "categoryGroup": {
          "id": 1,
          "name": "工业用品"
        },
        "products": [
          {
            "product": {
              "id": 1,
              "title": "产品标题",
              "currentPrice": 29.99,
              ...
            },
            "hotScore": 95.5,
            "daysOnList": 3,
            "rankChange": 2
          }
        ]
      }
    ],
    "total": 20
  },
  "timestamp": 1737360000000
}
```

#### 2. 测试指定日期的爆品推荐

```bash
# 获取指定日期的爆品推荐
curl -X GET "http://localhost:8080/api/hot-products?date=2025-01-20" \
  -H "Content-Type: application/json" | jq
```

#### 3. 测试指定品类组的爆品推荐

```bash
# 获取工业用品类目组的爆品推荐
curl -X GET "http://localhost:8080/api/hot-products?categoryGroupId=1" \
  -H "Content-Type: application/json" | jq
```

#### 4. 测试 Top 4 爆品推荐

```bash
# 获取今日 Top 4 爆品（用于首页展示）
curl -X GET "http://localhost:8080/api/hot-products/top4" \
  -H "Content-Type: application/json" | jq

# 预期响应：最多返回 4 个爆品
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "product": {...},
      "hotScore": 98.5,
      "daysOnList": 5,
      "rankChange": 1
    },
    ...
  ]
}
```

#### 5. 测试产品爆品历史趋势

```bash
# 获取产品的爆品历史（假设产品ID=1）
curl -X GET "http://localhost:8080/api/hot-products/history?productId=1&days=7" \
  -H "Content-Type: application/json" | jq

# 预期响应
{
  "code": 200,
  "message": "success",
  "data": {
    "productId": 1,
    "history": [
      {
        "date": "2025-01-14",
        "rank": 15,
        "hotScore": 85.0
      },
      {
        "date": "2025-01-15",
        "rank": 12,
        "hotScore": 87.5
      },
      ...
    ]
  }
}
```

### 方式三：手动触发定时任务

#### 使用管理接口触发

```bash
# 触发完整的爆品分析任务（遍历所有 45 个品类）
curl -X POST "http://localhost:8080/api/admin/hot-products/trigger" \
  -H "Content-Type: application/json" | jq

# 预期响应
{
  "code": 200,
  "message": "success",
  "data": "爆品分析任务已触发，请查看日志了解执行进度"
}

# 触发单个品类的分析（用于测试）
curl -X POST "http://localhost:8080/api/admin/hot-products/analyze-category?categoryId=1" \
  -H "Content-Type: application/json" | jq
```

#### 查看任务执行日志

```bash
# 查看应用日志
tail -f flashsell/start/logs/flashsell.log

# 或者查看控制台输出
# 关键日志：
# - "========== 开始执行爆品分析定时任务 =========="
# - "开始分析品类: id=X, name=XXX"
# - "品类分析完成: id=X, name=XXX"
# - "========== 爆品分析定时任务完成 =========="
```

## 验证要点

### 1. API 响应验证

- [ ] 所有 API 返回状态码 200
- [ ] 响应格式符合 ApiResponse 规范
- [ ] 数据字段完整（date, groups, products, hotScore 等）
- [ ] 爆品列表按评分降序排列
- [ ] Top 4 接口最多返回 4 个产品

### 2. 缓存功能验证

```bash
# 第一次请求（无缓存）
time curl -s "http://localhost:8080/api/hot-products" > /dev/null

# 第二次请求（有缓存，应该更快）
time curl -s "http://localhost:8080/api/hot-products" > /dev/null

# 检查 Redis 缓存
redis-cli
> KEYS hot_products:*
> GET hot_products:2025-01-20
> TTL hot_products:2025-01-20  # 应该显示剩余 TTL（秒）
```

验证点：
- [ ] 第二次请求明显快于第一次
- [ ] Redis 中存在缓存 Key
- [ ] 缓存 TTL 为 24 小时（86400 秒）

### 3. 定时任务验证

如果手动触发了定时任务，检查：

- [ ] 日志显示任务开始和结束
- [ ] 遍历了所有 45 个品类
- [ ] 每个品类都有分析日志
- [ ] 数据库中插入了爆品记录
- [ ] 任务执行时间合理（取决于品类数量和 API 响应时间）

```sql
-- 检查爆品数据
SELECT 
    recommend_date,
    category_id,
    COUNT(*) as product_count,
    AVG(hot_score) as avg_score
FROM hot_products
WHERE recommend_date = CURRENT_DATE
GROUP BY recommend_date, category_id
ORDER BY category_id;

-- 检查最近 7 天的爆品数据
SELECT 
    recommend_date,
    COUNT(DISTINCT category_id) as category_count,
    COUNT(*) as total_products
FROM hot_products
WHERE recommend_date >= CURRENT_DATE - INTERVAL '7 days'
GROUP BY recommend_date
ORDER BY recommend_date DESC;
```

验证点：
- [ ] 数据库中有今日的爆品记录
- [ ] 每个品类最多 20 个爆品
- [ ] 爆品评分在 0-100 之间
- [ ] 排名字段正确（1-20）

### 4. 数据完整性验证

```sql
-- 验证爆品数据完整性
SELECT 
    hp.id,
    hp.product_id,
    hp.category_id,
    hp.hot_score,
    hp.rank_in_category,
    hp.recommend_date,
    p.title,
    p.current_price,
    p.bsr_rank,
    c.name as category_name
FROM hot_products hp
LEFT JOIN products p ON hp.product_id = p.id
LEFT JOIN categories c ON hp.category_id = c.id
WHERE hp.recommend_date = CURRENT_DATE
ORDER BY hp.category_id, hp.rank_in_category
LIMIT 20;
```

验证点：
- [ ] 所有爆品都关联到有效的产品
- [ ] 所有爆品都关联到有效的品类
- [ ] 评分和排名数据合理
- [ ] 没有重复的产品（同一天同一品类）

## 常见问题排查

### 问题 1: API 返回空数据

**原因**：数据库中没有爆品数据

**解决方案**：
1. 手动触发定时任务生成数据
2. 或者插入测试数据：

```sql
-- 插入测试爆品数据
INSERT INTO hot_products (product_id, category_id, hot_score, rank_in_category, recommend_date)
SELECT 
    p.id,
    p.category_id,
    RANDOM() * 100 as hot_score,
    ROW_NUMBER() OVER (PARTITION BY p.category_id ORDER BY RANDOM()) as rank,
    CURRENT_DATE
FROM products p
WHERE p.category_id IS NOT NULL
LIMIT 100;
```

### 问题 2: 定时任务执行失败

**可能原因**：
- Bright Data API Token 未配置或无效
- 智谱 API Key 未配置或无效
- 网络连接问题
- 品类数据不存在

**排查步骤**：
1. 检查环境变量：
```bash
echo $BRIGHTDATA_API_TOKEN
echo $ZHIPU_API_KEY
```

2. 检查品类数据：
```sql
SELECT COUNT(*) FROM categories;  -- 应该有 45 个品类
SELECT COUNT(*) FROM category_groups;  -- 应该有 4 个品类组
```

3. 查看详细错误日志：
```bash
grep -i "error\|exception" flashsell/start/logs/flashsell.log
```

### 问题 3: 缓存未生效

**可能原因**：
- Redis 未启动
- Redis 连接配置错误

**排查步骤**：
1. 检查 Redis 状态：
```bash
redis-cli ping  # 应该返回 PONG
```

2. 检查 Redis 连接配置：
```bash
redis-cli
> CONFIG GET bind
> CONFIG GET port
```

3. 检查应用日志中的 Redis 连接错误

### 问题 4: 编译错误

**可能原因**：
- Maven 依赖未正确下载
- 代码有语法错误

**解决方案**：
```bash
cd flashsell
mvn clean install -U  # 强制更新依赖
```

## 测试结果记录

### 测试环境

- 操作系统：macOS
- Java 版本：17
- PostgreSQL 版本：15+
- Redis 版本：7+
- 测试日期：2025-01-20

### 测试结果

| 测试项 | 状态 | 备注 |
|--------|------|------|
| 爆品推荐列表 API | ⬜ 待测试 | |
| 指定日期爆品推荐 | ⬜ 待测试 | |
| 指定品类组爆品推荐 | ⬜ 待测试 | |
| Top 4 爆品推荐 | ⬜ 待测试 | |
| 产品爆品历史趋势 | ⬜ 待测试 | |
| 缓存功能 | ⬜ 待测试 | |
| 手动触发定时任务 | ⬜ 待测试 | |
| 数据完整性 | ⬜ 待测试 | |

### 问题记录

| 问题描述 | 严重程度 | 状态 | 解决方案 |
|----------|----------|------|----------|
| | | | |

## 下一步

测试通过后，可以继续进行：
1. 阶段八：仪表盘与历史记录功能开发
2. 性能优化和监控配置
3. 生产环境部署准备

## 参考文档

- 需求文档：`.kiro/specs/flashsell-technical-solution/requirements.md`
- 设计文档：`.kiro/specs/flashsell-technical-solution/design.md`
- 任务列表：`.kiro/specs/flashsell-technical-solution/tasks.md`
- Property 16-18: 爆品推荐相关的正确性属性
