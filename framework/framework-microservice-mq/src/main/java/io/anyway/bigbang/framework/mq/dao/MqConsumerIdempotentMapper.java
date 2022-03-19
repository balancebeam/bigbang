package io.anyway.bigbang.framework.mq.dao;

import io.anyway.bigbang.framework.mq.entity.MqClientIdempotentEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface MqConsumerIdempotentMapper {

    @Insert("insert ignore into framework_mq_consumer_idempotent (\n" +
            "          mq_type,\n" +
            "          transaction_id,\n" +
            "          message_id,\n" +
            "          destination,\n" +
            "          message_header,\n" +
            "          message_body,\n" +
            "          tags,\n" +
            "          attribute\n" +
            "        ) values (\n" +
            "          #{mqType},\n" +
            "          #{transactionId},\n" +
            "          #{messageId},\n" +
            "          #{destination},\n" +
            "          #{messageHeader},\n" +
            "          #{messageBody},\n" +
            "          #{tags},\n" +
            "          #{attribute}\n" +
            "        )")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer insert(MqClientIdempotentEntity mqClientIdempotentEntity);

    @Delete("delete from framework_mq_consumer_idempotent where id = #{id}")
    void deleteById(Long id);

    @Select("select * from framework_mq_consumer_idempotent order by id DESC")
    List<MqClientIdempotentEntity> findAll();

    @Delete("delete from framework_mq_consumer_idempotent")
    Integer purgeAll();

    @Delete("delete from framework_mq_consumer_idempotent where create_time<= #{date}")
    Integer purge(Map<String,Object> param);


}
