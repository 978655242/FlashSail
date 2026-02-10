package com.flashsell.client.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注销账户请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAccountReq {

    /**
     * 密码（用于验证）
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 注销原因（可选）
     */
    @Size(max = 500, message = "注销原因不能超过500个字符")
    private String reason;
}
