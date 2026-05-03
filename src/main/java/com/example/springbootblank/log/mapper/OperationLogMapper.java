package com.example.springbootblank.log.mapper;

import com.example.springbootblank.log.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper {
    int insert(OperationLog log);
}
