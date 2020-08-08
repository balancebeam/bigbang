package io.anyway.bigbang.framework.mqclient.dao;

import io.anyway.bigbang.framework.mqclient.entity.MqClientMessageEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface MqClientMessageRepository {

    @Insert("insert into mq_client_messages (" +
            "partition_key," +
            "biz_id," +
            "biz_type," +
            "message," +
            "message_type," +
            "message_key," +
            "destination," +
            "tags," +
            "status," +
            "next_retry_at," +
            "mq_type," +
            "send_opts," +
            "message_header," +
            "persist_mode ) " +
            "values (" +
            "#{partitionKey,jdbcType=BIGINT}," +
            "#{bizId,jdbcType=VARCHAR}," +
            "#{bizType,jdbcType=VARCHAR}," +
            "#{message,jdbcType=VARCHAR}," +
            "#{messageType,jdbcType=VARCHAR}," +
            "#{messageKey,jdbcType=VARCHAR}," +
            "#{destination,jdbcType=VARCHAR}," +
            "#{tags,jdbcType=VARCHAR}," +
            "#{status,jdbcType=VARCHAR}," +
            "#{nextRetryAt,jdbcType=TIMESTAMP}," +
            "#{mqType,jdbcType=VARCHAR}," +
            "#{sendOpts,jdbcType=VARCHAR}," +
            "#{messageHeader,jdbcType=VARCHAR}," +
            "#{persistMode,jdbcType=TINYINT}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Long insertMessage(MqClientMessageEntity mqClientMessageEntity);

    @Update("<script>\n" +
            "     update mq_client_messages\n" +
            "        <set>\n" +
            "            next_retry_at = #{nextRetryAt,jdbcType=TIMESTAMP},\n" +
            "            status = #{status,jdbcType=VARCHAR},\n" +
            "            <if test=\"messageId != null\">\n" +
            "              message_id = #{messageId,jdbcType=VARCHAR},\n" +
            "            </if>\n" +
            "            <if test=\"retryCount != null\">\n" +
            "              retry_count = #{retryCount,jdbcType=INTEGER},\n" +
            "            </if>\n" +
            "            updated_at = now()\n" +
            "        </set>\n" +
            "        WHERE\n" +
            "          id = #{id,jdbcType=BIGINT}\n" +
            "</script>")
    int updateMessageStatus(Map params);

    @Select("<script>\n" +
            "        select *\n" +
            "        from mq_client_messages\n" +
            "        where\n" +
            "          status IN\n" +
            "          <foreach item=\"item\" index=\"index\" collection=\"statusList\" open=\"(\" separator=\",\" close=\")\">\n" +
            "              #{item}\n" +
            "          </foreach>\n" +
            "        AND retry_count <![CDATA[<]]> #{retryCount,jdbcType=INTEGER}\n" +
            "        AND next_retry_at <![CDATA[<]]> #{nextTryAt,jdbcType=TIMESTAMP}\n" +
            "        order by id\n" +
            "        limit #{size,jdbcType=INTEGER}\n" +
            "</script>")
    List<MqClientMessageEntity> findReSendingMessages(Map params);

    @Select("select * from mq_client_messages order by id")
    List<MqClientMessageEntity> findAll(Map params);

    @Delete("delete from mq_client_messages where #{partitionKey,jdbcType=BIGINT} >= partition_key")
    int purgeMessageByPartitionKey(Map params);

    @Delete("delete from mq_client_messages where id= #{id,jdbcType=BIGINT}")
    Integer purgeMessageById(@Param("id") long id);

    @Delete("delete from mq_client_messages")
    Integer purgeAll(Map params);
}
