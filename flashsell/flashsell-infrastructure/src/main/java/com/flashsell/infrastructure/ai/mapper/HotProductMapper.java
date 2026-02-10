package com.flashsell.infrastructure.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashsell.infrastructure.ai.dataobject.HotProductDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 爆品推荐Mapper
 */
@Mapper
public interface HotProductMapper extends BaseMapper<HotProductDO> {

    /**
     * 根据日期和品类查询爆品列表
     *
     * @param date 推荐日期
     * @param categoryId 品类ID
     * @return 爆品列表
     */
    List<HotProductDO> selectByDateAndCategory(@Param("date") LocalDate date, 
                                                 @Param("categoryId") Long categoryId);

    /**
     * 根据日期查询所有爆品列表
     *
     * @param date 推荐日期
     * @return 爆品列表
     */
    List<HotProductDO> selectByDate(@Param("date") LocalDate date);

    /**
     * 查询产品的爆品历史记录
     *
     * @param productId 产品ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 爆品历史记录
     */
    List<HotProductDO> selectProductHistory(@Param("productId") Long productId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /**
     * 删除指定日期之前的爆品记录
     *
     * @param beforeDate 日期
     * @return 删除的记录数
     */
    int deleteBeforeDate(@Param("beforeDate") LocalDate beforeDate);

    /**
     * 根据日期和品类获取Top N爆品
     *
     * @param date 推荐日期
     * @param categoryId 品类ID
     * @param topN 数量限制
     * @return Top N爆品列表
     */
    List<HotProductDO> selectTopNByDateAndCategory(@Param("date") LocalDate date,
                                                     @Param("categoryId") Long categoryId,
                                                     @Param("topN") int topN);

    /**
     * 检查指定日期的品类是否已有爆品数据
     *
     * @param date 推荐日期
     * @param categoryId 品类ID
     * @return 记录数
     */
    int countByDateAndCategory(@Param("date") LocalDate date, 
                                @Param("categoryId") Long categoryId);

    /**
     * 删除指定日期和品类的爆品数据
     *
     * @param date 推荐日期
     * @param categoryId 品类ID
     * @return 删除的记录数
     */
    int deleteByDateAndCategory(@Param("date") LocalDate date, 
                                 @Param("categoryId") Long categoryId);
}
