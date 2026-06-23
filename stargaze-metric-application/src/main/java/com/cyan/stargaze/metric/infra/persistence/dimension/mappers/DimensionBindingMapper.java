package com.cyan.stargaze.metric.infra.persistence.dimension.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.stargaze.metric.infra.persistence.dimension.dos.DimensionBindingDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DimensionBindingMapper extends BaseMapper<DimensionBindingDO> {
}
