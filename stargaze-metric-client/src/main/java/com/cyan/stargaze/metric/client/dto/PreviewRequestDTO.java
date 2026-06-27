package com.cyan.stargaze.metric.client.dto;

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
public class PreviewRequestDTO {

    /** 预览模式: singleValue */
    private String previewMode;

    /** 业务日期: latest 或 yyyy-MM-dd */
    private String bizDate;
}
