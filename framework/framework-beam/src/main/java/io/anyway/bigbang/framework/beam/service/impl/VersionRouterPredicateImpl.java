package io.anyway.bigbang.framework.beam.service.impl;

import io.anyway.bigbang.framework.beam.BeamContextHolder;
import io.anyway.bigbang.framework.beam.domain.PredicateKey;
import io.anyway.bigbang.framework.beam.service.NodeRouterPredicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class VersionRouterPredicateImpl implements NodeRouterPredicate {

    @Override
    public boolean apply(PredicateKey routerPredicateKey) {

        String requestVersion= BeamContextHolder.getContext().getVersion();
        String serverVersion= routerPredicateKey.getVersion();

        if(!StringUtils.isEmpty(requestVersion) && !StringUtils.isEmpty(serverVersion)){
            String[] versions= serverVersion.split(",");
            if(versions[0].matches("^(\\[|\\().*")){
                String request= fillgapVersion(requestVersion);
                char comparator= versions[0].charAt(0);
                String lower= versions[0]= versions[0].substring(1);
                if(!StringUtils.isEmpty(lower)){
                    lower= fillgapVersion(lower);
                    switch (comparator){
                        case '[':
                            if(request.compareTo(lower)<0){
                                log.debug("request version {} < service lower version {}",requestVersion,versions[0]);
                                return false;
                            }
                            break;
                        case '(':
                            if(request.compareTo(lower)<=0){
                                log.debug("request version {} < service lower version {}",requestVersion,versions[0]);
                                return false;
                            }
                            break;
                    }
                }
                comparator= versions[1].charAt(versions[1].length()-1);
                String upper= versions[1]= versions[1].substring(0,versions[1].length()-1);
                if(!StringUtils.isEmpty(upper)){
                    upper= fillgapVersion(upper);
                    switch (comparator){
                        case ']':
                            if(request.compareTo(upper)> 0){
                                log.debug("request version {} > service upper version {}",requestVersion,versions[1]);
                                return false;
                            }
                            break;
                        case ')':
                            if(request.compareTo(upper)>= 0){
                                log.debug("request version {} >= service upper version {}",requestVersion,versions[1]);
                                return false;
                            }
                            break;
                    }
                }
            }
            else{
                for(int i=0;i<versions.length;i++) {
                    if (versions[i].startsWith(requestVersion)) {
                        return true;
                    }
                }
                log.debug("request version {} was not in server version list {}",requestVersion,serverVersion);
                return false;
            }
        }
        return true;
    }

    private String fillgapVersion(String version){
        StringBuilder builder= new StringBuilder();
        for(String each: version.split("\\.")){
            String every = each.trim();
            if(every.length()<3){
                every= "000".substring(every.length()).concat(every);
            }
            builder.append(every);
        }
        return builder.toString();
    }
}
