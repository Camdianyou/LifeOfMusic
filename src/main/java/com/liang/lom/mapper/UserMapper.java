package com.liang.lom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liang.lom.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
