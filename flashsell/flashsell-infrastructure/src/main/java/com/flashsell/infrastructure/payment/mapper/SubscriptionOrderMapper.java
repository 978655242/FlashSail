package com.flashsell.infrastructure.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashsell.infrastructure.payment.dataobject.SubscriptionOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订阅订单 Mapper
 */
@Mapper
public interface SubscriptionOrderMapper extends BaseMapper<SubscriptionOrderDO> {

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单数据对象
     */
    @Select("SELECT * FROM subscription_orders WHERE order_no = #{orderNo} AND deleted_at IS NULL")
    SubscriptionOrderDO selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据用户ID查询订单列表
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    @Select("SELECT * FROM subscription_orders WHERE user_id = #{userId} AND deleted_at IS NULL ORDER BY created_at DESC")
    List<SubscriptionOrderDO> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的最新有效订单
     *
     * @param userId 用户ID
     * @return 订单数据对象
     */
    @Select("SELECT * FROM subscription_orders WHERE user_id = #{userId} AND status = 'SUCCESS' AND subscribe_to > NOW() AND deleted_at IS NULL ORDER BY subscribe_to DESC LIMIT 1")
    SubscriptionOrderDO selectLatestActiveOrder(@Param("userId") Long userId);
}
