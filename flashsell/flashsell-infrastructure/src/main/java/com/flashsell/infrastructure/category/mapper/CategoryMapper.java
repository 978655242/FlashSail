package com.flashsell.infrastructure.category.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashsell.infrastructure.category.dataobject.CategoryDO;

/**
 * 品类 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供基础 CRUD 操作
 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryDO> {

    /**
     * 根据品类组ID查询品类列表
     *
     * @param groupId 品类组ID
     * @return 品类列表
     */
    @Select("SELECT * FROM categories WHERE group_id = #{groupId} ORDER BY id ASC")
    List<CategoryDO> selectByGroupId(@Param("groupId") Long groupId);

    /**
     * 根据Amazon品类ID查询品类
     *
     * @param amazonCategoryId Amazon品类ID
     * @return 品类数据对象
     */
    @Select("SELECT * FROM categories WHERE amazon_category_id = #{amazonCategoryId}")
    CategoryDO selectByAmazonCategoryId(@Param("amazonCategoryId") String amazonCategoryId);

    /**
     * 查询所有品类
     *
     * @return 品类列表
     */
    @Select("SELECT * FROM categories ORDER BY group_id ASC, id ASC")
    List<CategoryDO> selectAllOrdered();

    /**
     * 更新品类产品数量
     *
     * @param categoryId 品类ID
     * @param productCount 产品数量
     * @return 影响行数
     */
    @Update("UPDATE categories SET product_count = #{productCount} WHERE id = #{categoryId}")
    int updateProductCount(@Param("categoryId") Long categoryId, @Param("productCount") Integer productCount);

    /**
     * 获取品类总数
     *
     * @return 品类总数
     */
    @Select("SELECT COUNT(*) FROM categories")
    int countAll();

    /**
     * 根据ID列表批量查询品类
     *
     * @param ids 品类ID列表
     * @return 品类列表
     */
    @Select("<script>" +
            "SELECT * FROM categories WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<CategoryDO> selectByIds(@Param("ids") List<Long> ids);
}
