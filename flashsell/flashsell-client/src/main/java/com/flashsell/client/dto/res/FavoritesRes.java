package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 收藏列表响应 DTO
 * 
 * Requirements: 4.4
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoritesRes {

    /**
     * 收藏的产品列表
     */
    private List<FavoriteItemRes> products;

    /**
     * 总数量
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 是否有更多数据
     */
    private Boolean hasMore;
}
