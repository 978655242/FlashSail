package com.flashsell.infrastructure.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashsell.infrastructure.payment.dataobject.SubscriptionPlanDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订阅套餐 Mapper
 */
@Mapper
public interface SubscriptionPlanMapper extends BaseMapper<SubscriptionPlanDO> {
}
