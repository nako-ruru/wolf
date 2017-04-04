package com.example.administrator.wolf;

import android.util.Log;

import com.google.common.collect.ImmutableMap;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Administrator on 2017/4/4.
 */

public class WolfEngine {

    private static final String CODE = "code";
    private static final String PROPERTIES = "properties";

    private String playerId;
    WebSocketClient wsClient;

    private Callback callback;

    public void login(LoginCommand command) {
        performPostCall("http://localhost:8080/router/login", ImmutableMap.of("playerId", playerId), "POST");
    }

    public void route() {
        String response = performPostCall("http://localhost:8080/router/route", ImmutableMap.<String, String>of(), "POST");
        Map map = JsonUtils.toBean(response, Map.class);
        String address = (String) map.get("address");
        String roomId = (String) map.get("roomId");
        String ws = "ws://" + address + "/" + playerId;
        connectWebSocket(address, roomId);
    }

    /**
     * 6 玩家准备、玩家取消准备、房主开始(其中flag是true或false)
     {
     code: "prepare",
     properties: {flag: "${flag}"}
     }
     * @param flag
     */
    public void prepare(boolean flag) {
        Map<String, Object> map = ImmutableMap.<String, Object>builder()
                .put(CODE, "prepare")
                .put(PROPERTIES, ImmutableMap.of("flag", Boolean.toString(flag)))
                .build();
        String json = JsonUtils.toString(map);
        wsClient.send(json);
    }

    /**
     * 9 玩家竞选角色
     {
     code: "competeRole",
     properties: {role: "${role}"}
     }
     * @param role
     */
    public void competeRole(String role) {
        Collection<String> roles = Arrays.asList();
        if(!roles.contains(role)) {
            throw new IllegalArgumentException();
        }
        Map<String, Object> map = ImmutableMap.<String, Object>builder()
                .put(CODE, "competeRole")
                .put(PROPERTIES, ImmutableMap.of("role", role))
                .build();
        String json = JsonUtils.toString(map);
        wsClient.send(json);
    }

    /**
         11 狼人投票
         {
         code: "wolfVote",
         properties: {playerId: "${playerId}"}
         }
     * @param playerId
     */
    public void wolfVote(String playerId) {
        Map<String, Object> map = ImmutableMap.<String, Object>builder()
                .put(CODE, "wolfVote")
                .put(PROPERTIES, ImmutableMap.of("playerId", playerId))
                .build();
        String json = JsonUtils.toString(map);
        wsClient.send(json);
    }

    /**
     11 女巫毒杀
     {
     code: "witchPoison",
     properties: {playerId: "${playerId}"}
     }
     * @param playerId
     */
    public void witchPoison(String playerId) {
        Map<String, Object> map = ImmutableMap.<String, Object>builder()
                .put(CODE, "witchPoison")
                .put(PROPERTIES, ImmutableMap.of("playerId", playerId))
                .build();
        String json = JsonUtils.toString(map);
        wsClient.send(json);
    }

    /**
     12 预言家查看其他玩家身份
     {
     code: "seerForecast",
     properties: {playerId: "${playerId}"}
     }
     * @param playerId
     */
    public void seerForecast(String playerId) {
        Map<String, Object> map = ImmutableMap.<String, Object>builder()
                .put(CODE, "seerForecast")
                .put(PROPERTIES, ImmutableMap.of("playerId", playerId))
                .build();
        String json = JsonUtils.toString(map);
        wsClient.send(json);
    }

    /**
     15 女巫救治
     {
     code: "witchSave",
     properties: {playerId: "${playerId}"}
     }
     * @param playerId
     */
    public void witchSave(String playerId) {
        Map<String, Object> map = ImmutableMap.<String, Object>builder()
                .put(CODE, "witchSave")
                .put(PROPERTIES, ImmutableMap.of("playerId", playerId))
                .build();
        String json = JsonUtils.toString(map);
        wsClient.send(json);
    }

    /**
     17 猎人反补
     {
     code: "hunterKill",
     properties: {playerId: "${playerId}"}
     }
     * @param playerId
     */
    public void hunterKill(String playerId) {
        Map<String, Object> map = ImmutableMap.<String, Object>builder()
                .put(CODE, "hunterKill")
                .put(PROPERTIES, ImmutableMap.of("playerId", playerId))
                .build();
        String json = JsonUtils.toString(map);
        wsClient.send(json);
    }

    /**
     20 发言人允许/不允许他人发言(其中flag是true或false)
     {
     code: "enableMicrophone"
     properties: {flag: "${flag}"}
     }
     * @param flag
     */
    public void enableMicrophone(boolean flag) {
        Map<String, Object> map = ImmutableMap.<String, Object>builder()
                .put(CODE, "enableMicrophone")
                .put(PROPERTIES, ImmutableMap.of("flag", Boolean.toString(flag)))
                .build();
        String json = JsonUtils.toString(map);
        wsClient.send(json);
    }

    /**
     22 玩家投票
     {
     code: "playerVote",
     properties: {playerId: "${playerId}"}
     }
     * @param playerId
     */
    public void playerVote(String playerId) {
        Map<String, Object> map = ImmutableMap.<String, Object>builder()
                .put(CODE, "playerVote")
                .put(PROPERTIES, ImmutableMap.of("playerId", playerId))
                .build();
        String json = JsonUtils.toString(map);
        wsClient.send(json);
    }

    private void connectWebSocket(String address, final String roomId) {
        URI uri;
        try {
            uri = new URI(address);
        } catch (URISyntaxException e) {
            return;
        }

        wsClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("websocket", "Opened");
                Map<String, Object> map = ImmutableMap.<String, Object>of(CODE, "enter", PROPERTIES, ImmutableMap.of("roomId", roomId));
                wsClient.send(JsonUtils.toString(map));
            }

            @Override
            public void onMessage(String s) {
                Message message = JsonUtils.toBean(s, Message.class);
                String code = message.getCode();
                Map<String, String> properties = message.getProperties();
                Method method = Stream.of(Callback.class.getMethods())
                        .filter(m -> m.getName().equals("on" + Character.toUpperCase(code.charAt(0)) + code.substring(1)))
                        .findAny()
                        .get();
                try {
                    if(method.getParameterTypes().length == 0) {
                        method.invoke(callback);
                    } else {
                        method.invoke(callback, properties);
                    }
                } catch (InvocationTargetException e) {
                    Log.e("websocket", e.getMessage(), e.getTargetException());
                } catch (IllegalAccessException e) {
                    Log.e("websocket", e.getMessage(), e);
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("websocket", "Error " + e.getMessage());
            }
        };
        wsClient.connect();
    }

    public String  performPostCall(String requestURL, Map<String, String> postDataParams, String requestMethod) {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod(requestMethod);
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    private String getPostDataString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

}
