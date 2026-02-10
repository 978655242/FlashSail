#!/bin/bash

# Bright Data API 配置和测试脚本

echo "=========================================="
echo "Bright Data API 配置向导"
echo "=========================================="
echo

# 检查是否已设置环境变量
if [ -n "$BRIGHTDATA_API_TOKEN" ]; then
    echo "✅ 检测到环境变量中的 API Token"
    echo "   Token: ${BRIGHTDATA_API_TOKEN:0:20}..."
else
    echo "⚠️  未检测到环境变量 BRIGHTDATA_API_TOKEN"
    echo
    echo "请按以下步骤配置："
    echo
    echo "1. 访问 Bright Data 官网注册："
    echo "   https://brightdata.com/"
    echo
    echo "2. 登录后进入控制面板"
    echo
    echo "3. 导航到 Settings → API Access"
    echo
    echo "4. 点击 'Generate API Token'"
    echo
    echo "5. 复制您的 API Token"
    echo
    echo "6. 设置环境变量："
    echo "   export BRIGHTDATA_API_TOKEN=\"your-token-here\""
    echo
    echo "7. 或者直接编辑配置文件："
    echo "   flashsell/start/src/main/resources/application-dev.yml"
    echo
    read -p "配置完成后按 Enter 继续..."
fi

echo
echo "=========================================="
echo "测试 Bright Data API 连接"
echo "=========================================="
echo

# 检查后端是否在运行
BACKEND_URL="http://localhost:8082"
HEALTH_CHECK=$(curl -s "$BACKEND_URL/api/categories" 2>/dev/null | jq -r '.code // empty')

if [ "$HEALTH_CHECK" = "200" ]; then
    echo "✅ 后端服务运行正常"
else
    echo "❌ 后端服务未运行，请先启动后端："
    echo "   cd flashsell/start && mvn spring-boot:run"
    exit 1
fi

# 测试搜索 API（需要登录 token）
echo
echo "=========================================="
echo "测试产品搜索功能"
echo "=========================================="

# 注册/登录获取 token
TEST_PHONE="138$(( $(date +%s) % 100000000 ))"
echo "使用测试手机号: $TEST_PHONE"

REGISTER_RESPONSE=$(curl -s -X POST "$BACKEND_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"phone\":\"$TEST_PHONE\",\"verifyCode\":\"123456\"}")

ACCESS_TOKEN=$(echo "$REGISTER_RESPONSE" | jq -r '.data.token // .data.accessToken // empty')

if [ -z "$ACCESS_TOKEN" ] || [ "$ACCESS_TOKEN" = "null" ]; then
    echo "❌ 无法获取认证 Token"
    echo "Response: $REGISTER_RESPONSE"
    exit 1
fi

echo "✅ 登录成功"
echo

# 测试搜索 API
echo "测试搜索关键词: bluetooth speaker"
SEARCH_RESPONSE=$(curl -s -X POST "$BACKEND_URL/api/search" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"query":"bluetooth speaker","page":1,"pageSize":5}')

echo "搜索响应:"
echo "$SEARCH_RESPONSE" | jq '.'

# 检查搜索结果
CODE=$(echo "$SEARCH_RESPONSE" | jq -r '.code // empty')
MESSAGE=$(echo "$SEARCH_RESPONSE" | jq -r '.message // empty')

if [ "$CODE" = "200" ]; then
    PRODUCT_COUNT=$(echo "$SEARCH_RESPONSE" | jq -r '.data.products | length')
    echo
    echo "✅ 搜索成功！找到 $PRODUCT_COUNT 个产品"

    if [ "$PRODUCT_COUNT" -gt 0 ]; then
        echo "🎉 Bright Data API 集成正常工作！"
    else
        echo "⚠️  搜索成功但没有产品，可能需要检查搜索关键词"
    fi
else
    echo
    echo "⚠️  搜索响应码: $CODE"
    echo "消息: $MESSAGE"

    if echo "$MESSAGE" | grep -qi "bright"; then
        echo
        echo "❌ Bright Data API 配置可能有问题，请检查："
        echo "   1. API Token 是否正确"
        echo "   2. 账户是否有足够的额度"
        echo "   3. 网络连接是否正常"
    fi
fi

echo
echo "=========================================="
echo "测试完成"
echo "=========================================="
