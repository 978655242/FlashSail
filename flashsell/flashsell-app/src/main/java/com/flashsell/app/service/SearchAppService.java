package com.flashsell.app.service;

import com.flashsell.app.assembler.SearchAssembler;
import com.flashsell.client.dto.req.SearchReq;
import com.flashsell.client.dto.res.SearchRes;
import com.flashsell.domain.ai.entity.AiSearchResult;
import com.flashsell.domain.ai.service.AiDomainService;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.gateway.CategoryGateway;
import com.flashsell.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 搜索应用服务
 * 处理 AI 搜索相关的业务编排
 * 
 * Requirements: 2.1, 2.5, 14.1, 15.1
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SearchAppService {

    private final AiDomainService aiDomainService;
    private final ProductDataService productDataService;
    private final CategoryGateway categoryGateway;
    private final SearchAssembler searchAssembler;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 搜索结果缓存 Key 前缀
     */
    private static final String SEARCH_CACHE_PREFIX = "search:result:";

    /**
     * 搜索结果缓存 TTL（15分钟）
     */
    private static final long SEARCH_CACHE_TTL_MINUTES = 15;

    /**
     * 支持的品类数量
     */
    private static final int SUPPORTED_CATEGORY_COUNT = 45;

    /**
     * AI 选品搜索
     * 
     * @param req 搜索请求
     * @param userId 用户ID（用于记录搜索历史）
     * @return 搜索结果
     */
    @Transactional
    public SearchRes search(SearchReq req, Long userId) {
        log.info("AI 搜索: query={}, userId={}", req.getQuery(), userId);

        // 1. 检查缓存
        String cacheKey = buildCacheKey(req);
        SearchRes cachedResult = getCachedResult(cacheKey);
        if (cachedResult != null) {
            log.debug("从缓存获取搜索结果: cacheKey={}", cacheKey);
            // 记录搜索历史（即使是缓存结果也要记录）
            recordSearchHistory(userId, req.getQuery(), cachedResult.getTotal().intValue());
            return cachedResult;
        }

        // 2. 使用 AI 分析搜索查询
        AiSearchResult aiResult = aiDomainService.analyzeSearchQuery(req.getQuery());
        
        // 3. 验证品类是否在支持范围内
        Long categoryId = validateAndGetCategoryId(req.getCategoryId(), aiResult);
        
        // 4. 检查是否超出支持的品类范围
        if (categoryId == null && aiResult.getCategoryIds() != null && !aiResult.getCategoryIds().isEmpty()) {
            // AI 推荐了品类但都不在支持范围内
            return buildOutOfScopeResponse(req, aiResult);
        }

        // 5. 获取搜索关键词
        String searchKeyword = getSearchKeyword(req.getQuery(), aiResult);

        // 6. 从 Bright Data 获取实时数据
        ProductDataService.ProductSearchResult dataResult = 
                productDataService.searchProductsWithFallback(searchKeyword, categoryId);

        // 7. 应用筛选条件
        List<Product> filteredProducts = applyFilters(
                dataResult.getProducts(),
                req.getPriceMin() != null ? req.getPriceMin() : aiResult.getPriceMin(),
                req.getPriceMax() != null ? req.getPriceMax() : aiResult.getPriceMax(),
                req.getMinRating() != null ? req.getMinRating() : aiResult.getMinRating(),
                categoryId
        );

        // 8. 分页
        int page = req.getPage() != null ? req.getPage() : 1;
        int pageSize = req.getPageSize() != null ? req.getPageSize() : 20;
        List<Product> pagedProducts = applyPagination(filteredProducts, page, pageSize);

        // 9. 构建响应
        SearchRes response = searchAssembler.toSearchRes(
                pagedProducts,
                filteredProducts.size(),
                page,
                pageSize,
                aiResult.getSummary(),
                dataResult.getDataFreshness()
        );

        // 10. 缓存结果
        cacheResult(cacheKey, response);

        // 11. 记录搜索历史
        recordSearchHistory(userId, req.getQuery(), filteredProducts.size());

        log.info("AI 搜索完成: query={}, 结果数量={}", req.getQuery(), filteredProducts.size());
        return response;
    }

    /**
     * 验证并获取品类ID
     * 确保品类在支持的 45 个品类范围内
     */
    private Long validateAndGetCategoryId(Long requestCategoryId, AiSearchResult aiResult) {
        // 优先使用请求中指定的品类
        if (requestCategoryId != null) {
            Optional<Category> category = categoryGateway.findById(requestCategoryId);
            if (category.isPresent()) {
                return requestCategoryId;
            }
            log.warn("请求的品类ID不存在: {}", requestCategoryId);
        }

        // 使用 AI 推荐的品类
        if (aiResult.getCategoryIds() != null && !aiResult.getCategoryIds().isEmpty()) {
            for (Long categoryId : aiResult.getCategoryIds()) {
                Optional<Category> category = categoryGateway.findById(categoryId);
                if (category.isPresent()) {
                    return categoryId;
                }
            }
        }

        return null;
    }

    /**
     * 获取搜索关键词
     */
    private String getSearchKeyword(String originalQuery, AiSearchResult aiResult) {
        // 优先使用 AI 提取的关键词
        if (aiResult.getKeywords() != null && !aiResult.getKeywords().isEmpty()) {
            return aiResult.getKeywords().get(0);
        }
        return originalQuery;
    }

    /**
     * 应用筛选条件
     */
    private List<Product> applyFilters(List<Product> products, 
                                       BigDecimal priceMin, 
                                       BigDecimal priceMax,
                                       Double minRating,
                                       Long categoryId) {
        return products.stream()
                .filter(p -> {
                    // 价格筛选
                    if (priceMin != null && p.getCurrentPrice() != null 
                            && p.getCurrentPrice().compareTo(priceMin) < 0) {
                        return false;
                    }
                    if (priceMax != null && p.getCurrentPrice() != null 
                            && p.getCurrentPrice().compareTo(priceMax) > 0) {
                        return false;
                    }
                    // 评分筛选
                    if (minRating != null && p.getRating() != null 
                            && p.getRating() < minRating) {
                        return false;
                    }
                    // 品类筛选
                    if (categoryId != null && !categoryId.equals(p.getCategoryId())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 应用分页
     */
    private List<Product> applyPagination(List<Product> products, int page, int pageSize) {
        int start = (page - 1) * pageSize;
        if (start >= products.size()) {
            return new ArrayList<>();
        }
        int end = Math.min(start + pageSize, products.size());
        return products.subList(start, end);
    }

    /**
     * 构建超出支持范围的响应
     */
    private SearchRes buildOutOfScopeResponse(SearchReq req, AiSearchResult aiResult) {
        String message = "您搜索的产品类型暂不在支持范围内。FlashSell 目前支持 " + SUPPORTED_CATEGORY_COUNT 
                + " 个品类，包括工业用品、节日装饰、家居生活与百货、数码配件与小家电等。请尝试在支持的品类中搜索。";
        
        return SearchRes.builder()
                .products(new ArrayList<>())
                .total(0L)
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .hasMore(false)
                .aiSummary(message)
                .dataFreshness("EMPTY")
                .build();
    }

    /**
     * 构建缓存 Key
     */
    private String buildCacheKey(SearchReq req) {
        StringBuilder sb = new StringBuilder(SEARCH_CACHE_PREFIX);
        sb.append(req.getQuery().hashCode());
        if (req.getCategoryId() != null) {
            sb.append(":c").append(req.getCategoryId());
        }
        if (req.getPriceMin() != null) {
            sb.append(":pmin").append(req.getPriceMin());
        }
        if (req.getPriceMax() != null) {
            sb.append(":pmax").append(req.getPriceMax());
        }
        if (req.getMinRating() != null) {
            sb.append(":r").append(req.getMinRating());
        }
        sb.append(":p").append(req.getPage());
        sb.append(":s").append(req.getPageSize());
        return sb.toString();
    }

    /**
     * 从缓存获取结果
     */
    @SuppressWarnings("unchecked")
    private SearchRes getCachedResult(String cacheKey) {
        try {
            return (SearchRes) redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.warn("获取搜索缓存失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 缓存搜索结果
     */
    private void cacheResult(String cacheKey, SearchRes result) {
        try {
            redisTemplate.opsForValue().set(cacheKey, result, 
                    SEARCH_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("缓存搜索结果失败: {}", e.getMessage());
        }
    }

    /**
     * 记录搜索历史
     * TODO: 实现搜索历史记录功能
     */
    private void recordSearchHistory(Long userId, String query, int resultCount) {
        if (userId == null) {
            return;
        }
        // 搜索历史记录将在后续任务中实现
        log.debug("记录搜索历史: userId={}, query={}, resultCount={}", userId, query, resultCount);
    }
}
