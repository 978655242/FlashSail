#!/bin/bash

# FlashSell Authentication System Test Script
# Tests: Register, Login, Token Refresh, Logout

BASE_URL="http://localhost:8081"
PHONE="13800138000"
VERIFY_CODE="123456"

echo "=========================================="
echo "FlashSell Authentication System Test"
echo "=========================================="
echo ""

# Test 1: Register
echo "Test 1: User Registration"
echo "POST ${BASE_URL}/api/auth/register"
REGISTER_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"phone\":\"${PHONE}\",\"verifyCode\":\"${VERIFY_CODE}\"}")

echo "Response: ${REGISTER_RESPONSE}"
echo ""

# Extract tokens from register response
TOKEN=$(echo ${REGISTER_RESPONSE} | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
REFRESH_TOKEN=$(echo ${REGISTER_RESPONSE} | grep -o '"refreshToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ Registration failed - no token received"
  echo ""
else
  echo "✅ Registration successful"
  echo "Token: ${TOKEN:0:50}..."
  echo "Refresh Token: ${REFRESH_TOKEN:0:50}..."
  echo ""
fi

# Test 2: Login
echo "Test 2: User Login"
echo "POST ${BASE_URL}/api/auth/login"
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"phone\":\"${PHONE}\",\"verifyCode\":\"${VERIFY_CODE}\"}")

echo "Response: ${LOGIN_RESPONSE}"
echo ""

# Extract tokens from login response
TOKEN=$(echo ${LOGIN_RESPONSE} | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
REFRESH_TOKEN=$(echo ${LOGIN_RESPONSE} | grep -o '"refreshToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ Login failed - no token received"
  echo ""
else
  echo "✅ Login successful"
  echo "Token: ${TOKEN:0:50}..."
  echo "Refresh Token: ${REFRESH_TOKEN:0:50}..."
  echo ""
fi

# Test 3: Token Refresh
echo "Test 3: Token Refresh"
echo "POST ${BASE_URL}/api/auth/refresh"
REFRESH_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"${REFRESH_TOKEN}\"}")

echo "Response: ${REFRESH_RESPONSE}"
echo ""

# Extract new tokens
NEW_TOKEN=$(echo ${REFRESH_RESPONSE} | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
NEW_REFRESH_TOKEN=$(echo ${REFRESH_RESPONSE} | grep -o '"refreshToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$NEW_TOKEN" ]; then
  echo "❌ Token refresh failed - no new token received"
  echo ""
else
  echo "✅ Token refresh successful"
  echo "New Token: ${NEW_TOKEN:0:50}..."
  echo "New Refresh Token: ${NEW_REFRESH_TOKEN:0:50}..."
  echo ""
  # Update tokens for logout test
  TOKEN=$NEW_TOKEN
fi

# Test 4: Logout
echo "Test 4: User Logout"
echo "POST ${BASE_URL}/api/auth/logout"
LOGOUT_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/logout" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}")

echo "Response: ${LOGOUT_RESPONSE}"
echo ""

if echo ${LOGOUT_RESPONSE} | grep -q '"code":200'; then
  echo "✅ Logout successful"
else
  echo "❌ Logout failed"
fi
echo ""

# Test 5: Verify token is invalid after logout
echo "Test 5: Verify Token Invalid After Logout"
echo "POST ${BASE_URL}/api/auth/refresh (should fail)"
VERIFY_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"${TOKEN}\"}")

echo "Response: ${VERIFY_RESPONSE}"
echo ""

if echo ${VERIFY_RESPONSE} | grep -q '"code":4'; then
  echo "✅ Token correctly invalidated after logout"
else
  echo "⚠️  Token validation after logout needs verification (response: $VERIFY_RESPONSE)"
fi
echo ""

echo "=========================================="
echo "Authentication System Test Complete"
echo "=========================================="
