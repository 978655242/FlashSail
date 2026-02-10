package com.flashsell.infrastructure.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Bright Data API 响应 DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrightDataResponse {

    /**
     * 响应状态
     */
    private String status;

    /**
     * 响应内容（用于 scrape_as_markdown）
     */
    private String content;

    /**
     * 响应数据（用于结构化数据）
     */
    private List<Map<String, Object>> data;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 请求 ID
     */
    @JsonProperty("request_id")
    private String requestId;

    /**
     * 检查响应是否成功
     */
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status) || (error == null && (data != null || content != null));
    }
}
