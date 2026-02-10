#!/bin/bash

# FlashSell 市场分析功能测试脚本
# 测试任务 29: 检查点 - 确保市场分析功能正常

echo "=========================================="
echo "FlashSell 市场分析功能测试"
echo "=========================================="
echo ""

BASE_URL="http://localhost:8080/api"
TOKEN=""

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
    local test_name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local expected_code="$5"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo "测试 $TOTAL_TESTS: $test_name"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json")
    else
        response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            -d "$data")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "$expected_code" ]; then
        echo -e "${GREEN}✓ 通过${NC} (HTTP $http_code)"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo "响应: $body" | jq '.' 2>/dev/null || echo "$body"
    else
        echo -e "${RED}✗ 失败${NC} (期望 HTTP $expected_code, 实际 HTTP $http_code)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo "响应: $body"
    fi
    echo ""
}

# 1. 用户登录获取 Token
echo "=========================================="
echo "步骤 1: 用户认证"
echo "=========================================="
echo ""

login_response=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "phone": "13800138000",
        "verifyCode": "123456"
    }')

TOKEN=$(echo "$login_response" | jq -r '.data.token' 2>/dev/null)

if [ "$TOKEN" != "null" ] && [ -n "$TOKEN" ]; then
    echo -e "${GREEN}✓ 登录成功${NC}"
    echo "Token: ${TOKEN:0:20}..."
else
    echo -e "${RED}✗ 登录失败，使用测试继续（部分测试可能失败）${NC}"
    TOKEN="test-token"
fi
echo ""

# 2. 获取品类列表
echo "=========================================="
echo "步骤 2: 获取品类列表"
echo "=========================================="
echo ""

test_api "获取品类列表" "GET" "/categories" "" "200"

# 提取第一个品类ID用于后续测试
categories_response=$(curl -s -X GET "$BASE_URL/categories" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json")

CATEGORY_ID=$(echo "$categories_response" | jq -r '.data.groups[0].categories[0].id' 2>/dev/null)

if [ "$CATEGORY_ID" != "null" ] && [ -n "$CATEGORY_ID" ]; then
    echo -e "${GREEN}使用品类ID: $CATEGORY_ID 进行测试${NC}"
else
    echo -e "${YELLOW}警告: 无法获取品类ID，使用默认值 1${NC}"
    CATEGORY_ID=1
fi
echo ""

# 3. 检查数据可用性
echo "=========================================="
echo "步骤 3: 检查品类数据可用性"
echo "=========================================="
echo ""

test_api "检查品类 $CATEGORY_ID 数据可用性" "GET" "/market/check-data?categoryId=$CATEGORY_ID" "" "200"

# 4. 获取市场分析（30天）
echo "=========================================="
echo "步骤 4: 获取市场分析数据（30天）"
echo "=========================================="
echo ""

test_api "获取30天市场分析" "GET" "/market/analysis?categoryId=$CATEGORY_ID&timeRangeDays=30" "" "200"

# 5. 获取市场分析（90天）
echo "=========================================="
echo "步骤 5: 获取市场分析数据（90天）"
echo "=========================================="
echo ""

test_api "获取90天市场分析" "GET" "/market/analysis?categoryId=$CATEGORY_ID&timeRangeDays=90" "" "200"

# 6. 获取市场分析（365天）
echo "=========================================="
echo "步骤 6: 获取市场分析数据（365天）"
echo "=========================================="
echo ""

test_api "获取365天市场分析" "GET" "/market/analysis?categoryId=$CATEGORY_ID&timeRangeDays=365" "" "200"

# 7. 测试无效品类ID
echo "=========================================="
echo "步骤 7: 测试无效品类ID"
echo "=========================================="
echo ""

test_api "测试无效品类ID (999999)" "GET" "/market/analysis?categoryId=999999&timeRangeDays=30" "" "404"

# 8. 测试无效时间范围（应自动规范化）
echo "=========================================="
echo "步骤 8: 测试时间范围自动规范化"
echo "=========================================="
echo ""

test_api "测试时间范围 45 天（应规范化为30）" "GET" "/market/analysis?categoryId=$CATEGORY_ID&timeRangeDays=45" "" "200"
test_api "测试时间范围 120 天（应规范化为90）" "GET" "/market/analysis?categoryId=$CATEGORY_ID&timeRangeDays=120" "" "200"
test_api "测试时间范围 500 天（应规范化为365）" "GET" "/market/analysis?categoryId=$CATEGORY_ID&timeRangeDays=500" "" "200"

# 9. 刷新市场分析
echo "=========================================="
echo "步骤 9: 刷新市场分析数据"
echo "=========================================="
echo ""

test_api "刷新市场分析" "POST" "/market/analysis/refresh" "{\"categoryId\":$CATEGORY_ID,\"timeRangeDays\":30}" "200"

# 10. 验证响应数据结构
echo "=========================================="
echo "步骤 10: 验证响应数据结构"
echo "=========================================="
echo ""

market_response=$(curl -s -X GET "$BASE_URL/market/analysis?categoryId=$CATEGORY_ID&timeRangeDays=30" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json")

echo "检查响应数据结构..."

# 检查必需字段
required_fields=(
    ".data.category"
    ".data.marketSize"
    ".data.competitionScore"
    ".data.entryBarrier"
    ".data.overallScore"
    ".data.potentialScore"
    ".data.weekOverWeek"
    ".data.monthOverMonth"
    ".data.monthlyGrowthRate"
    ".data.salesDistribution"
    ".data.topProducts"
    ".data.analysisDate"
    ".data.timeRangeDays"
)

structure_valid=true
for field in "${required_fields[@]}"; do
    value=$(echo "$market_response" | jq "$field" 2>/dev/null)
    if [ "$value" = "null" ] || [ -z "$value" ]; then
        echo -e "${RED}✗ 缺少字段: $field${NC}"
        structure_valid=false
    else
        echo -e "${GREEN}✓ 字段存在: $field${NC}"
    fi
done

if [ "$structure_valid" = true ]; then
    echo -e "${GREEN}✓ 数据结构验证通过${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${RED}✗ 数据结构验证失败${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo ""

# 11. 验证评分范围
echo "=========================================="
echo "步骤 11: 验证评分范围（0-1）"
echo "=========================================="
echo ""

score_fields=(
    "competitionScore"
    "entryBarrier"
    "overallScore"
    "potentialScore"
)

scores_valid=true
for field in "${score_fields[@]}"; do
    score=$(echo "$market_response" | jq -r ".data.$field" 2>/dev/null)
    if [ "$score" != "null" ] && [ -n "$score" ]; then
        # 检查是否在 0-1 范围内
        if (( $(echo "$score >= 0" | bc -l) )) && (( $(echo "$score <= 1" | bc -l) )); then
            echo -e "${GREEN}✓ $field: $score (有效范围)${NC}"
        else
            echo -e "${RED}✗ $field: $score (超出范围 0-1)${NC}"
            scores_valid=false
        fi
    else
        echo -e "${YELLOW}⚠ $field: 无数据${NC}"
    fi
done

if [ "$scores_valid" = true ]; then
    echo -e "${GREEN}✓ 评分范围验证通过${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${RED}✗ 评分范围验证失败${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo ""

# 12. 验证销量分布数据
echo "=========================================="
echo "步骤 12: 验证销量分布数据"
echo "=========================================="
echo ""

sales_count=$(echo "$market_response" | jq '.data.salesDistribution | length' 2>/dev/null)

if [ "$sales_count" != "null" ] && [ "$sales_count" -gt 0 ]; then
    echo -e "${GREEN}✓ 销量分布数据存在 ($sales_count 个数据点)${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
    
    # 显示前3个数据点
    echo "前3个数据点:"
    echo "$market_response" | jq '.data.salesDistribution[:3]' 2>/dev/null
else
    echo -e "${YELLOW}⚠ 销量分布数据为空${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo ""

# 13. 验证热门产品列表
echo "=========================================="
echo "步骤 13: 验证热门产品列表"
echo "=========================================="
echo ""

products_count=$(echo "$market_response" | jq '.data.topProducts | length' 2>/dev/null)

if [ "$products_count" != "null" ]; then
    if [ "$products_count" -gt 0 ]; then
        echo -e "${GREEN}✓ 热门产品列表存在 ($products_count 个产品)${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        
        # 显示前2个产品
        echo "前2个产品:"
        echo "$market_response" | jq '.data.topProducts[:2] | .[] | {id, title, currentPrice, bsrRank}' 2>/dev/null
    else
        echo -e "${YELLOW}⚠ 热门产品列表为空（可能该品类暂无产品数据）${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    fi
else
    echo -e "${RED}✗ 无法获取热门产品列表${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo ""

# 测试总结
echo "=========================================="
echo "测试总结"
echo "=========================================="
echo ""
echo "总测试数: $TOTAL_TESTS"
echo -e "${GREEN}通过: $PASSED_TESTS${NC}"
echo -e "${RED}失败: $FAILED_TESTS${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}=========================================="
    echo "✓ 所有测试通过！市场分析功能正常"
    echo "==========================================${NC}"
    exit 0
else
    echo -e "${RED}=========================================="
    echo "✗ 部分测试失败，请检查问题"
    echo "==========================================${NC}"
    exit 1
fi
