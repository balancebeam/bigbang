package io.anyway.bigbang.gateway.gray;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.gray.GrayContext;
import io.anyway.bigbang.framework.security.UserDetailContext;
import io.anyway.bigbang.framework.useragent.UserAgentContext;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Pattern;

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
                String candidate= userDetail.getType()+"_"+userDetail.getUid();
                GrayContext grayContext= new GrayContext();
                List<String> exVers= new ArrayList<>();
                for(GrayStrategy.UserDefinition each: userList){
                    exVers.add(each.getVersion());
                }
                for(GrayStrategy.UserDefinition each: userList){
                    for(Pattern user: each.getUsers()){
                        if(user.matcher(candidate).find()){
                            grayContext.setInVers(Collections.singileList(each.getVersion));
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

        List<GrayStrategy.HeaderDefinition> headerList= strategy.getHeaderList();
        if(!CollectionUtils.isEmpty(headerList)){
            String detail= headers.getFirst(UserDetailContext.USER_HEADER_NAME);
            if(!StringUtils.isEmpty(detail)){
                UserDetailContext userDetail= JSONObject.parseObject(detail, UserDetailContext.class);
                if(Objects.isNull(userDetail.getUid())){
                    userDetail.setUid(detail);
                }
                String candidate= "usr_"+userDetail.getType()+"_"+userDetail.getUid();
                for(GrayStrategy.UserDefinition each: uatList){
                    for(Pattern user: each.getUsers()){
                        if(user.matcher(candidate).find()){
                            return buildNewExchange(exchange, each.getCluster());
                        }
                    }
                }
            }
            detail= headers.getFirst(UserAgentContext.USER_AGENT_NAME);
            if(!StringUtils.isEmpty(detail)){
                UserAgentContext userAgent= JSONObject.parseObject(detail, UserAgentContext.class);
                String candidate= "cli_"+userAgent.getPlatform()+"_"+userAgent.getVersion();
                for(GrayStrategy.UserDefinition each: uatList){
                    for(Pattern user: each.getUsers()){
                        if(user.matcher(candidate).find()){
                            return buildNewExchange(exchange, each.getCluster());
                        }
                    }
                }
            }
        }
        //use random weight
        List<GrayStrategy.WeightDefinition> wgtList= strategy.getWgtList();
        if(!wgtList.isEmpty()){
            int total= 0;
            for(GrayStrategy.WeightDefinition each: wgtList){
                total+=each.getWeight();
            }
            int rdm= random.nextInt(total);
            int sum= 0;
            for(GrayStrategy.WeightDefinition each: wgtList){
                sum+= each.getWeight();
                if(sum>rdm){
                    return buildNewExchange(exchange,each.getCluster());
                }
            }
        }
        //use the default context
        return buildNewExchange(exchange,strategy.getDefGroup());
    }

    @Override
    public void onChangeEvent(String strategy) {

    }
}
