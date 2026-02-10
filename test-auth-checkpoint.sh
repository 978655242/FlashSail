#!/bin/bash

# FlashSell Authentication System Checkpoint Test
# Tests: Register, Login, Token Refresh, Logout

BASE_URL="http://localhost:8080/api"
echo "==================================="
echo "FlashSell Authentication Test"
echo "==================================="
echo ""

# Test 1: Register a new user
echo "Test 1: User Registration"
echo "-----------------------------------"
REGISTER_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "verifyCode": "123456"
  }')

echo "Response: $REGISTER_RESPONSE"
echo ""

# Extract token from registration response
TOKEN=$(echo $REGISTER_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
REFRESH_TOKEN=$(echo $REGISTER_RESPONSE | grep -o '"refreshToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ Registration failed - no token received"
  echo ""
else
  echo "✅ Registration successful - token received"
  echo "Token: ${TOKEN:0:20}..."
  echo ""
fi

# Test 2: Login with existing user
echo "Test 2: User Login"
echo "-----------------------------------"
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "verifyCode": "123456"
  }')

echo "Response: $LOGIN_RESPONSE"
echo ""

# Extract new token from login response
NEW_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
NEW_REFRESH_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"refreshToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$NEW_TOKEN" ]; then
  echo "❌ Login failed - no token received"
  echo ""
else
  echo "✅ Login successful - token received"
  echo "Token: ${NEW_TOKEN:0:20}..."
  TOKEN=$NEW_TOKEN
  REFRESH_TOKEN=$NEW_REFRESH_TOKEN
  echo ""
fi

# Test 3: Token Refresh
echo "Test 3: Token Refresh"
echo "-----------------------------------"
if [ -z "$REFRESH_TOKEN" ]; then
  echo "❌ Cannot test refresh - no refresh token available"
  echo ""
else
  REFRESH_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/refresh" \
    -H "Content-Type: application/json" \
    -d "{
      \"refreshToken\": \"$REFRESH_TOKEN\"
    }")

  echo "Response: $REFRESH_RESPONSE"
  echo ""

  REFRESHED_TOKEN=$(echo $REFRESH_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

  if [ -z "$REFRESHED_TOKEN" ]; then
    echo "❌ Token refresh failed"
    echo ""
  else
    echo "✅ Token refresh successful"
    echo "New Token: ${REFRESHED_TOKEN:0:20}..."
    TOKEN=$REFRESHED_TOKEN
    echo ""
  fi
fi

# Test 4: Logout
echo "Test 4: User Logout"
echo "-----------------------------------"
if [ -z "$TOKEN" ]; then
  echo "❌ Cannot test logout - no token available"
  echo ""
else
  LOGOUT_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/logout" \
    -H "Authorization: Bearer $TOKEN")

  echo "Response: $LOGOUT_RESPONSE"
  echo ""

  # Check if logout was successful
  if echo "$LOGOUT_RESPONSE" | grep -q "success"; then
    echo "✅ Logout successful"
  else
    echo "❌ Logout failed"
  fi
  echo ""
fi

# Test 5: Verify token is invalid after logout
echo "Test 5: Verify Token Invalidation After Logout"
echo "-----------------------------------"
if [ -z "$TOKEN" ]; then
  echo "⚠️  Skipping - no token to test"
  echo ""
else
  # Try to use the token after logout (should fail)
  VERIFY_RESPONSE=$(curl -s -X GET "${BASE_URL}/user/profile" \
    -H "Authorization: Bearer $TOKEN")

  echo "Response: $VERIFY_RESPONSE"
  echo ""

  if echo "$VERIFY_RESPONSE" | grep -q "401\|Unauthorized\|Invalid token"; then
    echo "✅ Token correctly invalidated after logout"
  else
    echo "⚠️  Token may still be valid (check response)"
  fi
  echo ""
fi

echo "==================================="
echo "Authentication Test Complete"
echo "==================================="
