package org.yeauty.standard;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.List;
import java.util.Map;
/**
 * @author dong_/alvin
 * @version: 1.0
 * @ProjectName netty-websocket-spring-boot-starter
 * @Description: TODO
 * @date 2019/8/20 8:56
 */
public class STOMPEncoder extends MessageToMessageEncoder<STOMPFrame> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, STOMPFrame stompFrame, List<Object> objects) throws Exception {
        System.out.println("==========before encode:" + JSON.toJSONString(stompFrame));
        String stompFrameString = "";
        if (stompFrame.getType() == null) {
            stompFrameString = stompFrame.getContent();
        } else {
            stompFrameString = getResult(stompFrame);
        }
        WebSocketFrame result = new TextWebSocketFrame(stompFrameString);
        System.out.println("==========after encode:" + stompFrameString);
        objects.add(result);
    }

    private String getResult(STOMPFrame stompFrame) {
        StringBuilder builder = new StringBuilder();

        builder.append(getStringFor(stompFrame.getType()));
        builder.append("\n");

        Map<String, String> headers = stompFrame.getHeaders();

        for (Map.Entry<String,String> header: headers.entrySet()) {
            builder.append(header.getKey());
            builder.append(":");
            builder.append(header.getValue());
            builder.append("\n");
        }

        builder.append("\n");

        String stompBody = stompFrame.getContent();

        if (stompBody != null) {
            builder.append(stompBody);
        }

        builder.append("\\u0000");
        return escaapeFrameString(builder.toString(),stompFrame.getType());
    }

    private String escaapeFrameString(String stompFrameString, STOMPCommandType type) {
        String target = "";
        target = stompFrameString.replace("\n", "\\n");
//                .replace(":", "\\c")
//                .replace("\\","\\\\");
        switch (type) {
            case MESSAGE:
                break;
            case SUBSCRIBE:
                break;
            case ACK:
                break;
            case CONNECT:
                break;
            case CONNECTED:
                target = "a[\"" + target + "\"]";
                break;
            case SEND:
                break;
            case UNSUBSCRIBE:
                break;
        }
        return target;
    }

    private String getStringFor(STOMPCommandType type) {
        switch (type) {
            case MESSAGE:
                return "MESSAGE";
            case SUBSCRIBE:
                return "SUBSCRIBE";
            case ACK:
                return "ACK";
            case CONNECT:
                return "CONNECTED";
            case CONNECTED:
                return "CONNECTED";
            case SEND:
                return "SEND";
            case UNSUBSCRIBE:
                return "UNSUBSCRIBE";
        }

        return "";
    }
}