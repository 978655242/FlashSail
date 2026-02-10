package com.flashsell.infrastructure.history.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashsell.infrastructure.history.dataobject.BrowseHistoryDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 浏览历史 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供基础 CRUD 操作
 */
@Mapper
public interface BrowseHistoryMapper extends BaseMapper<BrowseHistoryDO> {
    
    /**
     * 根据用户ID和产品ID查询浏览历史
     * 
     * @param userId 用户ID
     * @param productId 产品ID
     * @return 浏览历史
     */
    @Select("SELECT * FROM browse_history WHERE user_id = #{userId} AND product_id = #{productId}")
    BrowseHistoryDO selectByUserIdAndProductId(@Param("userId") Long userId, 
                                                @Param("productId") Long productId);
    
    /**
     * 根据用户ID查询浏览历史（分页，按浏览时间倒序）
     * 
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 浏览历史列表
     */
    @Select("SELECT * FROM browse_history WHERE user_id = #{userId} " +
            "ORDER BY browsed_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<BrowseHistoryDO> selectByUserId(@Param("userId") Long userId, 
                                          @Param("offset") int offset, 
                                          @Param("limit") int limit);
    
    /**
     * 根据用户ID查询最近的浏览历史
     * 
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 浏览历史列表
     */
    @Select("SELECT * FROM browse_history WHERE user_id = #{userId} " +
            "ORDER BY browsed_at DESC LIMIT #{limit}")
    List<BrowseHistoryDO> selectRecentByUserId(@Param("userId") Long userId, 
                                                @Param("limit") int limit);
    
    /**
     * 统计用户的浏览历史总数
     * 
     * @param userId 用户ID
     * @return 总数
     */
    @Select("SELECT COUNT(*) FROM browse_history WHERE user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);
    
    /**
     * 更新浏览时间
     * 
     * @param userId 用户ID
     * @param productId 产品ID
     * @param browsedAt 浏览时间
     * @return 影响行数
     */
    @Update("UPDATE browse_history SET browsed_at = #{browsedAt} " +
            "WHERE user_id = #{userId} AND product_id = #{productId}")
    int updateBrowsedAt(@Param("userId") Long userId, 
                        @Param("productId") Long productId, 
                        @Param("browsedAt") LocalDateTime browsedAt);
    
    /**
     * 根据用户ID和产品ID删除浏览历史
     * 
     * @param userId 用户ID
     * @param productId 产品ID
     * @return 影响行数
     */
    @Delete("DELETE FROM browse_history WHERE user_id = #{userId} AND product_id = #{productId}")
    int deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
    
    /**
     * 删除用户的所有浏览历史
     * 
     * @param userId 用户ID
     * @return 影响行数
     */
    @Delete("DELETE FROM browse_history WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 删除指定时间之前的浏览历史
     * 
     * @param beforeTime 时间点
     * @return 影响行数
     */
    @Delete("DELETE FROM browse_history WHERE browsed_at < #{beforeTime}")
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);
}
