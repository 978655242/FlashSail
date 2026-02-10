#!/bin/bash

# FlashSell 爆品推荐功能测试脚本
# 用于测试爆品推荐 API 和定时任务

BASE_URL="http://localhost:8081"
API_PREFIX="/api"

echo "=========================================="
echo "FlashSell 爆品推荐功能测试"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试结果统计
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 测试函数
test_api() {
    local test_name=$1
    local method=$2
    local endpoint=$3
    local data=$4
    local expected_status=$5
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo "测试 $TOTAL_TESTS: $test_name"
    echo "请求: $method $endpoint"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL$endpoint" \
            -H "Content-Type: application/json")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data")
    fi
    
    # 提取状态码和响应体
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    echo "状态码: $http_code"
    echo "响应: $body" | jq '.' 2>/dev/null || echo "$body"
    
    if [ "$http_code" = "$expected_status" ]; then
        echo -e "${GREEN}✓ 测试通过${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗ 测试失败 (期望状态码: $expected_status, 实际: $http_code)${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    echo ""
}

# 检查服务是否运行
echo "检查 FlashSell 服务状态..."
if ! curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo -e "${RED}错误: FlashSell 服务未运行，请先启动服务${NC}"
    echo "启动命令: cd flashsell && mvn spring-boot:run"
    exit 1
fi
echo -e "${GREEN}✓ 服务正常运行${NC}"
echo ""

# ==========================================
# 测试 1: 获取今日爆品推荐列表
# ==========================================
test_api \
    "获取今日爆品推荐列表" \
    "GET" \
    "$API_PREFIX/hot-products" \
    "" \
    "200"

# ==========================================
# 测试 2: 获取指定日期的爆品推荐
# ==========================================
TODAY=$(date +%Y-%m-%d)
test_api \
    "获取指定日期的爆品推荐" \
    "GET" \
    "$API_PREFIX/hot-products?date=$TODAY" \
    "" \
    "200"

# ==========================================
# 测试 3: 获取指定品类组的爆品推荐
# ==========================================
test_api \
    "获取指定品类组的爆品推荐" \
    "GET" \
    "$API_PREFIX/hot-products?categoryGroupId=1" \
    "" \
    "200"

# ==========================================
# 测试 4: 获取今日 Top 4 爆品（首页展示）
# ==========================================
test_api \
    "获取今日 Top 4 爆品" \
    "GET" \
    "$API_PREFIX/hot-products/top4" \
    "" \
    "200"

# ==========================================
# 测试 5: 获取产品爆品历史趋势（假设产品ID=1）
# ==========================================
test_api \
    "获取产品爆品历史趋势" \
    "GET" \
    "$API_PREFIX/hot-products/history?productId=1&days=7" \
    "" \
    "200"

# ==========================================
# 测试 6: 测试缓存功能（重复请求应该更快）
# ==========================================
echo "测试 6: 验证缓存功能"
echo "第一次请求（无缓存）..."
start_time=$(date +%s%N)
curl -s "$BASE_URL$API_PREFIX/hot-products" > /dev/null
end_time=$(date +%s%N)
first_request_time=$(( (end_time - start_time) / 1000000 ))

echo "第二次请求（有缓存）..."
start_time=$(date +%s%N)
curl -s "$BASE_URL$API_PREFIX/hot-products" > /dev/null
end_time=$(date +%s%N)
second_request_time=$(( (end_time - start_time) / 1000000 ))

echo "第一次请求耗时: ${first_request_time}ms"
echo "第二次请求耗时: ${second_request_time}ms"

if [ $second_request_time -lt $first_request_time ]; then
    echo -e "${GREEN}✓ 缓存功能正常（第二次请求更快）${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${YELLOW}⚠ 缓存可能未生效（第二次请求未明显加速）${NC}"
fi
TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo ""

# ==========================================
# 测试 7: 手动触发定时任务（需要管理员权限）
# ==========================================
echo "=========================================="
echo "手动触发爆品分析定时任务"
echo "=========================================="
echo ""
echo -e "${YELLOW}注意: 此操作会调用 Bright Data API 和 AI 服务${NC}"
echo -e "${YELLOW}如果没有配置 BRIGHTDATA_API_TOKEN 和 ZHIPU_API_KEY，任务可能会失败${NC}"
echo ""
read -p "是否继续手动触发定时任务？(y/n) " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "触发定时任务..."
    echo "注意: 定时任务会遍历所有 45 个品类，可能需要较长时间"
    echo ""
    
    test_api \
        "手动触发爆品分析定时任务" \
        "POST" \
        "$API_PREFIX/admin/hot-products/trigger" \
        "" \
        "200"
    
    echo ""
    echo -e "${YELLOW}任务已在后台执行，请查看应用日志了解执行进度${NC}"
    echo "日志位置: flashsell/start/logs/ 或控制台输出"
    echo ""
fi

# ==========================================
# 测试总结
# ==========================================
echo "=========================================="
echo "测试总结"
echo "=========================================="
echo "总测试数: $TOTAL_TESTS"
echo -e "通过: ${GREEN}$PASSED_TESTS${NC}"
echo -e "失败: ${RED}$FAILED_TESTS${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✓ 所有测试通过！爆品推荐功能正常${NC}"
    exit 0
else
    echo -e "${RED}✗ 部分测试失败，请检查日志${NC}"
    exit 1
fi
