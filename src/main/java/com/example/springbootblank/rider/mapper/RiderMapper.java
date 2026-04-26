package com.example.springbootblank.rider.mapper;

import com.example.springbootblank.rider.entity.Rider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RiderMapper {

    Rider findById(@Param("id") Long id);

    int updateWorkStatus(@Param("id") Long id, @Param("workStatus") String workStatus);
}
