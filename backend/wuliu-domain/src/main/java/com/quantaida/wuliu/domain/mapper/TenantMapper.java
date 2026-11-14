package com.quantaida.wuliu.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quantaida.wuliu.domain.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {
}
