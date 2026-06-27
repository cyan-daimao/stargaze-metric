package com.cyan.stargaze.metric.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 一键同步结果。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricSyncResultDTO {

    /** 同步的数据集 ID */
    private String datasetId;

    /** 成功创建的指标数量 */
    private Integer created;

    /** 成功创建的维度数量 */
    private Integer dimensionCreated;

    /** 成功创建的维度绑定数量 */
    private Integer dimensionBindingCreated;

    /** 重复跳过的指标 */
    private List<DuplicateMetricDTO> duplicates;

    /** 重复跳过的维度 */
    private List<DuplicateDimensionDTO> dimensionDuplicates;

    /** 创建的指标列表 */
    private List<MetricDTO> metrics;

    /** 创建的维度列表 */
    private List<DimensionDTO> dimensions;

    /**
     * 重复指标项。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class DuplicateMetricDTO {
        /** 新指标名 */
        private String newName;
        /** 已存在指标名 */
        private String existingName;
        /** 已存在指标 ID */
        private String existingId;
    }

    /**
     * 重复维度项。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class DuplicateDimensionDTO {
        /** 新维度名 */
        private String newName;
        /** 已存在维度名 */
        private String existingName;
        /** 已存在维度 ID */
        private String existingId;
    }
}
