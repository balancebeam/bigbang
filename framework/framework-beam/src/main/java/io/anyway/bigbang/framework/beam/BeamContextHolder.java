package io.anyway.bigbang.framework.beam;

public abstract class BeamContextHolder {

    private static ThreadLocal<BeamContext> holder= new ThreadLocal<>();

    public static void setContext(BeamContext context){
        holder.set(context);
    }

    public static BeamContext getContext(){
        return holder.get();
    }

    public static void remove(){
        holder.remove();
    }
}
