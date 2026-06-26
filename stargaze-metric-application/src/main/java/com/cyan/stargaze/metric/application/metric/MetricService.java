package com.cyan.stargaze.metric.application.metric;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyan.stargaze.metric.application.metric.cmd.MetricBindingCmd;
import com.cyan.stargaze.metric.application.metric.cmd.MetricCmd;
import com.cyan.stargaze.metric.client.dto.CheckDimensionRequestDTO;
import com.cyan.stargaze.metric.client.dto.CheckDimensionResultDTO;
import com.cyan.stargaze.metric.client.dto.CheckNameResultDTO;
import com.cyan.stargaze.metric.client.dto.DatasetListItemDTO;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
import com.cyan.stargaze.metric.client.dto.MetricResolveDTO;
import com.cyan.stargaze.metric.client.dto.MetricSyncRequestDTO;
import com.cyan.stargaze.metric.client.dto.MetricSyncResultDTO;
import com.cyan.stargaze.metric.client.dto.PageDTO;
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

    PageDTO<MetricDTO> list(Integer page, Integer size, String keyword, String status, String folder);

    List<MetricDTO> list(boolean publishedOnly);

    void delete(String id);

    /** 发布:draft → published,记版本 */
    MetricDTO publish(String id);

    /** 下线:published → offline */
    MetricDTO offline(String id);

    /** 废弃:published → deprecated */
    MetricDTO deprecate(String id);

    /** 指标绑定数据集字段 */
    MetricBinding addBinding(MetricBindingCmd cmd);

    void removeBinding(String bindingId);

    List<MetricBinding> listBindings(String metricId);

    /**
     * 校验指标名称是否可用
     */
    CheckNameResultDTO checkName(String name, String excludeId);

    /**
     * 校验指标标识是否可用
     */
    CheckNameResultDTO checkCode(String code, String excludeId);

    /**
     * 跨数据集维度重复检测
     */
    CheckDimensionResultDTO checkDimensions(CheckDimensionRequestDTO request);

    /**
     * 获取可同步的数据集列表
     */
    PageDTO<DatasetListItemDTO> listSyncDatasets(Integer page, Integer size, String keyword, String type, String datasource);

    /**
     * 从数据集一键同步度量字段为指标
     */
    MetricSyncResultDTO syncFromDataset(MetricSyncRequestDTO request, String createdBy);

    /**
     * 解析指标到指定数据集(纯函数,query 调用)
     */
    MetricResolveDTO resolve(String metricId, String datasetId);

    /**
     * 校验指标×维度组合
     */
    ValidationResultDTO validate(List<String> metricIds, List<String> dimensionIds);
}
