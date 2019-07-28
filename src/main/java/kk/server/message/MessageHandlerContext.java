package kk.server.message;

import io.netty.buffer.ByteBuf;
import kk.server.object.Poolable;

public class MessageHandlerContext implements Poolable {
	private MessageFrame request;
	private MessageFrame response;

	public MessageHandlerContext() {
		clear();
	}

	public MessageHandlerContext(int msgId, byte[] header, byte[] body) throws Exception {
		this();
		request = MessageUtil.createMessage(msgId, header, body);
	}

	protected void setRequest(int msgId, byte[] header, byte[] body) throws Exception {
		request = MessageUtil.createMessage(msgId, header, body);
	}

	protected MessageHandlerContext setRequest(MessageFrame request) {
		this.request = request;
		return this;
	}

	public MessageHandlerContext createResponse(int msgId, byte[] header, byte[] body) throws Exception {
		response = MessageUtil.createMessage(msgId, header, body);
		return this;
	}

	public ByteBuf getEncodedResponse() {
		return MessageUtil.encode(response);
	}

	public MessageFrame getRequest() {
		return request;
	}

	protected void setResponse(MessageFrame response) {
		this.response = response;
	}

	public MessageFrame getResponse() {
		return response;
	}

	public void clear() {
		request = null;
		response = null;
	}

}
