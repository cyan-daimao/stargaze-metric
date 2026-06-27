package com.cyan.stargaze.metric.adapter.metric.http.dto;

import com.cyan.stargaze.metric.enums.Freshness;
import com.cyan.stargaze.metric.enums.MetricDslKind;
import com.cyan.stargaze.metric.enums.MetricFormat;
import com.cyan.stargaze.metric.enums.MetricSourceType;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.enums.QueryMode;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * 指标详情。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricDetailDTO {

    /** 指标业务编码 */
    private String metricCode;

    /** 指标名称 */
    private String metricName;

    /** 业务描述 */
    private String description;

    /** 所属目录 */
    private String folder;

    /** 数据格式 */
    private MetricFormat format;

    /** 状态 */
    private MetricStatus status;

    /** 来源类型 */
    private MetricSourceType sourceType;

    /** 来源编码 */
    private String sourceCode;

    /** 来源名称 */
    private String sourceName;

    /** 来源类型展示标签 */
    private String sourceTypeLabel;

    /** 查询能力 */
    private QueryMode queryMode;

    /** 数据新鲜度 */
    private Freshness freshness;

    /** DSL 类型 */
    private MetricDslKind dslKind;

    /** 指标 DSL AST(已解析为对象) */
    private Map<String, Object> dsl;

    /** 来源绑定信息 */
    private MetricSourceBindingDTO sourceBinding;

    /** 来源解析快照 JSON */
    private String sourceSnapshot;

    /** 来源能力声明 JSON */
    private String supports;

    /** 小数位精度 */
    private Integer precision;

    /** 可关联维度 */
    private List<String> relatedDimensions;

    /** 计算逻辑摘要 */
    private String logicSummary;

    /** SQL 预览 */
    private String sqlPreview;

    /** API 点查计划(已解析为对象) */
    private Map<String, Object> apiLookupPlan;

    /** 创建人工号 */
    private String createdBy;

    /** 更新人工号 */
    private String updatedBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private OffsetDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private OffsetDateTime updatedAt;
}
