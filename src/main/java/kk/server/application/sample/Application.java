package kk.server.application.sample;

import java.net.InetSocketAddress;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.netty.channel.ChannelFuture;
import kk.server.application.sample.user.User;
import kk.server.object.OnlineObjectManager;
import kk.server.object.PoolableFactory;

@SpringBootApplication
public class Application implements CommandLineRunner {

	@Autowired
	private NettyServer socketServer;

	@Value("${server.ip:0.0.0.0}")
	private String ip;

	@Value("${server.port:8080}")
	private int port;

	public static void main(String[] args) {
		OnlineObjectManager.getInstance().init(User.class, new PoolableFactory<User>() {

			@Override
			public PooledObject<User> makeObject() throws Exception {
				return new DefaultPooledObject<User>(new User());
			}

		});
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... strings) {
		InetSocketAddress address = new InetSocketAddress(ip, port);
		ChannelFuture future = socketServer.run(address);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				socketServer.destroy();
			}
		});
		future.channel().closeFuture().syncUninterruptibly();
	}

}