package com.flashsell.domain.history.gateway;

import com.flashsell.domain.history.entity.BrowseHistory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 浏览历史网关接口
 * 定义浏览历史的持久化操作
 */
public interface BrowseHistoryGateway {
    
    /**
     * 保存或更新浏览历史
     * 如果用户已浏览过该产品，则更新浏览时间
     * 
     * @param browseHistory 浏览历史实体
     * @return 保存后的浏览历史
     */
    BrowseHistory saveOrUpdate(BrowseHistory browseHistory);
    
    /**
     * 根据用户ID和产品ID查询浏览历史
     * 
     * @param userId 用户ID
     * @param productId 产品ID
     * @return 浏览历史，不存在则返回null
     */
    BrowseHistory findByUserIdAndProductId(Long userId, Long productId);
    
    /**
     * 根据用户ID查询浏览历史（分页）
     * 
     * @param userId 用户ID
     * @param page 页码（从0开始）
     * @param pageSize 每页数量
     * @return 浏览历史列表
     */
    List<BrowseHistory> findByUserId(Long userId, int page, int pageSize);
    
    /**
     * 根据用户ID查询最近的浏览历史
     * 
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 浏览历史列表
     */
    List<BrowseHistory> findRecentByUserId(Long userId, int limit);
    
    /**
     * 统计用户的浏览历史总数
     * 
     * @param userId 用户ID
     * @return 总数
     */
    long countByUserId(Long userId);
    
    /**
     * 根据用户ID和产品ID删除浏览历史
     * 
     * @param userId 用户ID
     * @param productId 产品ID
     * @return 是否删除成功
     */
    boolean deleteByUserIdAndProductId(Long userId, Long productId);
    
    /**
     * 删除用户的所有浏览历史
     * 
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteByUserId(Long userId);
    
    /**
     * 删除指定时间之前的浏览历史
     * 
     * @param beforeTime 时间点
     * @return 删除的记录数
     */
    int deleteBeforeTime(LocalDateTime beforeTime);
}
