package io.anyway.bigbang.framework.logging.converter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.alibaba.fastjson.JSONArray;
import io.anyway.bigbang.framework.logging.InheritableThreadClassicConverter;
import io.anyway.bigbang.framework.logging.marker.LoggingMarkerContext;
import io.anyway.bigbang.framework.logging.marker.LoggingMarkerWrapper;

import java.util.ArrayList;
import java.util.List;

public class LoggingMarkerConverter extends InheritableThreadClassicConverter {

    @Override
    public String getInheritableThreadValue(ILoggingEvent event) {
        List<String> markers= LoggingMarkerContext.markers();
        if(event.getMarker()!=null && event.getMarker() instanceof LoggingMarkerWrapper){
            markers= new ArrayList<>(markers);
            String[] mks= ((LoggingMarkerWrapper)event.getMarker()).getMarkers();
            for(String each: mks){
                markers.add(each);
            }
        }
        return JSONArray.toJSONString(markers);
    }
}
