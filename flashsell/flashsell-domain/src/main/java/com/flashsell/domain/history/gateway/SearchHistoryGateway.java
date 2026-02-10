package com.flashsell.domain.history.gateway;

import com.flashsell.domain.history.entity.SearchHistory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜索历史网关接口
 * 定义搜索历史的持久化操作
 */
public interface SearchHistoryGateway {
    
    /**
     * 保存搜索历史
     * 
     * @param searchHistory 搜索历史实体
     * @return 保存后的搜索历史（包含ID）
     */
    SearchHistory save(SearchHistory searchHistory);
    
    /**
     * 根据用户ID查询搜索历史（分页）
     * 
     * @param userId 用户ID
     * @param page 页码（从0开始）
     * @param pageSize 每页数量
     * @return 搜索历史列表
     */
    List<SearchHistory> findByUserId(Long userId, int page, int pageSize);
    
    /**
     * 根据用户ID查询最近的搜索历史
     * 
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 搜索历史列表
     */
    List<SearchHistory> findRecentByUserId(Long userId, int limit);
    
    /**
     * 统计用户的搜索历史总数
     * 
     * @param userId 用户ID
     * @return 总数
     */
    long countByUserId(Long userId);
    
    /**
     * 根据ID删除搜索历史
     * 
     * @param id 搜索历史ID
     * @param userId 用户ID（用于权限校验）
     * @return 是否删除成功
     */
    boolean deleteById(Long id, Long userId);
    
    /**
     * 删除用户的所有搜索历史
     * 
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteByUserId(Long userId);
    
    /**
     * 删除指定时间之前的搜索历史
     * 
     * @param beforeTime 时间点
     * @return 删除的记录数
     */
    int deleteBeforeTime(LocalDateTime beforeTime);
}
