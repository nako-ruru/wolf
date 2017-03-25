/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.wolf;

import com.mycompany.work.framework.spring.SpringContext;
import javax.websocket.Session;

/**
 *
 * @author Administrator
 */
public class Enter {
    
    private final Message enter;
    private final Session session;
    
    public Enter(Session session, Message enter) {
        this.session = session;
        this.enter = enter;
    }
    
    public void invoke() {
        String roomId = enter.getProperties().get("roomId");
        SpringContext.getBean(Game.class).enter(session, roomId);
    }
    
}
