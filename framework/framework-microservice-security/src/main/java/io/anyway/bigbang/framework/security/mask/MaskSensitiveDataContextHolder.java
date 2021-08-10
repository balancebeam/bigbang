package io.anyway.bigbang.framework.security.mask;

public class MaskSensitiveDataContextHolder {

    private static ThreadLocal<String> EXECUTOR_MARK_HOLDER = new ThreadLocal<>();

    public static void anchorExecutorMarkSensitive(){
        EXECUTOR_MARK_HOLDER.set("TRUE");
    }

    public static void resetExecutorMarkSensitive(){
        EXECUTOR_MARK_HOLDER.remove();
    }

    public static boolean isExecutorMarkSensitive(){
        return "TRUE".equals(EXECUTOR_MARK_HOLDER.get());
    }

}
