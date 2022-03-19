package io.anyway.bigbang.framework.mq.dao;

import io.anyway.bigbang.framework.mq.entity.MqClientMessageEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface MqProducerMessageMapper {

    @Insert("insert into framework_mq_producer_messages (" +
                "mq_type," +
                "message_type," +
                "transaction_id," +
                "transaction_type," +
                "destination," +
                "message_header," +
                "message_body," +
                "tags," +
                "attribute," +
                "state," +
                "retry_count,"+
                "retry_next_time," +
                "persist_mode " +
            ") values (" +
                "#{mqType}," +
                "#{messageType}," +
                "#{transactionId}," +
                "#{transactionType}," +
                "#{destination}," +
                "#{messageHeader}," +
                "#{messageBody}," +
                "#{tags}," +
                "#{attribute}," +
                "#{state}," +
                "#{retryCount}," +
                "#{retryNextAt}," +
                "#{persistMode}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertMessage(MqClientMessageEntity mqClientMessageEntity);

    @Delete("delete from framework_mq_producer_messages where id= #{id}")
    int purgeMessageById(@Param("id") long id);

    @Update("<script>\n" +
            "     update framework_mq_producer_messages\n" +
            "        <set>\n" +
            "            state = #{state},\n" +
            "            <if test=\"messageId != null\">\n" +
            "              message_id = #{messageId},\n" +
            "            </if>\n" +
            "            <if test=\"cause != null\">\n" +
            "               cause = #{cause},\n" +
            "            </if>\n" +
            "            <if test=\"retryCount != null\">\n" +
            "              retry_count = #{retryCount},\n" +
            "            </if>\n" +
            "            <if test=\"retryNextAt != null\">\n" +
            "              retry_next_at = #{retryNextAt}\n" +
            "            </if>\n" +
            "        </set>\n" +
            "        WHERE\n" +
            "          id = #{id}\n" +
            "</script>")
    int updateMessageState(Map params);


    @Select("select * from framework_mq_producer_messages where " +
                "state != 'Success' " +
            "and " +
                "retry_count >0 " +
            "and " +
                "retry_next_time < now()")
    @Results(id = "resultMap" ,value={
        @Result(property = "id", column = "id"),
        @Result(property = "mqType", column = "mq_type"),
        @Result(property = "messageType", column = "message_type"),
        @Result(property = "transactionId", column = "transaction_id"),
        @Result(property = "transactionType", column = "transaction_type"),
        @Result(property = "destination", column = "destination"),
        @Result(property = "messageHeader", column = "message_header"),
        @Result(property = "messageBody", column = "message_body"),
        @Result(property = "tags", column = "tags"),
        @Result(property = "attribute", column = "attribute"),
        @Result(property = "state", column = "state"),
        @Result(property = "cause", column = "cause"),
        @Result(property = "messageId", column = "message_id"),
        @Result(property = "retryCount", column = "retry_count"),
        @Result(property = "retryNextAt", column = "retry_next_time"),
        @Result(property = "persistMode", column = "persist_mode"),
        @Result(property = "createdAt", column = "created_time"),
        @Result(property = "updatedAt", column = "updated_time"),
        @Result(property = "deleted", column = "deleted")
    })
    List<MqClientMessageEntity> findReSendingMessages();

    @Select("select * from framework_mq_producer_messages order by id")
    @ResultMap("resultMap")
    List<MqClientMessageEntity> findAll();

    @Delete("delete from framework_mq_producer_messages")
    Integer purgeAll();

    @Delete("delete from framework_mq_producer_messages where state =#{state} and created_time <= #{date}")
    Integer purge(Map<String,Object> param);
}
