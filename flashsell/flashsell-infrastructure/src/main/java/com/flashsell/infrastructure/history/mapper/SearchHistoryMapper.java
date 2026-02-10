package com.flashsell.infrastructure.history.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashsell.infrastructure.history.dataobject.SearchHistoryDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜索历史 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供基础 CRUD 操作
 */
@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistoryDO> {
    
    /**
     * 根据用户ID查询搜索历史（分页，按时间倒序）
     * 
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 搜索历史列表
     */
    @Select("SELECT * FROM search_history WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<SearchHistoryDO> selectByUserId(@Param("userId") Long userId, 
                                          @Param("offset") int offset, 
                                          @Param("limit") int limit);
    
    /**
     * 根据用户ID查询最近的搜索历史
     * 
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 搜索历史列表
     */
    @Select("SELECT * FROM search_history WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<SearchHistoryDO> selectRecentByUserId(@Param("userId") Long userId, 
                                                @Param("limit") int limit);
    
    /**
     * 统计用户的搜索历史总数
     * 
     * @param userId 用户ID
     * @return 总数
     */
    @Select("SELECT COUNT(*) FROM search_history WHERE user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);
    
    /**
     * 根据ID和用户ID删除搜索历史
     * 
     * @param id 搜索历史ID
     * @param userId 用户ID
     * @return 影响行数
     */
    @Delete("DELETE FROM search_history WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    
    /**
     * 删除用户的所有搜索历史
     * 
     * @param userId 用户ID
     * @return 影响行数
     */
    @Delete("DELETE FROM search_history WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 删除指定时间之前的搜索历史
     * 
     * @param beforeTime 时间点
     * @return 影响行数
     */
    @Delete("DELETE FROM search_history WHERE created_at < #{beforeTime}")
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);
}
