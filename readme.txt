1 ��¼ 
http://115.28.172.156:8080/router/login?playerId=${playerId}

2 ���뷿��
http://115.28.172.156:8080/router/enter
����json����ʽ
{address:"${address}", roomId:"${roomId}"}
${address}�Ƿ���ķ�������ip��ַ
${roomId}�����ڷ���ķ������ķ���id

3 ��¼�����ķ�����
ws://115.28.172.156:8080/wolf/${playerId}

4 ��������ķ���
��ws����
{
	code: "enter",
	properties: {roomId : "${roomId}"}
}

5 �������㲥ĳ��ҽ��뷿��
{
	code: "enterResp",
	properties: {roomInfo: [{playerId: "${playerId1}"}, {playerId: "${playerId2}"}, ..., {playerId: "${playerIdn}"}]}
}

6 ���׼�������ȡ��׼����������ʼ(����flag��true��false)
{
	code: "prepare",
	properties: {flag: "${flag}"}
}

7 �������㲥ĳ���׼��
{
	code: "prepareResp",
	properties: {playerId: "${playerId}", flag: "${flag}"}
}

8 ������֪ͨ����������Ҿ�ѡ��ɫ������${role1}, ${role2}, ... , ${rolen}Ϊ��ɫ���룬���ܵ�ֵΪvillager, wolf, witch, hunter, seer
{
	code: "notifyCompeteRole",
	properties: {availableRoles: [{playerId: "${role1}"}, {playerId: "${role1}"}, ..., {playerId: "${rolen}"}]}
}

9 ��Ҿ�ѡ��ɫ
{
	code: "competeRole",
	properties: {role: "${role}"}
}

10 ������֪ͨ��������������˿�ʼͶƱ���˽׶����˿�ͶƱ��Ů�׿ɶ�ɱ��Ԥ�Լҿɲ鿴����������
{
	code: "notifyWolvesKillVillagers"
}

11 ����ͶƱ
{
	code: "wolfVote",
	properties: {playerId: "${playerId}"}
}

11 Ů�׶�ɱ
{
	code: "witchPoison",
	properties: {playerId: "${playerId}"}
}

12 Ԥ�ԼҲ鿴����������
{
	code: "seerForecast",
	properties: {playerId: "${playerId}"}
}

13 Ԥ�ԼҲ鿴���������ݷ���
{
	code: "seerForecastResp",
	properties: {playerId: "${playerId}", role: "${role}"}
}

14 ������֪ͨ���������������ͶƱ�������˽׶�Ů�׿���ѡ���Ƿ����
{
	code: "notifyWolfVoted",
	properties: {playerId: "${playerId}"}
}

15 Ů�׾���
{
	code: "witchSave",
	properties: {playerId: "${playerId}"}
}

16 ������֪ͨ��������������˷������˽׶����˿ɷ���
{
	code: "notifyHunterKill"
}

17 ���˷���
{
	code: "hunterKill",
	properties: {playerId: "${playerId}"}
}

18 ������֪ͨ���������������
{
	code: "notifyDayBreak",
	properties: {dead: ["${dead1}", "${dead2}", ..., "${deadn}"], newlyDead: ["${newlyDead1}", "${newlyDead2}", ..., "${newlyDeadn}"]}
}

19 ������֪ͨ�����������ĳ��ɫ��ʼ����
{
	code: "notifyNextTurn",
	properties: {playerId: "${playerId}"}
}

20 ����������/���������˷���(����flag��true��false)
{
	code: "enableMicrophone"
	properties: {flag: "${flag}"}
}

21 ������֪ͨ����������ҿ�ʼͶƱ
{
	code: "notifyPlayersVote"
}

22 ���ͶƱ
{
	code: "playerVote",
	properties: {playerId: "${playerId}"}
}

