package com.cyan.stargaze.metric.adapter.metric.rpc;

import com.cyan.arch.common.api.Response;
import com.cyan.stargaze.metric.adapter.MetricAdapterConvert;
import com.cyan.stargaze.metric.application.metric.MetricService;
import com.cyan.stargaze.metric.client.MetricClient;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
import com.cyan.stargaze.metric.client.dto.MetricResolveDTO;
import com.cyan.stargaze.metric.client.dto.ValidationResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 指标 RPC 控制器:实现 {@link MetricClient} 契约(/rpc/metric)。
 * <p>
 * 供 query/dashboard 依赖。resolve 为纯函数,query 编译期调用。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
public class MetricRpcController implements MetricClient {

    private final MetricService metricService;

    @Override
    public Response<MetricResolveDTO> resolve(String metricId, String datasetId) {
        return Response.success(metricService.resolve(metricId, datasetId));
    }

    @Override
    public Response<List<MetricDTO>> listPublished(String workspaceId) {
        return Response.success(metricService.list(workspaceId, true));
    }

    @Override
    public Response<ValidationResultDTO> validate(List<String> metricIds, List<String> dimensionIds) {
        return Response.success(metricService.validate(metricIds, dimensionIds));
    }

    @Override
    public Response<Boolean> exists(String metricId) {
        return Response.success(metricService.findById(metricId) != null);
    }
}
