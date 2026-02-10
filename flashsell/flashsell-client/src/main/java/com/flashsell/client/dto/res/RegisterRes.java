package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRes {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * JWT访问令牌
     */
    private String token;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
}
