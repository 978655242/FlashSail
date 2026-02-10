package com.flashsell.domain.history.gateway;

import com.flashsell.domain.history.entity.HotKeyword;

import java.time.LocalDate;
import java.util.List;

/**
 * 热门关键词网关接口
 * 定义热门关键词的持久化操作
 */
public interface HotKeywordGateway {
    
    /**
     * 保存或更新热门关键词
     * 
     * @param hotKeyword 热门关键词实体
     * @return 保存后的热门关键词
     */
    HotKeyword saveOrUpdate(HotKeyword hotKeyword);
    
    /**
     * 根据关键词和日期查询
     * 
     * @param keyword 关键词
     * @param statDate 统计日期
     * @return 热门关键词，不存在则返回null
     */
    HotKeyword findByKeywordAndDate(String keyword, LocalDate statDate);
    
    /**
     * 查询指定日期的热门关键词（按搜索次数降序）
     * 
     * @param statDate 统计日期
     * @param limit 数量限制
     * @return 热门关键词列表
     */
    List<HotKeyword> findTopByDate(LocalDate statDate, int limit);
    
    /**
     * 查询指定日期范围的热门关键词
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 热门关键词列表
     */
    List<HotKeyword> findByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * 批量保存热门关键词
     * 
     * @param hotKeywords 热门关键词列表
     */
    void batchSave(List<HotKeyword> hotKeywords);
}
