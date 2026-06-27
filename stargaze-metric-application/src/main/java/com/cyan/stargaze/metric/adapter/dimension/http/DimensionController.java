package com.cyan.stargaze.metric.adapter.dimension.http;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cyan.arch.common.api.Page;
import com.cyan.arch.common.api.Response;
import com.cyan.employee.login.filter.UserContextHolder;
import com.cyan.stargaze.metric.adapter.MetricAdapterConvert;
import com.cyan.stargaze.metric.adapter.dimension.http.dto.DimensionBindingDTO;
import com.cyan.stargaze.metric.adapter.dimension.http.dto.DimensionDTO;
import com.cyan.stargaze.metric.application.dimension.DimensionService;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionBindingCmd;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionCmd;
import com.cyan.stargaze.metric.domain.dimension.DimensionBinding;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
  private final MetricAdapterConvert adapterConvert;

  // ---- 目录 ----

  @GetMapping("/folders")
  public Response<List<String>> listFolders() {
    return Response.success(dimensionService.listFolders());
  }

  @PostMapping
  public Response<DimensionDTO> create(@RequestBody @Valid DimensionCmd cmd) {
    cmd.setCreatedBy(UserContextHolder.getCurrentEmployee().getPassport());
    return Response.success(adapterConvert.toDimensionDTO(dimensionService.create(cmd)));
  }

  @PutMapping("/{id}")
  public Response<DimensionDTO> update(@PathVariable String id, @RequestBody @Valid DimensionCmd cmd) {
    cmd.setId(id);
    cmd.setCreatedBy(UserContextHolder.getCurrentEmployee().getPassport());
    return Response.success(adapterConvert.toDimensionDTO(dimensionService.update(cmd)));
  }

  @GetMapping("/{id}")
  public Response<DimensionDTO> findById(@PathVariable String id) {
    return Response.success(adapterConvert.toDimensionDTO(dimensionService.findDetail(id)));
  }

  @GetMapping
  public Response<Page<DimensionDTO>> list(
      @RequestParam(value = "page", defaultValue = "1") Integer page,
      @RequestParam(value = "size", defaultValue = "20") Integer size,
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "folder", required = false) String folder,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "publishedOnly", defaultValue = "false") boolean publishedOnly) {
    if (publishedOnly) {
      List<DimensionDTO> data = adapterConvert.toDimensionDTOListFromDetail(dimensionService.listDetail(true));
      return Response.success(new Page<>(data, 1L, data.size(), data.size()));
    }
    var result = dimensionService.pageDetail(page, size, keyword, folder, status);
    List<DimensionDTO> records = adapterConvert.toDimensionDTOListFromDetail(result.getData());
    return Response.success(new Page<>(records, result.getCurrent(), result.getSize(), result.getTotal()));
  }

  @DeleteMapping("/{id}")
  public Response<Void> delete(@PathVariable String id) {
    dimensionService.delete(id);
    return Response.success();
  }

  @PostMapping("/{id}/publish")
  public Response<DimensionDTO> publish(@PathVariable String id) {
    return Response.success(adapterConvert.toDimensionDTO(dimensionService.publish(id)));
  }

  @PostMapping("/{id}/bindings")
  public Response<DimensionBindingDTO> addBinding(@PathVariable("id") String dimensionId,
      @RequestBody @Valid DimensionBindingCmd cmd) {
    cmd.setDimensionId(dimensionId);
    return Response.success(MetricAdapterConvert.INSTANCE.toDimensionBindingDTO(dimensionService.addBinding(cmd)));
  }

  @GetMapping("/{id}/bindings")
  public Response<List<DimensionBindingDTO>> listBindings(@PathVariable("id") String dimensionId) {
    return Response
        .success(MetricAdapterConvert.INSTANCE.toDimensionBindingDTOList(dimensionService.listBindings(dimensionId)));
  }

  @DeleteMapping("/bindings/{bindingId}")
  public Response<Void> removeBinding(@PathVariable String bindingId) {
    dimensionService.removeBinding(bindingId);
    return Response.success();
  }

}
