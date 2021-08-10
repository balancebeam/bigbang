package io.anyway.bigbang.gateway.testcase;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.gateway.utils.MacSigner;
import org.apache.commons.codec.binary.Base64;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;

import java.util.TreeMap;


public class MacSignerTest {

    public void test()throws Exception{
        String verifierKey= new RandomValueStringGenerator().generate();
        MacSigner macSigner= new MacSigner(verifierKey);
        System.out.println(verifierKey);

        TreeMap<String, String> content = new TreeMap<>();
        content.put("path", "/hello");
        content.put("param_" + "appId", "test");
        content.put("param_" + "apiCode", "C18002");
        content.put("param_" + "timestamp", "632877398181");
        String body = "{\"name\": \"wangliu\"}";
        content.put("body", body);

        byte[] bytes= macSigner.sign(JSONObject.toJSONString(content).getBytes("UTF-8"));
        String decodeSign= Base64.encodeBase64String(bytes);
        System.out.println(decodeSign);
    }

    public static void main(String[] args)throws Exception{
        new MacSignerTest().test();
    }
}
