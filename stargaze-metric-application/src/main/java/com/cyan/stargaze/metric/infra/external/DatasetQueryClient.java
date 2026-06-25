package com.cyan.stargaze.metric.infra.external;

import com.cyan.arch.common.api.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 数据集平台公开 API 查询客户端。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@FeignClient(name = "stargaze-dataset", url = "${feign.stargaze-dataset.url:}")
public interface DatasetQueryClient {

    @GetMapping("/api/v1/datasets")
    Response<PageResult<DatasetItem>> list(
            @RequestParam("workspaceId") String workspaceId,
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "source_type", required = false) String sourceType,
            @RequestParam(value = "status", required = false) String status);

    @GetMapping("/api/v1/datasets/{id}")
    Response<DatasetDetail> findById(@PathVariable("id") String id);

    /**
     * 数据集列表项（按数据集平台实际返回字段映射）。
     */
    record DatasetItem(
            String id,
            String name,
            String sourceType,
            String datasourceName,
            String status,
            Integer fieldCount,
            Integer dimensionCount,
            Integer measureCount,
            String updatedAt) {
    }

    /**
     * 分页结果。
     */
    record PageResult<T>(List<T> list, Long total, Integer page, Integer size) {
    }

    /**
     * 数据集详情（简化）。
     */
    record DatasetDetail(
            String id,
            String name,
            String sourceType,
            String datasourceName,
            String status,
            List<FieldItem> fields) {
    }

    /**
     * 字段项。
     */
    record FieldItem(
            String id,
            String fieldName,
            String displayName,
            String dataType,
            String fieldType) {
    }
}
