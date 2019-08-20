package org.yeauty.standard;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dong_/alvin
 * @version: 1.0
 * @ProjectName netty-websocket-spring-boot-starter
 * @Description: TODO
 * @date 2019/8/20 8:52
 */
public class STOMPDecoder extends MessageToMessageDecoder<TextWebSocketFrame> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame frame, List<Object> out) throws Exception {
        String stompBody = frame.text();
        STOMPFrame result = null;
        if (stompBody.startsWith("[") && stompBody.endsWith("]")) {
            result = getStompFrame(stompBody);
        } else {
            result = new DefaultSTOMPFrame(null, null, stompBody);
        }
        System.out.println("==========after decode:" + JSON.toJSONString(result));
        out.add(result);
    }

    private STOMPFrame getStompFrame(String rawContent) {
        String stompBody;
        STOMPFrame result;
        System.out.println("==========before decode:" + rawContent);
        String escapedContent = escapeContent(rawContent);
        String lines[] = escapedContent.split("\\n");

        String commandStr = lines[0];
        STOMPCommandType type = getTypeForString(commandStr);

        int index = 1;
        Map<String, String> headers = new HashMap<String, String>();

        for (; index < lines.length; index++) {
            if (lines[index].isEmpty()) {
                break;
            } else {
                String header[] = lines[index].split(":");
                headers.put(header[0], header[1]);
            }
        }

        StringBuilder builder = new StringBuilder();
        for (; index < lines.length; index++) {
            builder.append(lines[index]);
        }

        stompBody = builder.toString();

        result = new DefaultSTOMPFrame(type, headers, stompBody);
        return result;
    }

    private STOMPCommandType getTypeForString(String commandStr) {
        if ("CONNECT".equals(commandStr)) {
            return STOMPCommandType.CONNECT;
        } else if ("CONNECTED".equals(commandStr)) {
            return STOMPCommandType.CONNECTED;
        } else if ("SEND".equals(commandStr)) {
            return STOMPCommandType.SEND;
        } else if ("SUBSCRIBE".equals(commandStr)) {
            return STOMPCommandType.SUBSCRIBE;
        } else if ("ACK".equals(commandStr)) {
            return STOMPCommandType.ACK;
        } else if ("UNSUBSCRIBE".equals(commandStr)) {
            return STOMPCommandType.SUBSCRIBE;
        } else if ("MESSAGE".equals(commandStr)) {
            return STOMPCommandType.MESSAGE;
        } else {
            return null;
        }
    }

    private String escapeContent(String rawContent) {
        String escaped = rawContent.replace("\\n", "\n")
                .replace("\\c", ":")
                .replace("\\\\", "\\")
                .replace("[", "")
                .replace("]", "");

        int frameEnd = escaped.indexOf("\\u0000");

        return frameEnd == -1 ? rawContent : escaped.substring(1, frameEnd);
    }
}