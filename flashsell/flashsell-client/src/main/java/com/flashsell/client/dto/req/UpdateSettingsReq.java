package com.flashsell.client.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户设置请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSettingsReq {

    /**
     * 是否开启消息通知
     */
    @NotNull(message = "消息通知设置不能为空")
    private Boolean notificationEnabled;

    /**
     * 是否订阅邮件
     */
    @NotNull(message = "邮件订阅设置不能为空")
    private Boolean emailSubscribed;
}
