
public class PostHandle {
	public static String stausLog="logIn";
	public static String stausRefresh="refreshScore";
	public void handle(String data)
	{//�ֽ��ַ�������ȡusername��password��status,score��ֵ��
	 //����status��¼��ˢ�³ɼ�
		String[] parseData=data.split(",");
		String username="";
		String password="";
		String status="";
		String score="";
		
		if(parseData.length>=3)
		{
			username=parseData[0];
			password=parseData[1];
			status=parseData[2];
			if(parseData.length==4)
			{
				score=parseData[3];
			}
		}
		else {
			postData("WONG");
		}
		
		 if(status.equals(stausLog))
		 {
			 logIn(username,password);
			 postData("login successfully");
			 
		 }
		 else if(status.equals(stausRefresh))
		 {
			 updateScore(username,password,Integer.parseInt(score));
			 postData("refresh successfully");
		 }
	}
	
	private void postData(String data) {
	//����ͷ��֮���ٷ��ͷ�����Ϣ
		sendHeader();
	}
	private void sendHeader()
	{//������Ӧͷ��
		
	}
	private void logIn(String username,String password)
	{//��������û�������û���Ϣ
		
	}
	private void updateScore(String username,String password,int score)
	{//��ѯ�û��ɼ��Ƿ���Ҫˢ��
	 //��ȡǰ5�ĳɼ�
	 	
	}
}
