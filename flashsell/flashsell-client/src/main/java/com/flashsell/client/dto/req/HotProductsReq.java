package com.flashsell.client.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 爆品推荐查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotProductsReq {

    /**
     * 类目组ID（可选）
     */
    private Long categoryGroupId;

    /**
     * 品类ID（可选）
     */
    private Long categoryId;

    /**
     * 日期（可选，默认今天）
     */
    private LocalDate date;
}
