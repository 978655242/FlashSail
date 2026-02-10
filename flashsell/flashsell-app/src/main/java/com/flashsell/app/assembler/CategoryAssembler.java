package com.flashsell.app.assembler;

import com.flashsell.client.dto.res.CategoriesRes;
import com.flashsell.client.dto.res.CategoryGroupRes;
import com.flashsell.client.dto.res.CategoryRes;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.entity.CategoryGroup;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 品类转换器
 * 负责领域实体和 DTO 之间的转换
 */
@Component
public class CategoryAssembler {

    /**
     * 将品类组列表转换为响应 DTO
     *
     * @param groups 品类组列表
     * @return 品类列表响应 DTO
     */
    public CategoriesRes toCategoriesRes(List<CategoryGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return CategoriesRes.builder()
                    .groups(Collections.emptyList())
                    .build();
        }

        List<CategoryGroupRes> groupResList = groups.stream()
                .map(this::toCategoryGroupRes)
                .collect(Collectors.toList());

        return CategoriesRes.builder()
                .groups(groupResList)
                .build();
    }

    /**
     * 将品类组转换为响应 DTO
     *
     * @param group 品类组
     * @return 品类组响应 DTO
     */
    public CategoryGroupRes toCategoryGroupRes(CategoryGroup group) {
        if (group == null) {
            return null;
        }

        List<CategoryRes> categoryResList = group.getCategories() != null
                ? group.getCategories().stream()
                        .map(this::toCategoryRes)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return CategoryGroupRes.builder()
                .id(group.getId())
                .name(group.getName())
                .categories(categoryResList)
                .build();
    }

    /**
     * 将品类转换为响应 DTO
     *
     * @param category 品类
     * @return 品类响应 DTO
     */
    public CategoryRes toCategoryRes(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryRes.builder()
                .id(category.getId())
                .name(category.getName())
                .productCount(category.getProductCount())
                .build();
    }

    /**
     * 批量将品类转换为响应 DTO
     *
     * @param categories 品类列表
     * @return 品类响应 DTO 列表
     */
    public List<CategoryRes> toCategoryResList(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyList();
        }
        return categories.stream()
                .map(this::toCategoryRes)
                .collect(Collectors.toList());
    }
}
