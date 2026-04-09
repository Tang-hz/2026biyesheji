package com.gk.study.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gk.study.entity.Thing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ThingMapper extends BaseMapper<Thing> {

    void incrementPv(@Param("id") long id);
//    List<Thing> getList();
//    boolean update(Thing thing);
}
