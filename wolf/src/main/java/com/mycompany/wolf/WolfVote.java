/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.wolf;

import javax.websocket.Session;

/**
 *
 * @author Administrator
 */
public class WolfVote {
    
    private Session session;
    private String playerId;

    public WolfVote(Session session, String playerId) {
        this.session = session;
        this.playerId = playerId;
    }
    
    public void invoke() {
        
    }
    
}
