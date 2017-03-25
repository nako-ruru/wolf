/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.wolf;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.Session;

/**
 *
 * @author Administrator
 */
public class ServerInfo {
    
    private Collection<RoomInfo> rooms = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private Session currentSession;
    private String intranetAddress;
    private String internetAddress;

    public String getIntranetAddress() {
        return intranetAddress;
    }

    public void setIntranetAddress(String intranetAddress) {
        this.intranetAddress = intranetAddress;
    }

    public String getInternetAddress() {
        return internetAddress;
    }

    public void setInternetAddress(String internetAddress) {
        this.internetAddress = internetAddress;
    }
    
    public void addRoom(RoomInfo room) {
        rooms.add(room);
    }

    public Collection<RoomInfo> getRooms() {
        return rooms;
    }

    public void setRooms(Collection<RoomInfo> rooms) {
        this.rooms = rooms;
    }
    
    public int roomCount() {
        return rooms.size();
    }
    public int playerCount() {
        return rooms.stream()
                .mapToInt(room -> room.playerCount)
                .sum();
    }
    
    public Collection<RoomInfo> rooms() {
        return Collections.unmodifiableCollection(rooms);
    }
    
    public void update(RoomInfo room) {
        RoomInfo existingRoom = rooms.stream()
                .filter(r -> Objects.equals(room.roomId, r.roomId))
                .findAny()
                .orElse(null);
        if(room.playerCount == 0) {
            if(existingRoom != null) {
                rooms.remove(existingRoom);
            } else {
            }
        }
        else {
            if(existingRoom != null) {
                existingRoom.roomId = room.roomId;
                existingRoom.playerCount = room.playerCount;
            } else {
                rooms.add(room);
            }
        }
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(Session currentSession) {
        this.currentSession = currentSession;
    }
    
}
