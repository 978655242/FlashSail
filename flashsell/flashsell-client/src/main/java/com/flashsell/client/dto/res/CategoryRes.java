package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 品类响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRes {

    /**
     * 品类ID
     */
    private Long id;

    /**
     * 品类名称
     */
    private String name;

    /**
     * 品类下产品数量
     */
    private Integer productCount;
}
