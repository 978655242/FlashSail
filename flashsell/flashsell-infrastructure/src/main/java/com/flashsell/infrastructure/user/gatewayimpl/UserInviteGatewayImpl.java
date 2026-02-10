package com.flashsell.infrastructure.user.gatewayimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flashsell.domain.user.entity.UserInvite;
import com.flashsell.domain.user.gateway.UserInviteGateway;
import com.flashsell.infrastructure.user.convertor.UserInviteConvertor;
import com.flashsell.infrastructure.user.dataobject.UserInviteDO;
import com.flashsell.infrastructure.user.mapper.UserInviteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户邀请网关实现
 */
@Repository
@RequiredArgsConstructor
public class UserInviteGatewayImpl implements UserInviteGateway {
    
    private final UserInviteMapper userInviteMapper;
    private final UserInviteConvertor userInviteConvertor;
    
    @Override
    public Optional<UserInvite> findByUserId(Long userId) {
        LambdaQueryWrapper<UserInviteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInviteDO::getUserId, userId);
        
        UserInviteDO inviteDO = userInviteMapper.selectOne(wrapper);
        return Optional.ofNullable(userInviteConvertor.toEntity(inviteDO));
    }
    
    @Override
    public Optional<UserInvite> findByInviteCode(String inviteCode) {
        LambdaQueryWrapper<UserInviteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInviteDO::getInviteCode, inviteCode);
        
        UserInviteDO inviteDO = userInviteMapper.selectOne(wrapper);
        return Optional.ofNullable(userInviteConvertor.toEntity(inviteDO));
    }
    
    @Override
    public UserInvite save(UserInvite userInvite) {
        UserInviteDO inviteDO = userInviteConvertor.toDO(userInvite);
        userInviteMapper.insert(inviteDO);
        userInvite.setId(inviteDO.getId());
        return userInvite;
    }
    
    @Override
    public void update(UserInvite userInvite) {
        UserInviteDO inviteDO = userInviteConvertor.toDO(userInvite);
        userInviteMapper.updateById(inviteDO);
    }
    
    @Override
    public boolean existsByInviteCode(String inviteCode) {
        LambdaQueryWrapper<UserInviteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInviteDO::getInviteCode, inviteCode);
        
        return userInviteMapper.selectCount(wrapper) > 0;
    }
}
