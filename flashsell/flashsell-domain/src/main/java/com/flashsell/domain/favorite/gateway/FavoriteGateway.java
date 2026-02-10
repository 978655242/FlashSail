package com.flashsell.domain.favorite.gateway;

import com.flashsell.domain.favorite.entity.Favorite;

import java.util.List;
import java.util.Optional;

/**
 * 收藏网关接口
 * 定义收藏数据访问的抽象接口，由 infrastructure 层实现
 */
public interface FavoriteGateway {

    /**
     * 根据ID查询收藏
     *
     * @param id 收藏ID
     * @return 收藏实体（可能为空）
     */
    Optional<Favorite> findById(Long id);

    /**
     * 根据用户ID和产品ID查询收藏
     *
     * @param userId 用户ID
     * @param productId 产品ID
     * @return 收藏实体（可能为空）
     */
    Optional<Favorite> findByUserIdAndProductId(Long userId, Long productId);

    /**
     * 查询用户的收藏列表（分页）
     *
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param pageSize 每页数量
     * @return 收藏列表
     */
    List<Favorite> findByUserId(Long userId, int page, int pageSize);

    /**
     * 查询用户的所有收藏
     *
     * @param userId 用户ID
     * @return 收藏列表
     */
    List<Favorite> findAllByUserId(Long userId);

    /**
     * 统计用户的收藏数量
     *
     * @param userId 用户ID
     * @return 收藏数量
     */
    long countByUserId(Long userId);

    /**
     * 检查用户是否已收藏某产品
     *
     * @param userId 用户ID
     * @param productId 产品ID
     * @return 是否已收藏
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    /**
     * 保存收藏
     *
     * @param favorite 收藏实体
     * @return 保存后的收藏实体（包含生成的ID）
     */
    Favorite save(Favorite favorite);

    /**
     * 删除收藏
     *
     * @param id 收藏ID
     */
    void deleteById(Long id);

    /**
     * 根据用户ID和产品ID删除收藏
     *
     * @param userId 用户ID
     * @param productId 产品ID
     * @return 是否删除成功
     */
    boolean deleteByUserIdAndProductId(Long userId, Long productId);

    /**
     * 批量查询用户是否收藏了指定产品
     *
     * @param userId 用户ID
     * @param productIds 产品ID列表
     * @return 已收藏的产品ID列表
     */
    List<Long> findFavoriteProductIds(Long userId, List<Long> productIds);
}
