package com.cyan.stargaze.metric.infra.persistence.metric.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.stargaze.metric.domain.metric.MetricDimensionBinding;
import com.cyan.stargaze.metric.domain.metric.repository.MetricDimensionBindingRepository;
import com.cyan.stargaze.metric.infra.persistence.MetricInfraConvert;
import com.cyan.stargaze.metric.infra.persistence.metric.dos.MetricDimensionBindingDO;
import com.cyan.stargaze.metric.infra.persistence.metric.mappers.MetricDimensionBindingMapper;
import com.cyan.stargaze.metric.infra.util.IdUtil;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class MetricDimensionBindingRepositoryImpl implements MetricDimensionBindingRepository {

    private final MetricDimensionBindingMapper mapper;
    private final MetricInfraConvert convert;

    public MetricDimensionBindingRepositoryImpl(MetricDimensionBindingMapper mapper, MetricInfraConvert convert) {
        this.mapper = mapper;
        this.convert = convert;
    }

    @Override
    public List<MetricDimensionBinding> listByMetric(String metricId) {
        return mapper.selectList(new LambdaQueryWrapper<MetricDimensionBindingDO>()
                        .eq(MetricDimensionBindingDO::getMetricId, IdUtil.toLong(metricId))
                        .orderByAsc(MetricDimensionBindingDO::getDimensionName))
                .stream().map(convert::toMetricDimensionBinding).toList();
    }

    @Override
    @Transactional
    public void saveBatch(String metricId, List<MetricDimensionBinding> bindings) {
        mapper.delete(new LambdaQueryWrapper<MetricDimensionBindingDO>()
                .eq(MetricDimensionBindingDO::getMetricId, IdUtil.toLong(metricId)));
        if (bindings == null || bindings.isEmpty()) {
            return;
        }
        Long mid = IdUtil.toLong(metricId);
        OffsetDateTime now = OffsetDateTime.now();
        for (MetricDimensionBinding binding : bindings) {
            binding.setMetricId(metricId);
            binding.setCreatedAt(now);
            binding.setUpdatedAt(now);
            MetricDimensionBindingDO doObj = convert.toMetricDimensionBindingDO(binding);
            doObj.setMetricId(mid);
            mapper.insert(doObj);
        }
    }

    @Override
    public void deleteByMetric(String metricId) {
        mapper.delete(new LambdaQueryWrapper<MetricDimensionBindingDO>()
                .eq(MetricDimensionBindingDO::getMetricId, IdUtil.toLong(metricId)));
    }
}
