package com.flashsell.client.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导出数据报告请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportReportReq {

    /**
     * 导出类型：FAVORITES-收藏, SEARCH_HISTORY-搜索历史, ALL-全部
     */
    @NotBlank(message = "导出类型不能为空")
    private String type;
}
