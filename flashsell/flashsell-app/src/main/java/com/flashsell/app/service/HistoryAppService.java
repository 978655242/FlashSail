package com.flashsell.app.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flashsell.app.assembler.SearchAssembler;
import com.flashsell.client.dto.res.BrowseHistoryDTO;
import com.flashsell.client.dto.res.BrowseHistoryRes;
import com.flashsell.client.dto.res.ProductItemRes;
import com.flashsell.client.dto.res.SearchHistoryDTO;
import com.flashsell.client.dto.res.SearchHistoryRes;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.gateway.CategoryGateway;
import com.flashsell.domain.history.entity.BrowseHistory;
import com.flashsell.domain.history.entity.SearchHistory;
import com.flashsell.domain.history.gateway.BrowseHistoryGateway;
import com.flashsell.domain.history.gateway.SearchHistoryGateway;
import com.flashsell.domain.product.entity.Product;
import com.flashsell.domain.product.gateway.ProductGateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 历史记录应用服务
 * 提供搜索历史和浏览历史相关的业务编排
 * 
 * Requirements: 14.1, 14.2, 14.3, 14.4, 14.5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryAppService {
    
    private final SearchHistoryGateway searchHistoryGateway;
    private final BrowseHistoryGateway browseHistoryGateway;
    private final ProductGateway productGateway;
    private final CategoryGateway categoryGateway;
    private final SearchAssembler searchAssembler;
    
    // ==================== 搜索历史相关方法 ====================
    
    /**
     * 记录搜索历史
     * 
     * @param userId 用户ID
     * @param query 搜索查询
     * @param resultCount 搜索结果数量
     */
    @Transactional
    public void recordSearchHistory(Long userId, String query, Integer resultCount) {
        log.debug("记录搜索历史: userId={}, query={}, resultCount={}", userId, query, resultCount);
        
        SearchHistory searchHistory = SearchHistory.builder()
                .userId(userId)
                .query(query)
                .resultCount(resultCount)
                .createdAt(LocalDateTime.now())
                .build();
        
        searchHistoryGateway.save(searchHistory);
        log.info("搜索历史记录成功: userId={}, query={}", userId, query);
    }
    
    /**
     * 获取搜索历史（分页）
     * 
     * @param userId 用户ID
     * @param page 页码（从0开始）
     * @param pageSize 每页数量
     * @return 搜索历史响应
     */
    public SearchHistoryRes getSearchHistory(Long userId, int page, int pageSize) {
        log.debug("获取搜索历史: userId={}, page={}, pageSize={}", userId, page, pageSize);
        
        // 1. 获取搜索历史总数
        long total = searchHistoryGateway.countByUserId(userId);
        
        // 2. 获取分页搜索历史
        List<SearchHistory> histories = searchHistoryGateway.findByUserId(userId, page, pageSize);
        
        // 3. 转换为 DTO
        List<SearchHistoryDTO> historyDTOs = histories.stream()
                .map(this::toSearchHistoryDTO)
                .collect(Collectors.toList());
        
        return SearchHistoryRes.builder()
                .histories(historyDTOs)
                .total(total)
                .page(page)
                .build();
    }
    
    /**
     * 获取最近的搜索历史
     * 
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 搜索历史列表
     */
    public List<SearchHistoryDTO> getRecentSearchHistory(Long userId, int limit) {
        log.debug("获取最近搜索历史: userId={}, limit={}", userId, limit);
        
        List<SearchHistory> histories = searchHistoryGateway.findRecentByUserId(userId, limit);
        
        return histories.stream()
                .map(this::toSearchHistoryDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 删除单条搜索历史
     * 
     * @param userId 用户ID
     * @param historyId 搜索历史ID
     * @return 是否删除成功
     */
    @Transactional
    public boolean deleteSearchHistory(Long userId, Long historyId) {
        log.debug("删除搜索历史: userId={}, historyId={}", userId, historyId);
        
        boolean deleted = searchHistoryGateway.deleteById(historyId, userId);
        if (deleted) {
            log.info("搜索历史删除成功: userId={}, historyId={}", userId, historyId);
        } else {
            log.debug("搜索历史不存在或无权限: userId={}, historyId={}", userId, historyId);
        }
        return deleted;
    }
    
    /**
     * 清空搜索历史
     * 
     * @param userId 用户ID
     * @return 删除的记录数
     */
    @Transactional
    public int clearSearchHistory(Long userId) {
        log.debug("清空搜索历史: userId={}", userId);
        
        int deletedCount = searchHistoryGateway.deleteByUserId(userId);
        log.info("搜索历史清空成功: userId={}, deletedCount={}", userId, deletedCount);
        return deletedCount;
    }
    
    // ==================== 浏览历史相关方法 ====================
    
    /**
     * 记录浏览历史
     * 如果已浏览过，则更新浏览时间
     * 
     * @param userId 用户ID
     * @param productId 产品ID
     */
    @Transactional
    public void recordBrowseHistory(Long userId, Long productId) {
        log.debug("记录浏览历史: userId={}, productId={}", userId, productId);
        
        BrowseHistory browseHistory = BrowseHistory.builder()
                .userId(userId)
                .productId(productId)
                .browsedAt(LocalDateTime.now())
                .build();
        
        browseHistoryGateway.saveOrUpdate(browseHistory);
        log.info("浏览历史记录成功: userId={}, productId={}", userId, productId);
    }
    
    /**
     * 获取浏览历史（分页）
     * 
     * @param userId 用户ID
     * @param page 页码（从0开始）
     * @param pageSize 每页数量
     * @return 浏览历史响应
     */
    public BrowseHistoryRes getBrowseHistory(Long userId, int page, int pageSize) {
        log.debug("获取浏览历史: userId={}, page={}, pageSize={}", userId, page, pageSize);
        
        // 1. 获取浏览历史总数
        long total = browseHistoryGateway.countByUserId(userId);
        
        // 2. 获取分页浏览历史
        List<BrowseHistory> histories = browseHistoryGateway.findByUserId(userId, page, pageSize);
        
        // 3. 批量获取产品信息
        List<Long> productIds = histories.stream()
                .map(BrowseHistory::getProductId)
                .collect(Collectors.toList());
        Map<Long, Product> productMap = getProductMap(productIds);
        
        // 4. 批量获取品类信息
        Set<Long> categoryIds = productMap.values().stream()
                .map(Product::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, Category> categoryMap = getCategoryMap(categoryIds);
        
        // 5. 转换为 DTO
        List<BrowseHistoryDTO> historyDTOs = histories.stream()
                .map(history -> toBrowseHistoryDTO(history, productMap, categoryMap))
                .filter(dto -> dto.getProduct() != null) // 过滤掉产品不存在的记录
                .collect(Collectors.toList());
        
        return BrowseHistoryRes.builder()
                .products(historyDTOs)
                .total(total)
                .page(page)
                .build();
    }
    
    /**
     * 获取最近的浏览历史
     * 
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 浏览历史列表
     */
    public List<BrowseHistoryDTO> getRecentBrowseHistory(Long userId, int limit) {
        log.debug("获取最近浏览历史: userId={}, limit={}", userId, limit);
        
        List<BrowseHistory> histories = browseHistoryGateway.findRecentByUserId(userId, limit);
        
        // 批量获取产品信息
        List<Long> productIds = histories.stream()
                .map(BrowseHistory::getProductId)
                .collect(Collectors.toList());
        Map<Long, Product> productMap = getProductMap(productIds);
        
        // 批量获取品类信息
        Set<Long> categoryIds = productMap.values().stream()
                .map(Product::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, Category> categoryMap = getCategoryMap(categoryIds);
        
        return histories.stream()
                .map(history -> toBrowseHistoryDTO(history, productMap, categoryMap))
                .filter(dto -> dto.getProduct() != null)
                .collect(Collectors.toList());
    }
    
    /**
     * 删除单条浏览历史
     * 
     * @param userId 用户ID
     * @param productId 产品ID
     * @return 是否删除成功
     */
    @Transactional
    public boolean deleteBrowseHistory(Long userId, Long productId) {
        log.debug("删除浏览历史: userId={}, productId={}", userId, productId);
        
        boolean deleted = browseHistoryGateway.deleteByUserIdAndProductId(userId, productId);
        if (deleted) {
            log.info("浏览历史删除成功: userId={}, productId={}", userId, productId);
        } else {
            log.debug("浏览历史不存在: userId={}, productId={}", userId, productId);
        }
        return deleted;
    }
    
    /**
     * 清空浏览历史
     * 
     * @param userId 用户ID
     * @return 删除的记录数
     */
    @Transactional
    public int clearBrowseHistory(Long userId) {
        log.debug("清空浏览历史: userId={}", userId);
        
        int deletedCount = browseHistoryGateway.deleteByUserId(userId);
        log.info("浏览历史清空成功: userId={}, deletedCount={}", userId, deletedCount);
        return deletedCount;
    }
    
    // ==================== 定时清理方法 ====================
    
    /**
     * 清理30天前的历史记录
     * 由定时任务调用
     * 
     * @return 清理的记录数
     */
    @Transactional
    public int cleanExpiredHistory() {
        log.info("开始清理过期历史记录");
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        int searchDeleted = searchHistoryGateway.deleteBeforeTime(thirtyDaysAgo);
        int browseDeleted = browseHistoryGateway.deleteBeforeTime(thirtyDaysAgo);
        
        int totalDeleted = searchDeleted + browseDeleted;
        log.info("过期历史记录清理完成: searchDeleted={}, browseDeleted={}, total={}", 
                searchDeleted, browseDeleted, totalDeleted);
        
        return totalDeleted;
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 转换搜索历史为 DTO
     */
    private SearchHistoryDTO toSearchHistoryDTO(SearchHistory history) {
        return SearchHistoryDTO.builder()
                .id(history.getId())
                .query(history.getQuery())
                .resultCount(history.getResultCount())
                .createdAt(history.getCreatedAt())
                .build();
    }
    
    /**
     * 转换浏览历史为 DTO
     */
    private BrowseHistoryDTO toBrowseHistoryDTO(BrowseHistory history, 
                                                 Map<Long, Product> productMap, 
                                                 Map<Long, Category> categoryMap) {
        Product product = productMap.get(history.getProductId());
        if (product == null) {
            return BrowseHistoryDTO.builder()
                    .browsedAt(history.getBrowsedAt())
                    .build();
        }
        
        // 构建品类名称映射
        Map<Long, String> categoryNameMap = new HashMap<>();
        if (product.getCategoryId() != null && categoryMap.containsKey(product.getCategoryId())) {
            Category category = categoryMap.get(product.getCategoryId());
            categoryNameMap.put(category.getId(), category.getName());
        }
        
        ProductItemRes productRes = searchAssembler.toProductItemRes(product, categoryNameMap);
        
        return BrowseHistoryDTO.builder()
                .product(productRes)
                .browsedAt(history.getBrowsedAt())
                .build();
    }
    
    /**
     * 批量获取产品映射
     */
    private Map<Long, Product> getProductMap(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return productIds.stream()
                .map(productGateway::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Product::getId, p -> p));
    }
    
    /**
     * 批量获取品类映射
     */
    private Map<Long, Category> getCategoryMap(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryIds.stream()
                .map(categoryGateway::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Category::getId, c -> c));
    }
}
