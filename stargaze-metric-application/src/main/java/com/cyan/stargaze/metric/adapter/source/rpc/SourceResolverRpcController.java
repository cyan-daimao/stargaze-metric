package com.cyan.stargaze.metric.adapter.source.rpc;

import com.cyan.arch.common.api.Response;
import com.cyan.stargaze.metric.application.source.SourceResolverService;
import com.cyan.stargaze.metric.client.SourceResolverClient;
import com.cyan.stargaze.metric.client.dto.SourceResolveDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 来源解析 RPC 控制器(/rpc/v1)。
 * <p>
 * 仅供服务间调用,将 dataset/画像/实时表/API 来源解析为物理执行信息。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/rpc/v1")
@RequiredArgsConstructor
public class SourceResolverRpcController implements SourceResolverClient {

    private final SourceResolverService sourceResolverService;

    @Override
    @GetMapping("/dataset/{datasetCode}/resolve")
    public Response<SourceResolveDTO> resolveDataset(@PathVariable("datasetCode") String datasetCode) {
        return Response.success(sourceResolverService.resolveDataset(datasetCode));
    }

    @Override
    @GetMapping("/portrait-feature/{featureCode}/resolve")
    public Response<SourceResolveDTO> resolvePortraitFeature(@PathVariable("featureCode") String featureCode) {
        return Response.success(sourceResolverService.resolvePortraitFeature(featureCode));
    }

    @Override
    @GetMapping("/realtime-table/{tableCode}/resolve")
    public Response<SourceResolveDTO> resolveRealtimeTable(@PathVariable("tableCode") String tableCode) {
        return Response.success(sourceResolverService.resolveRealtimeTable(tableCode));
    }

    @Override
    @GetMapping("/http-api/{apiCode}/resolve")
    public Response<SourceResolveDTO> resolveHttpApi(@PathVariable("apiCode") String apiCode) {
        return Response.success(sourceResolverService.resolveHttpApi(apiCode));
    }
}
