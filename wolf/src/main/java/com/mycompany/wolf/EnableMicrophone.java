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
public class EnableMicrophone {

    private final Session session;
    private final boolean flag;

    public EnableMicrophone(Session session, boolean flag) {
        this.session = session;
        this.flag = flag;
    }
    
    public void invoke() {
        SpringContext.getBean(Game.class).enableMicrohpone(session, flag);
    }
    
}
