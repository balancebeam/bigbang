package io.anyway.bigbang.framework.mqclient.dao;

import io.anyway.bigbang.framework.mqclient.entity.MqClientIdempotentEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface MqClientIdempotentRepository {

    @Insert("insert into mq_client_idempotent (\n" +
            "          message_id,\n" +
            "          message,\n" +
            "          destination,\n" +
            "          tags,\n" +
            "          partition_key,\n" +
            "          mq_type,\n" +
            "          message_type,\n" +
            "          message_key\n" +
            "        ) values (\n" +
            "          #{messageId,jdbcType=VARCHAR},\n" +
            "          #{message,jdbcType=VARCHAR},\n" +
            "          #{destination,jdbcType=VARCHAR},\n" +
            "          #{tags,jdbcType=VARCHAR},\n" +
            "          #{partitionKey,jdbcType=BIGINT},\n" +
            "          #{mqType,jdbcType=VARCHAR},\n" +
            "          #{messageType,jdbcType=VARCHAR},\n" +
            "          #{messageKey,jdbcType=VARCHAR}\n" +
            "        )")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Long insert(MqClientIdempotentEntity mqClientIdempotentEntity);

    @Select("select * from mq_client_idempotent where\n" +
            "          mq_type = #{mqType,jdbcType=VARCHAR} AND\n" +
            "          message_key = #{messageKey,jdbcType=VARCHAR}")
    MqClientIdempotentEntity findByMessageKey(Map params);

    @Delete("delete from mq_client_idempotent where #{partitionKey,jdbcType=BIGINT} >= partition_key")
    Integer purgeMessageByPartitionKey(Map params);

    @Select("select * from mq_client_idempotent order by id DESC")
    List<MqClientIdempotentEntity> findAll(Map params);

    @Delete("delete from mq_client_idempotent")
    Integer purgeAll(Map params);

    @Delete("delete from mq_client_idempotent where id = #{id,jdbcType=BIGINT}")
    void deleteById(Map params);
}
