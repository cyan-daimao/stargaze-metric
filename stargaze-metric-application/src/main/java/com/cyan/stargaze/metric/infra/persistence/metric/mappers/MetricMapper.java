package com.cyan.stargaze.metric.infra.persistence.metric.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.stargaze.metric.infra.persistence.metric.dos.MetricDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MetricMapper extends BaseMapper<MetricDO> {
}
