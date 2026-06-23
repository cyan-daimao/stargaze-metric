package com.cyan.stargaze.metric.application.metric;

import com.cyan.stargaze.metric.application.metric.cmd.MetricBindingCmd;
import com.cyan.stargaze.metric.application.metric.cmd.MetricCmd;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
import com.cyan.stargaze.metric.client.dto.MetricResolveDTO;
import com.cyan.stargaze.metric.client.dto.ValidationResultDTO;
import com.cyan.stargaze.metric.domain.metric.MetricBinding;

import java.util.List;

/**
 * 指标应用服务。
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MetricService {

    MetricDTO create(MetricCmd cmd);

    MetricDTO update(MetricCmd cmd);

    MetricDTO findById(String id);

    List<MetricDTO> list(String workspaceId, boolean publishedOnly);

    void delete(String id);

    /** 发布:draft → published,记版本 */
    MetricDTO publish(String id);

    /** 废弃 */
    MetricDTO deprecate(String id);

    /** 指标绑定数据集字段 */
    MetricBinding addBinding(MetricBindingCmd cmd);

    void removeBinding(String bindingId);

    List<MetricBinding> listBindings(String metricId);

    /**
     * 解析指标到指定数据集(纯函数,query 调用)
     */
    MetricResolveDTO resolve(String metricId, String datasetId);

    /**
     * 校验指标×维度组合
     */
    ValidationResultDTO validate(List<String> metricIds, List<String> dimensionIds);
}
