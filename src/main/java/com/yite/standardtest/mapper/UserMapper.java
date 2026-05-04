package com.yite.standardtest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yite.standardtest.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * BaseMapper: Mybatis提供已有功能
 *     int insert(T entity);
 *     int deleteById(Serializable id);
 *     int delete(@Param(Constants.WRAPPER) Wrapper<T> wrapper);
 *     int updateById(@Param(Constants.ENTITY) T entity);
 *     int update(@Param(Constants.ENTITY) T entity, @Param(Constants.WRAPPER) Wrapper<T> updateWrapper);
 *     T selectById(Serializable id);
 *     List<T> selectList(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
 *     T selectOne(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
 *     Integer selectCount(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
 *     <E extends IPage<T>> E selectPage(E page, @Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * mybatis-plus 另一种写法，适合小项目
     * @Select("SELECT * FROM t_user WHERE username = #{username}")
     *     UserEntity selectByUsername(@Param("username") String username);
     */
    // UserEntity selectByUsername(@Param("username") String username);

}

