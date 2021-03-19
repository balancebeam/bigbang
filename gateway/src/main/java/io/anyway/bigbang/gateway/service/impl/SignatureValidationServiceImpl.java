package io.anyway.bigbang.gateway.service.impl;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.gateway.service.SignatureValidationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.TreeMap;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "spring.cloud.gateway.signature-validator",name="enabled",havingValue = "true")
public class SignatureValidationServiceImpl implements SignatureValidationService {

    @Override
    public boolean valid(String appId,String sign,String timestamp,TreeMap<String,String> content) {
        long ts= Long.parseLong(timestamp);
        long cts= System.currentTimeMillis();
        if(cts < ts && ts-cts< 5000 ) { //must be an effective request in 5 seconds .
            try {
                Signature signature = Signature.getInstance("RSA");
                signature.initVerify(getPublicKey(appId));
                signature.update(JSONObject.toJSONString(content).getBytes("UTF-8"));
                return signature.verify(Base64.decodeBase64(sign));
            } catch (Exception e) {
                log.error("rsa verification was error", e);
            }
        }
        return false;
    }

    PublicKey getPublicKey(String appId){
        //TODO
        return null;
    }

    PrivateKey getPrivateKey(String appId){
        return null;
    }

    public void sign(){
//        PrivateKey privateKey="自己的私钥";
////设置签名加密方式
//        Signature signature = Signature.getInstance("RSA");
//        signature.initSign(privateKey);//设置私钥
////签名和加密一样 要以字节形式 utf-8字符集得到字节
//        signature.update(line.getBytes("UTF-8"));
////得到base64编码的签名后的字段
//        String sign = Base64.encodeBase64String(signature.sign());
    }
}
