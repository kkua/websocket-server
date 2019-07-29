# websocket-server

使用Spring Boot和netty，实现WebSocket服务。构建此项目需要使用JDK 1.8

## 消息格式

服务器和客户端采用二进制消息进行通信，其结构如下图
![消息结构](https://raw.githubusercontent.com/kkua/websocket-server/master/protocol.svg?sanitize=true)
