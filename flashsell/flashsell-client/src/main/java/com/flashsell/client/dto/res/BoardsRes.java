package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 看板列表响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardsRes {

    /**
     * 看板列表
     */
    private List<BoardRes> boards;

    /**
     * 用户最大看板数
     */
    private Integer maxBoards;

    /**
     * 当前看板数量
     */
    private Integer currentCount;
}
