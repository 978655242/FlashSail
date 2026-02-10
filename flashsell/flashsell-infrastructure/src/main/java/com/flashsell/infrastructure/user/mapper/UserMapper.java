package com.flashsell.infrastructure.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashsell.infrastructure.user.dataobject.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供基础 CRUD 操作
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

    /**
     * 根据手机号查询用户（包含已删除的）
     *
     * @param phone 手机号
     * @return 用户数据对象
     */
    @Select("SELECT * FROM users WHERE phone = #{phone} AND deleted_at IS NULL")
    UserDO selectByPhone(@Param("phone") String phone);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户数据对象
     */
    @Select("SELECT * FROM users WHERE email = #{email} AND deleted_at IS NULL")
    UserDO selectByEmail(@Param("email") String email);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return 存在数量
     */
    @Select("SELECT COUNT(*) FROM users WHERE phone = #{phone} AND deleted_at IS NULL")
    int countByPhone(@Param("phone") String phone);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 存在数量
     */
    @Select("SELECT COUNT(*) FROM users WHERE email = #{email} AND deleted_at IS NULL")
    int countByEmail(@Param("email") String email);

    /**
     * 软删除用户
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE users SET deleted_at = NOW(), updated_at = NOW() WHERE id = #{userId} AND deleted_at IS NULL")
    int softDeleteById(@Param("userId") Long userId);

    /**
     * 更新用户订阅级别
     *
     * @param userId     用户ID
     * @param level      订阅级别
     * @param expireDate 过期时间
     * @return 影响行数
     */
    @Update("UPDATE users SET subscription_level = #{level}::subscription_level, subscription_expire_date = #{expireDate}, updated_at = NOW() WHERE id = #{userId} AND deleted_at IS NULL")
    int updateSubscriptionLevel(@Param("userId") Long userId,
                                  @Param("level") com.flashsell.domain.user.entity.SubscriptionLevel level,
                                  @Param("expireDate") java.time.LocalDateTime expireDate);
}
