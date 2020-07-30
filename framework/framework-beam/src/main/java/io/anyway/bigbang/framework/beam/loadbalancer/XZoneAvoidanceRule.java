package io.anyway.bigbang.framework.beam.loadbalancer;

import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;

import io.anyway.bigbang.framework.beam.BeamContext;
import io.anyway.bigbang.framework.beam.BeamContextHolder;
import io.anyway.bigbang.framework.beam.service.*;
import io.anyway.bigbang.framework.beam.domain.PredicateKey;
import io.anyway.bigbang.framework.core.client.ClientAgent;
import io.anyway.bigbang.framework.core.client.ClientAgentContextHolder;
import io.anyway.bigbang.framework.core.security.SecurityContextHolder;
import io.anyway.bigbang.framework.core.security.UserDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class XZoneAvoidanceRule extends ZoneAvoidanceRule{

	final private static String VERSION= "version";

	final private static String UNIT = "unit";

	private String targetServiceId;

	@Value("${spring.application.name}")
	private String sourceServiceId;

	@Value("${eureka.instance.metadata-map.unit:blue}")
	private String sourceServiceUnit;

	@Resource
	private List<NodeRouterPredicate> predicates= Collections.EMPTY_LIST;

	@Resource
	private WeightBalancerService weightBalancerService;

	@Resource
	private OfflinePredictService offlinePredictService;

	@Resource
	private NodeMetadataService nodeMetadataService;

	@Autowired(required = false)
	private EnhanceContextProcessor enhanceContextProcessor= ctx -> {
	};

	@Override
	public void setLoadBalancer(ILoadBalancer lb) {
		super.setLoadBalancer(lb);
		if(lb instanceof BaseLoadBalancer){
			targetServiceId = ((BaseLoadBalancer)lb).getName().toLowerCase().trim();
			log.info("Setting service {} loader", targetServiceId);
		}
	}

	@Override
	public Server choose(Object key) {
		try {
			BeamContext ctx= BeamContextHolder.getContext();
			if(ctx == null){
				ctx= new BeamContext();
			}
			if(ctx.getUid()== null){
				UserDetail userDetail= SecurityContextHolder.getUserDetail();
				if(userDetail!= null) {
					ctx.setUid(userDetail.getType()+"_"+userDetail.getUid());
				}
			}
			ClientAgent clientAgent= ClientAgentContextHolder.getClientAgentContext();
			if(clientAgent!= null) {
				if (ctx.getPlatform() == null) {
					ctx.setPlatform(clientAgent.getPlatform());
				}
				if (ctx.getVersion() == null) {
					ctx.setVersion(clientAgent.getVersion());
				}
				if (ctx.getUnit() == null) {
					ctx.setUnit(clientAgent.getUnit());
				}
			}

			String clientId= "";
			if(!StringUtils.isEmpty(ctx.getPlatform())){
				clientId= clientId+(StringUtils.isEmpty(clientId)?"":"_")+ctx.getPlatform();
			}
			if(!StringUtils.isEmpty(ctx.getVersion())){
				clientId= clientId+(StringUtils.isEmpty(clientId)?"":"_")+ctx.getVersion();
			}
			ctx.setClientId(clientId);

			if(ctx.getSourceServiceId()==null){
				ctx.setSourceServiceId(sourceServiceId);
			}
			if(ctx.getSourceServiceUnit()==null){
				ctx.setSourceServiceUnit(sourceServiceUnit);
			}

			enhanceContextProcessor.enhance(ctx);
			BeamContextHolder.setContext(ctx);
			log.debug("BeamContext: {}",ctx);
			return select(key);
		}
		finally{
			BeamContextHolder.remove();
		}
	}

	private Server select(Object key){
		List<Server> serviceList= getLoadBalancer().getAllServers();
		if(log.isDebugEnabled()){
			log.debug("All the server list of {} service were: {}", targetServiceId,serviceList);
		}
		List<Server> enabledServiceList = serviceList
			.stream()
			.filter((Server server)-> {
				boolean bool= offlinePredictService.isOffline(server.getHostPort());
				if(bool){
					log.info("The server {} was in blacklist, does not participate in load balancing.",server);
				}
				return !bool;
			})
			.collect(Collectors.toList());

		if(!CollectionUtils.isEmpty(enabledServiceList)
				&& !CollectionUtils.isEmpty(predicates)) {
			List<Server> candidateServerList= new LinkedList<>();
			BeamContext ctx= BeamContextHolder.getContext();
			PredicateKey routerPredicateKey= new PredicateKey();
			String platformVersion= ctx.getPlatform()+"-"+VERSION;
loop:		for(Server server: enabledServiceList){
				routerPredicateKey.setServer(server);
				String version= nodeMetadataService.getValue(server,platformVersion);
				if(StringUtils.isEmpty(version)){
					version= nodeMetadataService.getValue(server,"platform*-version");
				}
				routerPredicateKey.setVersion(version);
				String unit= nodeMetadataService.getValue(server,UNIT);
				if(StringUtils.isEmpty(unit)){
					unit= "blue";
				}
				routerPredicateKey.setUnit(unit);
				log.debug("RouterPredicateKey: {}",routerPredicateKey);
				for (NodeRouterPredicate each : predicates) {
					if(!each.apply(routerPredicateKey)){
						continue loop;
					}
				}
				candidateServerList.add(server);
			}
			if(!CollectionUtils.isEmpty(candidateServerList)) {
				log.debug("The service {} candidate service list: {}", targetServiceId,candidateServerList);
				return weightBalancerService.choose(targetServiceId,candidateServerList);
			}
		}
		// it will choose the server from available blue servers firstly,
		// if blue servers are empty then choose from the all of reachable server
		List<Server> blueServers= enabledServiceList.stream()
				.filter(server -> "blue".equals(nodeMetadataService.getValue(server,"unit")))
				.collect(Collectors.toList());
		if(!blueServers.isEmpty()){
			enabledServiceList= blueServers;
		}
		log.debug("The service {} predicate serverList was empty, firstly choose blue server from list: {}", targetServiceId,enabledServiceList);
		if(enabledServiceList.isEmpty()){
			return null;
		}
		return weightBalancerService.choose(targetServiceId,enabledServiceList);
	}

}
