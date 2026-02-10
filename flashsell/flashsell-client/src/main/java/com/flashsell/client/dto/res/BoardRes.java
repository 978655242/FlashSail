package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 看板响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardRes {

    /**
     * 看板ID
     */
    private Long id;

    /**
     * 看板名称
     */
    private String name;

    /**
     * 产品数量
     */
    private Integer productCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
