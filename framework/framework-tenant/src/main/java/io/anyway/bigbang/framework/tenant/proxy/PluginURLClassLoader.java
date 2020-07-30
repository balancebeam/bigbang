package io.anyway.bigbang.framework.tenant.proxy;

import java.net.URL;
import java.net.URLClassLoader;

public class PluginURLClassLoader extends URLClassLoader {

    public PluginURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public URL getResource(String name) {
        URL url = findResource(name);
        if(url== null){
            return super.getResource(name);
        }
        return url;
    }
}
