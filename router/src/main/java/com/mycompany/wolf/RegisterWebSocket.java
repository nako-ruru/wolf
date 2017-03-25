package com.mycompany.wolf;

import com.mycompany.work.framework.spring.SpringContext;
import com.mycompany.work.util.JsonUtils;
import java.io.IOException;
import javax.websocket.EndpointConfig;
 
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
//该注解用来指定一个URI，客户端可以通过这个URI来连接到WebSocket。类似Servlet的注解mapping。无需在web.xml中配置。
@ServerEndpoint("/")
public class RegisterWebSocket {
    
    private final Logger logger = LoggerFactory.getLogger(RegisterWebSocket.class);
     
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
     
    /**
     * 连接建立成功调用的方法
     * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
        this.session = session;
        SpringContext.getBean(Router.class).register(session);
    }
     
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(){
        SpringContext.getBean(Router.class).unregister(session);
    }
     
    /**
     * 收到客户端消息后调用的方法
     * @param messageText 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String messageText, Session session) throws IOException {
        logger.info("来自客户端的消息:" + messageText);
         
        Message message = JsonUtils.toBean(messageText, Message.class);
        switch(message.getCode()) {
            case "exit":
                String roomId = message.getProperties().get("roomId");
                SpringContext.getBean(Router.class).quitOne(session, roomId);
                break;
        }
    }
     
    /**
     * 发生错误时调用
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
        logger.error("", error);
    }
    
}