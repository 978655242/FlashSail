package com.flashsell.infrastructure.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashsell.infrastructure.favorite.dataobject.FavoriteDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 收藏 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供基础 CRUD 操作
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<FavoriteDO> {

    /**
     * 根据用户ID和产品ID查询收藏
     *
     * @param userId 用户ID
     * @param productId 产品ID
     * @return 收藏数据对象
     */
    @Select("SELECT * FROM user_favorites WHERE user_id = #{userId} AND product_id = #{productId}")
    FavoriteDO selectByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 查询用户的收藏列表（分页）
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 收藏列表
     */
    @Select("SELECT * FROM user_favorites WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<FavoriteDO> selectByUserIdWithPagination(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询用户的所有收藏
     *
     * @param userId 用户ID
     * @return 收藏列表
     */
    @Select("SELECT * FROM user_favorites WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<FavoriteDO> selectAllByUserId(@Param("userId") Long userId);

    /**
     * 统计用户的收藏数量
     *
     * @param userId 用户ID
     * @return 收藏数量
     */
    @Select("SELECT COUNT(*) FROM user_favorites WHERE user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否已收藏某产品
     *
     * @param userId 用户ID
     * @param productId 产品ID
     * @return 存在数量
     */
    @Select("SELECT COUNT(*) FROM user_favorites WHERE user_id = #{userId} AND product_id = #{productId}")
    int countByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 根据用户ID和产品ID删除收藏
     *
     * @param userId 用户ID
     * @param productId 产品ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM user_favorites WHERE user_id = #{userId} AND product_id = #{productId}")
    int deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 批量查询用户已收藏的产品ID
     *
     * @param userId 用户ID
     * @param productIds 产品ID列表
     * @return 已收藏的产品ID列表
     */
    @Select("<script>" +
            "SELECT product_id FROM user_favorites WHERE user_id = #{userId} AND product_id IN " +
            "<foreach collection='productIds' item='productId' open='(' separator=',' close=')'>" +
            "#{productId}" +
            "</foreach>" +
            "</script>")
    List<Long> selectFavoriteProductIds(@Param("userId") Long userId, @Param("productIds") List<Long> productIds);
}
