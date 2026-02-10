package com.flashsell.domain.category.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 品类组领域实体
 * 代表四大类目：工业用品、节日装饰、家居生活与百货、数码配件与小家电
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryGroup {

    /**
     * 品类组ID
     */
    private Long id;

    /**
     * 品类组名称
     */
    private String name;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 该组下的品类列表
     */
    private List<Category> categories;

    // ==================== 领域行为 ====================

    /**
     * 获取该组下的品类数量
     *
     * @return 品类数量
     */
    public int getCategoryCount() {
        return categories != null ? categories.size() : 0;
    }

    /**
     * 检查是否包含指定品类
     *
     * @param categoryId 品类ID
     * @return 是否包含
     */
    public boolean containsCategory(Long categoryId) {
        if (categories == null || categoryId == null) {
            return false;
        }
        return categories.stream()
                .anyMatch(c -> categoryId.equals(c.getId()));
    }
}
