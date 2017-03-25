/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.wolf;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.Session;
import org.springframework.stereotype.Component;

/**
 *
 * @author Administrator
 */
@Component
public class Router {
    
    private final int ROOM_LIMIT = 4000, PLAYER_LIMIT = 10000;
    
    private final Collection<ServerInfo> servers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    Router() {
    }
    
    public void register(Session session) {
        ServerInfo server = servers.stream()
                .filter(s -> Objects.equals(s.getIntranetAddress(), remoteAddress(session)))
                .findAny()
                .orElse(null);
        if(server != null) {
            server.setCurrentSession(session);
        }
    }
    
    public void unregister(Session session) {
        ServerInfo server = servers.stream()
                .filter(s -> Objects.equals(s.getIntranetAddress(), remoteAddress(session)))
                .findAny()
                .orElse(null);
        if(server != null) {
            server.setCurrentSession(null);
        }
    }
    
    public RouteResult route() {
        ServerInfo serverInfo = null;
        RoomInfo roomInfo = null;
O:      
        for(ServerInfo si : servers) {
            Session s = si.getCurrentSession();
            if(/*s != null && s.isOpen() &&*/  si.playerCount() < PLAYER_LIMIT && si.roomCount() < ROOM_LIMIT) {
I:
                for(RoomInfo room : si.rooms()) {
                    synchronized(room.mutex) {
                        if(room.playerCount < room.availabeCount) {
                            room.playerCount++;
                            serverInfo = si;
                            roomInfo = room;
                            break O;
                        }
                    }
                }
                RoomInfo room = new RoomInfo();
                room.availabeCount = 12;
                room.playerCount = 1;
                room.roomId = UUID.randomUUID().toString();
                serverInfo = si;
                roomInfo = room;
                si.addRoom(room);
                break O;
            }
        }
        RouteResult result = new RouteResult();
        if(serverInfo != null) {
            result.setRoomId(roomInfo.roomId);
            result.setAddress(serverInfo.getInternetAddress());
        }
        return result;
    }   
    
    public void quitOne(HttpServletRequest req, String roomId) {
        String remoteAddr = req.getRemoteAddr();
        ServerInfo server = servers.stream()
                .filter(s -> Objects.equals(s.getIntranetAddress(), remoteAddr))
                .findAny()
                .orElse(null);
        if(server != null) {
            RoomInfo roomInfo = server.getRooms().stream()
                    .filter(r -> Objects.equals(r.roomId, roomId))
                    .findAny()
                    .orElse(null);
            synchronized(roomInfo.mutex) {
                roomInfo.playerCount--;
            }
        }
    }
    
    public void quitOne(Session session, String roomId) {
        ServerInfo server = servers.stream()
                .filter(s -> Objects.equals(s.getIntranetAddress(), remoteAddress(session)))
                .findAny()
                .orElse(null);
        if(server != null) {
            RoomInfo roomInfo = server.getRooms().stream()
                    .filter(r -> Objects.equals(r.roomId, roomId))
                    .findAny()
                    .orElse(null);
            synchronized(roomInfo.mutex) {
                roomInfo.playerCount--;
            }
        }
    }
    
    private static String remoteAddress(Session session) {
        return Optional.ofNullable(session.getUserProperties().get("javax.websocket.endpoint.remoteAddress"))
                .map(Object::toString)
                .orElse("?.?.?.?");
    }
    
    @PostConstruct
    private void postConstruct() throws IOException {
        Properties properties = new Properties();
        InputStream is = Router.class.getResourceAsStream("/server.properties");
        Reader reader = new InputStreamReader(is, Charsets.UTF_8);
        properties.load(reader);
        
        String[] intranetAddresses = properties.getProperty("intranetAddresses").split(",");
        String[] internetAddresses = properties.getProperty("internetAddresses").split(",");
        Preconditions.checkArgument(intranetAddresses.length == internetAddresses.length);
        IntStream.range(0, internetAddresses.length)
                .mapToObj(i -> {
                    ServerInfo info = new ServerInfo();
                    info.setInternetAddress(intranetAddresses[i].trim());
                    info.setIntranetAddress(internetAddresses[i].trim());
                    return info;
                })
                .forEach(servers::add);
    }
    
}
