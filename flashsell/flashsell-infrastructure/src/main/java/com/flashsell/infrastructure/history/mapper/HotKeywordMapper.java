package com.flashsell.infrastructure.history.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashsell.infrastructure.history.dataobject.HotKeywordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 热门关键词 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供基础 CRUD 操作
 */
@Mapper
public interface HotKeywordMapper extends BaseMapper<HotKeywordDO> {
    
    /**
     * 根据关键词和日期查询
     * 
     * @param keyword 关键词
     * @param statDate 统计日期
     * @return 热门关键词
     */
    @Select("SELECT * FROM hot_keywords WHERE keyword = #{keyword} AND stat_date = #{statDate}")
    HotKeywordDO selectByKeywordAndDate(@Param("keyword") String keyword, 
                                         @Param("statDate") LocalDate statDate);
    
    /**
     * 查询指定日期的热门关键词（按搜索次数降序）
     * 
     * @param statDate 统计日期
     * @param limit 数量限制
     * @return 热门关键词列表
     */
    @Select("SELECT * FROM hot_keywords WHERE stat_date = #{statDate} " +
            "ORDER BY search_count DESC LIMIT #{limit}")
    List<HotKeywordDO> selectTopByDate(@Param("statDate") LocalDate statDate, 
                                        @Param("limit") int limit);
    
    /**
     * 查询指定日期范围的热门关键词
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 热门关键词列表
     */
    @Select("SELECT * FROM hot_keywords WHERE stat_date >= #{startDate} AND stat_date <= #{endDate} " +
            "ORDER BY stat_date DESC, search_count DESC")
    List<HotKeywordDO> selectByDateRange(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
}
