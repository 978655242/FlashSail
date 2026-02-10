package com.flashsell.adapter.web;

import com.flashsell.app.service.FavoriteAppService;
import com.flashsell.client.dto.ApiResponse;
import com.flashsell.client.dto.req.AddFavoriteReq;
import com.flashsell.client.dto.res.FavoriteItemRes;
import com.flashsell.client.dto.res.FavoritesRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.flashsell.adapter.web.SecurityUtils.getCurrentUserId;

/**
 * 收藏控制器
 * 处理收藏相关的 API 请求
 *
 * Requirements: 4.1, 4.4
 */
@Slf4j
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteAppService favoriteAppService;

    /**
     * 添加收藏
     * 如果已收藏则返回现有收藏（幂等操作）
     *
     * @param userDetails 当前登录用户
     * @param req 添加收藏请求
     * @return 收藏项响应
     */
    @PostMapping
    public ApiResponse<FavoriteItemRes> addFavorite(
            @Valid @RequestBody AddFavoriteReq req) {
        Long userId = getCurrentUserId();
        log.info("添加收藏: userId={}, productId={}", userId, req.getProductId());
        
        try {
            FavoriteItemRes result = favoriteAppService.addFavorite(userId, req);
            return ApiResponse.success(result);
        } catch (IllegalArgumentException e) {
            log.warn("添加收藏失败: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 取消收藏
     *
     * @param userDetails 当前登录用户
     * @param productId 产品ID
     * @return 操作结果
     */
    @DeleteMapping("/{productId}")
    public ApiResponse<Void> removeFavorite(
            @PathVariable Long productId) {
        Long userId = getCurrentUserId();
        log.info("取消收藏: userId={}, productId={}", userId, productId);
        
        boolean removed = favoriteAppService.removeFavorite(userId, productId);
        if (removed) {
            return ApiResponse.success();
        } else {
            return ApiResponse.error(404, "收藏不存在");
        }
    }

    /**
     * 获取收藏列表（分页）
     *
     * @param userDetails 当前登录用户
     * @param page 页码（从1开始，默认1）
     * @param pageSize 每页数量（默认20）
     * @return 收藏列表响应
     */
    @GetMapping
    public ApiResponse<FavoritesRes> getFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = getCurrentUserId();
        log.info("获取收藏列表: userId={}, page={}, pageSize={}", userId, page, pageSize);
        
        // 参数校验
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }
        
        FavoritesRes result = favoriteAppService.getFavorites(userId, page, pageSize);
        return ApiResponse.success(result);
    }

    /**
     * 检查是否已收藏
     *
     * @param userDetails 当前登录用户
     * @param productId 产品ID
     * @return 是否已收藏
     */
    @GetMapping("/check/{productId}")
    public ApiResponse<Boolean> checkFavorite(
            @PathVariable Long productId) {
        Long userId = getCurrentUserId();
        log.debug("检查收藏状态: userId={}, productId={}", userId, productId);
        
        boolean isFavorited = favoriteAppService.isFavorited(userId, productId);
        return ApiResponse.success(isFavorited);
    }

    /**
     * 批量检查是否已收藏
     *
     * @param userDetails 当前登录用户
     * @param productIds 产品ID列表
     * @return 已收藏的产品ID列表
     */
    @PostMapping("/check/batch")
    public ApiResponse<List<Long>> checkFavoritesBatch(
            @RequestBody List<Long> productIds) {
        Long userId = getCurrentUserId();
        log.debug("批量检查收藏状态: userId={}, productIds={}", userId, productIds);
        
        List<Long> favoritedIds = favoriteAppService.getFavoritedProductIds(userId, productIds);
        return ApiResponse.success(favoritedIds);
    }

    /**
     * 获取收藏数量
     *
     * @param userDetails 当前登录用户
     * @return 收藏数量
     */
    @GetMapping("/count")
    public ApiResponse<Long> getFavoriteCount() {
        Long userId = getCurrentUserId();
        log.debug("获取收藏数量: userId={}", userId);
        
        long count = favoriteAppService.getFavoriteCount(userId);
        return ApiResponse.success(count);
    }
}
