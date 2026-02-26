package com.flashsell.infrastructure.product.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashsell.infrastructure.product.dataobject.ProductDO;

/**
 * 产品 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供基础 CRUD 操作
 */
@Mapper
public interface ProductMapper extends BaseMapper<ProductDO> {

    /**
     * 根据ASIN查询产品
     *
     * @param asin Amazon标准识别号
     * @return 产品数据对象
     */
    @Select("SELECT * FROM products WHERE asin = #{asin}")
    ProductDO selectByAsin(@Param("asin") String asin);

    /**
     * 根据品类ID查询产品列表
     *
     * @param categoryId 品类ID
     * @return 产品列表
     */
    @Select("SELECT * FROM products WHERE category_id = #{categoryId} ORDER BY bsr_rank ASC NULLS LAST")
    List<ProductDO> selectByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 检查ASIN是否存在
     *
     * @param asin Amazon标准识别号
     * @return 存在数量
     */
    @Select("SELECT COUNT(*) FROM products WHERE asin = #{asin}")
    int countByAsin(@Param("asin") String asin);

    /**
     * 查询热销产品（BSR排名前N）
     *
     * @param categoryId 品类ID
     * @param limit 数量限制
     * @return 产品列表
     */
    @Select("SELECT * FROM products WHERE category_id = #{categoryId} AND bsr_rank IS NOT NULL ORDER BY bsr_rank ASC LIMIT #{limit}")
    List<ProductDO> selectTopByCategoryId(@Param("categoryId") Long categoryId, @Param("limit") int limit);

    /**
     * 查询高评分产品
     *
     * @param minRating 最低评分
     * @param limit 数量限制
     * @return 产品列表
     */
    @Select("SELECT * FROM products WHERE rating >= #{minRating} ORDER BY rating DESC, review_count DESC LIMIT #{limit}")
    List<ProductDO> selectByMinRating(@Param("minRating") Double minRating, @Param("limit") int limit);

    /**
     * 统计品类下的产品数量
     *
     * @param categoryId 品类ID
     * @return 产品数量
     */
    @Select("SELECT COUNT(*) FROM products WHERE category_id = #{categoryId}")
    Long countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 获取品类下的平均BSR排名
     *
     * @param categoryId 品类ID
     * @return 平均BSR排名
     */
    @Select("SELECT AVG(bsr_rank) FROM products WHERE category_id = #{categoryId} AND bsr_rank IS NOT NULL")
    Double getAverageBsrRank(@Param("categoryId") Long categoryId);

    /**
     * 获取品类下的平均评分
     *
     * @param categoryId 品类ID
     * @return 平均评分
     */
    @Select("SELECT AVG(rating) FROM products WHERE category_id = #{categoryId} AND rating IS NOT NULL")
    Double getAverageRating(@Param("categoryId") Long categoryId);

    /**
     * 获取品类下的平均评论数
     *
     * @param categoryId 品类ID
     * @return 平均评论数
     */
    @Select("SELECT AVG(review_count) FROM products WHERE category_id = #{categoryId} AND review_count IS NOT NULL")
    Double getAverageReviewCount(@Param("categoryId") Long categoryId);

    /**
     * 获取品类下的平均竞争评分
     *
     * @param categoryId 品类ID
     * @return 平均竞争评分
     */
    @Select("SELECT AVG(competition_score) FROM products WHERE category_id = #{categoryId} AND competition_score IS NOT NULL")
    Double getAverageCompetitionScore(@Param("categoryId") Long categoryId);

    /**
     * 统计指定日期范围内创建的产品数量
     *
     * @param categoryId 品类ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 产品数量
     */
    @Select("SELECT COUNT(*) FROM products WHERE category_id = #{categoryId} AND created_at BETWEEN #{startTime} AND #{endTime}")
    Long countProductsCreatedBetween(@Param("categoryId") Long categoryId,
                                     @Param("startTime") java.time.LocalDateTime startTime,
                                     @Param("endTime") java.time.LocalDateTime endTime);

    /**
     * 根据标题模糊搜索产品
     *
     * @param keyword 搜索关键词
     * @return 匹配的产品列表
     */
    @Select("SELECT * FROM products WHERE title ILIKE CONCAT('%', #{keyword}, '%') ORDER BY last_updated DESC LIMIT 50")
    List<ProductDO> selectByTitleContaining(@Param("keyword") String keyword);
}
