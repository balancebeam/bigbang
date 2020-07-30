package io.anyway.bigbang.framework.core.client;

final public class ClientAgentContextHolder {

    final private static ThreadLocal<ClientAgent> context= new ThreadLocal<>();

    final public static ClientAgent getClientAgentContext(){
        return context.get();
    }

    final public static void setClientAgentContext(ClientAgent clientAgent){
        context.set(clientAgent);
    }

    final public static void remove(){
        context.remove();
    }
}
