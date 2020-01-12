# websocket-server

使用Spring Boot和netty，实现WebSocket服务。构建此项目需要使用JDK 1.8

## 消息格式

服务器和客户端采用二进制消息进行通信，其结构如下图
![消息结构](https://raw.githubusercontent.com/kkua/websocket-server/master/protocol.svg?sanitize=true)

## 使用说明

### 1. 继承OnlineObject

每个客户端（即用户）连接到服务器时，都会生成一个对象，它是`OnlineObject`的子类对象，通过不同的类来区分不同的用户类型如区分未登录和已登录用户。所以首先根据需要实现一到多个`OnlineObject`的子类

### 2.初始化OnlineObjectManager

调用`OnlineObjectManager`的`init`方法初始化。`init`方法的原型为`void init(Class<? extends OnlineObject> newlyConnectedObjectClass, PoolableFactory<? extends OnlineObject>... factories)`，`newlyConnectedObjectClass`是客户端刚连接到服务器时使用的类型，`factories`参数是一个或多个`PoolableFactory<? extends OnlineObject>`对象；`PoolableFactory`是一个抽象类，所有继承了`OnlineObject`的类都实例化该泛型抽象类，除非该类没有实际被使用。子类必须使用对象池管理。例如

```java
OnlineObjectManager.getInstance().init(User.class, new PoolableFactory<User>() {

    @Override
    public PooledObject<User> makeObject() throws Exception {
        return new DefaultPooledObject<User>(new User());
    }

});
```
### 3.RequestHandler注解

`@RequestHandler`用于修饰处理请求的方法，注解的值是请求id（消息结构图中的`msgId`字段）。`RequestDispatcher`根据`msgId`自动通过反射调用相应的方法。这些方法的参数列表的类型是`OnlineObject`或`OnlineObject`子类类型（必要，下文使用arg1表示）和`MessageHandlerContext`（可选，下文使用arg2表示）。一个`msgId`可以有多个**不同**的arg1类型RequestHandler方法，但如果arg1为限定为`OnlineObject`类型则所有的对应`msgId`的请求都会分发给该方法，此时不能再其他有处理该`msgId`的RequestHandler。通过arg2可以获得请求消息的数据，也可以生成响应消息数据（之后调用`OnlineObject`的`response`方法将响应消息数据发送给客户端）。`@RequestHandler`必须位于在`@Controller`类中，其他的类使用该注解没有效果。

### 4.配置RequestDispatcher（非必须的配置）

当收到消息的`msgId`没有对应的RequestHandler方法或RequestHandler方法执行时出现异常，`RequestDispatcher`会调用`RequestDispatcherBehavior`中的接口进行异常处理。调用`RequestDispatcher.setDispatcherBehavior`来设置这些异常处理流程。例如

```java
RequestDispatcher.setDispatcherBehavior(new RequestDispatcherBehavior() {

    @Override
    public void requestHasNoCorHandler(OnlineObject object, MessageHandlerContext ctx) {
        // TODO 发现请求没有对应处理方法时

    }

    @Override
    public void onHandlerException(Method handler, OnlineObject object, MessageHandlerContext ctx,
                                   Exception e) {
        // TODO RequestHandler异常处理

    }

});
```

这个配置不是必须的，当没有配置`RequestDispatcher`时，出现相应的情况时`RequestDispatcher`不会调用这些接口。
