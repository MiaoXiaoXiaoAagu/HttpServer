package src;

import java.util.List;

public class PostHandle {
	private static String statusLog="logIn";
	private static String statusRefresh="refreshScore";
	private static String statusRecord="getRecord";
	private DBHelper dbHelper = new DBHelper();
	private String response = "";
	public String handle(String data)
	{//�ֽ��ַ�������ȡusername��password��status,score��ֵ��
	 //����status��¼��ˢ�³ɼ�
		//��ȥ������
		data = data.substring(1, data.length()-1);
		//�ָ��ַ���
		String[] parseData=data.split(",");
		String username="";
		String password="";
		String status="";
		int score = 0;
		for(int i=0;i<parseData.length;i++)
		{
			String key=parseData[i].split(":")[0];
			key=key.substring(1,key.length()-1);
			String value=parseData[i].split(":")[1];
			value=value.substring(1,value.length()-1);
			
			//System.out.println("key:"+key+"   value:"+value);
			if(key.equals("username"))
			{
				username=value;
			}
			else if(key.equals("status"))
			{
				status=value;
			
			}
			else if(key.equals("password")){
				password=value;
			}
			else if(key.equals("score")){
				score=Integer.parseInt(value);
			}
		}
	
		if(status.equals(""))
		{
			response = "{\n\"status\":\"fail\"\n}";
		}
		if(statusLog.equals(status))
		{
			if (logIn(username,password)) response = "{\n\"status\":\"ok\"\n}";
			else response = "{\n\"status\":\"fail\"\n}";
		}
		else if(statusRefresh.equals(status))
		{
			updateScore(username,score);
			String[] rankData=getRank();
			response ="{\n\"status\":\"ok\""+",\n\"users\":"+rankData[0]
					+"\n"+",\n\"scores\":"+rankData[1]+"}";
		}
		else if(statusRecord.equals(status))
		{
			int record=getScoreRecord(username);
			response = "{\n\"status\":\"ok\""+",\n\"scoreRecord\":"+record+"\n}";
			
		}
		System.out.println("status"+status+"postRespons:"+response);
		return response;
	}
	
	private int getScoreRecord(String username) {
		//{"username":username,"status":"getRecord"};
		//�����û��������롢"status":"getRecord"��ȡ��߷�
		//��������Ϊ{status��"ok"��scoreRecord��}
		int record=-1;
		dbHelper.open();
		List<String> users = dbHelper.query("score");
		for(String u : users)
		{
			if(username.equals(u.split(",")[0]))
			{
				record=Integer.parseInt(u.split(",")[1]);
			}
		}
		if(-1==record)
		{
			record=0;
			
			dbHelper.insertScore(username,record);
		}
		dbHelper.close();
		return record;
	}
	
	private boolean logIn(String username,String password)
	{//��������û�������û���Ϣ
		dbHelper.open();
		List<String> users = dbHelper.query("user");
		for (String u : users) {
			if (username.equals(u.split(",")[0])) {
				if (password.equals(u.split(",")[1])) return true;
				else return false;//�������
			}
		}
		//����û�
		String[] args = {username, password};
		dbHelper.insertUser(args);
		dbHelper.close();
		return true;
	}
	
	private void updateScore(String username,int score)
	{//��ѯ�û��ɼ��Ƿ���Ҫˢ��
	 //��ȡǰ5�ĳɼ�
	 	dbHelper.open();
	 	dbHelper.updateScore(username,score);
	 	dbHelper.close();
	 	/*
	 	* ��δʵ����ȡǰ��ɼ�
	 	* */
	}
	private String[] getRank()
	{//
		dbHelper.open();
		List<String> rankList=dbHelper.getRank();
		String[] rank = new String[]{};;
		rank[0]="[";
		rank[1]="[";
		for(int i=0;i<5;i++)
		{
			rank[0]+=rankList.get(i+1).split(",")[0];
			rank[1]+=rankList.get(i+1).split(",")[1];
		}
		rank[0]+="]";
		rank[1]+="]";
		dbHelper.close();
		return rank;//rank[0]=[u1,u2,u3,u4,u5];rank[1]=[s1,s2,s3,s4,s5]
	}

	/*
	* �ַ�������ȡ����
	* */
	private String parseString(String origin) {
		String str = origin.split(":")[1];
		return str.substring(1, str.length()-1);
	}
}
