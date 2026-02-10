package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新令牌响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRes {
    
    /**
     * 新的JWT访问令牌
     */
    private String token;
    
    /**
     * 新的刷新令牌
     */
    private String refreshToken;
}
