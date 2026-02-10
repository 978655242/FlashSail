package com.flashsell.adapter.web;

import com.flashsell.app.service.CategoryAppService;
import com.flashsell.client.dto.ApiResponse;
import com.flashsell.client.dto.res.CategoriesRes;
import com.flashsell.client.dto.res.CategoryGroupRes;
import com.flashsell.client.dto.res.CategoryRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 品类控制器
 * 处理品类相关的 API 请求
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryAppService categoryAppService;

    /**
     * 获取所有品类列表
     * 返回按品类组分组的品类数据
     *
     * @return 品类列表响应
     */
    @GetMapping
    public ApiResponse<CategoriesRes> getAllCategories() {
        log.info("获取所有品类列表");
        CategoriesRes res = categoryAppService.getAllCategories();
        return ApiResponse.success(res);
    }

    /**
     * 根据品类组ID获取品类组详情
     *
     * @param groupId 品类组ID
     * @return 品类组响应
     */
    @GetMapping("/groups/{groupId}")
    public ApiResponse<CategoryGroupRes> getCategoryGroup(@PathVariable Long groupId) {
        log.info("获取品类组详情: groupId={}", groupId);
        return categoryAppService.getCategoryGroupById(groupId)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "品类组不存在"));
    }

    /**
     * 根据品类ID获取品类详情
     *
     * @param categoryId 品类ID
     * @return 品类响应
     */
    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryRes> getCategory(@PathVariable Long categoryId) {
        log.info("获取品类详情: categoryId={}", categoryId);
        return categoryAppService.getCategoryById(categoryId)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "品类不存在"));
    }

    /**
     * 根据品类组ID获取该组下的品类列表
     *
     * @param groupId 品类组ID
     * @return 品类列表
     */
    @GetMapping("/groups/{groupId}/categories")
    public ApiResponse<List<CategoryRes>> getCategoriesByGroup(@PathVariable Long groupId) {
        log.info("获取品类组下的品类列表: groupId={}", groupId);
        List<CategoryRes> categories = categoryAppService.getCategoriesByGroupId(groupId);
        return ApiResponse.success(categories);
    }
}
