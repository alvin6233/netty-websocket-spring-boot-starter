package org.yeauty.standard;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.stomp.DefaultStompFrame;
import io.netty.handler.codec.stomp.StompCommand;
import io.netty.handler.codec.stomp.StompFrame;
import io.netty.handler.codec.stomp.StompHeaders;
import org.yeauty.pojo.PojoEndpointServer;

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
            handleStompFrame(ctx, (StompFrame)msg);
        }
    }

    private void handleStompFrame(ChannelHandlerContext ctx, StompFrame frame) {
        String subscrReceiptId = "001";
        String disconReceiptId = "002";
        switch (frame.command()) {
            case CONNECTED:
                StompFrame subscribeFrame = new DefaultStompFrame(StompCommand.SUBSCRIBE);
                subscribeFrame.headers().set(StompHeaders.DESTINATION, "jms.topic.exampleTopic");
                subscribeFrame.headers().set(StompHeaders.RECEIPT, subscrReceiptId);
                subscribeFrame.headers().set(StompHeaders.ID, "1");
                System.out.println("connected, sending subscribe frame: " + subscribeFrame);
                state = ClientState.AUTHENTICATED;
                ctx.writeAndFlush(subscribeFrame);
                break;
            case RECEIPT:
                String receiptHeader = frame.headers().getAsString(StompHeaders.RECEIPT_ID);
                if (state == ClientState.AUTHENTICATED && receiptHeader.equals(subscrReceiptId)) {
                    StompFrame msgFrame = new DefaultStompFrame(StompCommand.SEND);
                    msgFrame.headers().set(StompHeaders.DESTINATION, "jms.topic.exampleTopic");
                    msgFrame.content().writeBytes("some payload".getBytes());
                    System.out.println("subscribed, sending message frame: " + msgFrame);
                    state = ClientState.SUBSCRIBED;
                    ctx.writeAndFlush(msgFrame);
                } else if (state == ClientState.DISCONNECTING && receiptHeader.equals(disconReceiptId)) {
                    System.out.println("disconnected");
                    ctx.close();
                } else {
                    throw new IllegalStateException("received: " + frame + ", while internal state is " + state);
                }
                break;
            case MESSAGE:
                if (state == ClientState.SUBSCRIBED) {
                    System.out.println("received frame: " + frame);
                    StompFrame disconnFrame = new DefaultStompFrame(StompCommand.DISCONNECT);
                    disconnFrame.headers().set(StompHeaders.RECEIPT, disconReceiptId);
                    System.out.println("sending disconnect frame: " + disconnFrame);
                    state = ClientState.DISCONNECTING;
                    ctx.writeAndFlush(disconnFrame);
                }
                break;
            default:
                break;
        }
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