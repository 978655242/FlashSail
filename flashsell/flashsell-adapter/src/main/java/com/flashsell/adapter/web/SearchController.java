package com.flashsell.adapter.web;

import com.flashsell.app.service.SearchAppService;
import com.flashsell.client.dto.ApiResponse;
import com.flashsell.client.dto.req.SearchReq;
import com.flashsell.client.dto.res.SearchRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索控制器
 * 处理 AI 选品搜索相关的 HTTP 请求
 * 
 * Requirements: 2.1, 2.2, 2.3
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchAppService searchAppService;

    /**
     * AI 选品搜索
     * 
     * POST /api/search
     * 
     * @param req 搜索请求
     * @param userDetails 当前登录用户（可选）
     * @return 搜索结果
     */
    @PostMapping
    public ApiResponse<SearchRes> search(
            @Valid @RequestBody SearchReq req,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("收到搜索请求: query={}", req.getQuery());
        
        // 获取用户ID（如果已登录）
        Long userId = extractUserId(userDetails);
        
        SearchRes result = searchAppService.search(req, userId);
        
        return ApiResponse.success(result);
    }

    /**
     * 从 UserDetails 中提取用户ID
     */
    private Long extractUserId(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        try {
            // 假设 username 是用户ID
            return Long.parseLong(userDetails.getUsername());
        } catch (NumberFormatException e) {
            log.warn("无法解析用户ID: {}", userDetails.getUsername());
            return null;
        }
    }
}
