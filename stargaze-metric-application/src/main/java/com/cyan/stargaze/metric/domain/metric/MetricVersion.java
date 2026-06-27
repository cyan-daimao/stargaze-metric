package com.cyan.stargaze.metric.domain.metric;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.SilentException;
import com.cyan.stargaze.metric.domain.metric.repository.MetricVersionRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * 指标版本(口径变更审计)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricVersion {

    /** 主键 */
    private String id;

    /** 指标 ID */
    private String metricId;

    /** 指标业务编码 */
    private String metricCode;

    /** 版本号 */
    private Integer version;

    /** DSL 快照 */
    private String dsl;

    /** 来源快照 */
    private String sourceSnapshot;

    /** 变更日志 */
    private String changeLog;

    /** 创建人工号 */
    private String createdBy;

    /** 修改人 */
    private String updatedBy;

    /** 创建时间 */
    private OffsetDateTime createdAt;

    /** 更新时间 */
    private OffsetDateTime updatedAt;

    /** 逻辑删除时间 */
    private OffsetDateTime deletedAt;

    /**
     * 校验版本合法性。
     */
    public void validate() {
        Assert.notBlank(this.metricId, new SilentException("指标 ID 不能为空"));
        Assert.notBlank(this.metricCode, new SilentException("指标编码不能为空"));
        Assert.notBlank(this.dsl, new SilentException("DSL 快照不能为空"));
        Assert.isTrue(this.version != null && this.version > 0, new SilentException("版本号必须大于 0"));
    }

    /**
     * 保存版本记录。
     */
    public MetricVersion save(MetricVersionRepository repo) {
        this.validate();
        return repo.save(this);
    }
}
