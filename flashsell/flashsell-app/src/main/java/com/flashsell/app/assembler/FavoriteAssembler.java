package com.flashsell.app.assembler;

import com.flashsell.client.dto.res.FavoriteItemRes;
import com.flashsell.client.dto.res.FavoritesRes;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.favorite.entity.Favorite;
import com.flashsell.domain.product.entity.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏转换器
 * 负责将领域实体转换为 DTO
 */
@Component
public class FavoriteAssembler {

    /**
     * 将收藏和产品信息转换为收藏项响应
     *
     * @param favorite 收藏实体
     * @param product 产品实体
     * @param category 品类实体（可为空）
     * @return 收藏项响应
     */
    public FavoriteItemRes toFavoriteItemRes(Favorite favorite, Product product, Category category) {
        if (favorite == null || product == null) {
            return null;
        }

        return FavoriteItemRes.builder()
                .favoriteId(favorite.getId())
                .productId(product.getId())
                .title(product.getTitle())
                .image(product.getImageUrl())
                .price(product.getCurrentPrice())
                .bsrRank(product.getBsrRank())
                .reviewCount(product.getReviewCount())
                .rating(product.getRating())
                .categoryId(product.getCategoryId())
                .categoryName(category != null ? category.getName() : null)
                .favoritedAt(favorite.getCreatedAt())
                .build();
    }

    /**
     * 批量转换收藏列表为响应
     *
     * @param favorites 收藏列表
     * @param productMap 产品ID到产品实体的映射
     * @param categoryMap 品类ID到品类实体的映射
     * @return 收藏项响应列表
     */
    public List<FavoriteItemRes> toFavoriteItemResList(
            List<Favorite> favorites,
            Map<Long, Product> productMap,
            Map<Long, Category> categoryMap) {
        
        if (favorites == null) {
            return List.of();
        }

        return favorites.stream()
                .map(favorite -> {
                    Product product = productMap.get(favorite.getProductId());
                    if (product == null) {
                        return null;
                    }
                    Category category = product.getCategoryId() != null 
                            ? categoryMap.get(product.getCategoryId()) 
                            : null;
                    return toFavoriteItemRes(favorite, product, category);
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());
    }

    /**
     * 构建收藏列表响应
     *
     * @param items 收藏项列表
     * @param total 总数量
     * @param page 当前页码
     * @param pageSize 每页数量
     * @return 收藏列表响应
     */
    public FavoritesRes toFavoritesRes(List<FavoriteItemRes> items, long total, int page, int pageSize) {
        boolean hasMore = (long) page * pageSize < total;
        
        return FavoritesRes.builder()
                .products(items)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .hasMore(hasMore)
                .build();
    }
}
