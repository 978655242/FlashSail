package com.flashsell.app.service;

import com.flashsell.app.assembler.FavoriteAssembler;
import com.flashsell.client.dto.req.AddFavoriteReq;
import com.flashsell.client.dto.res.FavoriteItemRes;
import com.flashsell.client.dto.res.FavoritesRes;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.gateway.CategoryGateway;
import com.flashsell.domain.favorite.entity.Favorite;
import com.flashsell.domain.favorite.gateway.FavoriteGateway;
import com.flashsell.domain.product.entity.Product;
import com.flashsell.domain.product.gateway.ProductGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 收藏应用服务
 * 提供收藏相关的业务编排
 * 
 * Requirements: 4.1, 4.4
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteAppService {

    private final FavoriteGateway favoriteGateway;
    private final ProductGateway productGateway;
    private final CategoryGateway categoryGateway;
    private final FavoriteAssembler favoriteAssembler;

    /**
     * 添加收藏
     * 如果已收藏则返回现有收藏（幂等操作）
     *
     * @param userId 用户ID
     * @param req 添加收藏请求
     * @return 收藏项响应
     * @throws IllegalArgumentException 如果产品不存在
     */
    @Transactional
    public FavoriteItemRes addFavorite(Long userId, AddFavoriteReq req) {
        log.debug("添加收藏: userId={}, productId={}", userId, req.getProductId());

        // 1. 检查产品是否存在
        Optional<Product> productOpt = productGateway.findById(req.getProductId());
        if (productOpt.isEmpty()) {
            log.warn("产品不存在: productId={}", req.getProductId());
            throw new IllegalArgumentException("产品不存在");
        }
        Product product = productOpt.get();

        // 2. 检查是否已收藏（幂等性）
        Optional<Favorite> existingFavorite = favoriteGateway.findByUserIdAndProductId(userId, req.getProductId());
        if (existingFavorite.isPresent()) {
            log.debug("产品已收藏，返回现有收藏: userId={}, productId={}", userId, req.getProductId());
            return buildFavoriteItemRes(existingFavorite.get(), product);
        }

        // 3. 创建新收藏
        Favorite favorite = Favorite.create(userId, req.getProductId());
        Favorite savedFavorite = favoriteGateway.save(favorite);
        log.info("收藏添加成功: userId={}, productId={}, favoriteId={}", userId, req.getProductId(), savedFavorite.getId());

        return buildFavoriteItemRes(savedFavorite, product);
    }

    /**
     * 取消收藏
     *
     * @param userId 用户ID
     * @param productId 产品ID
     * @return 是否取消成功
     */
    @Transactional
    public boolean removeFavorite(Long userId, Long productId) {
        log.debug("取消收藏: userId={}, productId={}", userId, productId);

        boolean deleted = favoriteGateway.deleteByUserIdAndProductId(userId, productId);
        if (deleted) {
            log.info("收藏取消成功: userId={}, productId={}", userId, productId);
        } else {
            log.debug("收藏不存在或已取消: userId={}, productId={}", userId, productId);
        }
        return deleted;
    }

    /**
     * 获取用户收藏列表（分页）
     *
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param pageSize 每页数量
     * @return 收藏列表响应
     */
    public FavoritesRes getFavorites(Long userId, int page, int pageSize) {
        log.debug("获取收藏列表: userId={}, page={}, pageSize={}", userId, page, pageSize);

        // 1. 获取收藏总数
        long total = favoriteGateway.countByUserId(userId);

        // 2. 获取分页收藏列表
        List<Favorite> favorites = favoriteGateway.findByUserId(userId, page, pageSize);

        // 3. 批量获取产品信息
        List<Long> productIds = favorites.stream()
                .map(Favorite::getProductId)
                .collect(Collectors.toList());
        Map<Long, Product> productMap = getProductMap(productIds);

        // 4. 批量获取品类信息
        Set<Long> categoryIds = productMap.values().stream()
                .map(Product::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, Category> categoryMap = getCategoryMap(categoryIds);

        // 5. 组装响应
        List<FavoriteItemRes> items = favoriteAssembler.toFavoriteItemResList(favorites, productMap, categoryMap);
        return favoriteAssembler.toFavoritesRes(items, total, page, pageSize);
    }

    /**
     * 检查用户是否已收藏某产品
     *
     * @param userId 用户ID
     * @param productId 产品ID
     * @return 是否已收藏
     */
    public boolean isFavorited(Long userId, Long productId) {
        return favoriteGateway.existsByUserIdAndProductId(userId, productId);
    }

    /**
     * 批量检查用户是否已收藏指定产品
     *
     * @param userId 用户ID
     * @param productIds 产品ID列表
     * @return 已收藏的产品ID列表
     */
    public List<Long> getFavoritedProductIds(Long userId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return favoriteGateway.findFavoriteProductIds(userId, productIds);
    }

    /**
     * 获取用户收藏数量
     *
     * @param userId 用户ID
     * @return 收藏数量
     */
    public long getFavoriteCount(Long userId) {
        return favoriteGateway.countByUserId(userId);
    }

    // ==================== 私有方法 ====================

    /**
     * 构建收藏项响应
     */
    private FavoriteItemRes buildFavoriteItemRes(Favorite favorite, Product product) {
        Category category = null;
        if (product.getCategoryId() != null) {
            category = categoryGateway.findById(product.getCategoryId()).orElse(null);
        }
        return favoriteAssembler.toFavoriteItemRes(favorite, product, category);
    }

    /**
     * 批量获取产品映射（优化：使用批量查询避免N+1问题）
     */
    private Map<Long, Product> getProductMap(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        // 使用批量查询替代循环单个查询
        List<Product> products = productGateway.findByIds(productIds);
        return products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
    }

    /**
     * 批量获取品类映射（优化：使用批量查询避免N+1问题）
     */
    private Map<Long, Category> getCategoryMap(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Map.of();
        }
        // 使用批量查询替代循环单个查询
        List<Category> categories = categoryGateway.findByIds(new ArrayList<>(categoryIds));
        return categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));
    }
}
