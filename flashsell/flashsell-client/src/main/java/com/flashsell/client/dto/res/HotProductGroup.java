package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 爆品分组（按品类组）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotProductGroup {

    /**
     * 品类组信息
     */
    private CategoryGroupRes categoryGroup;

    /**
     * 爆品列表
     */
    private List<HotProductDTO> products;
}
