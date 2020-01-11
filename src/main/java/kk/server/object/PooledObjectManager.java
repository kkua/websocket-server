package kk.server.object;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class PooledObjectManager {

	private static final Logger logger = LoggerFactory.getLogger(PooledObjectManager.class);

	private static Map<Class<? extends Poolable>, GenericObjectPool<? extends Poolable>> objectPoolMap = new HashMap<>();
	private static GenericObjectPoolConfig<? extends Poolable> defaultPoolConfig = new GenericObjectPoolConfig<>();

	static {
		defaultPoolConfig.setMaxTotal(1 << 15); // 16384‬
		defaultPoolConfig.setMinIdle(16);
		defaultPoolConfig.setMaxIdle(128);
		defaultPoolConfig.setMaxWaitMillis(5000L);
	}

	private static <T extends Poolable> Class<T> getGenericClass(Class<?> t) {
		Class<T> tClass = (Class<T>) ((ParameterizedType) t.getGenericSuperclass()).getActualTypeArguments()[0];
		return tClass;
	}

	public static <T extends Poolable> boolean addPoolablePool(GenericObjectPool<T> objectPool) {
		return objectPoolMap.putIfAbsent(getGenericClass(objectPool.getClass()), objectPool) == null;
	}

	public static <T extends Poolable> boolean addPoolablePool(PoolableFactory<T> factory,
			GenericObjectPoolConfig<T> poolConfig) {
		return objectPoolMap.putIfAbsent(getGenericClass(factory.getClass()),
				new GenericObjectPool<T>(factory, poolConfig)) == null;
	}

	public static <T extends Poolable> boolean addPoolablePool(PoolableFactory<T> factory) {
		return objectPoolMap.putIfAbsent(getGenericClass(factory.getClass()),
				new GenericObjectPool<T>(factory, (GenericObjectPoolConfig<T>) defaultPoolConfig)) == null;
	}

	public static <T extends Poolable> T borrowObject(Class<T> clazz) throws Exception {
		return ((GenericObjectPool<T>) objectPoolMap.get(clazz)).borrowObject();
	}

	public static <T extends Poolable> void returnObject(T p) {
		GenericObjectPool<T> pool = ((GenericObjectPool<T>) objectPoolMap.get(p.getClass()));
		if (pool == null) {
			logger.error("Object " + p.getClass() + " didn't configured to use object pool.");
			return;
		}
		pool.returnObject(p);
	}

}
