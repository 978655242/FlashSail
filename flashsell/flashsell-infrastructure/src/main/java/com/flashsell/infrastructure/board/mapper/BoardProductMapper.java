package com.flashsell.infrastructure.board.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashsell.infrastructure.board.dataobject.BoardProductDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 看板产品关联 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供基础 CRUD 操作
 */
@Mapper
public interface BoardProductMapper extends BaseMapper<BoardProductDO> {

    /**
     * 获取看板中的产品ID列表
     *
     * @param boardId 看板ID
     * @return 产品ID列表
     */
    @Select("SELECT product_id FROM board_products WHERE board_id = #{boardId} ORDER BY added_at DESC")
    List<Long> selectProductIdsByBoardId(@Param("boardId") Long boardId);

    /**
     * 统计看板中的产品数量
     *
     * @param boardId 看板ID
     * @return 产品数量
     */
    @Select("SELECT COUNT(*) FROM board_products WHERE board_id = #{boardId}")
    int countByBoardId(@Param("boardId") Long boardId);

    /**
     * 检查产品是否在看板中
     *
     * @param boardId 看板ID
     * @param productId 产品ID
     * @return 存在数量
     */
    @Select("SELECT COUNT(*) FROM board_products WHERE board_id = #{boardId} AND product_id = #{productId}")
    int countByBoardIdAndProductId(@Param("boardId") Long boardId, @Param("productId") Long productId);

    /**
     * 根据看板ID和产品ID查询关联记录
     *
     * @param boardId 看板ID
     * @param productId 产品ID
     * @return 关联记录
     */
    @Select("SELECT * FROM board_products WHERE board_id = #{boardId} AND product_id = #{productId}")
    BoardProductDO selectByBoardIdAndProductId(@Param("boardId") Long boardId, @Param("productId") Long productId);

    /**
     * 从看板移除产品
     *
     * @param boardId 看板ID
     * @param productId 产品ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM board_products WHERE board_id = #{boardId} AND product_id = #{productId}")
    int deleteByBoardIdAndProductId(@Param("boardId") Long boardId, @Param("productId") Long productId);

    /**
     * 删除看板的所有产品关联
     *
     * @param boardId 看板ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM board_products WHERE board_id = #{boardId}")
    int deleteByBoardId(@Param("boardId") Long boardId);

    /**
     * 获取看板中的所有产品关联记录
     *
     * @param boardId 看板ID
     * @return 关联记录列表
     */
    @Select("SELECT * FROM board_products WHERE board_id = #{boardId} ORDER BY added_at DESC")
    List<BoardProductDO> selectByBoardId(@Param("boardId") Long boardId);
}
