
public class PostHandle {
	public static String stausLog="logIn";
	public static String stausRefresh="refreshScore";
	public void handle(String data)
	{//分解字符串，获取username，password，status,score键值对
	 //根据status登录和刷新成绩
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
	//发送头部之后再发送反馈信息
		sendHeader();
	}
	private void sendHeader()
	{//发送响应头部
		
	}
	private void logIn(String username,String password)
	{//如果是新用户则添加用户信息
		
	}
	private void updateScore(String username,String password,int score)
	{//查询用户成绩是否需要刷新
	 //拉取前5的成绩
	 	
	}
}
