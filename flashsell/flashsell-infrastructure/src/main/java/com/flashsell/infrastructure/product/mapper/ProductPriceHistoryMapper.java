package com.flashsell.infrastructure.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashsell.infrastructure.product.dataobject.ProductPriceHistoryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 产品价格历史 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供基础 CRUD 操作
 */
@Mapper
public interface ProductPriceHistoryMapper extends BaseMapper<ProductPriceHistoryDO> {

    /**
     * 根据产品ID查询价格历史（按日期升序）
     *
     * @param productId 产品ID
     * @return 价格历史列表
     */
    @Select("SELECT * FROM product_price_history WHERE product_id = #{productId} ORDER BY recorded_date ASC")
    List<ProductPriceHistoryDO> selectByProductId(@Param("productId") Long productId);

    /**
     * 根据产品ID和日期范围查询价格历史
     *
     * @param productId 产品ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 价格历史列表
     */
    @Select("SELECT * FROM product_price_history WHERE product_id = #{productId} " +
            "AND recorded_date >= #{startDate} AND recorded_date <= #{endDate} " +
            "ORDER BY recorded_date ASC")
    List<ProductPriceHistoryDO> selectByProductIdAndDateRange(
            @Param("productId") Long productId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 获取产品最新的价格记录
     *
     * @param productId 产品ID
     * @return 最新价格记录
     */
    @Select("SELECT * FROM product_price_history WHERE product_id = #{productId} " +
            "ORDER BY recorded_date DESC LIMIT 1")
    ProductPriceHistoryDO selectLatestByProductId(@Param("productId") Long productId);

    /**
     * 检查指定日期是否已有价格记录
     *
     * @param productId 产品ID
     * @param recordedDate 记录日期
     * @return 存在数量
     */
    @Select("SELECT COUNT(*) FROM product_price_history WHERE product_id = #{productId} " +
            "AND recorded_date = #{recordedDate}")
    int countByProductIdAndDate(@Param("productId") Long productId, @Param("recordedDate") LocalDate recordedDate);

    /**
     * 删除指定产品的所有价格历史
     *
     * @param productId 产品ID
     * @return 删除数量
     */
    @Select("DELETE FROM product_price_history WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Long productId);
}
