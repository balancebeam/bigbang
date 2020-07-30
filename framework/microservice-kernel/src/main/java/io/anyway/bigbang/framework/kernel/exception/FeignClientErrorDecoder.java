package io.anyway.bigbang.framework.kernel.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@ConditionalOnClass(ErrorDecoder.class)
public class FeignClientErrorDecoder implements ErrorDecoder {

    @Resource
    private ObjectMapper mapper;

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() != HttpStatus.OK.value()) {
            String errorContent;
            try {
                errorContent = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                InternalApiException internalApiException =mapper.readValue(errorContent,InternalApiException.class);
                internalApiException.setHttpStatus(response.status());
                return internalApiException;
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return new InternalApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(),"unknown error");
    }
}
