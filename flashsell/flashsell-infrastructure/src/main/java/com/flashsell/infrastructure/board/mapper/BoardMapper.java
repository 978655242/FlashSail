package com.flashsell.infrastructure.board.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashsell.infrastructure.board.dataobject.BoardDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 看板 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供基础 CRUD 操作
 */
@Mapper
public interface BoardMapper extends BaseMapper<BoardDO> {

    /**
     * 查询用户的所有看板（不包含已删除的）
     *
     * @param userId 用户ID
     * @return 看板列表
     */
    @Select("SELECT * FROM boards WHERE user_id = #{userId} AND deleted_at IS NULL ORDER BY created_at DESC")
    List<BoardDO> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的看板（分页，不包含已删除的）
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 看板列表
     */
    @Select("SELECT * FROM boards WHERE user_id = #{userId} AND deleted_at IS NULL ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<BoardDO> selectByUserIdWithPagination(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计用户的看板数量（不包含已删除的）
     *
     * @param userId 用户ID
     * @return 看板数量
     */
    @Select("SELECT COUNT(*) FROM boards WHERE user_id = #{userId} AND deleted_at IS NULL")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否拥有指定名称的看板（不包含已删除的）
     *
     * @param userId 用户ID
     * @param name 看板名称
     * @return 存在数量
     */
    @Select("SELECT COUNT(*) FROM boards WHERE user_id = #{userId} AND name = #{name} AND deleted_at IS NULL")
    int countByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);

    /**
     * 软删除看板
     *
     * @param id 看板ID
     * @return 更新的行数
     */
    @Update("UPDATE boards SET deleted_at = CURRENT_TIMESTAMP WHERE id = #{id} AND deleted_at IS NULL")
    int softDeleteById(@Param("id") Long id);

    /**
     * 根据ID查询看板（不包含已删除的）
     *
     * @param id 看板ID
     * @return 看板数据对象
     */
    @Select("SELECT * FROM boards WHERE id = #{id} AND deleted_at IS NULL")
    BoardDO selectByIdNotDeleted(@Param("id") Long id);
}
