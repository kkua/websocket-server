package kk.server.message;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kk.server.object.PoolableFactory;
import kk.server.object.PooledObjectManager;

public class MessageUtil {

	static {
		PooledObjectManager.addPoolablePool(new PoolableFactory<MessageFrame>() {

			@Override
			public PooledObject<MessageFrame> makeObject() throws Exception {
				return new DefaultPooledObject<MessageFrame>(new MessageFrame());
			}

		});
		PooledObjectManager.addPoolablePool(new PoolableFactory<MessageHandlerContext>() {

			@Override
			public PooledObject<MessageHandlerContext> makeObject() throws Exception {
				return new DefaultPooledObject<MessageHandlerContext>(new MessageHandlerContext());
			}
		});
	}

	public static MessageHandlerContext createContext(ByteBuf msgBuf) throws Exception {
		MessageFrame message = decode(msgBuf);
		if (message == null) {
			return null;
		}
		return createMessageHandlerContext().setRequest(message);
	}

	public static MessageFrame createMessage(int msgId, byte[] header, byte[] body) throws Exception {
		MessageFrame frame = PooledObjectManager.borrowObject(MessageFrame.class);
		frame.setMsgId(msgId);
		frame.setHeader(header);
		frame.setBody(body);
		return frame;
	}

	public static MessageFrame decode(ByteBuf msgBuf) throws Exception {
		int dataLength = msgBuf.readableBytes();
		if (dataLength < 4) {
			return null;
		}
		int msgId = msgBuf.readShort();
		int headerLen = msgBuf.readShort();

		byte[] header = new byte[headerLen];
		if (headerLen > 0) {
			msgBuf.readBytes(header);
		}
		int bodyLen = msgBuf.readableBytes();
		byte[] body = new byte[bodyLen];
		if (bodyLen > 0) {
			msgBuf.readBytes(body);
		}
		return createMessage(msgId, header, body);
	}

	public static ByteBuf encode(MessageFrame message) {
		int msgId = message.getMsgId();
		byte[] header = message.getHeader();
		byte[] body = message.getBody();
		return encode(msgId, header, body);
	}

	public static ByteBuf encode(int msgId, byte[] header, byte[] body) {
		int headerLen = header == null ? 0 : header.length;
		int bodyLen = body == null ? 0 : body.length;
		int msgLength = 4 + headerLen + bodyLen;
		ByteBuf buffer = Unpooled.directBuffer(msgLength);
		buffer.writeShort(msgId);
		buffer.writeShort(headerLen);
		if (headerLen > 0) {
			buffer.writeBytes(header);
		}
		if (bodyLen > 0) {
			buffer.writeBytes(body);
		}
		return buffer;
	}

	public static MessageHandlerContext createMessageHandlerContext() throws Exception {
		return PooledObjectManager.borrowObject(MessageHandlerContext.class);
	}

	public static void returnMessageHandlerContext(MessageHandlerContext ctx) {
		MessageFrame request = ctx.getRequest();
		if (request != null) {
			PooledObjectManager.returnObject(request);
		}
		MessageFrame response = ctx.getResponse();
		if (response != null) {
			PooledObjectManager.returnObject(response);
		}
		PooledObjectManager.returnObject(ctx);
	}

}
