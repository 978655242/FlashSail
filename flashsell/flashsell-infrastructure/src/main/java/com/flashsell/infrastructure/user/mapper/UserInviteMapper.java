package com.flashsell.infrastructure.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashsell.infrastructure.user.dataobject.UserInviteDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户邀请Mapper
 */
@Mapper
public interface UserInviteMapper extends BaseMapper<UserInviteDO> {
}
