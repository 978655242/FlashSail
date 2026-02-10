# 快速测试指南 - Checkpoint 22

## 快速开始（5分钟测试）

### 1. 启动服务

```bash
# 终端 1: 启动 PostgreSQL（如果未运行）
# 使用 Docker
docker run --name flashsell-postgres -e POSTGRES_PASSWORD=flashsell123 -e POSTGRES_USER=flashsell -e POSTGRES_DB=flashsell -p 5432:5432 -d postgres:15

# 或使用 Homebrew
brew services start postgresql@15

# 终端 2: 启动 Redis（如果未运行）
# 使用 Docker
docker run --name flashsell-redis -p 6379:6379 -d redis:7

# 或使用 Homebrew
brew services start redis

# 终端 3: 启动 FlashSell 应用
cd flashsell/start
mvn spring-boot:run
```

### 2. 运行自动化测试

```bash
# 在新终端执行
./test-hot-products.sh
```

### 3. 查看结果

测试脚本会自动：
- ✅ 检查服务健康状态
- ✅ 测试所有爆品推荐 API
- ✅ 验证缓存功能
- ✅ 提供手动触发定时任务的选项

## 手动快速测试（1分钟）

```bash
# 1. 检查服务
curl http://localhost:8080/api/admin/health

# 2. 测试爆品列表
curl http://localhost:8080/api/hot-products | jq

# 3. 测试 Top 4 爆品
curl http://localhost:8080/api/hot-products/top4 | jq
```

## 如果没有数据

```bash
# 手动触发定时任务生成数据
curl -X POST http://localhost:8080/api/admin/hot-products/trigger

# 等待 1-2 分钟后再次测试
curl http://localhost:8080/api/hot-products | jq
```

## 预期结果

✅ **成功标志**：
- 所有 API 返回 200 状态码
- 响应包含 `"code": 200, "message": "success"`
- 数据结构完整（date, groups, products 等）
- 缓存功能正常（第二次请求更快）

❌ **失败标志**：
- API 返回 500 错误
- 响应数据为空或格式错误
- 缓存未生效
- 定时任务执行失败

## 故障排查

### 问题：API 返回空数据

```bash
# 检查数据库
psql -h localhost -U flashsell -d flashsell -c "SELECT COUNT(*) FROM hot_products WHERE recommend_date = CURRENT_DATE;"

# 如果为 0，手动触发任务
curl -X POST http://localhost:8080/api/admin/hot-products/trigger
```

### 问题：服务无法启动

```bash
# 检查端口占用
lsof -i :8080

# 检查数据库连接
psql -h localhost -U flashsell -d flashsell -c "SELECT 1;"

# 检查 Redis 连接
redis-cli ping
```

### 问题：定时任务失败

```bash
# 查看日志
tail -f flashsell/start/logs/flashsell.log | grep -i "error\|exception"

# 检查环境变量
echo $BRIGHTDATA_API_TOKEN
echo $ZHIPU_API_KEY
```

## 完整测试

详细测试步骤请参考：`CHECKPOINT-22-HOT-PRODUCTS-TEST-GUIDE.md`

## 测试完成后

- [ ] 所有 API 测试通过
- [ ] 缓存功能正常
- [ ] 定时任务可以手动触发
- [ ] 数据完整性验证通过

✅ **测试通过** → 继续下一个检查点（阶段八：仪表盘与历史记录）

❌ **测试失败** → 查看详细测试指南排查问题
