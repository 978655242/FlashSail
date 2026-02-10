package com.flashsell.domain.ai.gateway;

import com.flashsell.domain.ai.entity.HotProductScore;

import java.time.LocalDate;
import java.util.List;

/**
 * 爆品推荐网关接口
 * 定义爆品数据的持久化操作
 */
public interface HotProductGateway {

    /**
     * 保存爆品评分
     *
     * @param hotProductScore 爆品评分
     */
    void save(HotProductScore hotProductScore);

    /**
     * 批量保存爆品评分
     *
     * @param hotProductScores 爆品评分列表
     */
    void batchSave(List<HotProductScore> hotProductScores);

    /**
     * 根据日期和品类获取爆品列表
     *
     * @param date 推荐日期
     * @param categoryId 品类ID（可选）
     * @return 爆品列表
     */
    List<HotProductScore> findByDateAndCategory(LocalDate date, Long categoryId);

    /**
     * 根据日期获取所有爆品列表
     *
     * @param date 推荐日期
     * @return 爆品列表
     */
    List<HotProductScore> findByDate(LocalDate date);

    /**
     * 获取产品的爆品历史记录
     *
     * @param productId 产品ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 爆品历史记录
     */
    List<HotProductScore> findProductHistory(Long productId, LocalDate startDate, LocalDate endDate);

    /**
     * 删除指定日期之前的爆品记录
     *
     * @param beforeDate 日期
     * @return 删除的记录数
     */
    int deleteBeforeDate(LocalDate beforeDate);

    /**
     * 根据日期和品类获取Top N爆品
     *
     * @param date 推荐日期
     * @param categoryId 品类ID
     * @param topN 数量限制
     * @return Top N爆品列表
     */
    List<HotProductScore> findTopNByDateAndCategory(LocalDate date, Long categoryId, int topN);

    /**
     * 检查指定日期的品类是否已有爆品数据
     *
     * @param date 推荐日期
     * @param categoryId 品类ID
     * @return 是否存在
     */
    boolean existsByDateAndCategory(LocalDate date, Long categoryId);

    /**
     * 删除指定日期和品类的爆品数据
     *
     * @param date 推荐日期
     * @param categoryId 品类ID
     * @return 删除的记录数
     */
    int deleteByDateAndCategory(LocalDate date, Long categoryId);
}
