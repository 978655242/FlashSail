package com.flashsell.infrastructure.user.gatewayimpl;

import com.flashsell.domain.user.entity.User;
import com.flashsell.domain.user.gateway.UserGateway;
import com.flashsell.infrastructure.user.convertor.UserConvertor;
import com.flashsell.infrastructure.user.dataobject.UserDO;
import com.flashsell.infrastructure.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户网关实现
 * 实现 UserGateway 接口，提供用户数据访问的具体实现
 */
@Repository
@RequiredArgsConstructor
public class UserGatewayImpl implements UserGateway {

    private final UserMapper userMapper;
    private final UserConvertor userConvertor;

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        UserDO userDO = userMapper.selectById(id);
        return Optional.ofNullable(userConvertor.toEntity(userDO));
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return Optional.empty();
        }
        UserDO userDO = userMapper.selectByPhone(phone);
        return Optional.ofNullable(userConvertor.toEntity(userDO));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return Optional.empty();
        }
        UserDO userDO = userMapper.selectByEmail(email);
        return Optional.ofNullable(userConvertor.toEntity(userDO));
    }

    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        UserDO userDO = userConvertor.toDataObject(user);
        LocalDateTime now = LocalDateTime.now();

        if (userDO.getId() == null) {
            // 新增用户
            userDO.setCreatedAt(now);
            userDO.setUpdatedAt(now);
            userMapper.insert(userDO);
        } else {
            // 更新用户
            userDO.setUpdatedAt(now);
            userMapper.updateById(userDO);
        }

        // 返回包含生成ID的用户实体
        return userConvertor.toEntity(userDO);
    }

    @Override
    public void update(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User and user ID cannot be null");
        }

        UserDO userDO = userConvertor.toDataObject(user);
        userDO.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(userDO);
    }

    @Override
    public boolean existsByPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return userMapper.countByPhone(phone) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return userMapper.countByEmail(email) > 0;
    }

    @Override
    public void softDelete(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        userMapper.softDeleteById(userId);
    }

    @Override
    public void updateSubscriptionLevel(Long userId, com.flashsell.domain.user.entity.SubscriptionLevel level, java.time.LocalDateTime expireDate) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        userMapper.updateSubscriptionLevel(userId, level, expireDate);
    }
}
