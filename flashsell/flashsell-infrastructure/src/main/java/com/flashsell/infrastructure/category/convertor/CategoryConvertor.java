package com.flashsell.infrastructure.category.convertor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.entity.CategoryGroup;
import com.flashsell.infrastructure.category.dataobject.CategoryDO;
import com.flashsell.infrastructure.category.dataobject.CategoryGroupDO;

/**
 * 品类转换器
 * 负责 CategoryDO/CategoryGroupDO 和领域实体之间的转换
 */
@Component
public class CategoryConvertor {

    /**
     * 将品类组数据对象转换为领域实体
     *
     * @param categoryGroupDO 品类组数据对象
     * @return 品类组领域实体
     */
    public CategoryGroup toGroupEntity(CategoryGroupDO categoryGroupDO) {
        if (categoryGroupDO == null) {
            return null;
        }

        return CategoryGroup.builder()
                .id(categoryGroupDO.getId())
                .name(categoryGroupDO.getName())
                .sortOrder(categoryGroupDO.getSortOrder())
                .createdAt(categoryGroupDO.getCreatedAt())
                .categories(Collections.emptyList())
                .build();
    }

    /**
     * 将品类组数据对象转换为领域实体（包含品类列表）
     *
     * @param categoryGroupDO 品类组数据对象
     * @param categoryDOs 品类数据对象列表
     * @return 品类组领域实体
     */
    public CategoryGroup toGroupEntity(CategoryGroupDO categoryGroupDO, List<CategoryDO> categoryDOs) {
        if (categoryGroupDO == null) {
            return null;
        }

        List<Category> categories = categoryDOs != null
                ? categoryDOs.stream().map(this::toEntity).collect(Collectors.toList())
                : Collections.emptyList();

        return CategoryGroup.builder()
                .id(categoryGroupDO.getId())
                .name(categoryGroupDO.getName())
                .sortOrder(categoryGroupDO.getSortOrder())
                .createdAt(categoryGroupDO.getCreatedAt())
                .categories(categories)
                .build();
    }

    /**
     * 将品类数据对象转换为领域实体
     *
     * @param categoryDO 品类数据对象
     * @return 品类领域实体
     */
    public Category toEntity(CategoryDO categoryDO) {
        if (categoryDO == null) {
            return null;
        }

        return Category.builder()
                .id(categoryDO.getId())
                .groupId(categoryDO.getGroupId())
                .name(categoryDO.getName())
                .amazonCategoryId(categoryDO.getAmazonCategoryId())
                .productCount(categoryDO.getProductCount())
                .createdAt(categoryDO.getCreatedAt())
                .build();
    }

    /**
     * 将品类领域实体转换为数据对象
     *
     * @param category 品类领域实体
     * @return 品类数据对象
     */
    public CategoryDO toDataObject(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryDO.builder()
                .id(category.getId())
                .groupId(category.getGroupId())
                .name(category.getName())
                .amazonCategoryId(category.getAmazonCategoryId())
                .productCount(category.getProductCount())
                .createdAt(category.getCreatedAt())
                .build();
    }

    /**
     * 将品类组领域实体转换为数据对象
     *
     * @param categoryGroup 品类组领域实体
     * @return 品类组数据对象
     */
    public CategoryGroupDO toGroupDataObject(CategoryGroup categoryGroup) {
        if (categoryGroup == null) {
            return null;
        }

        return CategoryGroupDO.builder()
                .id(categoryGroup.getId())
                .name(categoryGroup.getName())
                .sortOrder(categoryGroup.getSortOrder())
                .createdAt(categoryGroup.getCreatedAt())
                .build();
    }

    /**
     * 批量转换品类数据对象为领域实体
     *
     * @param categoryDOs 品类数据对象列表
     * @return 品类领域实体列表
     */
    public List<Category> toEntityList(List<CategoryDO> categoryDOs) {
        if (categoryDOs == null || categoryDOs.isEmpty()) {
            return Collections.emptyList();
        }
        return categoryDOs.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换品类组数据对象为领域实体
     *
     * @param categoryGroupDOs 品类组数据对象列表
     * @return 品类组领域实体列表
     */
    public List<CategoryGroup> toGroupEntityList(List<CategoryGroupDO> categoryGroupDOs) {
        if (categoryGroupDOs == null || categoryGroupDOs.isEmpty()) {
            return Collections.emptyList();
        }
        return categoryGroupDOs.stream()
                .map(this::toGroupEntity)
                .collect(Collectors.toList());
    }
}
