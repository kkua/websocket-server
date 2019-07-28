package kk.server.message;

import kk.server.object.Poolable;

public class MessageFrame implements Poolable {
	int msgId;
	byte[] header;
	byte[] body;

	public MessageFrame() {
		msgId = 0;
	}

	public MessageFrame(int msgId, byte[] header, byte[] body) {
		this.msgId = msgId;
		this.header = header;
		this.body = body;
	}

	public int getMsgId() {
		return msgId;
	}

	protected void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	public byte[] getHeader() {
		return header;
	}

	protected void setHeader(byte[] header) {
		this.header = header;
	}

	public byte[] getBody() {
		return body;
	}

	protected void setBody(byte[] body) {
		this.body = body;
	}

	@Override
	public void clear() {
		msgId = 0;
		header = null;
		body = null;
	}

}
