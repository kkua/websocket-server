package kk.server.websocket;

import java.net.InetSocketAddress;
import java.util.Map;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Controller;

import io.netty.channel.ChannelFuture;

public class NettyWebsocketInitializer extends ApplicationObjectSupport implements SmartInitializingSingleton, CommandLineRunner {

	@Value("${server.ip:0.0.0.0}")
	private String ip;

	@Value("${server.port:8080}")
	private int port;
	
	
    @Override
    public void afterSingletonsInstantiated() {
    	registRequestHandler();
    }

    protected void registRequestHandler() {
        ApplicationContext context = getApplicationContext();
        if (context != null) {
             Map<String, Object> endpointBeans = context.getBeansWithAnnotation(Controller.class);
            for (Object bean : endpointBeans.values()) {
                RequestDispatcher.scanSpringBean(bean);
            }
        }

        RequestDispatcher.init(16, getApplicationContext());
    }
	
	@Override
	public void run(String... strings) {
		NettyServer socketServer = new NettyServer();
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
