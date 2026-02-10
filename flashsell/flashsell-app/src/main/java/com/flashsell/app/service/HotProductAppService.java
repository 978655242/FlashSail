package com.flashsell.app.service;

import com.flashsell.domain.ai.entity.HotProductScore;
import com.flashsell.domain.ai.gateway.HotProductGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 爆品推荐应用服务
 * 负责爆品推荐的业务编排
 * 
 * Requirements: 11.3, 11.4, 11.8
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HotProductAppService {

    private final HotProductGateway hotProductGateway;

    /**
     * 保存爆品推荐结果
     * 如果该品类当天已有数据，先删除再保存
     *
     * @param hotProductScores 爆品评分列表
     * @param categoryId 品类ID
     * @param recommendDate 推荐日期
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveHotProducts(List<HotProductScore> hotProductScores, 
                                 Long categoryId, 
                                 LocalDate recommendDate) {
        if (hotProductScores == null || hotProductScores.isEmpty()) {
            log.warn("爆品列表为空，不保存: categoryId={}, date={}", categoryId, recommendDate);
            return;
        }

        log.info("开始保存爆品推荐: categoryId={}, date={}, count={}", 
                categoryId, recommendDate, hotProductScores.size());

        // 检查是否已存在当天的数据
        if (hotProductGateway.existsByDateAndCategory(recommendDate, categoryId)) {
            log.info("删除已存在的爆品数据: categoryId={}, date={}", categoryId, recommendDate);
            hotProductGateway.deleteByDateAndCategory(recommendDate, categoryId);
        }

        // 批量保存
        hotProductGateway.batchSave(hotProductScores);

        log.info("爆品推荐保存完成: categoryId={}, date={}, count={}", 
                categoryId, recommendDate, hotProductScores.size());
    }

    /**
     * 获取指定日期和品类的爆品列表
     *
     * @param date 推荐日期
     * @param categoryId 品类ID（可选）
     * @return 爆品列表
     */
    public List<HotProductScore> getHotProducts(LocalDate date, Long categoryId) {
        log.info("查询爆品列表: date={}, categoryId={}", date, categoryId);

        List<HotProductScore> hotProducts;
        if (categoryId != null) {
            hotProducts = hotProductGateway.findByDateAndCategory(date, categoryId);
        } else {
            hotProducts = hotProductGateway.findByDate(date);
        }

        log.info("查询到爆品数量: {}", hotProducts.size());
        return hotProducts;
    }

    /**
     * 获取指定日期和品类的 Top N 爆品
     *
     * @param date 推荐日期
     * @param categoryId 品类ID
     * @param topN 数量限制
     * @return Top N 爆品列表
     */
    public List<HotProductScore> getTopNHotProducts(LocalDate date, Long categoryId, int topN) {
        log.info("查询 Top N 爆品: date={}, categoryId={}, topN={}", date, categoryId, topN);

        List<HotProductScore> hotProducts = hotProductGateway.findTopNByDateAndCategory(date, categoryId, topN);

        log.info("查询到 Top N 爆品数量: {}", hotProducts.size());
        return hotProducts;
    }

    /**
     * 获取产品的爆品历史记录
     *
     * @param productId 产品ID
     * @param days 查询天数（默认7天）
     * @return 爆品历史记录
     */
    public List<HotProductScore> getProductHotHistory(Long productId, int days) {
        log.info("查询产品爆品历史: productId={}, days={}", productId, days);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<HotProductScore> history = hotProductGateway.findProductHistory(productId, startDate, endDate);

        log.info("查询到历史记录数量: {}", history.size());
        return history;
    }

    /**
     * 删除过期的爆品数据
     *
     * @param beforeDate 删除此日期之前的数据
     * @return 删除的记录数
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteExpiredHotProducts(LocalDate beforeDate) {
        log.info("删除过期爆品数据: beforeDate={}", beforeDate);

        int deletedCount = hotProductGateway.deleteBeforeDate(beforeDate);

        log.info("删除过期爆品数据完成: 删除记录数={}", deletedCount);
        return deletedCount;
    }

    /**
     * 检查指定日期的品类是否已有爆品数据
     *
     * @param date 推荐日期
     * @param categoryId 品类ID
     * @return 是否存在
     */
    public boolean hasHotProducts(LocalDate date, Long categoryId) {
        return hotProductGateway.existsByDateAndCategory(date, categoryId);
    }

    /**
     * 获取今日爆品推荐
     *
     * @param categoryId 品类ID（可选）
     * @return 今日爆品列表
     */
    public List<HotProductScore> getTodayHotProducts(Long categoryId) {
        return getHotProducts(LocalDate.now(), categoryId);
    }

    /**
     * 获取今日 Top 4 爆品（用于首页展示）
     *
     * @return Top 4 爆品列表
     */
    public List<HotProductScore> getTodayTop4HotProducts() {
        log.info("查询今日 Top 4 爆品");

        // 获取今日所有爆品
        List<HotProductScore> allHotProducts = hotProductGateway.findByDate(LocalDate.now());

        // 按评分排序，取前4个
        List<HotProductScore> top4 = allHotProducts.stream()
                .sorted((a, b) -> b.getHotScore().compareTo(a.getHotScore()))
                .limit(4)
                .toList();

        log.info("查询到今日 Top 4 爆品数量: {}", top4.size());
        return top4;
    }
}
