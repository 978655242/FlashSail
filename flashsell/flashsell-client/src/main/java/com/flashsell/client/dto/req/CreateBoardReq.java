package com.flashsell.client.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建看板请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBoardReq {

    /**
     * 看板名称
     */
    @NotBlank(message = "看板名称不能为空")
    @Size(max = 100, message = "看板名称不能超过100个字符")
    private String name;
}
