package org.yeauty.standard;

import java.util.Map;

/**
 * @author dong_/alvin
 * @version: 1.0
 * @ProjectName netty-websocket-spring-boot-starter
 * @Description: TODO
 * @date 2019/8/20 8:54
 */
public class DefaultSTOMPFrame implements STOMPFrame {

    STOMPCommandType type;
    Map<String, String> headers;
    String stomBody;

    public DefaultSTOMPFrame(STOMPCommandType type, Map<String, String> headers, String stompBody) {
        this(type, headers);
        this.stomBody = stompBody;
    }

    public DefaultSTOMPFrame(STOMPCommandType type, Map<String, String> headers) {
        this.type = type;
        this.headers = headers;
    }

    @Override
    public STOMPCommandType getType() {
        return type;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String getContent() {
        return stomBody;
    }
}