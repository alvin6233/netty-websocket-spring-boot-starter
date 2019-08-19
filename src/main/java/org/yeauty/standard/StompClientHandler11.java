//package org.yeauty.standard;
//
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.SimpleChannelInboundHandler;
//import io.netty.handler.codec.stomp.DefaultStompFrame;
//import io.netty.handler.codec.stomp.StompCommand;
//import io.netty.handler.codec.stomp.StompFrame;
//import io.netty.handler.codec.stomp.StompHeaders;
//
///**
// * @author dong_/alvin
// * @version: 1.0
// * @ProjectName netty-websocket-spring-boot-starter
// * @Description: TODO
// * @date 2019/8/19 16:15
// */
//public class StompClientHandler11 extends SimpleChannelInboundHandler<StompFrame> {
//    private enum ClientState {
//        AUTHENTICATING,
//        AUTHENTICATED,
//        SUBSCRIBED,
//        DISCONNECTING
//    }
//
//    private ClientState state;
//
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        state = ClientState.AUTHENTICATING;
//        StompFrame connFrame = new DefaultStompFrame(StompCommand.CONNECT);
//        connFrame.headers().set(StompHeaders.ACCEPT_VERSION, "1.2");
//        connFrame.headers().set(StompHeaders.HOST, "127.0.0.1");
//        connFrame.headers().set(StompHeaders.LOGIN, "guest");
//        connFrame.headers().set(StompHeaders.PASSCODE, "guest");
//        ctx.writeAndFlush(connFrame);
//    }
//
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, StompFrame frame) throws Exception {
//        String subscrReceiptId = "001";
//        String disconReceiptId = "002";
//        switch (frame.command()) {
//            case CONNECTED:
//                StompFrame subscribeFrame = new DefaultStompFrame(StompCommand.SUBSCRIBE);
//                subscribeFrame.headers().set(StompHeaders.DESTINATION, "jms.topic.exampleTopic");
//                subscribeFrame.headers().set(StompHeaders.RECEIPT, subscrReceiptId);
//                subscribeFrame.headers().set(StompHeaders.ID, "1");
//                System.out.println("connected, sending subscribe frame: " + subscribeFrame);
//                state = ClientState.AUTHENTICATED;
//                ctx.writeAndFlush(subscribeFrame);
//                break;
//            case RECEIPT:
//                String receiptHeader = frame.headers().getAsString(StompHeaders.RECEIPT_ID);
//                if (state == ClientState.AUTHENTICATED && receiptHeader.equals(subscrReceiptId)) {
//                    StompFrame msgFrame = new DefaultStompFrame(StompCommand.SEND);
//                    msgFrame.headers().set(StompHeaders.DESTINATION, "jms.topic.exampleTopic");
//                    msgFrame.content().writeBytes("some payload".getBytes());
//                    System.out.println("subscribed, sending message frame: " + msgFrame);
//                    state = ClientState.SUBSCRIBED;
//                    ctx.writeAndFlush(msgFrame);
//                } else if (state == ClientState.DISCONNECTING && receiptHeader.equals(disconReceiptId)) {
//                    System.out.println("disconnected");
//                    ctx.close();
//                } else {
//                    throw new IllegalStateException("received: " + frame + ", while internal state is " + state);
//                }
//                break;
//            case MESSAGE:
//                if (state == ClientState.SUBSCRIBED) {
//                    System.out.println("received frame: " + frame);
//                    StompFrame disconnFrame = new DefaultStompFrame(StompCommand.DISCONNECT);
//                    disconnFrame.headers().set(StompHeaders.RECEIPT, disconReceiptId);
//                    System.out.println("sending disconnect frame: " + disconnFrame);
//                    state = ClientState.DISCONNECTING;
//                    ctx.writeAndFlush(disconnFrame);
//                }
//                break;
//            default:
//                break;
//        }
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        cause.printStackTrace();
//        ctx.close();
//    }
//}
