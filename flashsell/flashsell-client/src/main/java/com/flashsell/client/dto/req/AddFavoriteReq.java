package com.flashsell.client.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加收藏请求 DTO
 * 
 * Requirements: 4.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddFavoriteReq {

    /**
     * 产品ID
     */
    @NotNull(message = "产品ID不能为空")
    private Long productId;
}
