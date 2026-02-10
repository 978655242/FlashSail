package com.flashsell.infrastructure.category.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashsell.infrastructure.category.dataobject.CategoryGroupDO;

/**
 * 品类组 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供基础 CRUD 操作
 */
@Mapper
public interface CategoryGroupMapper extends BaseMapper<CategoryGroupDO> {

    /**
     * 查询所有品类组，按排序序号排序
     *
     * @return 品类组列表
     */
    @Select("SELECT * FROM category_groups ORDER BY sort_order ASC")
    List<CategoryGroupDO> selectAllOrdered();

    /**
     * 获取品类组总数
     *
     * @return 品类组总数
     */
    @Select("SELECT COUNT(*) FROM category_groups")
    int countAll();
}
