/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.wolf;

import com.mycompany.work.framework.spring.SpringContext;
import java.io.IOException;
import javax.websocket.Session;

/**
 *
 * @author Administrator
 */
public class Exit {
    
    private final Session session;
    
    public Exit(Session session) {
        this.session = session;
    }
    
    public void invoke() throws IOException {
        String playerId = (String) session.getUserProperties().get("playerId");
        SpringContext.getBean(Game.class).exit(playerId);
    }
}
