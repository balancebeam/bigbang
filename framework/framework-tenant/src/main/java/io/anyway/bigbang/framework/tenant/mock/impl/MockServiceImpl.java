package io.anyway.bigbang.framework.tenant.mock.impl;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.tenant.TenantDetail;
import io.anyway.bigbang.framework.tenant.mock.MockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;

@Slf4j
public class MockServiceImpl implements MockService {

    @Value("${spring.application.name}")
    private String appName;

    @Resource
    private RestTemplate restTemplate;

    @Value("${spring.bigbang.tenant.mock.service-url:}")
    private String mockServicePath;

    public void invoke(HttpServletRequest request, HttpServletResponse response){
        String requestURI= request.getRequestURI();

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames= request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            String name= headerNames.nextElement();
            String value= request.getHeader(name);
            headers.set(name,value);
        }
        headers.remove(TenantDetail.HEADER_TENANT_KEY);

        JSONObject data = new JSONObject();
        data.put("requestURI",requestURI);
        data.put("requestMethod",request.getMethod());
        data.put("requestAppName",appName);

        Enumeration<String> enumeration= request.getParameterNames();
        if(enumeration!= null){
            JSONObject parameterMap = new JSONObject();
            for(;enumeration.hasMoreElements();){
                String name= enumeration.nextElement();
                parameterMap.put(name,request.getParameter(name));
            }
            if(!parameterMap.isEmpty()) {
                data.put("requestParameter", parameterMap);
            }
        }

        try(InputStream in= request.getInputStream();){
            if(in.available()>0){
                JSONObject requestBody = JSONObject.parseObject(in,JSONObject.class);
                data.put("requestBody",requestBody);
            }
        }
        catch (IOException e) {
            log.error(e.getMessage());
        }
        HttpEntity<JSONObject> entity = new HttpEntity<>(data, headers);
        ResponseEntity<JSONObject> result= restTemplate.postForEntity(mockServicePath, entity, JSONObject.class);
        response.setStatus(result.getStatusCode().value());
        for(String name: result.getHeaders().keySet()){
            String value= result.getHeaders().getFirst(name);
            response.setHeader(name,value);
        }
        String body= result.getBody().toJSONString();
        try {
            PrintWriter writer = response.getWriter();
            writer.println(body);
            writer.flush();
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
    }

}
