package kk.server.object;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

public enum OnlineObjectManager {
	INSTANCE;

	private static final Logger log = LoggerFactory.getLogger(OnlineObjectManager.class);
	private Map<Channel, OnlineObject> onlineObjectMap = new ConcurrentHashMap<>();
	private Class<? extends OnlineObject> newlyConnObjClass = null;

	public static OnlineObjectManager getInstance() {
		return INSTANCE;
	}

	public void addOnlineObjectPool(PoolableFactory<OnlineObject> factory) {
		PooledObjectManager.addPoolablePool(factory);
	}

	@SafeVarargs
	public final void init(Class<? extends OnlineObject> newlyConnectedObjectClass,
			PoolableFactory<? extends OnlineObject>... factories) {
		for (PoolableFactory<? extends OnlineObject> factory : factories) {
			PooledObjectManager.addPoolablePool(factory);
		}
		this.newlyConnObjClass = newlyConnectedObjectClass;
	}

	public void addChannel(Channel channel) {
		OnlineObject object = null;
		try {
			object = PooledObjectManager.borrowObject(newlyConnObjClass);
			object.setChannel(channel);
			onlineObjectMap.put(channel, object);
		} catch (Exception e) {
			log.error("Failed to create OnlineObject when receive new connection", e);
			channel.close();
		}
	}

	public void replace(OnlineObject oldObj, OnlineObject newObj) {
		Channel channel = oldObj.channel;
		newObj.setChannel(channel);
		if (onlineObjectMap.containsKey(channel)) {
			oldObj.setChannel(null);
		}
		onlineObjectMap.put(channel, newObj);
		PooledObjectManager.returnObject(oldObj);
	}

	public OnlineObject getOnlineObject(Channel channel) {
		return onlineObjectMap.get(channel);
	}

	public OnlineObject disconnected(Channel channel) {
		OnlineObject onlineObject = onlineObjectMap.remove(channel);
		if (onlineObject != null) {
			onlineObject.disconnected();
			PooledObjectManager.returnObject(onlineObject);
		}
		return onlineObject;
	}

}
