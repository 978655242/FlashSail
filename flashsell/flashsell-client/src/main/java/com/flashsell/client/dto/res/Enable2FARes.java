package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 开启两步验证响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enable2FARes {

    /**
     * TOTP密钥
     */
    private String secret;

    /**
     * 二维码URL
     */
    private String qrCodeUrl;
}
