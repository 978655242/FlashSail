package com.flashsell.adapter.web;

import com.flashsell.app.assembler.HotProductAssembler;
import com.flashsell.app.service.HotProductAppService;
import com.flashsell.client.dto.ApiResponse;
import com.flashsell.client.dto.req.HotProductsReq;
import com.flashsell.client.dto.res.*;
import com.flashsell.domain.ai.entity.HotProductScore;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.entity.CategoryGroup;
import com.flashsell.domain.category.gateway.CategoryGateway;
import com.flashsell.domain.product.entity.Product;
import com.flashsell.domain.product.gateway.ProductGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 爆品推荐控制器
 * 
 * Requirements: 11.4, 11.5, 11.8, 11.9
 */
@RestController
@RequestMapping("/api/hot-products")
@Slf4j
@RequiredArgsConstructor
public class HotProductController {

    private final HotProductAppService hotProductAppService;
    private final HotProductAssembler hotProductAssembler;
    private final CategoryGateway categoryGateway;
    private final ProductGateway productGateway;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "hot_products:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    /**
     * 获取爆品推荐列表
     * GET /api/hot-products
     *
     * @param req 查询请求
     * @return 爆品推荐列表
     */
    @GetMapping
    public ApiResponse<HotProductsRes> getHotProducts(HotProductsReq req) {
        log.info("查询爆品推荐列表: req={}", req);

        LocalDate date = req.getDate() != null ? req.getDate() : LocalDate.now();
        Long categoryGroupId = req.getCategoryGroupId();
        Long categoryId = req.getCategoryId();

        // 尝试从缓存获取
        String cacheKey = buildCacheKey(date, categoryGroupId, categoryId);
        HotProductsRes cached = (HotProductsRes) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("从缓存获取爆品推荐: cacheKey={}", cacheKey);
            return ApiResponse.success(cached);
        }

        // 查询爆品数据
        List<HotProductScore> hotProductScores;
        if (categoryId != null) {
            // 查询指定品类的爆品
            hotProductScores = hotProductAppService.getHotProducts(date, categoryId);
        } else {
            // 查询所有爆品
            hotProductScores = hotProductAppService.getHotProducts(date, null);
        }

        if (hotProductScores.isEmpty()) {
            log.info("未找到爆品数据: date={}, categoryGroupId={}, categoryId={}", 
                    date, categoryGroupId, categoryId);
            return ApiResponse.success(HotProductsRes.builder()
                    .date(date)
                    .groups(new ArrayList<>())
                    .total(0)
                    .build());
        }

        // 获取产品详情
        List<Long> productIds = hotProductScores.stream()
                .map(HotProductScore::getProductId)
                .distinct()
                .collect(Collectors.toList());
        List<Product> products = productGateway.findByIds(productIds);

        // 转换为DTO
        List<HotProductDTO> hotProductDTOs = hotProductAssembler.toDTOList(hotProductScores, products);

        // 按品类组分组
        List<HotProductGroup> groups = groupByCategory(hotProductDTOs, categoryGroupId);

        // 构建响应
        HotProductsRes response = hotProductAssembler.toHotProductsRes(date, groups);

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);

        log.info("查询爆品推荐完成: date={}, total={}", date, response.getTotal());
        return ApiResponse.success(response);
    }

    /**
     * 获取产品的爆品历史趋势
     * GET /api/hot-products/history
     *
     * @param productId 产品ID
     * @param days 查询天数（默认7天）
     * @return 爆品历史趋势
     */
    @GetMapping("/history")
    public ApiResponse<HotProductHistoryRes> getHotProductHistory(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "7") int days) {
        log.info("查询产品爆品历史: productId={}, days={}", productId, days);

        // 限制查询天数不超过30天
        if (days > 30) {
            days = 30;
        }

        // 查询历史数据
        List<HotProductScore> history = hotProductAppService.getProductHotHistory(productId, days);

        // 转换为响应
        HotProductHistoryRes response = hotProductAssembler.toHistoryRes(productId, history);

        log.info("查询产品爆品历史完成: productId={}, historyCount={}", productId, history.size());
        return ApiResponse.success(response);
    }

    /**
     * 获取今日 Top 4 爆品（用于首页展示）
     * GET /api/hot-products/top4
     *
     * @return Top 4 爆品列表
     */
    @GetMapping("/top4")
    public ApiResponse<List<HotProductDTO>> getTop4HotProducts() {
        log.info("查询今日 Top 4 爆品");

        // 尝试从缓存获取
        String cacheKey = "hot_products:top4:" + LocalDate.now();
        @SuppressWarnings("unchecked")
        List<HotProductDTO> cached = (List<HotProductDTO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("从缓存获取 Top 4 爆品");
            return ApiResponse.success(cached);
        }

        // 查询今日 Top 4 爆品
        List<HotProductScore> top4Scores = hotProductAppService.getTodayTop4HotProducts();

        if (top4Scores.isEmpty()) {
            log.info("未找到今日爆品数据");
            return ApiResponse.success(new ArrayList<>());
        }

        // 获取产品详情
        List<Long> productIds = top4Scores.stream()
                .map(HotProductScore::getProductId)
                .collect(Collectors.toList());
        List<Product> products = productGateway.findByIds(productIds);

        // 转换为DTO
        List<HotProductDTO> top4DTOs = hotProductAssembler.toDTOList(top4Scores, products);

        // 缓存结果（5分钟）
        redisTemplate.opsForValue().set(cacheKey, top4DTOs, Duration.ofMinutes(5));

        log.info("查询今日 Top 4 爆品完成: count={}", top4DTOs.size());
        return ApiResponse.success(top4DTOs);
    }

    /**
     * 按品类组分组爆品
     *
     * @param hotProductDTOs 爆品DTO列表
     * @param categoryGroupId 品类组ID（可选，用于过滤）
     * @return 爆品分组列表
     */
    private List<HotProductGroup> groupByCategory(List<HotProductDTO> hotProductDTOs, Long categoryGroupId) {
        // 获取所有品类
        List<Category> categories = categoryGateway.findAll();

        // 获取所有品类组
        List<CategoryGroup> categoryGroups = categoryGateway.findAllGroups();
        
        // 如果指定了品类组ID，只保留该品类组
        if (categoryGroupId != null) {
            categoryGroups = categoryGroups.stream()
                    .filter(group -> group.getId().equals(categoryGroupId))
                    .collect(Collectors.toList());
        }

        // 按品类组分组
        List<HotProductGroup> groups = new ArrayList<>();
        for (CategoryGroup categoryGroup : categoryGroups) {
            // 获取该品类组下的所有品类ID
            List<Long> categoryIds = categories.stream()
                    .filter(c -> c.getGroupId().equals(categoryGroup.getId()))
                    .map(Category::getId)
                    .collect(Collectors.toList());

            // 过滤出该品类组下的爆品
            List<HotProductDTO> groupProducts = hotProductDTOs.stream()
                    .filter(dto -> {
                        Long catId = dto.getProduct().getCategoryId();
                        return catId != null && categoryIds.contains(catId);
                    })
                    .collect(Collectors.toList());

            if (!groupProducts.isEmpty()) {
                HotProductGroup group = hotProductAssembler.toGroup(categoryGroup, groupProducts);
                groups.add(group);
            }
        }

        return groups;
    }

    /**
     * 构建缓存Key
     *
     * @param date 日期
     * @param categoryGroupId 品类组ID
     * @param categoryId 品类ID
     * @return 缓存Key
     */
    private String buildCacheKey(LocalDate date, Long categoryGroupId, Long categoryId) {
        StringBuilder key = new StringBuilder(CACHE_KEY_PREFIX);
        key.append(date);
        if (categoryGroupId != null) {
            key.append(":group:").append(categoryGroupId);
        }
        if (categoryId != null) {
            key.append(":cat:").append(categoryId);
        }
        return key.toString();
    }
}
