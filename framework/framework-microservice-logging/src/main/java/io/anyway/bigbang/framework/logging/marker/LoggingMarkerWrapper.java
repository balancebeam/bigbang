package io.anyway.bigbang.framework.logging.marker;

import io.anyway.bigbang.framework.utils.JsonUtil;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Marker;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Setter
@Getter
public class LoggingMarkerWrapper implements Marker {

    private String[] tags;

    private Object data;

    public LoggingMarkerWrapper(String... tags){
        this.tags= tags;
    }

    public LoggingMarkerWrapper(Object data,String... tags){
        this.data= data;
        this.tags= tags;
    }

    @Override
    public String getName() {
        return "Customized-Marker";
    }

    @Override
    public void add(Marker reference) {

    }

    @Override
    public boolean remove(Marker reference) {
        return false;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public boolean hasReferences() {
        return false;
    }

    @Override
    public Iterator<Marker> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public boolean contains(Marker other) {
        return false;
    }

    @Override
    public boolean contains(String name) {
        return false;
    }

    @Override
    public String toString(){
        Map<String, Object> map = new LinkedHashMap<>();
        List<String> tagList = new ArrayList<>(LoggingMarkerContext.markers());
        String[] tags = getTags();
        if (tags != null) {
            tagList.addAll(Arrays.asList(tags));
        }
        map.put("name", getName());
        if (!CollectionUtils.isEmpty(tagList)) {
            map.put("tags", tagList);
        }
        if(Objects.nonNull(getData())){
            map.put("data", getData());
        }
        return JsonUtil.fromObject2String(map);
    }
}
