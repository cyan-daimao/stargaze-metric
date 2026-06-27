package com.cyan.stargaze.metric.adapter.metric.rpc;

import com.cyan.arch.common.api.Response;
import com.cyan.stargaze.metric.adapter.MetricAdapterConvert;
import com.cyan.stargaze.metric.application.metric.MetricService;
import com.cyan.stargaze.metric.client.MetricClient;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
import com.cyan.stargaze.metric.client.dto.MetricResolveDTO;
import com.cyan.stargaze.metric.client.dto.ResolveBatchRequestDTO;
import com.cyan.stargaze.metric.client.dto.ValidationResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 指标 RPC 控制器:实现 {@link MetricClient} 契约(/rpc/v1/metric)。
 * <p>
 * 供 query/dashboard 依赖。resolve 为纯函数,query 编译期调用。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/rpc/v1/metric")
@RequiredArgsConstructor
public class MetricRpcController implements MetricClient {

    private final MetricService metricService;
    private final MetricAdapterConvert adapterConvert;

    @Override
    public Response<MetricResolveDTO> resolve(String metricCode, String datasetCode) {
        return Response.success(metricService.resolve(metricCode, datasetCode));
    }

    @Override
    public Response<List<MetricDTO>> list() {
        return Response.success(adapterConvert.toMetricDTOListFromBO(metricService.listPublished()));
    }

    @Override
    public Response<ValidationResultDTO> validate(List<String> metricCodes, List<String> dimCodes) {
        return Response.success(metricService.validate(metricCodes, dimCodes));
    }

    @Override
    public Response<Boolean> exists(String metricCode) {
        return Response.success(metricService.isPublished(metricCode));
    }

    @Override
    public Response<List<MetricResolveDTO>> resolveBatch(ResolveBatchRequestDTO request) {
        return Response.success(metricService.resolveBatch(request));
    }
}
