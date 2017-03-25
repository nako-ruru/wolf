/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.wolf;

import com.google.gson.Gson;

/**
 *
 * @author Administrator
 */
public class RoomInfo {
    
    public String roomId;
    public int playerCount;
    public int availabeCount;
    
    public final Object mutex = new Object();

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
    
}
