package org.yeauty.standard;

import java.util.Map;

/**
 * @author dong_/alvin
 * @version: 1.0
 * @ProjectName netty-websocket-spring-boot-starter
 * @Description: TODO
 * @date 2019/8/20 8:54
 */
public interface STOMPFrame {
    public STOMPCommandType getType();
    public Map<String,String> getHeaders();
    public String getContent();
}
