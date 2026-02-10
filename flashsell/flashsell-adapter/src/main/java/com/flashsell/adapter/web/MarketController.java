package com.flashsell.adapter.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flashsell.app.service.MarketAppService;
import com.flashsell.client.dto.ApiResponse;
import com.flashsell.client.dto.req.MarketAnalysisReq;
import com.flashsell.client.dto.res.MarketAnalysisRes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 市场分析控制器
 * 处理市场分析相关的 API 请求
 */
@Slf4j
@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketAppService marketAppService;

    /**
     * 获取市场分析
     * 返回品类的市场分析数据，包括销量分布、竞争强度、趋势分析等
     *
     * GET /api/market/analysis?categoryId=1&timeRangeDays=30
     *
     * @param categoryId 品类ID
     * @param timeRangeDays 时间范围（天数：30、90、365），默认30天
     * @return 市场分析响应
     */
    @GetMapping("/analysis")
    public ApiResponse<MarketAnalysisRes> getMarketAnalysis(
            @RequestParam(name = "categoryId") Long categoryId,
            @RequestParam(name = "timeRangeDays", required = false, defaultValue = "30") Integer timeRangeDays) {
        log.info("获取市场分析: categoryId={}, timeRangeDays={}", categoryId, timeRangeDays);

        // 构建请求对象
        MarketAnalysisReq req = MarketAnalysisReq.builder()
                .categoryId(categoryId)
                .timeRangeDays(timeRangeDays)
                .build();

        // 调用应用服务
        return marketAppService.getMarketAnalysis(req)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "品类不存在或数据不足"));
    }

    /**
     * 获取市场分析
     * 返回品类的市场分析数据，包括销量分布、竞争强度、趋势分析等
     *
     * POST /api/market/analyze
     *
     * @param req 市场分析请求对象
     * @return 市场分析响应
     */
    @PostMapping("/analyze")
    public ApiResponse<MarketAnalysisRes> analyze(@Valid @RequestBody MarketAnalysisReq req) {
        log.info("获取市场分析: categoryId={}, timeRangeDays={}", req.getCategoryId(), req.getTimeRangeDays());

        // 调用应用服务
        return marketAppService.getMarketAnalysis(req)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "品类不存在或数据不足"));
    }

    /**
     * 刷新市场分析
     * 强制重新生成市场分析数据
     *
     * POST /api/market/analysis/refresh
     *
     * @param req 市场分析请求
     * @return 市场分析响应
     */
    @PostMapping("/analysis/refresh")
    public ApiResponse<MarketAnalysisRes> refreshMarketAnalysis(@Valid @RequestBody MarketAnalysisReq req) {
        log.info("刷新市场分析: categoryId={}, timeRangeDays={}", req.getCategoryId(), req.getTimeRangeDays());

        return marketAppService.refreshMarketAnalysis(req.getCategoryId(), req.getTimeRangeDays())
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "品类不存在或数据不足"));
    }

    /**
     * 检查品类是否有足够的数据进行分析
     *
     * GET /api/market/check-data?categoryId=1
     *
     * @param categoryId 品类ID
     * @return 是否有足够数据
     */
    @GetMapping("/check-data")
    public ApiResponse<Boolean> checkDataAvailability(@RequestParam(name = "categoryId") Long categoryId) {
        log.info("检查品类数据可用性: categoryId={}", categoryId);

        boolean hasEnoughData = marketAppService.hasEnoughDataForAnalysis(categoryId);
        return ApiResponse.success(hasEnoughData);
    }

    /**
     * 导出市场分析报告
     * 生成并下载市场分析报告（PDF格式）
     *
     * GET /api/market/export?categoryId=1&timeRangeDays=30
     *
     * @param categoryId 品类ID
     * @param timeRangeDays 时间范围（天数）
     * @return 报告下载URL或错误信息
     */
    @GetMapping("/export")
    public ApiResponse<String> exportMarketReport(
            @RequestParam(name = "categoryId") Long categoryId,
            @RequestParam(name = "timeRangeDays", required = false, defaultValue = "30") Integer timeRangeDays) {
        log.info("导出市场分析报告: categoryId={}, timeRangeDays={}", categoryId, timeRangeDays);

        // TODO: 实现报告导出功能
        // 1. 获取市场分析数据
        // 2. 生成PDF报告
        // 3. 上传到OSS或本地存储
        // 4. 返回下载URL

        return ApiResponse.error(501, "报告导出功能暂未实现");
    }
}
