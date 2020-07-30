package io.anyway.bigbang.framework.kernel.logging;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Marker;

import java.util.Collections;
import java.util.Iterator;

@Setter
@Getter
public class LoggingMarkerWrapper implements Marker {

    private String[] markers;

    public LoggingMarkerWrapper(String... markers){
        this.markers= markers;
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
}
