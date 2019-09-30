package com.bairock.iot.hamaServer.communication;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class PadHandler extends ChannelInboundHandlerAdapter{
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		PadChannelBridge.channelGroup.add(ctx.channel());
		PadChannelBridgeHelper.getIns().setChannelId(ctx.channel().id().asShortText());
//		OrderBase ob = new OrderBase();
//		ob.setOrderType(OrderType.HEAD_USER_INFO);
//		String order = Util.orderBaseToString(ob);
//		ctx.channel().writeAndFlush(Unpooled.copiedBuffer(order.getBytes()));
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		String str = (String)msg;
		PadChannelBridgeHelper.getIns().channelReceived(ctx.channel().id().asShortText(), str);
		
//		ByteBuf m = (ByteBuf)msg;
//		try{
//			byte[] req = new byte[m.readableBytes()];
//			m.readBytes(req);
//			String str = new String(req, "GBK");
//			PadChannelBridgeHelper.getIns().channelReceived(ctx.channel().id().asShortText(), str);
//		}finally{
//			m.release();
////			ReferenceCountUtil.release(msg);
//		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		PadChannelBridgeHelper.getIns().channelUnRegistered(ctx.channel().id().asShortText());
		ctx.close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		PadChannelBridgeHelper.getIns().channelUnRegistered(ctx.channel().id().asShortText());
		ctx.close();
	}
	
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {  // 2
            IdleStateEvent event = (IdleStateEvent) evt;  
            String type = "";
            if (event.state() == IdleState.READER_IDLE) {
                type = "read idle";
                //读数据超时后关闭连接
                PadChannelBridgeHelper.getIns().channelUnRegistered(ctx.channel().id().asShortText());
                ctx.close();
            } else if (event.state() == IdleState.WRITER_IDLE) {
                type = "write idle";
            } else if (event.state() == IdleState.ALL_IDLE) {
                type = "all idle";
            }
            System.out.println( ctx.channel().remoteAddress()+"超时类型：" + type);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
	
}
