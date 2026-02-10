package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 爆品推荐列表响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotProductsRes {

    /**
     * 推荐日期
     */
    private LocalDate date;

    /**
     * 爆品分组列表
     */
    private List<HotProductGroup> groups;

    /**
     * 总数
     */
    private Integer total;
}
