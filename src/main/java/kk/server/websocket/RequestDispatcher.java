package kk.server.websocket;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import io.netty.channel.Channel;
import kk.server.message.MessageHandlerContext;
import kk.server.message.MessageUtil;
import kk.server.object.OnlineObject;
import kk.server.object.OnlineObjectManager;

public class RequestDispatcher {

	private static final Logger logger = LoggerFactory.getLogger(RequestDispatcher.class);

	// <msgId, <onlineObjectParamType, clazzMehtodPair>>
	private static HashMap<Integer, Map<Class<?>, ClazzMehtodPair>> handlerMethodMap = new HashMap<>();

	private static ApplicationContext context;

	private static RequestDispatcherBehavior dispatcherBehavior = null;

	private static ExecutorService workerThreadPool = null;
	
	private static int shutdownTimeout = 3;

	public static void init(int workerThreadCount, ApplicationContext applicationContext) {
		workerThreadPool = Executors.newFixedThreadPool(workerThreadCount);
		context = applicationContext;
	}

	public static void setDispatcherBehavior(RequestDispatcherBehavior dispatcherBehavior) {
		RequestDispatcher.dispatcherBehavior = dispatcherBehavior;
	}

	public static <T> T getBean(Class<T> clazz) {
		return context.getBean(clazz);
	}

	protected static void scanSpringBean(Object bean) {
		Class<? extends Object> clazz = bean.getClass();
		Method[] methods = ReflectionUtils.getAllDeclaredMethods(clazz);
		if (methods != null) {
			for (Method method : methods) {
				RequestHandler handler = method.getAnnotation(RequestHandler.class);
				if (handler != null) {
					addHandler(handler.value(), clazz, method);
				}
			}
		}
	}

	private static void addHandler(int msgId, Class<?> clazz, Method method) {
		Class<?>[] paramTypes = method.getParameterTypes();
		Class<?> onlineObjectParamType = null;
		for (Class<?> paramType : paramTypes) {
			if (OnlineObject.class.isAssignableFrom(paramType)) {
				onlineObjectParamType = paramType;
			}
		}
		Map<Class<?>, ClazzMehtodPair> clazzMethodMap = handlerMethodMap.get(msgId);
		if (clazzMethodMap == null) {
			clazzMethodMap = new HashMap<>();
			clazzMethodMap.put(onlineObjectParamType, new ClazzMehtodPair(clazz, method));
			handlerMethodMap.put(msgId, clazzMethodMap);
		} else {
			if (clazzMethodMap.containsKey(null) || clazzMethodMap.containsKey(OnlineObject.class)
					|| onlineObjectParamType == null || onlineObjectParamType == OnlineObject.class
					|| clazzMethodMap.containsKey(onlineObjectParamType)) {
				StringBuilder builder = new StringBuilder("Some methods in [");
				for (ClazzMehtodPair pair : clazzMethodMap.values()) {
					builder.append(pair.getMethod()).append(", ");
				}
				builder.append(method).append(']')
						.append(" are wrong, confused to determin which handler should be used when recieve request ")
						.append(msgId).append('.');
				throw new RuntimeException(builder.toString());
			} else {
				clazzMethodMap.put(onlineObjectParamType, new ClazzMehtodPair(clazz, method));
			}
		}
	}

	private static Object[] buildHandlerArgs(Method method, OnlineObject object, MessageHandlerContext context) {
		Class<?>[] paramTypes = method.getParameterTypes();
		int methodParamCount = paramTypes.length;
		if (methodParamCount == 0) {
			return null;
		}
		List<Object> handlerArgs = new ArrayList<Object>();
		boolean rightObject = false;
		for (int i = 0; i < methodParamCount; ++i) {
			if (paramTypes[i].isInstance(object)) {
				handlerArgs.add(object);
				rightObject = true;
			} else if (paramTypes[i].isInstance(context)) {
				handlerArgs.add(context);
			} else {
				handlerArgs.add(null);
			}
		}
		if (rightObject) {
			return handlerArgs.toArray();
		} else {
			return null;
		}
	}

	public static void dispatchRequest(Channel channel, MessageHandlerContext ctx) {
		if (ctx == null) {
			channel.close();
			return;
		}
		OnlineObject object = OnlineObjectManager.getInstance().getOnlineObject(channel);
		workerThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				int msgId = ctx.getRequest().getMsgId();
				Map<Class<?>, ClazzMehtodPair> pairMap = handlerMethodMap.get(msgId);
				if (pairMap == null) {
					onNoCorHandler(ctx, object);
					return;
				}
				ClazzMehtodPair clazzMehtodPair = null;
				for (Entry<Class<?>, ClazzMehtodPair> entry : pairMap.entrySet()) {
					if (pairMap.size() == 1) {
						clazzMehtodPair = entry.getValue();
						break;
					}
					if (entry.getKey().isInstance(object)) {
						clazzMehtodPair = entry.getValue();
					}
				}
				Method handler = clazzMehtodPair.getMethod();
				Object[] handlerArgs = buildHandlerArgs(handler, object, ctx);
				if (handlerArgs == null) {
					onNoCorHandler(ctx, object);
					return;
				}
				try {
					handler.invoke(context.getBean(clazzMehtodPair.getClazz()), handlerArgs);
				} catch (Exception e) {
					if (dispatcherBehavior != null) {
						dispatcherBehavior.onHandlerException(handler, object, ctx, e);
					} else {
						logger.error("Failed invoke handler, msgId: " + msgId, e);
					}
				} finally {
					MessageUtil.returnMessageHandlerContext(ctx);
				}
			}

			private void onNoCorHandler(MessageHandlerContext ctx, OnlineObject object) {
				if (dispatcherBehavior != null) {
					dispatcherBehavior.requestHasNoCorHandler(object, ctx);
				}
				MessageUtil.returnMessageHandlerContext(ctx);
			}
		});
	}

	public static void shutdownGracefully() {
		if (workerThreadPool == null) {
			return;
		}
		workerThreadPool.shutdown();
		try {
			workerThreadPool.awaitTermination(shutdownTimeout, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("thread pool awaitTermination interrupted:", e);
		} finally {
			workerThreadPool.shutdownNow();
		}
	}
	
}

class ClazzMehtodPair {
	private Class<?> clazz;
	private Method method;

	public ClazzMehtodPair(Class<?> clazz, Method method) {
		super();
		this.clazz = clazz;
		this.method = method;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public Method getMethod() {
		return method;
	}

}