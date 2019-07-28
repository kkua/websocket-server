package kk.server.object;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;

public abstract class PoolableFactory<T extends Poolable> implements PooledObjectFactory<T> {

	@Override
	public void destroyObject(PooledObject<T> p) throws Exception {
		p.getObject().clear();
	}

	@Override
	public boolean validateObject(PooledObject<T> p) {
		return true;
	}

	@Override
	public void activateObject(PooledObject<T> p) throws Exception {

	}

	@Override
	public void passivateObject(PooledObject<T> p) throws Exception {
		p.getObject().clear();
	}

}
