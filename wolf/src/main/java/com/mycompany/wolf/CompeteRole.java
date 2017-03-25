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
public class CompeteRole {
    
    private Session session;
    private String competingRole;

    public CompeteRole(Session session, String competingRole) {
        this.session = session;
        this.competingRole = competingRole;
    }
    
    public void invoke() {
        
    }
    
}
