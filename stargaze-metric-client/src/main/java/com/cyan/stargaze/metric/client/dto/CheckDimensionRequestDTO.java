package com.cyan.stargaze.metric.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 维度重复检测请求。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CheckDimensionRequestDTO {

    /** 主数据集 ID */
    private String primaryDatasetId;

    /** 辅助数据集 ID 列表 */
    private List<String> secondaryDatasetIds;
}
