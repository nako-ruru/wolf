1 登录 
http://115.28.172.156:8080/router/login?playerId=${playerId}

2 进入房间
http://115.28.172.156:8080/router/enter
换回json串格式
{address:"${address}", roomId:"${roomId}"}
${address}是分配的服务器的ip地址
${roomId}是所在分配的服务器的房间id

3 登录分配后的服务器
ws://115.28.172.156:8080/wolf/${playerId}

4 进入分配后的房间
向ws发送
{
	code: "enter",
	properties: {roomId : "${roomId}"}
}

5 服务器广播某玩家进入房间
{
	code: "enterResp",
	properties: {roomInfo: [{playerId: "${playerId1}"}, {playerId: "${playerId2}"}, ..., {playerId: "${playerIdn}"}]}
}

6 玩家准备、玩家取消准备、房主开始(其中flag是true或false)
{
	code: "prepare",
	properties: {flag: "${flag}"}
}

7 服务器广播某玩家准备
{
	code: "prepareResp",
	properties: {playerId: "${playerId}", flag: "${flag}"}
}

8 服务器通知房间所有玩家竞选角色，其中${role1}, ${role2}, ... , ${rolen}为角色代码，可能的值为villager, wolf, witch, hunter, seer
{
	code: "notifyCompeteRole",
	properties: {availableRoles: [{playerId: "${role1}"}, {playerId: "${role1}"}, ..., {playerId: "${rolen}"}]}
}

9 玩家竞选角色
{
	code: "competeRole",
	properties: {role: "${role}"}
}

10 服务器通知房间所有玩家狼人开始投票，此阶段狼人可投票，女巫可毒杀，预言家可查看其他玩家身份
{
	code: "notifyWolvesKillVillagers"
}

11 狼人投票
{
	code: "wolfVote",
	properties: {playerId: "${playerId}"}
}

11 女巫毒杀
{
	code: "witchPoison",
	properties: {playerId: "${playerId}"}
}

12 预言家查看其他玩家身份
{
	code: "seerForecast",
	properties: {playerId: "${playerId}"}
}

13 预言家查看其他玩家身份反馈
{
	code: "seerForecastResp",
	properties: {playerId: "${playerId}", role: "${role}"}
}

14 服务器通知房间所有玩家狼人投票结束，此阶段女巫可以选择是否救治
{
	code: "notifyWolfVoted",
	properties: {playerId: "${playerId}"}
}

15 女巫救治
{
	code: "witchSave",
	properties: {playerId: "${playerId}"}
}

16 服务器通知房间所有玩家猎人反补，此阶段猎人可反补
{
	code: "notifyHunterKill"
}

17 猎人反补
{
	code: "hunterKill",
	properties: {playerId: "${playerId}"}
}

18 服务器通知房间所有玩家天亮
{
	code: "notifyDayBreak",
	properties: {dead: ["${dead1}", "${dead2}", ..., "${deadn}"], newlyDead: ["${newlyDead1}", "${newlyDead2}", ..., "${newlyDeadn}"]}
}

19 服务器通知房间所有玩家某角色开始讲话
{
	code: "notifyNextTurn",
	properties: {playerId: "${playerId}"}
}

20 发言人允许/不允许他人发言(其中flag是true或false)
{
	code: "enableMicrophone"
	properties: {flag: "${flag}"}
}

21 服务器通知房间所有玩家开始投票
{
	code: "notifyPlayersVote"
}

22 玩家投票
{
	code: "playerVote",
	properties: {playerId: "${playerId}"}
}

