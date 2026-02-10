package com.flashsell.domain.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 数据新鲜度信息
 * 用于标注数据的时效性状态
 * 
 * Requirements: 15.5, 15.7
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataFreshness {

    /**
     * 数据状态
     */
    private DataStatus status;

    /**
     * 数据获取/缓存时间
     */
    private LocalDateTime fetchedAt;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 数据状态枚举
     */
    public enum DataStatus {
        /**
         * 新鲜数据（实时获取）
         */
        FRESH,

        /**
         * 过期数据（来自缓存）
         */
        STALE,

        /**
         * 无数据
         */
        EMPTY
    }

    /**
     * 创建新鲜数据状态
     */
    public static DataFreshness fresh() {
        return DataFreshness.builder()
                .status(DataStatus.FRESH)
                .fetchedAt(LocalDateTime.now())
                .message(null)
                .build();
    }

    /**
     * 创建过期数据状态
     */
    public static DataFreshness stale(LocalDateTime cachedAt, String message) {
        return DataFreshness.builder()
                .status(DataStatus.STALE)
                .fetchedAt(cachedAt)
                .message(message != null ? message : "数据来自缓存，可能已过期")
                .build();
    }

    /**
     * 创建空数据状态
     */
    public static DataFreshness empty() {
        return DataFreshness.builder()
                .status(DataStatus.EMPTY)
                .fetchedAt(null)
                .message("无可用数据")
                .build();
    }

    /**
     * 是否为新鲜数据
     */
    public boolean isFresh() {
        return status == DataStatus.FRESH;
    }

    /**
     * 是否为过期数据
     */
    public boolean isStale() {
        return status == DataStatus.STALE;
    }

    /**
     * 是否为空数据
     */
    public boolean isEmpty() {
        return status == DataStatus.EMPTY;
    }

    /**
     * 获取状态字符串
     */
    public String getStatusString() {
        return status != null ? status.name() : "UNKNOWN";
    }
}
