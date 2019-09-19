package kk.server.websocket;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

//@Component
public class NettyServer {

	private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

	private final EventLoopGroup bossGroup = new NioEventLoopGroup();
	private final EventLoopGroup workerGroup = new NioEventLoopGroup();

	private Channel channel;

	/**
	 * 启动服务
	 */
	public ChannelFuture run(InetSocketAddress address) {

		ChannelFuture f = null;
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ServerChannelInitializer()).option(ChannelOption.SO_BACKLOG, 1000)
					.childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.TCP_NODELAY, true);
			f = b.bind(address).syncUninterruptibly();
			channel = f.channel();
		} catch (Exception e) {
			logger.error("Netty start error", e);
		} finally {
			if (f != null && f.isSuccess()) {
				logger.info("Netty server listening " + address.getHostName() + " on port " + address.getPort()
						+ " and ready for connections...");
			} else {
				logger.error("Netty server start up Error!");
			}
		}
		return f;
	}

	public void destroy() {
		logger.info("Shutdown Netty Server...");
		if (channel != null) {
			channel.close();
		}
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
		logger.info("Shutdown Netty Server Success!");
	}
}
