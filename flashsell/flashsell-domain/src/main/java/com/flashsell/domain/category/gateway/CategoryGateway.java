package com.flashsell.domain.category.gateway;

import java.util.List;
import java.util.Optional;

import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.entity.CategoryGroup;

/**
 * 品类网关接口
 * 定义品类数据访问的抽象接口，由 infrastructure 层实现
 */
public interface CategoryGateway {

    /**
     * 获取所有品类组（包含品类列表）
     *
     * @return 品类组列表
     */
    List<CategoryGroup> findAllGroups();

    /**
     * 根据ID查询品类组
     *
     * @param id 品类组ID
     * @return 品类组实体（可能为空）
     */
    Optional<CategoryGroup> findGroupById(Long id);

    /**
     * 根据ID查询品类
     *
     * @param id 品类ID
     * @return 品类实体（可能为空）
     */
    Optional<Category> findById(Long id);

    /**
     * 根据品类组ID查询品类列表
     *
     * @param groupId 品类组ID
     * @return 品类列表
     */
    List<Category> findByGroupId(Long groupId);

    /**
     * 获取所有品类
     *
     * @return 品类列表
     */
    List<Category> findAll();

    /**
     * 根据Amazon品类ID查询品类
     *
     * @param amazonCategoryId Amazon品类ID
     * @return 品类实体（可能为空）
     */
    Optional<Category> findByAmazonCategoryId(String amazonCategoryId);

    /**
     * 更新品类产品数量
     *
     * @param categoryId 品类ID
     * @param productCount 产品数量
     */
    void updateProductCount(Long categoryId, Integer productCount);

    /**
     * 获取品类总数
     *
     * @return 品类总数
     */
    int countAll();

    /**
     * 获取品类组总数
     *
     * @return 品类组总数
     */
    int countAllGroups();
}
