package kk.server.application.sample;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import kk.server.message.MessageHandlerContext;
import kk.server.object.OnlineObject;
import kk.server.websocket.RequestDispatcher;
import kk.server.websocket.RequestDispatcherBehavior;


@Component
public class RequestDispatcherInitializer implements BeanPostProcessor {

	@Autowired
	public RequestDispatcherInitializer(@Autowired ApplicationContext applicationContext) {
		RequestDispatcher.init(16, applicationContext, new RequestDispatcherBehavior() {

			@Override
			public void requestHasNoCorHandler(OnlineObject object, MessageHandlerContext ctx) {
				// TODO 发现请求没有对应处理方法时

			}
		});
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		RequestDispatcher.scanSpringBean(bean);
		return bean;
	}

}
