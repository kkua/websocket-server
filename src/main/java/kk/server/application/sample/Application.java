package kk.server.application.sample;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import kk.server.application.sample.user.User;
import kk.server.message.MessageHandlerContext;
import kk.server.object.OnlineObject;
import kk.server.object.OnlineObjectManager;
import kk.server.object.PoolableFactory;
import kk.server.websocket.RequestDispatcher;
import kk.server.websocket.RequestDispatcherBehavior;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		RequestDispatcher.setDispatcherBehavior(new RequestDispatcherBehavior() {

			@Override
			public void requestHasNoCorHandler(OnlineObject object, MessageHandlerContext ctx) {
				// TODO 发现请求没有对应处理方法时

			}
		});
		OnlineObjectManager.getInstance().init(User.class, new PoolableFactory<User>() {

			@Override
			public PooledObject<User> makeObject() throws Exception {
				return new DefaultPooledObject<User>(new User());
			}

		});
		SpringApplication.run(Application.class, args);
	}

}