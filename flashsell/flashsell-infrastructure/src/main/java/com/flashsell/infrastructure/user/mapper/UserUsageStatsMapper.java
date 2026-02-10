package com.flashsell.infrastructure.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashsell.infrastructure.user.dataobject.UserUsageStatsDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户使用统计Mapper
 */
@Mapper
public interface UserUsageStatsMapper extends BaseMapper<UserUsageStatsDO> {
}
