package io.anyway.bigbang.framework.mutex.dao;

import io.anyway.bigbang.framework.mutex.entity.MutexEntity;
import org.apache.ibatis.annotations.*;

import java.util.Map;

@Mapper
public interface MutexMapper {


    @Select("select * from framework_mutex where service_id = #{serviceId} and mutex= #{mutex}")
    @Results(id = "resultMap" ,value={
            @Result(property = "id", column = "id"),
            @Result(property = "serviceId", column = "service_id"),
            @Result(property = "mutex", column = "mutex"),
            @Result(property = "host", column = "host"),
            @Result(property = "version", column = "version"),
            @Result(property = "dueAt", column = "due_time"),
            @Result(property = "heartbeat", column = "heartbeat"),
            @Result(property = "createdAt", column = "created_time"),
            @Result(property = "updatedAt", column = "updated_time"),
            @Result(property = "deleted", column = "deleted")
    })
    MutexEntity queryMutex(@Param("serviceId") String serviceId, @Param("mutex") String mutex);

    @Insert("insert ignore into framework_mutex(" +
                "service_id," +
                "mutex," +
                "host," +
                "version," +
                "due_time," +
                "heartbeat" +
            ") values(" +
                "#{serviceId}," +
                "#{mutex}," +
                "#{host}," +
                "#{version}," +
                "#{dueAt}," +
                "#{heartbeat}" +
            ")")
    int insertMutex(MutexEntity mutexEntity);

    @Update("update framework_mutex " +
            "set " +
                "host= #{host}, " +
                "version= #{nVersion}, " +
                "due_time= #{dueAt}" +
            " where service_id= " +
                "#{serviceId} " +
            "and " +
                "mutex= #{mutex} " +
            "and " +
                "(due_time<= now() or (host= #{host} and version= #{oVersion}))")
    int updateMutex(Map<String,Object> inData);


}
