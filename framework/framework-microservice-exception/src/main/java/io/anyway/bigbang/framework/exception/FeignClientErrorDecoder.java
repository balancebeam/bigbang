package io.anyway.bigbang.framework.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FeignClientErrorDecoder implements ErrorDecoder {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public Exception decode(String methodKey, Response response) {
        String errorContent= "";
        if (response.status() != HttpStatus.OK.value()) {
            try {
                errorContent = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                InternalException internalApiException = objectMapper.readValue(errorContent, InternalException.class);
                internalApiException.setHttpStatus(response.status());
                return internalApiException;
            } catch (Exception e) {
                log.error("(de)serialize error failure",e);
            }
        }
        InternalException internalException= new InternalException(HttpStatus.INTERNAL_SERVER_ERROR.value()+"","UnknownHttpException");
        internalException.setBody(errorContent);
        return internalException;
    }
}
