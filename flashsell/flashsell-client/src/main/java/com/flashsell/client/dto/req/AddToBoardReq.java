package com.flashsell.client.dto.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 添加产品到看板请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToBoardReq {

    /**
     * 产品ID列表
     */
    @NotEmpty(message = "产品ID列表不能为空")
    private List<Long> productIds;
}
