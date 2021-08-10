package io.anyway.bigbang.framework.logging.marker;

public class MarkDown extends LoggingMarkerWrapper{

    private String name;

    private MarkDown(String name, String... tags){
        super(tags);
        this.name= name;
    }

    private <T>MarkDown(String name, T data, String... tags){
        super(data,tags);
        this.name= name;
    }

    @Override
    public String getName() {
        return name;
    }

    public static MarkDown def(String name, String... tags){
        return new MarkDown(name,tags);
    }

    public static <T> MarkDown def(String name, T data, String... tags){
        return new MarkDown(name,data,tags);
    }

}
