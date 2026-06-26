package com.cyan.stargaze.metric.adapter.dimension.http;

import com.cyan.arch.common.api.Response;
import com.cyan.employee.client.dto.EmployeeDTO;
import com.cyan.employee.login.filter.UserContextHolder;
import com.cyan.stargaze.metric.adapter.MetricAdapterConvert;
import com.cyan.stargaze.metric.adapter.dimension.http.dto.DimensionBindingDTO;
import com.cyan.stargaze.metric.adapter.dimension.http.dto.DimensionDTO;
import com.cyan.stargaze.metric.application.dimension.DimensionService;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionBindingCmd;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionCmd;
import com.cyan.stargaze.metric.client.dto.PageDTO;
import com.cyan.stargaze.metric.domain.dimension.Dimension;
import com.cyan.stargaze.metric.domain.dimension.DimensionBinding;
import com.cyan.stargaze.metric.domain.dimension.repository.DimensionBindingRepository;
import com.cyan.stargaze.metric.domain.metric.repository.MetricDimensionBindingRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 维度控制器(/api/v1/dimensions)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/dimensions")
@RequiredArgsConstructor
public class DimensionController {

    private final DimensionService dimensionService;
    private final DimensionBindingRepository dimensionBindingRepository;
    private final MetricDimensionBindingRepository metricDimensionBindingRepository;

    @PostMapping
    public Response<DimensionDTO> create(@RequestBody @Valid DimensionCmd cmd) {
        fillUser(cmd);
        return Response.success(toDTO(dimensionService.create(cmd)));
    }

    @PutMapping("/{id}")
    public Response<DimensionDTO> update(@PathVariable String id, @RequestBody @Valid DimensionCmd cmd) {
        cmd.setId(id);
        fillUser(cmd);
        return Response.success(toDTO(dimensionService.update(cmd)));
    }

    @GetMapping("/{id}")
    public Response<DimensionDTO> findById(@PathVariable String id) {
        return Response.success(toDTO(dimensionService.findById(id)));
    }

    @GetMapping
    public Response<PageDTO<DimensionDTO>> list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "publishedOnly", defaultValue = "false") boolean publishedOnly) {
        if (publishedOnly) {
            List<DimensionDTO> data = dimensionService.list(true).stream()
                    .map(this::toDTO).toList();
            return Response.success(new PageDTO<DimensionDTO>()
                    .setData(data)
                    .setTotal(data.size())
                    .setPage(1L)
                    .setSize((long) data.size()));
        }
        var result = dimensionService.page(page, size, keyword, folder, status);
        List<DimensionDTO> records = result.getRecords().stream().map(this::toDTO).toList();
        return Response.success(new PageDTO<DimensionDTO>()
                .setData(records)
                .setTotal(result.getTotal())
                .setPage(result.getCurrent())
                .setSize(result.getSize()));
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable String id) {
        dimensionService.delete(id);
        return Response.success();
    }

    @PostMapping("/{id}/publish")
    public Response<DimensionDTO> publish(@PathVariable String id) {
        return Response.success(toDTO(dimensionService.publish(id)));
    }

    @PostMapping("/{id}/bindings")
    public Response<DimensionBindingDTO> addBinding(@PathVariable("id") String dimensionId,
                                                    @RequestBody @Valid DimensionBindingCmd cmd) {
        cmd.setDimensionId(dimensionId);
        return Response.success(MetricAdapterConvert.INSTANCE.toDimensionBindingDTO(dimensionService.addBinding(cmd)));
    }

    @GetMapping("/{id}/bindings")
    public Response<List<DimensionBindingDTO>> listBindings(@PathVariable("id") String dimensionId) {
        return Response.success(MetricAdapterConvert.INSTANCE.toDimensionBindingDTOList(dimensionService.listBindings(dimensionId)));
    }

    @DeleteMapping("/bindings/{bindingId}")
    public Response<Void> removeBinding(@PathVariable String bindingId) {
        dimensionService.removeBinding(bindingId);
        return Response.success();
    }

    private DimensionDTO toDTO(Dimension dimension) {
        DimensionDTO dto = MetricAdapterConvert.INSTANCE.toDimensionDTO(dimension);
        // 关联数据集
        List<DimensionBinding> bindings = dimensionBindingRepository.listByDimension(dimension.getId());
        List<String> datasets = bindings.stream()
                .map(DimensionBinding::getDatasetId)
                .distinct()
                .collect(Collectors.toList());
        dto.setFieldName(bindings.isEmpty() ? dimension.getName() : bindings.get(0).getFieldId());
        dto.setRelatedDatasets(datasets);
        // 关联指标
        var metricBindings = metricDimensionBindingRepository.listByDimensionId(dimension.getId());
        List<String> metrics = metricBindings.stream()
                .map(com.cyan.stargaze.metric.domain.metric.MetricDimensionBinding::getMetricId)
                .distinct()
                .collect(Collectors.toList());
        dto.setRelatedMetrics(metrics);
        dto.setRelatedMetricCount(metrics.size());
        return dto;
    }

    private void fillUser(DimensionCmd cmd) {
        EmployeeDTO employee = UserContextHolder.getCurrentEmployee();
        if (employee != null && employee.getId() != null) {
            cmd.setCreatedBy(employee.getId());
        }
    }
}
