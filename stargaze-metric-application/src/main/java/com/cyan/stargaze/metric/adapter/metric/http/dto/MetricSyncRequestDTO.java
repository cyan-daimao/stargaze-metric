package com.cyan.stargaze.metric.adapter.metric.http.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 一键同步请求。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricSyncRequestDTO {

    /** 工作空间 ID */
    private String workspaceId;

    /** 待同步的数据集 ID 列表 */
    @NotEmpty(message = "数据集不能为空")
    private List<String> datasetIds;
}
