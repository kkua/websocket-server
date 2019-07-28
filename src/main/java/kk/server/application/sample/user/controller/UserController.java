package kk.server.application.sample.user.controller;

import org.springframework.stereotype.Controller;

import kk.server.message.MessageFrame;
import kk.server.message.MessageHandlerContext;
import kk.server.object.OnlineObject;
import kk.server.websocket.RequestHandler;

@Controller
public class UserController {
	
	@RequestHandler(1)
	public void echo(OnlineObject user, MessageHandlerContext ctx) throws Exception {
		MessageFrame req = ctx.getRequest();
		user.response(ctx.createResponse(2, req.getHeader(), req.getBody()));
	}
	
}
