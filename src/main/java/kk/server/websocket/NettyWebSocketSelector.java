package kk.server.websocket;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;

public class NettyWebSocketSelector implements ImportSelector, Ordered {

	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		return new String[] { NettyWebsocketInitializer.class.getName() };
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}
}