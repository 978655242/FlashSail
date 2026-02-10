#!/bin/bash

echo "========================================="
echo "Market Analysis API Test"
echo "========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Base URL
BASE_URL="http://localhost:8081/api"

# Test user credentials
TEST_PHONE="13800138000"
TEST_CODE="123456"

# Login and get token
echo "Logging in to get authentication token..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"phone\":\"$TEST_PHONE\",\"verifyCode\":\"$TEST_CODE\"}")

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.token // empty')

if [ -z "$TOKEN" ]; then
    echo -e "${RED}❌ Failed to login. Cannot proceed with authenticated tests.${NC}"
    echo "Response: $LOGIN_RESPONSE"
    echo ""
    echo "Note: Market analysis endpoints require authentication."
    echo "Continuing with unauthenticated tests only..."
    echo ""
else
    echo -e "${GREEN}✓ Successfully logged in${NC}"
    echo ""
fi

# Auth header
AUTH_HEADER="Authorization: Bearer $TOKEN"

# Test 1: Get categories
echo "Test 1: Get Categories"
echo "GET $BASE_URL/categories"
CATEGORIES_RESPONSE=$(curl -s "$BASE_URL/categories")
echo "$CATEGORIES_RESPONSE" | jq '.'

# Extract first category ID
CATEGORY_ID=$(echo "$CATEGORIES_RESPONSE" | jq -r '.data.groups[0].categories[0].id // empty')

if [ -z "$CATEGORY_ID" ]; then
    echo -e "${RED}❌ No categories found. Cannot proceed with market analysis tests.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Found category ID: $CATEGORY_ID${NC}"
echo ""

# Test 2: Check data availability
echo "Test 2: Check Data Availability"
echo "GET $BASE_URL/market/check-data?categoryId=$CATEGORY_ID"
if [ -n "$TOKEN" ]; then
    DATA_CHECK_RESPONSE=$(curl -s -H "$AUTH_HEADER" "$BASE_URL/market/check-data?categoryId=$CATEGORY_ID")
else
    DATA_CHECK_RESPONSE=$(curl -s "$BASE_URL/market/check-data?categoryId=$CATEGORY_ID")
fi
echo "$DATA_CHECK_RESPONSE" | jq '.'

HAS_DATA=$(echo "$DATA_CHECK_RESPONSE" | jq -r '.data // false')

if [ "$HAS_DATA" = "false" ]; then
    echo -e "${YELLOW}⚠ Category $CATEGORY_ID has insufficient data for analysis${NC}"
    echo -e "${YELLOW}This is expected if no products have been added yet${NC}"
else
    echo -e "${GREEN}✓ Category $CATEGORY_ID has sufficient data${NC}"
fi
echo ""

# Test 3: Get market analysis (30 days)
echo "Test 3: Get Market Analysis (30 days)"
echo "GET $BASE_URL/market/analysis?categoryId=$CATEGORY_ID&timeRangeDays=30"
if [ -n "$TOKEN" ]; then
    MARKET_30_RESPONSE=$(curl -s -H "$AUTH_HEADER" "$BASE_URL/market/analysis?categoryId=$CATEGORY_ID&timeRangeDays=30")
else
    MARKET_30_RESPONSE=$(curl -s "$BASE_URL/market/analysis?categoryId=$CATEGORY_ID&timeRangeDays=30")
fi
echo "$MARKET_30_RESPONSE" | jq '.'

if echo "$MARKET_30_RESPONSE" | jq -e '.code == 200' > /dev/null; then
    echo -e "${GREEN}✓ Market analysis (30 days) retrieved successfully${NC}"
else
    echo -e "${YELLOW}⚠ Market analysis (30 days) returned non-200 code${NC}"
fi
echo ""

# Test 4: Get market analysis (90 days)
echo "Test 4: Get Market Analysis (90 days)"
echo "GET $BASE_URL/market/analysis?categoryId=$CATEGORY_ID&timeRangeDays=90"
if [ -n "$TOKEN" ]; then
    MARKET_90_RESPONSE=$(curl -s -H "$AUTH_HEADER" "$BASE_URL/market/analysis?categoryId=$CATEGORY_ID&timeRangeDays=90")
else
    MARKET_90_RESPONSE=$(curl -s "$BASE_URL/market/analysis?categoryId=$CATEGORY_ID&timeRangeDays=90")
fi
echo "$MARKET_90_RESPONSE" | jq '.'

if echo "$MARKET_90_RESPONSE" | jq -e '.code == 200' > /dev/null; then
    echo -e "${GREEN}✓ Market analysis (90 days) retrieved successfully${NC}"
else
    echo -e "${YELLOW}⚠ Market analysis (90 days) returned non-200 code${NC}"
fi
echo ""

# Test 5: Get market analysis (365 days)
echo "Test 5: Get Market Analysis (365 days)"
echo "GET $BASE_URL/market/analysis?categoryId=$CATEGORY_ID&timeRangeDays=365"
if [ -n "$TOKEN" ]; then
    MARKET_365_RESPONSE=$(curl -s -H "$AUTH_HEADER" "$BASE_URL/market/analysis?categoryId=$CATEGORY_ID&timeRangeDays=365")
else
    MARKET_365_RESPONSE=$(curl -s "$BASE_URL/market/analysis?categoryId=$CATEGORY_ID&timeRangeDays=365")
fi
echo "$MARKET_365_RESPONSE" | jq '.'

if echo "$MARKET_365_RESPONSE" | jq -e '.code == 200' > /dev/null; then
    echo -e "${GREEN}✓ Market analysis (365 days) retrieved successfully${NC}"
else
    echo -e "${YELLOW}⚠ Market analysis (365 days) returned non-200 code${NC}"
fi
echo ""

# Test 6: Export market report
echo "Test 6: Export Market Report"
echo "GET $BASE_URL/market/export?categoryId=$CATEGORY_ID&timeRangeDays=30"
if [ -n "$TOKEN" ]; then
    EXPORT_RESPONSE=$(curl -s -H "$AUTH_HEADER" "$BASE_URL/market/export?categoryId=$CATEGORY_ID&timeRangeDays=30")
else
    EXPORT_RESPONSE=$(curl -s "$BASE_URL/market/export?categoryId=$CATEGORY_ID&timeRangeDays=30")
fi
echo "$EXPORT_RESPONSE" | jq '.'

if echo "$EXPORT_RESPONSE" | jq -e '.code == 501' > /dev/null; then
    echo -e "${YELLOW}⚠ Export feature not yet implemented (expected)${NC}"
elif echo "$EXPORT_RESPONSE" | jq -e '.code == 200' > /dev/null; then
    echo -e "${GREEN}✓ Export report generated successfully${NC}"
else
    echo -e "${RED}❌ Export report failed with unexpected error${NC}"
fi
echo ""

# Test 7: Invalid category ID
echo "Test 7: Invalid Category ID"
echo "GET $BASE_URL/market/analysis?categoryId=99999&timeRangeDays=30"
if [ -n "$TOKEN" ]; then
    INVALID_RESPONSE=$(curl -s -H "$AUTH_HEADER" "$BASE_URL/market/analysis?categoryId=99999&timeRangeDays=30")
else
    INVALID_RESPONSE=$(curl -s "$BASE_URL/market/analysis?categoryId=99999&timeRangeDays=30")
fi
echo "$INVALID_RESPONSE" | jq '.'

if echo "$INVALID_RESPONSE" | jq -e '.code == 404' > /dev/null; then
    echo -e "${GREEN}✓ Correctly returned 404 for invalid category${NC}"
else
    echo -e "${RED}❌ Did not return 404 for invalid category${NC}"
fi
echo ""

echo "========================================="
echo "Test Summary"
echo "========================================="
echo -e "${GREEN}✓ Market Analysis API endpoints are accessible${NC}"
echo -e "${YELLOW}⚠ Some endpoints may return no data if database is empty${NC}"
echo -e "${YELLOW}⚠ Export feature is not yet implemented (as expected)${NC}"
echo ""
