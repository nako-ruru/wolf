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
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
//该注解用来指定一个URI，客户端可以通过这个URI来连接到WebSocket。类似Servlet的注解mapping。无需在web.xml中配置。
@ServerEndpoint("/{playerId}")
public class MyWebSocket {
    
    private final Logger logger = LoggerFactory.getLogger(MyWebSocket.class);
     
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
     
    /**
     * 连接建立成功调用的方法
     * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("token")String token, @PathParam("playerId")String playerId, EndpointConfig config) throws IOException {
        this.session = session;
        this.session.getUserProperties().put("playerId", playerId);
        final Game game = SpringContext.getBean(Game.class);
        game.add(session);
        logger.info("有新连接加入！当前在线人数为" + game.onlineCount());
    }
     
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(){
        final Game game = SpringContext.getBean(Game.class);
        game.remove(session);
        logger.info("有一连接关闭！当前在线人数为" + game.onlineCount());
    }
     
    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        logger.info("来自客户端的消息:" + message);
         
        Message m = JsonUtils.toBean(message, Message.class);
        switch(m.getCode()) {
            case "enter":
                new Enter(session, m).invoke();
                break;
            case "exit":
                new Exit(session).invoke();
                break;
            case "prepare":
                new Prepare(session, "true".equals(String.valueOf(m.getProperties().get("flag")))).invoke();
                break;
            case "competeRole":
                new CompeteRole(session, m.getProperties().get("role")).invoke();
                break;
            case "wolfVote":
                new WolfVote(session, m.getProperties().get("playerId")).invoke();
                break;
            case "witchSave":
                new WitchSave(session, m.getProperties().get("playerId")).invoke();
                break;
            case "witchPoison":
                new WitchPoison(session, m.getProperties().get("playerId")).invoke();
                break;
            case "hunterKill":
                new HunterKill(session, m.getProperties().get("playerId")).invoke();
                break;
            case "seerForecast":
                new SeerForecast(session, m.getProperties().get("playerId")).invoke();
                break;
            case "enableMicrophone":
                new EnableMicrophone(session, true).invoke();
                break;
            case "playerVote":
                new PlayerVote(session, m.getProperties().get("playerId")).invoke();
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