package kk.server.websocket.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import kk.server.websocket.NettyWebSocketSelector;
import kk.server.websocket.NettyWebSocketInitializer;


@Configuration
@Import(NettyWebSocketSelector.class)
@ConditionalOnMissingBean(NettyWebSocketInitializer.class)
public class NettyWebSocketAutoConfigure {
	
}
