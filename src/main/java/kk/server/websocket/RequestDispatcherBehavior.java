package kk.server.websocket;

import java.lang.reflect.Method;

import kk.server.message.MessageHandlerContext;
import kk.server.object.OnlineObject;

public interface RequestDispatcherBehavior {

	/**
	 * 收到一个没有对应处理方法的请求
	 * @param object
	 * @param ctx
	 */
	public void requestHasNoCorHandler(OnlineObject object, MessageHandlerContext ctx);

	/**
	 * RequestHandler异常处理
	 * @param handler
	 * @param object
	 * @param ctx
	 * @param e
	 */
	public void onHandlerException(Method handler, OnlineObject object, MessageHandlerContext ctx, Exception e);

}
