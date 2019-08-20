package org.yeauty.standard;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.stomp.DefaultStompFrame;
import io.netty.handler.codec.stomp.StompCommand;
import io.netty.handler.codec.stomp.StompFrame;
import io.netty.handler.codec.stomp.StompHeaders;
import org.springframework.util.StringUtils;
import org.yeauty.pojo.PojoEndpointServer;

import java.util.HashMap;
import java.util.Map;

class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private enum ClientState {
        AUTHENTICATING,
        AUTHENTICATED,
        SUBSCRIBED,
        DISCONNECTING
    }
    private WebSocketServerHandler.ClientState state;
    private final PojoEndpointServer pojoEndpointServer;

    public WebSocketServerHandler(PojoEndpointServer pojoEndpointServer) {
        this.pojoEndpointServer = pojoEndpointServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        } else {
            handleStompFrame(ctx, (DefaultSTOMPFrame)msg);
        }
    }

    private void handleStompFrame(ChannelHandlerContext ctx, DefaultSTOMPFrame frame) {
        STOMPCommandType type = frame.getType();
        if (StringUtils.isEmpty(type)) {
            if ("h".equals(frame.getContent())) {
                handleHeartBeat(ctx, frame);
            }
        } else {
            switch (type) {
                case CONNECT:
                    handleConnect(ctx,frame);
                    break;
                case SEND:
                    handleSend(ctx,frame);
                    break;
                case SUBSCRIBE:
                    handleSubscribe(ctx,frame);
                    break;
                case UNSUBSCRIBE:
                    handleUnsubscribe(ctx,frame);
                    break;
            }
        }
    }
    private void handleUnsubscribe(ChannelHandlerContext channelHandlerContext, DefaultSTOMPFrame frame) {
        System.out.println("handleUnsubscribe");
        // Add logic
    }

    private void handleSubscribe(ChannelHandlerContext channelHandlerContext, DefaultSTOMPFrame frame) {
        System.out.println("handleSubscribe");
        // Add logic
    }

    private void handleSend(ChannelHandlerContext channelHandlerContext, DefaultSTOMPFrame frame) {
        System.out.println("handleSend");
        // Add logic
    }

    private void handleConnect(ChannelHandlerContext channelHandlerContext, DefaultSTOMPFrame frame) {
        STOMPFrame oConnected = new DefaultSTOMPFrame(null, null, "o");
        channelHandlerContext.channel().writeAndFlush(oConnected);
        Map<String,String> headers = new HashMap<>();
        headers.put("version",headers.get("accept-version")==null?"1.1":headers.get("accept-version").split(",")[0]);
        headers.put("heart-beat",frame.getHeaders().get("heart-beat"));

        STOMPFrame connected = new DefaultSTOMPFrame(STOMPCommandType.CONNECTED,
                headers);
        channelHandlerContext.channel().writeAndFlush(connected);
    }

    private void handleHeartBeat(ChannelHandlerContext channelHandlerContext, DefaultSTOMPFrame frame) {
        STOMPFrame resultF = new DefaultSTOMPFrame(null, null, "a[\"\\n\"]");
        channelHandlerContext.channel().writeAndFlush(resultF);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        pojoEndpointServer.doOnError(ctx, cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        pojoEndpointServer.doOnClose(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        pojoEndpointServer.doOnEvent(ctx, evt);
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            pojoEndpointServer.doOnMessage(ctx, frame);
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof CloseWebSocketFrame) {
            ctx.writeAndFlush(frame.retainedDuplicate()).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        if (frame instanceof BinaryWebSocketFrame) {
            pojoEndpointServer.doOnBinary(ctx, frame);
            return;
        }
        if (frame instanceof PongWebSocketFrame) {
            return;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        state = WebSocketServerHandler.ClientState.AUTHENTICATING;
        StompFrame connFrame = new DefaultStompFrame(StompCommand.CONNECT);
        connFrame.headers().set(StompHeaders.ACCEPT_VERSION, "1.2");
        connFrame.headers().set(StompHeaders.HOST, "127.0.0.1");
        connFrame.headers().set(StompHeaders.LOGIN, "guest");
        connFrame.headers().set(StompHeaders.PASSCODE, "guest");
        ctx.writeAndFlush(connFrame);
    }




}