/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.wolf;

import javax.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Administrator
 */
@RestController
public class LoginController {
    
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public void login(HttpSession session, LoginCommand command) {
        session.setAttribute("playerId", command.getPlayerId());
    }
    
}
