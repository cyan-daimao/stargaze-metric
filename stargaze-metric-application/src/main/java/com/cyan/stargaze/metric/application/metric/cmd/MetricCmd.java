package com.cyan.stargaze.metric.application.metric.cmd;

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

    /** 空间 ID */
    @NotBlank(message = "空间 ID 不能为空")
    private String workspaceId;

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
    private MeasureKind measureKind;

    /** 计算表达式（对应原 DSL） */
    @NotBlank(message = "计算表达式不能为空")
    private String expression;

    /** 口径说明（兼容旧字段） */
    private String caliber;

    /** 主数据集 ID */
    @NotBlank(message = "主数据集不能为空")
    private String primaryDatasetId;

    /** 主数据集度量字段 ID */
    @NotBlank(message = "主数据集度量字段不能为空")
    private String primaryFieldId;

    /** 辅助数据集 ID 列表 */
    private List<String> boundDatasetIds;

    /** 绑定的维度字段名列表 */
    private List<String> dimensions;

    /** 负责人 ID */
    private String ownerId;

    /** 创建人(controller 透传) */
    private String createdBy;

    /** 修改人(controller 透传) */
    private String updatedBy;
}
