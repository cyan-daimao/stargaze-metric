package com.cyan.stargaze.metric.application.metric.cmd;

import com.cyan.stargaze.metric.client.dto.MetricDimensionRefDTO;
import com.cyan.stargaze.metric.enums.MeasureKind;
import com.cyan.stargaze.metric.enums.MetricFormat;
import com.cyan.stargaze.metric.enums.MetricType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 指标创建/更新命令。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricCmd {

    /** 主键(更新必填) */
    private String id;

    /** 指标名 */
    @NotBlank(message = "指标名不能为空")
    private String name;

    /** 指标标识（英文代码） */
    @NotBlank(message = "指标标识不能为空")
    private String code;

    /** 业务名 */
    private String businessName;

    /** 业务定义/口径说明 */
    private String description;

    /** 所属目录 */
    private String folder;

    /** 数据格式 */
    private MetricFormat format;

    /** 指标类型 */
    @NotNull(message = "指标类型不能为空")
    private MetricType type;

    /** 聚合方式 */
    @NotNull(message = "聚合方式不能为空")
    private MeasureKind aggregation;

    /** 指标 DSL */
    @NotBlank(message = "计算表达式不能为空")
    private String dsl;

    /** 过滤条件 */
    private String filterCondition;

    /** 小数位 */
    private Integer precision;

    /** 主数据集 ID */
    @NotBlank(message = "主数据集不能为空")
    private String primaryDatasetId;

    /** 主数据集度量字段 ID */
    @NotBlank(message = "主数据集度量字段不能为空")
    private String primaryFieldId;

    /** 辅助数据集 ID 列表 */
    private List<String> secondaryDatasetIds;

    /** 绑定维度引用列表 */
    private List<MetricDimensionRefDTO> dimensions;

    /** 负责人 ID */
    private String ownerId;

    /** 创建人(controller 透传) */
    private String createdBy;

    /** 修改人(controller 透传) */
    private String updatedBy;
}
