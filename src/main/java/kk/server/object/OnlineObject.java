package kk.server.object;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import kk.server.message.MessageFrame;
import kk.server.message.MessageHandlerContext;
import kk.server.message.MessageUtil;

public abstract class OnlineObject implements Poolable {
	Channel channel;
	long id;

	public boolean response(MessageHandlerContext ctx) {
		MessageFrame response = ctx.getResponse();
		if (response == null) {
			return false;
		}
		return sendMsg(MessageUtil.encode(response));
	}

	public boolean sendMsg(ByteBuf buffer) {
		if (isOnline()) {
			BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
			channel.writeAndFlush(frame);
			return true;
		}
		return false;
	}

	public boolean sendMsg(int msgId, byte[] header, byte[] body) {
		ByteBuf buffer = MessageUtil.encode(msgId, header, body);
		return sendMsg(buffer);
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	/**
	 * 主动断开连接
	 */
	public void closeConnection() {
		if (channel != null) {
			channel.close();
		}
	}

	/**
	 * socket连接被断开
	 */
	public void disconnected() {

	}

	public boolean isOnline() {
		return channel != null && channel.isActive();
	}

	@Override
	public void clear() {
		this.id = 0;
		this.channel = null;
	}
}
