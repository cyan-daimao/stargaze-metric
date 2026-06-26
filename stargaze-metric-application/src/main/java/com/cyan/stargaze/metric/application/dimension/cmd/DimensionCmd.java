package com.cyan.stargaze.metric.application.dimension.cmd;

import com.cyan.stargaze.metric.enums.SemanticType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 维度创建/更新命令。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DimensionCmd {

    /** 主键(更新必填) */
    private String id;

    /** 维度名 */
    @NotBlank(message = "维度名不能为空")
    private String name;

    /** 业务名 */
    private String businessName;

    /** 语义类型 */
    @NotNull(message = "语义类型不能为空")
    private SemanticType semanticType;

    /** 字典 ID */
    private String dictionaryId;

    /** 格式(jsonb 字符串) */
    private String format;

    /** 负责人 ID */
    private String ownerId;

    /** 创建人(controller 透传) */
    private String createdBy;
}
