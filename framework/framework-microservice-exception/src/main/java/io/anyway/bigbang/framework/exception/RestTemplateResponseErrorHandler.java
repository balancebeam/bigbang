package io.anyway.bigbang.framework.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RestTemplateResponseErrorHandler extends DefaultResponseErrorHandler implements ResponseErrorHandler {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        String errorContent= "";
        try {
            byte[] body = getResponseBody(response);
            errorContent = new String(body,StandardCharsets.UTF_8);
            InternalException internalApiException = objectMapper.readValue(errorContent, InternalException.class);
            internalApiException.setHttpStatus(response.getRawStatusCode());
            throw internalApiException;
        } catch (Exception e) {
            log.error("(de)serialize error failure",e);
        }
        InternalException internalException= new InternalException(HttpStatus.INTERNAL_SERVER_ERROR.value()+"","UnknownApiException");
        internalException.setBody(errorContent);
        throw  internalException;
    }
}
