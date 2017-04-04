package com.example.administrator.wolf;

import java.util.Map;

/**
 * Created by Administrator on 2017/4/4.
 */

public interface Callback {

    /**
     * <pre>
     *  5 服务器广播某玩家进入房间
     *  {
     *    code: "enterResp",
     *    properties: {roomInfo: [{playerId: "${playerId1}"}, {playerId: "${playerId2}"}, ..., {playerId: "${playerIdn}"}]}
     *   }
     *  </pre>
     * @param properties
     */
    void onEnterResp(Map<String, String> properties);

    /**
     * <pre>
     * 6 玩家准备、玩家取消准备、房主开始(其中flag是true或false)
     * {
     *  code: "prepare",
     *  properties: {flag: "${flag}"}
     *  }
     *  </pre>
     * @param properties
     */
    void onPrepareResp(Map<String, String> properties);

    /**
     * <pre>
     *  8 服务器通知房间所有玩家竞选角色，其中${role1}, ${role2}, ... , ${rolen}为角色代码，可能的值为villager, wolf, witch, hunter, seer
     *  {
     *    code: "notifyCompeteRole",
     *    properties: {availableRoles: [{playerId: "${role1}"}, {playerId: "${role1}"}, ..., {playerId: "${rolen}"}]}
     *   }
     * </pre>
     * @param properties
     */
    void onNotifyCompeteRole(Map<String, String> properties);

    /**
     * 10 服务器通知房间所有玩家狼人开始投票，此阶段狼人可投票，女巫可毒杀，预言家可查看其他玩家身份
     {
     code: "notifyWolvesKillVillagers"
     }
     */
    void onNotifyWolvesKillVillagers();

    /**
     13 预言家查看其他玩家身份反馈
     {
     code: "seerForecastResp",
     properties: {playerId: "${playerId}", role: "${role}"}
     }
     * @param properties
     */
    void onSeerForecastResp(Map<String, String> properties);

    /**
     * 14 服务器通知房间所有玩家狼人投票结束，此阶段女巫可以选择是否救治
     {
     code: "notifyWolfVoted",
     properties: {playerId: "${playerId}"}
     }
     * @param properties
     */
    void onNotifyWolfVoted(Map<String, String> properties);

    /**
     * 16 服务器通知房间所有玩家猎人反补，此阶段猎人可反补
     {
     code: "notifyHunterKill"
     }
     */
    void onNotifyHunterKill();

    /**
     *
     * 18 服务器通知房间所有玩家天亮
     {
     code: "notifyDayBreak",
     properties: {dead: ["${dead1}", "${dead2}", ..., "${deadn}"], newlyDead: ["${newlyDead1}", "${newlyDead2}", ..., "${newlyDeadn}"]}
     }
     * @param properties
     */
    void onNotifyDayBreak(Map<String, String> properties);

    /**
     *
     * 19 服务器通知房间所有玩家某角色开始讲话
     {
     code: "notifyNextTurn",
     properties: {playerId: "${playerId}"}
     }
     * @param properties
     */
    void onNotifyNextTurn(Map<String, String> properties);

    /**
     *
     21 服务器通知房间所有玩家开始投票
     {
     code: "notifyPlayersVote"
     }
     */
    void onNotifyPlayersVote();

}
