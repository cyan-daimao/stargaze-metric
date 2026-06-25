package com.cyan.stargaze.metric.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 数据集列表项 DTO（指标平台视角）。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DatasetListItemDTO {

    private String id;
    private String name;
    private String code;
    private String type;
    private String datasource;
    private String schema;
    private Integer fields;
    private String rows;
    private String status;
    private Integer metricCount;
    private Integer dimensionCount;
    private String updateTime;
}
