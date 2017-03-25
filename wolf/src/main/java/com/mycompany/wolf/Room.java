/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.wolf;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.gson.Gson;
import com.mycompany.work.framework.spring.SpringContext;
import com.mycompany.work.util.JsonUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.websocket.Session;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

/**
 *
 * @author Administrator
 */
public class Room {
    
    private static final String VILLAGER = "villager";
    private static final String WOLF = "wolf";
    private static final String WITCH = "witch";
    private static final String HUNTER = "hunter";
    private static final String SEER = "seer";
    
    private static final long COMPETE_ROLE_DURATION = TimeUnit.SECONDS.toMillis(15);
    private static final long WOLVIES_KILL_VILLAGERS_DURATION = TimeUnit.SECONDS.toMillis(15);
    
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    
    public final String roomId = UUID.randomUUID().toString();
    
    private final List<Session> sessions = new LinkedList<>();
    
    private final Map<String, RoleCompetition> competeRoles = new LinkedHashMap();
    private final Map<String, WolfVoting> wolfVotings = new LinkedHashMap<>();
    private final Map<String, WitchSaving> witchSavings = new LinkedHashMap<>();
    private final Map<String, WitchPoisoning> witchPoisonings = new LinkedHashMap<>();
    private final Map<String, HunterKilling> hunterKillings = new LinkedHashMap<>();
    private final Map<String, SeerForcasting> seerForcastings = new LinkedHashMap<>();
    private final Map<String, PlayerVoting> playerVotings = new LinkedHashMap<>();
    
    private final Set<String> dead = new HashSet();
    
    private int turnOffset;
    private int firstTurn;
    
    private final Object mutex = new Object();
    
    
    public void addPlayer(Session session) {
        synchronized(mutex) {
            sessions.add(session);
            List<Map<String, String>> roomInfo = sessions.stream()
                    .map(s -> ImmutableMap.of("playerId", getPlayerId(s)))
                    .collect(Collectors.toCollection(LinkedList::new ));
            Map<String, Object> m = ImmutableMap.of(
                    "code", "enterResp",
                    "properties", roomInfo
            );
            String json = new Gson().toJson(m);
            sessions.stream().forEach(s -> {
                s.getAsyncRemote().sendText(json);
            });
        }
    }
    
    public void removePlayer(String playerId) throws IOException {
        synchronized(mutex) {
            sessions.removeIf(equalityPredicate(playerId));
            List<Map<String, String>> roomInfo = sessions.stream()
                    .map(s -> ImmutableMap.of("playerId", getPlayerId(s)))
                    .collect(Collectors.toCollection(LinkedList::new ));
            Map<String, Object> m = ImmutableMap.of(
                    "code", "exitResp",
                    "properties", roomInfo
            );
            String json = new Gson().toJson(m);
            sessions.stream().forEach(s -> {
                s.getAsyncRemote().sendText(json);
            });
        }
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(3000)
                .setConnectTimeout(3000)
                .build();
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .setDefaultRequestConfig(requestConfig).build();
        httpclient.start();
        try {
            String routerAddress = SpringContext.getBean("routerAddress");
             
            httpclient.execute(new HttpGet(routerAddress), new FutureCallback<HttpResponse>() {
               @Override
               public void completed(HttpResponse t) {
               }
               @Override
               public void failed(Exception excptn) {
               }
               @Override
               public void cancelled() {
               }
            });
         } finally {
             httpclient.close();
         }
    }
    
    public void prepare(String playerId, boolean flag) {
        Session session = sessions.stream()
                .filter(s -> Objects.equals(getPlayerId(s), playerId))
                .findAny()
                .orElse(null);
        if(session != null) {
            session.getUserProperties().put("prepared", flag);
            Map<String, Object> m = ImmutableMap.of(
                    "code", "prepareResp",
                    "properties", ImmutableMap.of("playerId", playerId, "prepare", Boolean.toString(flag))
            );
            String json = new Gson().toJson(m);
            sessions.stream().forEach(s -> {
                s.getAsyncRemote().sendText(json);
            });
            if(sessions.stream().allMatch(s -> Boolean.TRUE.equals(isPrepared(s)))) {
                notifyCompeteRole();
            }
        }
    }

    public boolean contains(String playerId) {
        synchronized(mutex) {
            return sessions.stream()
                    .anyMatch(equalityPredicate(playerId));
        }
    }
    
    public int availableCount() {
        return 12;
    }
    
    public int count() {
        synchronized (mutex) {
            return sessions.size();
        }
    }
    
    public boolean isEmpty() {
        synchronized(mutex) {
            return sessions.isEmpty();
        }
    }
    
    /**
     * 通知玩家竞选角色
     */
    private void notifyCompeteRole() {
        Map<String, Object> m = ImmutableMap.of(
                "code", "notifyCompeteRole",
                "properties", availableRoles()
        );
        String json = new Gson().toJson(m);
        sessions.stream().forEach(s -> {
            s.getAsyncRemote().sendText(json);
        });
        ScheduledFuture[] holder = new ScheduledFuture[1];
        holder[0] = scheduledExecutorService.schedule(() -> {
            holder[0].cancel(true);
            assignRoles();
            notifyWolvesKillVillagers();
        }, COMPETE_ROLE_DURATION, TimeUnit.MILLISECONDS);
    }
    
    public void competeRole(String playerId, String role) {
        competeRoles.put(playerId, new RoleCompetition(playerId, role));
    }
    
    private Collection<String> availableRoles() {
        return Arrays.asList("wolf", "witch");
    }
    
    /**
     * 通知玩家赋予角色
     */
    private void assignRoles() {
        Multiset<String> roleCounts = HashMultiset.create(roleCounts());
        Map<String, String> roleMap = new HashMap();
        competeRoles.values().stream()
                .filter(c -> roleCounts.remove(c.role))
                .forEach(c -> {
                    roleMap.put(c.playerId, c.role);
                });
        List<String> restPlayerId = sessions.stream()
                .map(s -> getPlayerId(s))
                .filter(s -> !roleMap.containsKey(s))
                .collect(Collectors.toList());
        Collections.shuffle(restPlayerId);
        Iterator<String> restRoleIt = roleCounts.iterator();
        Iterator<String> restPlayerIdIt = restPlayerId.iterator();
        for(;restRoleIt.hasNext();) {
            String role = restRoleIt.next();
            String playerId = restPlayerIdIt.next();
            roleMap.put(playerId, role);
        }
        sessions.stream().forEach(s -> {
            s.getUserProperties().put("role", roleMap.get(getPlayerId(s)));
        });
        
        List<ImmutableMap<String, String>> assignedRoles = roleMap.entrySet().stream()
                .map(entry -> ImmutableMap.of("playerId", entry.getKey(), "role", entry.getValue()))
                .collect(Collectors.toCollection(LinkedList::new ));
        Map<String, Object> assignRoles = ImmutableMap.of(
                "code", "assignRoles",
                "properties", assignedRoles
        );
        String jsonText = JsonUtils.toString(assignRoles);
        sessions.stream().forEach(s -> {
            s.getAsyncRemote().sendText(jsonText);
        });
    }
    
    private Multiset<String> roleCounts() {
        return ImmutableMultiset.<String>builder()
                .addCopies(HUNTER, 1)
                .addCopies(WITCH, 1)
                .addCopies(SEER, 1)
                .addCopies(WOLF, availableCount() / 3)
                .addCopies(VILLAGER, availableCount() - 3 - (availableCount() / 3))
                .build();
    }
    
    /**
     * 通知狼杀村民
     */
    private void notifyWolvesKillVillagers() {
        wolfVotings.clear();
        
        Map<String, Object> assignRoles = ImmutableMap.of(
                "code", "notifyWolvesKillVillagers"
        );
        String jsonText = JsonUtils.toString(assignRoles);
        sessions.stream()
                .forEach(s -> s.getAsyncRemote().sendText(jsonText));
        ScheduledFuture[] holder = new ScheduledFuture[1];
        holder[0] = scheduledExecutorService.schedule(() -> {
            holder[0].cancel(true);
            notifyWitchSave();
        }, WOLVIES_KILL_VILLAGERS_DURATION, TimeUnit.MILLISECONDS);
    }
    
    public void wolfVote(Session session, String playerId) {
        if("wolf".equals(session.getUserProperties().get("role"))) {
            wolfVotings.put(getPlayerId(session), new WolfVoting(playerId));
        }
    }
    
    /**
     * 通知女巫救人
     */
    private void notifyWitchSave() {
        witchSavings.clear();
        
        Map<String, Object> assignRoles = ImmutableMap.of(
                "code", "notifyWitchSave"
        );
        String jsonText = JsonUtils.toString(assignRoles);
        sessions.stream()
                .forEach(s -> s.getAsyncRemote().sendText(jsonText));
        ScheduledFuture[] holder = new ScheduledFuture[1];
        holder[0] = scheduledExecutorService.schedule(() -> {
            holder[0].cancel(true);
            notifyWitchPoison();
        }, WOLVIES_KILL_VILLAGERS_DURATION, TimeUnit.MILLISECONDS);
    }
    
    public void witchSave(Session session, String playerId) {
        if(WITCH.equals(session.getUserProperties().get("role"))) {
            witchSavings.put(getPlayerId(session), new WitchSaving(playerId));
        }
    }
    
    /**
     * 通知女巫毒人
     */
    private void notifyWitchPoison() {
        witchPoisonings.clear();
        
        Map<String, Object> assignRoles = ImmutableMap.of(
                "code", "notifyWitchPoison"
        );
        String jsonText = JsonUtils.toString(assignRoles);
        sessions.stream()
                .forEach(s -> s.getAsyncRemote().sendText(jsonText));
        ScheduledFuture[] holder = new ScheduledFuture[1];
        holder[0] = scheduledExecutorService.schedule(() -> {
            holder[0].cancel(true);
            notifyHunterKillIfDead();
        }, WOLVIES_KILL_VILLAGERS_DURATION, TimeUnit.MILLISECONDS);
    }
    
    public void witchPoison(Session session, String playerId) {
        if(WITCH.equals(session.getUserProperties().get("role"))) {
            witchPoisonings.put(getPlayerId(session), new WitchPoisoning(playerId));
        }
    }
    
    /**
     * 通知猎人猎杀
     */
    private void notifyHunterKillIfDead() {
        hunterKillings.clear();
        
        Map<String, Object> assignRoles = ImmutableMap.of(
                "code", "notifyHunterKill"
        );
        String jsonText = JsonUtils.toString(assignRoles);
        sessions.stream()
                .forEach(s -> s.getAsyncRemote().sendText(jsonText));
        ScheduledFuture[] holder = new ScheduledFuture[1];
        holder[0] = scheduledExecutorService.schedule(() -> {
            holder[0].cancel(true);
            notifySeerForecasts();
        }, WOLVIES_KILL_VILLAGERS_DURATION, TimeUnit.MILLISECONDS);
    }
    
    public void hunterKills(Session session, String playerId) {
        if(HUNTER.equals(session.getUserProperties().get("role"))) {
            hunterKillings.put(getPlayerId(session), new HunterKilling(playerId));
        }
    }
    
    /**
     * 通知预言家查看
     */
    private void notifySeerForecasts() {
        seerForcastings.clear();
        
        Map<String, Object> assignRoles = ImmutableMap.of(
                "code", "notifySeerForecasts"
        );
        String jsonText = JsonUtils.toString(assignRoles);
        sessions.stream()
                .forEach(s -> s.getAsyncRemote().sendText(jsonText));
        ScheduledFuture[] holder = new ScheduledFuture[1];
        holder[0] = scheduledExecutorService.schedule(() -> {
            holder[0].cancel(true);
            notifyDead();
        }, WOLVIES_KILL_VILLAGERS_DURATION, TimeUnit.MILLISECONDS);
    }
    
    public void seerForecasts(Session session, String playerId) {
        if(SEER.equals(session.getUserProperties().get("role"))) {
            seerForcastings.put(getPlayerId(session), new SeerForcasting(playerId));
        }
    }
    
    private void notifyDead() {
        Collection<String> newlyDead = new LinkedHashSet<>();
        
        witchPoisonings.values().stream()
                .map(witchPoisoning -> witchPoisoning.playerId)
                .peek(newlyDead::add)
                .forEach(this.dead::add);
        hunterKillings.values().stream()
                .map(hunterKilling -> hunterKilling.playerId)
                .peek(newlyDead::add)
                .forEach(this.dead::add);
        
        List<Map.Entry<String, Long>> top2 = wolfVotings.values().stream()
                .collect(Collectors.groupingBy(wolfVoting -> wolfVoting.playerId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Comparator.comparingLong((Map.Entry<String, Long> entry) -> entry.getValue()).reversed())
                .limit(2)
                .collect(Collectors.toList());
        if(top2.size() == 1 || (top2.size() == 2 && top2.get(0).getValue().compareTo(top2.get(1).getValue()) > 0)) {
            newlyDead.add(top2.get(0).getKey());
            this.dead.add(top2.get(0).getKey());
            firstTurn = IntStream.range(0, sessions.size())
                    .filter(i -> Objects.equals(top2.get(0).getKey(), getPlayerId(sessions.get(i))))
                    .findAny()
                    .orElse(0);
            turnOffset = 0;
        } else {
            firstTurn = 0;
            turnOffset = 0;
        }
        
        Map<String, Object> notifyDead = ImmutableMap.of(
                "code", "notifyDead",
                "properties", newlyDead
        );
        String jsonText = JsonUtils.toString(notifyDead);
        sessions.stream()
                .forEach(s -> s.getAsyncRemote().sendText(jsonText));
        
        nextPlayer();
    }
    
    /**
     * 通知每一位玩家轮流讲话
     */
    private void nextPlayer() {
        Map<String, Object> nextPlayer = ImmutableMap.of(
                "code", "nextPlayer",
                "properties", turn()
        );
        String jsonText = JsonUtils.toString(nextPlayer);
        sessions.stream()
                .forEach(s -> s.getAsyncRemote().sendText(jsonText));
        ScheduledFuture[] holder = new ScheduledFuture[1];
        holder[0] = scheduledExecutorService.schedule(() -> {
            holder[0].cancel(true);
            turnOffset++;
            if(turnOffset < sessions.size()) {
                nextPlayer();
            } else {
                notifySomeoneBeVoted();
            }
        }, WOLVIES_KILL_VILLAGERS_DURATION, TimeUnit.MILLISECONDS);
    }
    
    public void enableMicrophone(Session session, boolean flag) {
        if(Objects.equals(getPlayerId(session), getPlayerId(sessions.get(turn())))) {
            Map<String, Object> enableMicrophone = ImmutableMap.of(
                    "code", "enableMicrophone",
                    "properties", flag
            );
            String jsonText = JsonUtils.toString(enableMicrophone);
            sessions.stream()
                    .forEach(s -> s.getAsyncRemote().sendText(jsonText));
        }
    }
    
    private void notifyPlayersVote() {
        playerVotings.clear();
        
        Map<String, Object> assignRoles = ImmutableMap.of(
                "code", "notifyPlayersVote"
        );
        String jsonText = JsonUtils.toString(assignRoles);
        sessions.stream()
                .forEach(s -> s.getAsyncRemote().sendText(jsonText));
        ScheduledFuture[] holder = new ScheduledFuture[1];
        holder[0] = scheduledExecutorService.schedule(() -> {
            holder[0].cancel(true);
            notifyWitchSave();
        }, WOLVIES_KILL_VILLAGERS_DURATION, TimeUnit.MILLISECONDS);
    }

    public void playerVote(Session session, String votedPlayerId) {
        playerVotings.put(getPlayerId(session), new PlayerVoting(votedPlayerId));
    }
    
    private void notifySomeoneBeVoted() {
        Collection<String> newlyDead = new LinkedHashSet<>();
        
        List<Map.Entry<String, Long>> top2 = playerVotings.values().stream()
                .collect(Collectors.groupingBy(playerVoting -> playerVoting.playerId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Comparator.comparingLong((Map.Entry<String, Long> entry) -> entry.getValue()).reversed())
                .limit(2)
                .collect(Collectors.toList());
        if(top2.size() == 1 || (top2.size() == 2 && top2.get(0).getValue().compareTo(top2.get(1).getValue()) > 0)) {
            newlyDead.add(top2.get(0).getKey());
            this.dead.add(top2.get(0).getKey());
        }
        
        Map<String, Object> notifyDead = ImmutableMap.of(
                "code", "notifyDead",
                "properties", newlyDead
        );
        String jsonText = JsonUtils.toString(notifyDead);
        sessions.stream()
                .forEach(s -> s.getAsyncRemote().sendText(jsonText));
        
        notifyWolvesKillVillagers();
    }
    
    private int turn() {
        return (firstTurn + turnOffset) % sessions.size();
    }
    
    private static String getPlayerId(Session session) {
        return (String) session.getUserProperties().get("playerId");
    }
    private static Boolean isPrepared(Session session) {
        return (Boolean) session.getUserProperties().get("prepared");
    }
    
    private static Predicate<Session> equalityPredicate(String playerId) {
        return session -> Objects.equals(getPlayerId(session), playerId);
    }
    
    private static class RoleCompetition {
        private final String playerId, role;
        public RoleCompetition(String playerId, String role) {
            this.playerId = playerId;
            this.role = role;
        }
    }
    private static class WolfVoting {
        private final String playerId;
        public WolfVoting(String playerId) {
            this.playerId = playerId;
        }
    }
    private static class WitchSaving {
        private final String playerId;
        public WitchSaving(String playerId) {
            this.playerId = playerId;
        }
    }
    private static class WitchPoisoning {
        private final String playerId;
        public WitchPoisoning(String playerId) {
            this.playerId = playerId;
        }
    }
    private static class HunterKilling {
        private final String playerId;
        public HunterKilling(String playerId) {
            this.playerId = playerId;
        }
    }
    private static class SeerForcasting {
        private final String playerId;
        public SeerForcasting(String playerId) {
            this.playerId = playerId;
        }
    }

    private static class PlayerVoting {
        private final String playerId;
        public PlayerVoting(String playerId) {
            this.playerId = playerId;
        }
    }
    
}
