package com.flashsell.adapter.web;

import com.flashsell.app.service.HistoryAppService;
import com.flashsell.client.dto.ApiResponse;
import com.flashsell.client.dto.res.BrowseHistoryRes;
import com.flashsell.client.dto.res.SearchHistoryRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.flashsell.adapter.web.SecurityUtils.getCurrentUserId;

/**
 * 历史记录控制器
 * 处理搜索历史和浏览历史相关的 API 请求
 *
 * Requirements: 14.1, 14.2, 14.3, 14.4, 14.5
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryAppService historyAppService;
    
    // ==================== 搜索历史接口 ====================
    
    /**
     * 获取搜索历史（分页）
     * 
     * @param userDetails 当前登录用户
     * @param page 页码（从0开始，默认0）
     * @param pageSize 每页数量（默认20）
     * @return 搜索历史响应
     */
    @GetMapping("/search/history")
    public ApiResponse<SearchHistoryRes> getSearchHistory(
                        @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = getCurrentUserId();
        log.info("获取搜索历史: userId={}, page={}, pageSize={}", userId, page, pageSize);
        
        // 参数校验
        if (page < 0) {
            page = 0;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }
        
        SearchHistoryRes result = historyAppService.getSearchHistory(userId, page, pageSize);
        return ApiResponse.success(result);
    }
    
    /**
     * 删除单条搜索历史
     * 
     * @param userDetails 当前登录用户
     * @param id 搜索历史ID
     * @return 操作结果
     */
    @DeleteMapping("/search/history/{id}")
    public ApiResponse<Void> deleteSearchHistory(
                        @PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("删除搜索历史: userId={}, historyId={}", userId, id);
        
        boolean deleted = historyAppService.deleteSearchHistory(userId, id);
        if (deleted) {
            return ApiResponse.success();
        } else {
            return ApiResponse.error(404, "搜索历史不存在或无权限");
        }
    }
    
    /**
     * 清空搜索历史
     * 
     * @param userDetails 当前登录用户
     * @return 操作结果
     */
    @DeleteMapping("/search/history")
    public ApiResponse<Void> clearSearchHistory() {
        Long userId = getCurrentUserId();
        log.info("清空搜索历史: userId={}", userId);
        
        int deletedCount = historyAppService.clearSearchHistory(userId);
        log.info("搜索历史清空完成: userId={}, deletedCount={}", userId, deletedCount);
        return ApiResponse.success();
    }
    
    // ==================== 浏览历史接口 ====================
    
    /**
     * 获取浏览历史（分页）
     * 
     * @param userDetails 当前登录用户
     * @param page 页码（从0开始，默认0）
     * @param pageSize 每页数量（默认20）
     * @return 浏览历史响应
     */
    @GetMapping("/browse/history")
    public ApiResponse<BrowseHistoryRes> getBrowseHistory(
                        @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = getCurrentUserId();
        log.info("获取浏览历史: userId={}, page={}, pageSize={}", userId, page, pageSize);
        
        // 参数校验
        if (page < 0) {
            page = 0;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }
        
        BrowseHistoryRes result = historyAppService.getBrowseHistory(userId, page, pageSize);
        return ApiResponse.success(result);
    }
    
    /**
     * 删除单条浏览历史
     * 
     * @param userDetails 当前登录用户
     * @param productId 产品ID
     * @return 操作结果
     */
    @DeleteMapping("/browse/history/{productId}")
    public ApiResponse<Void> deleteBrowseHistory(
                        @PathVariable Long productId) {
        Long userId = getCurrentUserId();
        log.info("删除浏览历史: userId={}, productId={}", userId, productId);
        
        boolean deleted = historyAppService.deleteBrowseHistory(userId, productId);
        if (deleted) {
            return ApiResponse.success();
        } else {
            return ApiResponse.error(404, "浏览历史不存在");
        }
    }
    
    /**
     * 清空浏览历史
     * 
     * @param userDetails 当前登录用户
     * @return 操作结果
     */
    @DeleteMapping("/browse/history")
    public ApiResponse<Void> clearBrowseHistory() {
        Long userId = getCurrentUserId();
        log.info("清空浏览历史: userId={}", userId);
        
        int deletedCount = historyAppService.clearBrowseHistory(userId);
        log.info("浏览历史清空完成: userId={}, deletedCount={}", userId, deletedCount);
        return ApiResponse.success();
    }
}
