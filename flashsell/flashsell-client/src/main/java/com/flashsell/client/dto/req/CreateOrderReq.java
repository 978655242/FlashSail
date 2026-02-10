package com.flashsell.client.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建订阅订单请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderReq {

    /**
     * 套餐ID
     */
    @NotNull(message = "套餐ID不能为空")
    private Long planId;
}
