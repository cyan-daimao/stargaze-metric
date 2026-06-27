package com.cyan.stargaze.metric.infra.persistence.metric.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.arch.common.api.Page;
import com.cyan.stargaze.metric.domain.metric.Metric;
import com.cyan.stargaze.metric.domain.metric.repository.MetricRepository;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.infra.persistence.MetricInfraConvert;
import com.cyan.stargaze.metric.infra.persistence.metric.dos.MetricDO;
import com.cyan.stargaze.metric.infra.persistence.metric.mappers.MetricMapper;
import com.cyan.stargaze.metric.infra.util.IdUtil;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MetricRepositoryImpl implements MetricRepository {

    private final MetricMapper mapper;
    private final MetricInfraConvert convert;

    public MetricRepositoryImpl(MetricMapper mapper, MetricInfraConvert convert) {
        this.mapper = mapper;
        this.convert = convert;
    }

    @Override
    public Metric findById(String id) {
        MetricDO doObj = mapper.selectById(IdUtil.toLong(id));
        return doObj == null ? null : convert.toMetric(doObj);
    }

    @Override
    public Metric findByMetricCode(String metricCode) {
        MetricDO doObj = mapper.selectOne(new LambdaQueryWrapper<MetricDO>()
                .eq(MetricDO::getMetricCode, metricCode));
        return doObj == null ? null : convert.toMetric(doObj);
    }

    @Override
    public Metric findByName(String name) {
        MetricDO doObj = mapper.selectOne(new LambdaQueryWrapper<MetricDO>()
                .eq(MetricDO::getName, name));
        return doObj == null ? null : convert.toMetric(doObj);
    }

    @Override
    public Metric findByCode(String code) {
        MetricDO doObj = mapper.selectOne(new LambdaQueryWrapper<MetricDO>()
                .eq(MetricDO::getCode, code));
        return doObj == null ? null : convert.toMetric(doObj);
    }

    @Override
    public List<Metric> list(MetricStatus status) {
        LambdaQueryWrapper<MetricDO> wrapper = new LambdaQueryWrapper<MetricDO>()
                .eq(status != null, MetricDO::getStatus, status)
                .orderByDesc(MetricDO::getCreatedAt);
        return mapper.selectList(wrapper).stream().map(convert::toMetric).toList();
    }

    @Override
    public Page<Metric> page(int current, int size, String keyword, MetricStatus status, String folder) {
        LambdaQueryWrapper<MetricDO> wrapper = new LambdaQueryWrapper<MetricDO>()
                .and(keyword != null && !keyword.isBlank(), w -> w
                        .like(MetricDO::getName, keyword)
                        .or()
                        .like(MetricDO::getCode, keyword)
                        .or()
                        .like(MetricDO::getMetricCode, keyword))
                .eq(status != null, MetricDO::getStatus, status)
                .eq(folder != null && !folder.isBlank(), MetricDO::getFolder, folder)
                .orderByDesc(MetricDO::getCreatedAt);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<MetricDO> doPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        var result = mapper.selectPage(doPage, wrapper);
        List<Metric> data = Optional.ofNullable(result.getRecords()).orElse(List.of()).stream()
                .map(convert::toMetric)
                .toList();
        return new Page<>(data, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public Metric save(Metric metric) {
        MetricDO doObj = convert.toMetricDO(metric);
        mapper.insert(doObj);
        return findById(IdUtil.toString(doObj.getId()));
    }

    @Override
    public Metric update(Metric metric) {
        MetricDO doObj = convert.toMetricDO(metric);
        mapper.updateById(doObj);
        return findById(IdUtil.toString(doObj.getId()));
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(IdUtil.toLong(id));
    }

    @Override
    public List<String> listDistinctFolders() {
        LambdaQueryWrapper<MetricDO> wrapper = new LambdaQueryWrapper<MetricDO>()
                .select(MetricDO::getFolder)
                .isNotNull(MetricDO::getFolder)
                .ne(MetricDO::getFolder, "")
                .groupBy(MetricDO::getFolder)
                .orderByAsc(MetricDO::getFolder);
        return mapper.selectList(wrapper).stream()
                .map(MetricDO::getFolder)
                .distinct()
                .toList();
    }
}
