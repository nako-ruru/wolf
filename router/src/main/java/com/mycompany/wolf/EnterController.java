/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.wolf;

import com.mycompany.work.framework.spring.SpringContext;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Administrator
 */
@RestController
public class EnterController {
    
    @RequestMapping(value = "/enter", method = RequestMethod.GET)
    public RouteResult enter() {

        return SpringContext.getBean(Router.class).route();
    }
    
    @RequestMapping(value = "/quit", method = RequestMethod.GET)
    public void quitOne(HttpServletRequest req, QuitCommand quit) {
        SpringContext.getBean(Router.class).quitOne(req, quit.getRoomId());
    }
    
}
