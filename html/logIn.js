//postData{userName:,password:,status:(logIn,refreshScore)}
//status是login的情况下判断是否是第一次登陆
//是的话服务器初始化用户信息。
//不是话就显示出最高成绩
var urlstring="http://192.168.43.143:80/";
function login()
{   var username = $("#username").val();
    var password = $("#password").val();
    var checkdata = { "username": username, "password": password,"status":"logIn"};
    $.ajax({
        type: "POST",
        url: urlstring,
        contentType: "application/json;charset=UTF-8",
        dataType: "json",
        data:JSON.stringify(checkdata),
        success(data) {
            if(data.status == "ok") {
                alert("登陆成功");
                document.cookie="username="+username+";path=/";//设置cookie
                window.location.href=urlstring+"game.html";
            }else if(data.status == "fail") {
                alert("密码错误");
            }
        }
    });
}


