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
public class HunterKill {
    
    private final Session session;
    private final String playerId;

    public HunterKill(Session session, String playerId) {
        this.session = session;
        this.playerId = playerId;
    }
    
    public void invoke() {
        SpringContext.getBean(Game.class).hunterKill(session, playerId);
    }
    
}