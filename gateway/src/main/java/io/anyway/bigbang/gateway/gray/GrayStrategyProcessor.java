package io.anyway.bigbang.gateway.gray;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.gray.GrayContext;
import io.anyway.bigbang.framework.security.UserDetailContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class GrayStrategyProcessor implements GrayStrategyListener {

    private Random random= new Random();

    private volatile GrayStrategy strategy= new GrayStrategy();

    public GrayContext invoke(ServerWebExchange exchange){
        HttpHeaders headers= exchange.getRequest().getHeaders();
        List<GrayStrategy.UserDefinition> userList= strategy.getUserList();
        if(!CollectionUtils.isEmpty(userList)){
            String detail= headers.getFirst(UserDetailContext.USER_HEADER_NAME);
            if(!StringUtils.isEmpty(detail)){
                UserDetailContext userDetail= JSONObject.parseObject(detail, UserDetailContext.class);
                if(Objects.isNull(userDetail.getUid())){
                    userDetail.setUid(detail);
                }
                String candidate= userDetail.getAppId()+"_"+userDetail.getUid();
                GrayContext grayContext= new GrayContext();
                List<String> exVers= new ArrayList<>();
                for(GrayStrategy.UserDefinition each: userList){
                    exVers.add(each.getVersion());
                }
                for(GrayStrategy.UserDefinition each: userList){
                    for(Pattern user: each.getUsers()){
                        if(user.matcher(candidate).find()){
                            grayContext.setInVers(Collections.singletonList(each.getVersion()));
                            exVers.remove(each.getVersion());
                            grayContext.setExVers(exVers);
                            return grayContext;
                        }
                    }
                }
                grayContext.setExVers(exVers);
                return grayContext;
            }
        }
        if(!CollectionUtils.isEmpty(strategy.headerMapping)) {
            List<GrayStrategy.HeaderDefinition> headerList = strategy.getHeaderList();
            List<String> exVers = new ArrayList<>();
            for (GrayStrategy.HeaderDefinition each : headerList) {
                exVers.add(each.getVersion());
            }
            GrayContext grayContext = new GrayContext();
            for (String key : strategy.headerMapping.keySet()) {
                String value = headers.getFirst(key);
                if (!StringUtils.isEmpty(value)) {
                    String version = strategy.headerMapping.get(key).get(value);
                    if (!StringUtils.isEmpty(version)) {
                        grayContext.setInVers(Collections.singletonList(version));
                        exVers.remove(version);
                        grayContext.setExVers(exVers);
                        return grayContext;
                    }
                }
            }
            grayContext.setExVers(exVers);
            return grayContext;
        }
        //use random weight
        List<GrayStrategy.WeightDefinition> wgtList= strategy.getWgtList();
        if(!CollectionUtils.isEmpty(wgtList)){
            int total= 0;
            for(GrayStrategy.WeightDefinition each: wgtList){
                total+=each.getWeight();
            }
            int rdm= random.nextInt(total);
            int sum= 0;
            for(GrayStrategy.WeightDefinition each: wgtList){
                sum+= each.getWeight();
                if(sum>rdm){
                    GrayContext grayContext = new GrayContext();
                    grayContext.setInVers(Collections.singletonList(each.getVersion()));
                    return grayContext;
                }
            }
        }
        return null;
    }

    @Override
    public void onChangeEvent(String text) {
        if(StringUtils.isEmpty(text)){
            text= "{}";
        }
        strategy= JSONObject.parseObject(text,GrayStrategy.class);
        if(!CollectionUtils.isEmpty(strategy.getHeaderList())){
            for(GrayStrategy.HeaderDefinition each: strategy.getHeaderList()){
                for(Map.Entry<String,String> item: each.getMapping().entrySet()){
                    if(!strategy.headerMapping.containsKey(item.getKey())){
                        strategy.headerMapping.put(item.getKey(),new HashMap<>());
                    }
                    Map<String,String> vv= strategy.headerMapping.get(item.getKey());
                    vv.put(item.getValue(),each.getVersion());
                }
            }
        }
    }
}
