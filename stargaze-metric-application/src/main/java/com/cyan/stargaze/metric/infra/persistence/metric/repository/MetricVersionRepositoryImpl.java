package com.cyan.stargaze.metric.infra.persistence.metric.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.stargaze.metric.domain.metric.MetricVersion;
import com.cyan.stargaze.metric.domain.metric.repository.MetricVersionRepository;
import com.cyan.stargaze.metric.infra.persistence.MetricInfraConvert;
import com.cyan.stargaze.metric.infra.persistence.metric.dos.MetricVersionDO;
import com.cyan.stargaze.metric.infra.persistence.metric.mappers.MetricVersionMapper;
import com.cyan.stargaze.metric.infra.util.IdUtil;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MetricVersionRepositoryImpl implements MetricVersionRepository {

    private final MetricVersionMapper mapper;
    private final MetricInfraConvert convert;

    public MetricVersionRepositoryImpl(MetricVersionMapper mapper, MetricInfraConvert convert) {
        this.mapper = mapper;
        this.convert = convert;
    }

    @Override
    public List<MetricVersion> listByMetric(String metricId) {
        return mapper.selectList(new LambdaQueryWrapper<MetricVersionDO>()
                .eq(MetricVersionDO::getMetricId, IdUtil.toLong(metricId))
                .orderByDesc(MetricVersionDO::getVersion))
                .stream().map(convert::toMetricVersion).toList();
    }

    @Override
    public MetricVersion save(MetricVersion version) {
        MetricVersionDO doObj = convert.toMetricVersionDO(version);
        mapper.insert(doObj);
        return convert.toMetricVersion(mapper.selectById(doObj.getId()));
    }
}
