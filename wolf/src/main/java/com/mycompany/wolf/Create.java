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
public class Create {

    private final Session session;
    
    public Create(Session session) {
        this.session = session;
    }

    public void invoke() {
        SpringContext.getBean(Game.class).create(session);
    }
    
}
