# Checkpoint 26 - 仪表盘和历史记录功能测试指南

## 测试环境

- **后端地址**: http://localhost:8080
- **应用状态**: ✅ 已启动
- **数据库**: PostgreSQL (localhost:5432)
- **缓存**: Redis (localhost:6379)

## 测试步骤

### 1. 用户注册/登录

首先需要获取认证令牌：

```bash
# 注册新用户
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "verifyCode": "123456"
  }'

# 或者登录已有用户
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "verifyCode": "123456"
  }'
```

**保存返回的 token**，后续请求需要使用。

---

### 2. 仪表盘功能测试

#### 2.1 测试数据概览接口

```bash
# 设置 TOKEN 变量（替换为实际的 token）
TOKEN="your_token_here"

# 获取仪表盘数据概览
curl -X GET http://localhost:8080/api/dashboard/overview \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**预期结果**:
- 返回今日新品发现数
- 返回潜力爆品推荐数
- 返回收藏产品数
- 返回 AI 推荐准确率
- 返回数据最后更新时间

#### 2.2 测试爆品推荐接口

```bash
# 获取 AI 爆品推荐 Top 4
curl -X GET http://localhost:8080/api/dashboard/hot-recommendations \
  -H "Content-Type: application/json"
```

**预期结果**:
- 返回最多 4 个爆品推荐
- 每个产品包含爆品评分、上榜天数、排名变化

#### 2.3 测试最近活动接口

```bash
# 获取用户最近活动
curl -X GET http://localhost:8080/api/dashboard/recent-activity \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**预期结果**:
- 返回最近 10 条搜索历史
- 返回最近浏览的 8 个产品

#### 2.4 测试热门品类趋势接口

```bash
# 获取热门品类趋势
curl -X GET http://localhost:8080/api/dashboard/trending-categories \
  -H "Content-Type: application/json"
```

**预期结果**:
- 返回热门品类列表
- 每个品类包含趋势评分、周环比增长、热门产品数

#### 2.5 测试热门关键词接口

```bash
# 获取热门搜索关键词
curl -X GET http://localhost:8080/api/dashboard/hot-keywords \
  -H "Content-Type: application/json"
```

**预期结果**:
- 返回热门关键词列表
- 每个关键词包含搜索次数和趋势（UP/DOWN/STABLE）

---

### 3. 历史记录功能测试

#### 3.1 测试搜索历史

```bash
# 获取搜索历史（分页）
curl -X GET "http://localhost:8080/api/search/history?page=0&pageSize=20" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"

# 删除单条搜索历史（替换 {id} 为实际的历史记录 ID）
curl -X DELETE http://localhost:8080/api/search/history/{id} \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"

# 清空搜索历史
curl -X DELETE http://localhost:8080/api/search/history \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**预期结果**:
- 获取历史：返回分页的搜索历史列表
- 删除单条：成功删除指定记录
- 清空历史：成功清空所有搜索历史

#### 3.2 测试浏览历史

```bash
# 获取浏览历史（分页）
curl -X GET "http://localhost:8080/api/browse/history?page=0&pageSize=20" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"

# 删除单条浏览历史（替换 {productId} 为实际的产品 ID）
curl -X DELETE http://localhost:8080/api/browse/history/{productId} \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"

# 清空浏览历史
curl -X DELETE http://localhost:8080/api/browse/history \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**预期结果**:
- 获取历史：返回分页的浏览历史列表
- 删除单条：成功删除指定产品的浏览记录
- 清空历史：成功清空所有浏览历史

---

## 测试检查清单

### 仪表盘功能
- [ ] 数据概览接口正常返回
- [ ] 爆品推荐接口正常返回（最多 4 个）
- [ ] 最近活动接口正常返回（需要认证）
- [ ] 热门品类趋势接口正常返回
- [ ] 热门关键词接口正常返回
- [ ] 未登录用户访问概览接口返回基础数据
- [ ] 已登录用户访问概览接口返回个性化数据

### 历史记录功能
- [ ] 搜索历史获取接口正常（分页）
- [ ] 搜索历史删除单条功能正常
- [ ] 搜索历史清空功能正常
- [ ] 浏览历史获取接口正常（分页）
- [ ] 浏览历史删除单条功能正常
- [ ] 浏览历史清空功能正常
- [ ] 未登录用户访问历史接口返回 401 错误
- [ ] 分页参数校验正常（page >= 0, pageSize 1-100）

---

## 常见问题

### 1. 返回 401 未授权错误
- 检查是否正确设置了 Authorization header
- 检查 token 是否过期（access token 有效期 1 小时）
- 如果过期，使用 refresh token 刷新

### 2. 返回 500 系统错误
- 检查后端日志：`tail -f flashsell/start/target/logs/flashsell.log`
- 检查数据库连接是否正常
- 检查 Redis 连接是否正常

### 3. 数据为空
- 仪表盘数据需要先执行爆品推荐定时任务或手动添加数据
- 历史记录需要先进行搜索或浏览操作才会有数据

---

## 快速测试脚本

将以下脚本保存为 `test-checkpoint-26.sh` 并执行：

```bash
#!/bin/bash

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== Checkpoint 26 测试脚本 ===${NC}\n"

# 1. 注册/登录
echo -e "${YELLOW}1. 注册用户...${NC}"
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"phone": "13800138000", "verifyCode": "123456"}')

TOKEN=$(echo $RESPONSE | jq -r '.data.token')

if [ "$TOKEN" != "null" ] && [ -n "$TOKEN" ]; then
    echo -e "${GREEN}✓ 注册成功，获取到 token${NC}\n"
else
    echo -e "${RED}✗ 注册失败，尝试登录...${NC}"
    RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{"phone": "13800138000", "verifyCode": "123456"}')
    TOKEN=$(echo $RESPONSE | jq -r '.data.token')
    echo -e "${GREEN}✓ 登录成功${NC}\n"
fi

# 2. 测试仪表盘接口
echo -e "${YELLOW}2. 测试仪表盘数据概览...${NC}"
curl -s -X GET http://localhost:8080/api/dashboard/overview \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""

echo -e "${YELLOW}3. 测试爆品推荐...${NC}"
curl -s -X GET http://localhost:8080/api/dashboard/hot-recommendations | jq .
echo ""

echo -e "${YELLOW}4. 测试最近活动...${NC}"
curl -s -X GET http://localhost:8080/api/dashboard/recent-activity \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""

echo -e "${YELLOW}5. 测试热门品类趋势...${NC}"
curl -s -X GET http://localhost:8080/api/dashboard/trending-categories | jq .
echo ""

echo -e "${YELLOW}6. 测试热门关键词...${NC}"
curl -s -X GET http://localhost:8080/api/dashboard/hot-keywords | jq .
echo ""

# 3. 测试历史记录接口
echo -e "${YELLOW}7. 测试搜索历史...${NC}"
curl -s -X GET "http://localhost:8080/api/search/history?page=0&pageSize=20" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""

echo -e "${YELLOW}8. 测试浏览历史...${NC}"
curl -s -X GET "http://localhost:8080/api/browse/history?page=0&pageSize=20" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""

echo -e "${GREEN}=== 测试完成 ===${NC}"
```

执行方式：
```bash
chmod +x test-checkpoint-26.sh
./test-checkpoint-26.sh
```

---

## 注意事项

1. **认证要求**：
   - `/api/dashboard/overview` - 可选认证（登录后返回个性化数据）
   - `/api/dashboard/recent-activity` - 必须认证
   - 所有 `/api/search/history` 和 `/api/browse/history` 接口 - 必须认证

2. **数据准备**：
   - 如果是新数据库，仪表盘数据可能为空或默认值
   - 建议先执行一些搜索和浏览操作，生成历史记录数据

3. **性能考虑**：
   - 仪表盘接口有缓存，TTL 为 5 分钟
   - 热门品类和关键词有缓存，TTL 为 30 分钟到 1 小时

---

## 测试完成后

请在测试完成后反馈：
1. 哪些接口测试通过 ✅
2. 哪些接口有问题 ❌
3. 遇到的具体错误信息

这样我可以帮您解决问题或继续下一步的开发工作。
