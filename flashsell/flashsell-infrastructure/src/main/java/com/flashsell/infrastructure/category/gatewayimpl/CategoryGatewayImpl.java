package com.flashsell.infrastructure.category.gatewayimpl;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.entity.CategoryGroup;
import com.flashsell.domain.category.gateway.CategoryGateway;
import com.flashsell.infrastructure.category.convertor.CategoryConvertor;
import com.flashsell.infrastructure.category.dataobject.CategoryDO;
import com.flashsell.infrastructure.category.dataobject.CategoryGroupDO;
import com.flashsell.infrastructure.category.mapper.CategoryGroupMapper;
import com.flashsell.infrastructure.category.mapper.CategoryMapper;
import com.flashsell.infrastructure.common.CacheConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 品类网关实现
 * 实现 CategoryGateway 接口，提供品类数据访问的具体实现
 * 包含 Redis 缓存支持
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CategoryGatewayImpl implements CategoryGateway {

    private final CategoryMapper categoryMapper;
    private final CategoryGroupMapper categoryGroupMapper;
    private final CategoryConvertor categoryConvertor;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public List<CategoryGroup> findAllGroups() {
        // 尝试从缓存获取
        List<CategoryGroup> cached = (List<CategoryGroup>) redisTemplate.opsForValue()
                .get(CacheConstants.CATEGORIES_ALL);
        if (cached != null) {
            log.debug("从缓存获取品类列表");
            return cached;
        }

        log.debug("从数据库获取品类列表");
        // 获取所有品类组
        List<CategoryGroupDO> groupDOs = categoryGroupMapper.selectAllOrdered();
        
        // 获取所有品类
        List<CategoryDO> allCategoryDOs = categoryMapper.selectAllOrdered();
        
        // 按品类组ID分组
        List<CategoryGroup> result = groupDOs.stream()
                .map(groupDO -> {
                    List<CategoryDO> groupCategories = allCategoryDOs.stream()
                            .filter(c -> groupDO.getId().equals(c.getGroupId()))
                            .collect(Collectors.toList());
                    return categoryConvertor.toGroupEntity(groupDO, groupCategories);
                })
                .collect(Collectors.toList());

        // 写入缓存
        redisTemplate.opsForValue().set(
                CacheConstants.CATEGORIES_ALL,
                result,
                CacheConstants.CATEGORIES_TTL,
                TimeUnit.SECONDS
        );

        return result;
    }

    @Override
    public Optional<CategoryGroup> findGroupById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        CategoryGroupDO groupDO = categoryGroupMapper.selectById(id);
        if (groupDO == null) {
            return Optional.empty();
        }
        
        // 获取该组下的品类
        List<CategoryDO> categoryDOs = categoryMapper.selectByGroupId(id);
        return Optional.of(categoryConvertor.toGroupEntity(groupDO, categoryDOs));
    }

    @Override
    public Optional<Category> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        CategoryDO categoryDO = categoryMapper.selectById(id);
        return Optional.ofNullable(categoryConvertor.toEntity(categoryDO));
    }

    @Override
    public List<Category> findByGroupId(Long groupId) {
        if (groupId == null) {
            return List.of();
        }
        List<CategoryDO> categoryDOs = categoryMapper.selectByGroupId(groupId);
        return categoryConvertor.toEntityList(categoryDOs);
    }

    @Override
    public List<Category> findAll() {
        List<CategoryDO> categoryDOs = categoryMapper.selectAllOrdered();
        return categoryConvertor.toEntityList(categoryDOs);
    }

    @Override
    public Optional<Category> findByAmazonCategoryId(String amazonCategoryId) {
        if (amazonCategoryId == null || amazonCategoryId.isEmpty()) {
            return Optional.empty();
        }
        CategoryDO categoryDO = categoryMapper.selectByAmazonCategoryId(amazonCategoryId);
        return Optional.ofNullable(categoryConvertor.toEntity(categoryDO));
    }

    @Override
    public void updateProductCount(Long categoryId, Integer productCount) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        categoryMapper.updateProductCount(categoryId, productCount != null ? productCount : 0);
        
        // 清除缓存
        redisTemplate.delete(CacheConstants.CATEGORIES_ALL);
        log.debug("品类产品数量更新，缓存已清除");
    }

    @Override
    public int countAll() {
        return categoryMapper.countAll();
    }

    @Override
    public int countAllGroups() {
        return categoryGroupMapper.countAll();
    }

    @Override
    public List<Category> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<CategoryDO> categoryDOs = categoryMapper.selectByIds(ids);
        return categoryConvertor.toEntityList(categoryDOs);
    }
}
