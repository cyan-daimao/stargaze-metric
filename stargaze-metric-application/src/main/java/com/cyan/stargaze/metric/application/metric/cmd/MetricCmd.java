package com.cyan.stargaze.metric.application.metric.cmd;

import com.cyan.stargaze.metric.enums.Freshness;
import com.cyan.stargaze.metric.enums.MetricDslKind;
import com.cyan.stargaze.metric.enums.MetricFormat;
import com.cyan.stargaze.metric.enums.MetricSourceType;
import com.cyan.stargaze.metric.enums.QueryMode;
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

    /** 指标业务编码(全局唯一) */
    @NotBlank(message = "指标业务编码不能为空")
    private String metricCode;

    /** 指标名称(全局唯一) */
    @NotBlank(message = "指标名称不能为空")
    private String name;

    /** 指标标识(英文代码,全局唯一) */
    @NotBlank(message = "指标标识不能为空")
    private String code;

    /** 业务定义/口径说明 */
    private String description;

    /** 所属目录 */
    private String folder;

    /** 数据格式 */
    private MetricFormat format;

    /** 来源类型 */
    @NotNull(message = "来源类型不能为空")
    private MetricSourceType sourceType;

    /** 来源编码 */
    @NotBlank(message = "来源编码不能为空")
    private String sourceCode;

    /** 来源名称 */
    private String sourceName;

    /** 查询能力 */
    @NotNull(message = "查询能力不能为空")
    private QueryMode queryMode;

    /** 数据新鲜度 */
    private Freshness freshness;

    /** DSL 类型 */
    @NotNull(message = "DSL 类型不能为空")
    private MetricDslKind dslKind;

    /** 指标 DSL JSON */
    @NotBlank(message = "指标 DSL 不能为空")
    private String dsl;

    /** 来源解析快照 JSON */
    private String sourceSnapshot;

    /** 来源能力声明 JSON */
    private String supports;

    /** 小数位精度 */
    private Integer precision;

    /** 可关联维度 */
    private List<MetricDimensionRef> relatedDimensions;

    /** 负责人 ID */
    private String ownerId;

    /** 创建人(controller 透传) */
    private String createdBy;

    /** 修改人(controller 透传) */
    private String updatedBy;
}
