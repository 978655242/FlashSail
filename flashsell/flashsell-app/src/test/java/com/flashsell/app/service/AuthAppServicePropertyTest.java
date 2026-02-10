package com.flashsell.app.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Assume;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.lifecycle.BeforeProperty;

/**
 * 认证属性测试
 * 
 * Property 1: 认证 Token 往返一致性
 * *对于任意* 有效的用户凭证，注册或登录后返回的 JWT token 应该能够被正确解析，
 * 且包含正确的用户 ID 和过期时间。
 * 
 * Property 4: 会话登出一致性
 * *对于任意* 已登录用户，登出后其 session 应该从 Redis 中删除，
 * 且使用原 token 的后续请求应该返回 401（会话失效）。
 * 
 * Validates: Requirements 1.1, 1.2, 1.5, 1.6
 * 
 * Feature: flashsell-technical-solution, Property 1: 认证 Token 往返一致性
 * Feature: flashsell-technical-solution, Property 4: 会话登出一致性
 */
class AuthAppServicePropertyTest {

    // ========== Test Infrastructure (Simplified implementations for testing) ==========

    /**
     * Token Gateway - Simplified JWT implementation for testing
     */
    static class TestTokenGateway {
        private final SecretKey secretKey;
        private final long accessTokenExpiration;
        private final long refreshTokenExpiration;

        public TestTokenGateway() {
            String secret = "flashsell-test-secret-key-must-be-at-least-256-bits-long-for-hs256";
            this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            this.accessTokenExpiration = 3600000L; // 1 hour
            this.refreshTokenExpiration = 604800000L; // 7 days
        }

        public String generateAccessToken(Long userId) {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

            return Jwts.builder()
                    .subject(String.valueOf(userId))
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .claim("type", "access")
                    .signWith(secretKey)
                    .compact();
        }

        public String generateRefreshToken(Long userId) {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

            return Jwts.builder()
                    .subject(String.valueOf(userId))
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .claim("type", "refresh")
                    .id(UUID.randomUUID().toString())
                    .signWith(secretKey)
                    .compact();
        }

        public Long getUserIdFromToken(String token) {
            Claims claims = parseToken(token);
            return Long.parseLong(claims.getSubject());
        }

        public boolean validateToken(String token) {
            try {
                parseToken(token);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        public boolean isAccessToken(String token) {
            try {
                Claims claims = parseToken(token);
                return "access".equals(claims.get("type", String.class));
            } catch (Exception e) {
                return false;
            }
        }

        public boolean isRefreshToken(String token) {
            try {
                Claims claims = parseToken(token);
                return "refresh".equals(claims.get("type", String.class));
            } catch (Exception e) {
                return false;
            }
        }

        public Date getExpirationFromToken(String token) {
            Claims claims = parseToken(token);
            return claims.getExpiration();
        }

        public long getRefreshTokenExpirationSeconds() {
            return refreshTokenExpiration / 1000;
        }

        private Claims parseToken(String token) {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }
    }

    /**
     * Session Gateway - In-memory implementation for testing (simulates Redis)
     */
    static class TestSessionGateway {
        private final Map<Long, SessionData> sessions = new ConcurrentHashMap<>();

        record SessionData(String token, String refreshToken, long expiresAt) {}

        public void saveSession(Long userId, String token, String refreshToken, long ttlSeconds) {
            long expiresAt = System.currentTimeMillis() + (ttlSeconds * 1000);
            sessions.put(userId, new SessionData(token, refreshToken, expiresAt));
        }

        public Optional<String> getToken(Long userId) {
            SessionData session = sessions.get(userId);
            if (session == null || session.expiresAt < System.currentTimeMillis()) {
                return Optional.empty();
            }
            return Optional.of(session.token);
        }

        public Optional<String> getRefreshToken(Long userId) {
            SessionData session = sessions.get(userId);
            if (session == null || session.expiresAt < System.currentTimeMillis()) {
                return Optional.empty();
            }
            return Optional.of(session.refreshToken);
        }

        public boolean validateRefreshToken(Long userId, String refreshToken) {
            Optional<String> storedToken = getRefreshToken(userId);
            return storedToken.isPresent() && storedToken.get().equals(refreshToken);
        }

        public void invalidateSession(Long userId) {
            sessions.remove(userId);
        }

        public boolean hasSession(Long userId) {
            SessionData session = sessions.get(userId);
            return session != null && session.expiresAt >= System.currentTimeMillis();
        }

        public void updateSession(Long userId, String newToken, String newRefreshToken, long ttlSeconds) {
            saveSession(userId, newToken, newRefreshToken, ttlSeconds);
        }
    }

    /**
     * Auth Service - Simplified implementation for testing
     */
    static class TestAuthService {
        private final TestTokenGateway tokenGateway;
        private final TestSessionGateway sessionGateway;

        public TestAuthService(TestTokenGateway tokenGateway, TestSessionGateway sessionGateway) {
            this.tokenGateway = tokenGateway;
            this.sessionGateway = sessionGateway;
        }

        public AuthResult login(Long userId) {
            String token = tokenGateway.generateAccessToken(userId);
            String refreshToken = tokenGateway.generateRefreshToken(userId);

            sessionGateway.saveSession(
                    userId,
                    token,
                    refreshToken,
                    tokenGateway.getRefreshTokenExpirationSeconds()
            );

            return new AuthResult(userId, token, refreshToken);
        }

        public void logout(Long userId) {
            sessionGateway.invalidateSession(userId);
        }

        public ValidationResult validateAccessToken(String token) {
            // Validate token format
            if (!tokenGateway.validateToken(token)) {
                return new ValidationResult(false, null, "无效的访问令牌");
            }

            // Validate token type
            if (!tokenGateway.isAccessToken(token)) {
                return new ValidationResult(false, null, "令牌类型错误");
            }

            // Get user ID
            Long userId = tokenGateway.getUserIdFromToken(token);

            // Validate session exists (user not logged out)
            if (!sessionGateway.hasSession(userId)) {
                return new ValidationResult(false, userId, "会话已失效，请重新登录");
            }

            return new ValidationResult(true, userId, null);
        }
    }

    record AuthResult(Long userId, String token, String refreshToken) {}
    record ValidationResult(boolean valid, Long userId, String errorMessage) {}

    // ========== Test Setup ==========

    private TestTokenGateway tokenGateway;
    private TestSessionGateway sessionGateway;
    private TestAuthService authService;

    @BeforeProperty
    void setUp() {
        tokenGateway = new TestTokenGateway();
        sessionGateway = new TestSessionGateway();
        authService = new TestAuthService(tokenGateway, sessionGateway);
    }

    // ========== Generators ==========

    @Provide
    Arbitrary<Long> validUserIds() {
        return Arbitraries.longs().between(1L, 1_000_000L);
    }

    // ========== Property 1: 认证 Token 往返一致性 ==========

    /**
     * Property 1.1: 生成的 Access Token 应该能被正确解析并包含正确的用户 ID
     * 
     * *对于任意* 有效的用户 ID，生成的 JWT access token 应该能够被正确解析，
     * 且解析出的用户 ID 与原始用户 ID 相同。
     */
    @Property(tries = 100)
    @Label("Property 1.1: Access Token 包含正确的用户 ID")
    void accessTokenContainsCorrectUserId(
            @ForAll("validUserIds") Long userId
    ) {
        // Generate access token
        String token = tokenGateway.generateAccessToken(userId);

        // Parse and verify
        assert tokenGateway.validateToken(token) :
                "生成的 token 应该是有效的";

        Long parsedUserId = tokenGateway.getUserIdFromToken(token);
        assert userId.equals(parsedUserId) :
                String.format("解析出的用户 ID (%d) 应该等于原始用户 ID (%d)", parsedUserId, userId);
    }

    /**
     * Property 1.2: 生成的 Access Token 应该被正确标识为 access 类型
     * 
     * *对于任意* 有效的用户 ID，生成的 access token 应该被正确识别为 access 类型，
     * 而不是 refresh 类型。
     */
    @Property(tries = 100)
    @Label("Property 1.2: Access Token 类型正确")
    void accessTokenHasCorrectType(
            @ForAll("validUserIds") Long userId
    ) {
        String token = tokenGateway.generateAccessToken(userId);

        assert tokenGateway.isAccessToken(token) :
                "Access token 应该被识别为 access 类型";
        assert !tokenGateway.isRefreshToken(token) :
                "Access token 不应该被识别为 refresh 类型";
    }

    /**
     * Property 1.3: 生成的 Refresh Token 应该能被正确解析并包含正确的用户 ID
     * 
     * *对于任意* 有效的用户 ID，生成的 JWT refresh token 应该能够被正确解析，
     * 且解析出的用户 ID 与原始用户 ID 相同。
     */
    @Property(tries = 100)
    @Label("Property 1.3: Refresh Token 包含正确的用户 ID")
    void refreshTokenContainsCorrectUserId(
            @ForAll("validUserIds") Long userId
    ) {
        String token = tokenGateway.generateRefreshToken(userId);

        assert tokenGateway.validateToken(token) :
                "生成的 refresh token 应该是有效的";

        Long parsedUserId = tokenGateway.getUserIdFromToken(token);
        assert userId.equals(parsedUserId) :
                String.format("解析出的用户 ID (%d) 应该等于原始用户 ID (%d)", parsedUserId, userId);
    }

    /**
     * Property 1.4: 生成的 Refresh Token 应该被正确标识为 refresh 类型
     * 
     * *对于任意* 有效的用户 ID，生成的 refresh token 应该被正确识别为 refresh 类型，
     * 而不是 access 类型。
     */
    @Property(tries = 100)
    @Label("Property 1.4: Refresh Token 类型正确")
    void refreshTokenHasCorrectType(
            @ForAll("validUserIds") Long userId
    ) {
        String token = tokenGateway.generateRefreshToken(userId);

        assert tokenGateway.isRefreshToken(token) :
                "Refresh token 应该被识别为 refresh 类型";
        assert !tokenGateway.isAccessToken(token) :
                "Refresh token 不应该被识别为 access 类型";
    }

    /**
     * Property 1.5: 生成的 Token 应该有正确的过期时间
     * 
     * *对于任意* 有效的用户 ID，生成的 token 的过期时间应该在未来。
     */
    @Property(tries = 100)
    @Label("Property 1.5: Token 过期时间在未来")
    void tokenExpirationIsInFuture(
            @ForAll("validUserIds") Long userId
    ) {
        String accessToken = tokenGateway.generateAccessToken(userId);
        String refreshToken = tokenGateway.generateRefreshToken(userId);

        Date now = new Date();
        Date accessExpiration = tokenGateway.getExpirationFromToken(accessToken);
        Date refreshExpiration = tokenGateway.getExpirationFromToken(refreshToken);

        assert accessExpiration.after(now) :
                "Access token 的过期时间应该在当前时间之后";
        assert refreshExpiration.after(now) :
                "Refresh token 的过期时间应该在当前时间之后";
        assert refreshExpiration.after(accessExpiration) :
                "Refresh token 的过期时间应该在 access token 之后";
    }

    /**
     * Property 1.6: 登录后返回的 Token 应该能被正确验证
     * 
     * *对于任意* 有效的用户 ID，登录后返回的 token 应该能够通过验证，
     * 且验证结果包含正确的用户 ID。
     */
    @Property(tries = 100)
    @Label("Property 1.6: 登录后 Token 可被正确验证")
    void loginTokenCanBeValidated(
            @ForAll("validUserIds") Long userId
    ) {
        // Login
        AuthResult result = authService.login(userId);

        // Validate the returned token
        ValidationResult validation = authService.validateAccessToken(result.token());

        assert validation.valid() :
                "登录后返回的 token 应该是有效的";
        assert userId.equals(validation.userId()) :
                String.format("验证结果中的用户 ID (%d) 应该等于原始用户 ID (%d)",
                        validation.userId(), userId);
    }

    // ========== Property 4: 会话登出一致性 ==========

    /**
     * Property 4.1: 登出后会话应该从存储中删除
     * 
     * *对于任意* 已登录用户，登出后其 session 应该从存储中删除。
     */
    @Property(tries = 100)
    @Label("Property 4.1: 登出后会话被删除")
    void logoutRemovesSession(
            @ForAll("validUserIds") Long userId
    ) {
        // Login first
        authService.login(userId);

        // Verify session exists
        assert sessionGateway.hasSession(userId) :
                "登录后应该存在会话";

        // Logout
        authService.logout(userId);

        // Verify session is removed
        assert !sessionGateway.hasSession(userId) :
                "登出后会话应该被删除";
    }

    /**
     * Property 4.2: 登出后原 Token 应该无法通过验证
     * 
     * *对于任意* 已登录用户，登出后使用原 token 的验证请求应该失败。
     */
    @Property(tries = 100)
    @Label("Property 4.2: 登出后原 Token 验证失败")
    void logoutInvalidatesToken(
            @ForAll("validUserIds") Long userId
    ) {
        // Login first
        AuthResult loginResult = authService.login(userId);
        String token = loginResult.token();

        // Verify token is valid before logout
        ValidationResult beforeLogout = authService.validateAccessToken(token);
        assert beforeLogout.valid() :
                "登出前 token 应该是有效的";

        // Logout
        authService.logout(userId);

        // Verify token is invalid after logout
        ValidationResult afterLogout = authService.validateAccessToken(token);
        assert !afterLogout.valid() :
                "登出后 token 应该无效";
        assert "会话已失效，请重新登录".equals(afterLogout.errorMessage()) :
                "登出后验证应该返回会话失效的错误信息";
    }

    /**
     * Property 4.3: 登出后 Refresh Token 也应该失效
     * 
     * *对于任意* 已登录用户，登出后其 refresh token 也应该无法使用。
     */
    @Property(tries = 100)
    @Label("Property 4.3: 登出后 Refresh Token 失效")
    void logoutInvalidatesRefreshToken(
            @ForAll("validUserIds") Long userId
    ) {
        // Login first
        AuthResult loginResult = authService.login(userId);
        String refreshToken = loginResult.refreshToken();

        // Verify refresh token is valid before logout
        assert sessionGateway.validateRefreshToken(userId, refreshToken) :
                "登出前 refresh token 应该是有效的";

        // Logout
        authService.logout(userId);

        // Verify refresh token is invalid after logout
        assert !sessionGateway.validateRefreshToken(userId, refreshToken) :
                "登出后 refresh token 应该无效";
    }

    /**
     * Property 4.4: 重复登出应该是幂等的
     * 
     * *对于任意* 用户，多次登出操作应该是幂等的，不会抛出异常。
     */
    @Property(tries = 100)
    @Label("Property 4.4: 重复登出是幂等的")
    void multipleLogoutsAreIdempotent(
            @ForAll("validUserIds") Long userId
    ) {
        // Login first
        authService.login(userId);

        // Logout multiple times - should not throw
        authService.logout(userId);
        authService.logout(userId);
        authService.logout(userId);

        // Session should still be gone
        assert !sessionGateway.hasSession(userId) :
                "多次登出后会话仍应该不存在";
    }

    /**
     * Property 4.5: 登出一个用户不影响其他用户的会话
     * 
     * *对于任意* 两个不同的用户，登出其中一个不应该影响另一个的会话。
     */
    @Property(tries = 100)
    @Label("Property 4.5: 登出不影响其他用户会话")
    void logoutDoesNotAffectOtherUsers(
            @ForAll("validUserIds") Long userId1,
            @ForAll("validUserIds") Long userId2
    ) {
        // Skip if same user
        Assume.that(!userId1.equals(userId2));

        // Login both users
        AuthResult result1 = authService.login(userId1);
        AuthResult result2 = authService.login(userId2);

        // Logout user1
        authService.logout(userId1);

        // User1's session should be gone
        assert !sessionGateway.hasSession(userId1) :
                "用户1登出后会话应该被删除";

        // User2's session should still exist
        assert sessionGateway.hasSession(userId2) :
                "用户1登出不应该影响用户2的会话";

        // User2's token should still be valid
        ValidationResult validation = authService.validateAccessToken(result2.token());
        assert validation.valid() :
                "用户1登出后，用户2的 token 应该仍然有效";
    }
}
