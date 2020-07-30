package io.anyway.bigbang.framework.cache.distributedlock;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class LockContext {
    private String id;
    private int count;

    public int increase(){
        return ++count;
    }

    public int decrease(){
        return --count;
    }
}
