package io.anyway.bigbang.framework.datasource.sharding;

import javax.sql.DataSource;

public abstract class DataSourceShardingContextHolder {

    final private static ThreadLocal<Boolean> markContext= new ThreadLocal<>();

    final private static ThreadLocal<DataSource> dataSourceContext = new ThreadLocal<>();

    final public static void markSharding(){
        markContext.set(true);
    }

    final public static void setTargetDataSource(DataSource ds){
        dataSourceContext.set(ds);
    }

    final public static boolean sharding(){
        return Boolean.TRUE.equals(markContext.get());
    }

    final public static DataSource getTargetDataSource(){
        return dataSourceContext.get();
    }

    final public static void remove(){
        markContext.remove();
        dataSourceContext.remove();
    }
}
