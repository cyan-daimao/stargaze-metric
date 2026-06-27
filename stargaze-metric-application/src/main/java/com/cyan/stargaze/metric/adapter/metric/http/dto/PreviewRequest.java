package com.cyan.stargaze.metric.adapter.metric.http.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 指标预览请求。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PreviewRequest {

    /** 预览模式: singleValue */
    @NotBlank(message = "预览模式不能为空")
    private String previewMode;

    /** 业务日期: latest 或 yyyy-MM-dd */
    @NotBlank(message = "业务日期不能为空")
    private String bizDate;
}
