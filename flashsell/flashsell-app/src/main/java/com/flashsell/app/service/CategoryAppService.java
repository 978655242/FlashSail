package com.flashsell.app.service;

import com.flashsell.app.assembler.CategoryAssembler;
import com.flashsell.client.dto.res.CategoriesRes;
import com.flashsell.client.dto.res.CategoryGroupRes;
import com.flashsell.client.dto.res.CategoryRes;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.entity.CategoryGroup;
import com.flashsell.domain.category.gateway.CategoryGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 品类应用服务
 * 提供品类相关的业务编排
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryAppService {

    private final CategoryGateway categoryGateway;
    private final CategoryAssembler categoryAssembler;

    /**
     * 获取所有品类（缓存由Gateway层处理）
     *
     * @return 品类列表响应
     */
    public CategoriesRes getAllCategories() {
        log.debug("获取所有品类列表");
        List<CategoryGroup> groups = categoryGateway.findAllGroups();
        return categoryAssembler.toCategoriesRes(groups);
    }

    /**
     * 根据ID获取品类组
     *
     * @param groupId 品类组ID
     * @return 品类组响应（可能为空）
     */
    public Optional<CategoryGroupRes> getCategoryGroupById(Long groupId) {
        return categoryGateway.findGroupById(groupId)
                .map(categoryAssembler::toCategoryGroupRes);
    }

    /**
     * 根据ID获取品类
     *
     * @param categoryId 品类ID
     * @return 品类响应（可能为空）
     */
    public Optional<CategoryRes> getCategoryById(Long categoryId) {
        return categoryGateway.findById(categoryId)
                .map(categoryAssembler::toCategoryRes);
    }

    /**
     * 根据品类组ID获取品类列表
     *
     * @param groupId 品类组ID
     * @return 品类响应列表
     */
    public List<CategoryRes> getCategoriesByGroupId(Long groupId) {
        List<Category> categories = categoryGateway.findByGroupId(groupId);
        return categoryAssembler.toCategoryResList(categories);
    }

    /**
     * 检查品类ID是否有效（在45个固定品类范围内）
     *
     * @param categoryId 品类ID
     * @return 是否有效
     */
    public boolean isValidCategoryId(Long categoryId) {
        if (categoryId == null) {
            return false;
        }
        return categoryGateway.findById(categoryId).isPresent();
    }

    /**
     * 获取品类总数
     *
     * @return 品类总数
     */
    public int getCategoryCount() {
        return categoryGateway.countAll();
    }

    /**
     * 获取品类组总数
     *
     * @return 品类组总数
     */
    public int getCategoryGroupCount() {
        return categoryGateway.countAllGroups();
    }
}
