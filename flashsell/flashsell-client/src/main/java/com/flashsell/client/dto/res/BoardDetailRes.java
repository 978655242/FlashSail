package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 看板详情响应（包含产品列表）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardDetailRes {

    /**
     * 看板ID
     */
    private Long id;

    /**
     * 看板名称
     */
    private String name;

    /**
     * 产品数量
     */
    private Integer productCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 看板中的产品列表
     */
    private List<ProductItemRes> products;
}
