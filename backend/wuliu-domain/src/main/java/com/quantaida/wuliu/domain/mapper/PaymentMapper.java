package com.quantaida.wuliu.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quantaida.wuliu.domain.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
