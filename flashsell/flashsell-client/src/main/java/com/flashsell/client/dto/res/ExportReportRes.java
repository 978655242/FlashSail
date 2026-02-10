package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 导出报告响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportReportRes {

    /**
     * 下载URL
     */
    private String downloadUrl;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
}
