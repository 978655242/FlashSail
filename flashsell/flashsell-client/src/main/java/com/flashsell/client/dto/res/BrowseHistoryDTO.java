package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 浏览历史 DTO
 * 
 * Requirements: 14.2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrowseHistoryDTO {
    
    /**
     * 产品信息
     */
    private ProductItemRes product;
    
    /**
     * 浏览时间
     */
    private LocalDateTime browsedAt;
}
