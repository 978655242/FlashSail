package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 品类组响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryGroupRes {

    /**
     * 品类组ID
     */
    private Long id;

    /**
     * 品类组名称
     */
    private String name;

    /**
     * 品类列表
     */
    private List<CategoryRes> categories;
}
