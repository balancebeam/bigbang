package io.anyway.bigbang.oauth2.mapper;


import io.anyway.bigbang.oauth2.entity.UserDetailEntity;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;


@Mapper
public interface UserRepository {

    @Select("select * from oauth_user_detail where app_id=#{appId} and user_id=#{userId}")
    @Results(
        id="userDetailList",
        value={
                @Result(column = "app_id",property = "appId",javaType = String.class,jdbcType = JdbcType.VARCHAR),
                @Result(column = "user_id",property = "userId",javaType = String.class,jdbcType = JdbcType.VARCHAR),
                @Result(column = "user_name",property = "userName",javaType = String.class,jdbcType = JdbcType.VARCHAR),
                @Result(column = "password",property = "password",javaType = String.class,jdbcType = JdbcType.VARCHAR),
                @Result(column = "status",property = "status",javaType = String.class,jdbcType = JdbcType.VARCHAR)
        }
    )
    UserDetailEntity getUserDetail(@Param("appId") String appId, @Param("userId") String userId);
}
