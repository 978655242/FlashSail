package com.flashsell.adapter.web;

import com.flashsell.app.service.ProductAppService;
import com.flashsell.client.dto.ApiResponse;
import com.flashsell.client.dto.res.PriceHistoryRes;
import com.flashsell.client.dto.res.ProductDetailRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 产品控制器
 * 处理产品相关的 API 请求
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductAppService productAppService;

    /**
     * 获取产品详情
     * 返回完整的产品信息，包括价格历史、竞争分析和 AI 推荐
     *
     * @param id 产品ID
     * @return 产品详情响应
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductDetailRes> getProductDetail(@PathVariable Long id) {
        log.info("获取产品详情: id={}", id);
        return productAppService.getProductDetail(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "产品不存在"));
    }

    /**
     * 获取产品价格历史
     * 返回产品的历史价格数据，用于绘制价格趋势图
     *
     * @param id 产品ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 价格历史响应
     */
    @GetMapping("/{id}/price-history")
    public ApiResponse<PriceHistoryRes> getPriceHistory(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("获取产品价格历史: id={}, startDate={}, endDate={}", id, startDate, endDate);

        // 如果指定了日期范围，使用日期范围查询
        if (startDate != null && endDate != null) {
            return productAppService.getPriceHistoryByDateRange(id, startDate, endDate)
                    .map(ApiResponse::success)
                    .orElse(ApiResponse.error(404, "产品不存在"));
        }

        // 否则返回全部价格历史
        return productAppService.getPriceHistory(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "产品不存在"));
    }
}
