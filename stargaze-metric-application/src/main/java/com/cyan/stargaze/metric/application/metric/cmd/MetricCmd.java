package com.cyan.stargaze.metric.application.metric.cmd;

import com.cyan.stargaze.metric.enums.MeasureKind;
import com.cyan.stargaze.metric.enums.MetricType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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

    /** 业务名 */
    private String businessName;

    /** 指标类型 */
    @NotNull(message = "指标类型不能为空")
    private MetricType type;

    /** 聚合方式 */
    @NotNull(message = "聚合方式不能为空")
    private MeasureKind measureKind;

    /** 指标 DSL */
    @NotBlank(message = "指标 DSL 不能为空")
    private String dsl;

    /** 口径说明 */
    private String caliber;

    /** 负责人 ID */
    private String ownerId;

    /** 创建人(controller 透传) */
    private String createdBy;
}
