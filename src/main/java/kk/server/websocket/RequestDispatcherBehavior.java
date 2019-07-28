package kk.server.websocket;

import kk.server.message.MessageHandlerContext;
import kk.server.object.OnlineObject;

public interface RequestDispatcherBehavior {

	/**
	 * 收到一个没有对应处理方法的请求
	 * @param object
	 * @param frame 
	 */
	public void requestHasNoCorHandler(OnlineObject object, MessageHandlerContext ctx);
	
}
