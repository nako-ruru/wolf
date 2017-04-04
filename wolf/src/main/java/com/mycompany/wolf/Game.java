/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.wolf;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import com.mycompany.work.util.JsonUtils;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.websocket.Session;
import org.springframework.stereotype.Component;

/**
 *
 * @author Administrator
 */
@Component
public class Game {
     
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private final Collection<Session> sessions = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());
    
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    
    private final Object mutex = new Object();
    
    public void add(Session session) {
        sessions.add(session);
    }
    public void remove(Session session) {
        sessions.remove(session);
    }
    public int onlineCount() {
        return sessions.size();
    }
    
    public void rooms(Session session) {
        List roomInfos = rooms.values().stream()
                .map(room -> ImmutableMap.of("roomId", room.roomId, "count", room.count()))
                .collect(Collectors.toCollection(LinkedList::new));
        Map<String, Object> resp = ImmutableMap.of(
                "code", "listRoomsResp",
                "properties", roomInfos
        );
        String json = JsonUtils.toString(resp);
        session.getAsyncRemote().sendText(json);
    }
    
    public String create(Session session) {
        Room room = new Room();
        rooms.put(room.roomId, room);
        room.addPlayer(session);
        return room.roomId;
    }
    
    public void enter(Session session, String roomId) {
        Room room = rooms.get(roomId);
        if(room == null) {
            Room newRoom = new Room();
            newRoom.roomId = roomId;
            rooms.putIfAbsent(roomId, newRoom);
            room = rooms.get(roomId);
        }
        room.addPlayer(session);
    }
    
    public void prepare(Session session, boolean flag) {
        String playerId = (String) session.getUserProperties().get("playerId");
        rooms.values().stream()
                .filter(r -> r.contains(playerId))
                .findAny()
                .ifPresent(room -> {
                    room.prepare(playerId, flag);
                });
    }
    
    public void competeRole(Session session, String role) {
        String playerId = (String) session.getUserProperties().get("playerId");
        rooms.values().stream()
                .filter(r -> r.contains(playerId))
                .findAny()
                .ifPresent(room -> {
                    room.competeRole(playerId, role);
                });
    }
    
    public void exit(String playerId) throws IOException {
        Map.Entry<String, Room> entry = rooms.entrySet().stream()
                .filter(e -> e.getValue().contains(playerId))
                .findAny()
                .orElse(null);
        if(entry != null) {
            entry.getValue().removePlayer(playerId);
            if(entry.getValue().isEmpty()) {
                rooms.remove(entry.getKey(), entry.getValue());
            }
        }
    }
    
    public void wolfVote(Session session, String votedPlayerId) {
        String playerId = (String) session.getUserProperties().get("playerId");
        rooms.values().stream()
                .filter(r -> r.contains(playerId))
                .findAny()
                .ifPresent(room -> {
                    room.wolfVote(session, votedPlayerId);
                });
    }

    public void witchSave(Session session, String savedPlayerId) {
        String playerId = (String) session.getUserProperties().get("playerId");
        rooms.values().stream()
                .filter(r -> r.contains(playerId))
                .findAny()
                .ifPresent(room -> {
                    room.witchSave(session, savedPlayerId);
                });
    }
    
    public void witchPoison(Session session, String poisonedPlayerId) {
        String playerId = (String) session.getUserProperties().get("playerId");
        rooms.values().stream()
                .filter(r -> r.contains(playerId))
                .findAny()
                .ifPresent(room -> {
                    room.witchPoison(session, poisonedPlayerId);
                });
    }
    
    public void hunterKill(Session session, String killedPlayerId) {
        String playerId = (String) session.getUserProperties().get("playerId");
        rooms.values().stream()
                .filter(r -> r.contains(playerId))
                .findAny()
                .ifPresent(room -> {
                    room.hunterKills(session, killedPlayerId);
                });
    }
    
    public void seerForcast(Session session, String forcastedPlayerId) {
        String playerId = (String) session.getUserProperties().get("playerId");
        rooms.values().stream()
                .filter(r -> r.contains(playerId))
                .findAny()
                .ifPresent(room -> {
                    room.seerForecasts(session, forcastedPlayerId);
                });
    }
    
    public void enableMicrohpone(Session session, boolean flag) {
        String playerId = (String) session.getUserProperties().get("playerId");
        rooms.values().stream()
                .filter(r -> r.contains(playerId))
                .findAny()
                .ifPresent(room -> {
                    room.enableMicrophone(session, flag);
                });
    }
    
    public void playerVote(Session session, String votedPlayerId) {
        String playerId = (String) session.getUserProperties().get("playerId");
        rooms.values().stream()
                .filter(r -> r.contains(playerId))
                .findAny()
                .ifPresent(room -> {
                    room.playerVote(session, votedPlayerId);
                });
    }
    
}
