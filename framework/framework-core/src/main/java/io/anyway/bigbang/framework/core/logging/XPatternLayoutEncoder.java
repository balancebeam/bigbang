package io.anyway.bigbang.framework.core.logging;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

import java.util.Properties;
import java.util.Random;

public class XPatternLayoutEncoder extends PatternLayoutEncoder {

    @Override
    public void setPattern(String pattern) {
        Properties properties= LoggingExtensionManager.getExtensionProperties();
        String[] colors= {"black","red","green","yellow","blue","magenta","cyan","white","gray"};
        Random random= new Random();
        StringBuilder builder= new StringBuilder();
        for(String each: properties.stringPropertyNames()){
            builder.append("%clr([").append(each).append(":%").append(each).append("]){");
            builder.append(colors[random.nextInt(colors.length)]);
            builder.append("} ");
        }
        int idx= pattern.lastIndexOf("%clr(:){faint}");
        String newPattern= pattern.substring(0,idx)+ builder.toString() + pattern.substring(idx);
        super.setPattern(newPattern);
    }
}
