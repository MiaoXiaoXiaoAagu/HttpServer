var urlstring="http://192.168.43.143:80/";
window.onload = function() {

    var canvas = document.getElementById('gameCanvas'),
        $score = document.getElementById('scoreNum'),
        $highScore = document.getElementById('highScoreNum'),
        cxt = canvas.getContext('2d'),
        ballRadius = 10,
        centerBall = new Ball(ballRadius, '#f0ad4e'),
        balls = [],
        spring = 0.03,
        bounce = -1,
        isMouseDown = false,
        mouse = util.captureMouse(canvas),
        anmiRequest;
    getScoreRecord();//获取最高分
    centerBall.x = canvas.width / 2;
    centerBall.y = canvas.height - ballRadius;

    canvas.addEventListener('mousedown', function() {
        if (util.containsPoint(centerBall.getBounds(), mouse.x, mouse.y)) {
            isMouseDown = true;
            canvas.addEventListener('mouseup', onMouseUp, false);
            canvas.addEventListener('mousemove', onMouseMove, false);
        }
    }, false);
    //获取cookie
    function onMouseUp() {
        isMouseDown = false;
        canvas.removeEventListener('mouseup', onMouseUp, false);
        canvas.removeEventListener('mousemove', onMouseMove, false);
    }

    function onMouseMove() {
        centerBall.x = mouse.x;
        centerBall.y = mouse.y;
    }

    // 创建安全小球
    function createSafeBall(num) {
        for (var ball, i = 0; i < num; i++) {
            ball = new Ball(ballRadius, '#5cb85c');
            ball.x = Math.random() * canvas.width;
            ball.y = 0;
            ball.vx = Math.random() * 6 - 3;
            ball.vy = Math.random() * 6 - 3;
            ball.ballType = 'safe';
            balls.push(ball);
        }
    }

    // 创建红球D
    function createangerBall(num) {
        for (var ball, i = 0; i < num; i++) {
            ball = new Ball(ballRadius, '#ff0000');
            ball.x = Math.random() * canvas.width;
            ball.y = 0;
            ball.vx = Math.random() * 6 - 3;
            ball.vy = Math.random() * 6 - 3;
            ball.ballType = 'danger';
            balls.push(ball);
        }
    }

    // 球移动
    function move(ball) {
        ball.x += ball.vx;
        ball.y += ball.vy;

        if (ball.x + ball.radius > canvas.width) {
            ball.x = canvas.width - ball.radius;
            ball.vx *= bounce;
        } else if (ball.x - ball.radius < 0) {
            ball.x = ball.radius;
            ball.vx *= bounce;
        }

        if (ball.y + ball.radius > canvas.height) {
            ball.y = canvas.height - ball.radius;
            ball.vy *= bounce;
        } else if (ball.y - ball.radius < 0) {
            ball.y = ball.radius;
            ball.vy *= bounce;
        }
    }

    // 绘制球
    function draw(ball) {
        var dx = ball.x - centerBall.x,
            dy = ball.y - centerBall.y,
            dist = Math.sqrt(dx * dx + dy * dy),
            min_dist = ball.radius + centerBall.radius;

        // 撞击了
        if (dist < min_dist) {
            var score = parseInt($score.textContent);
            if (ball.ballType === 'safe') {
                $score.textContent = score + 1;
                balls.splice(balls.indexOf(ball), 1);
            } else if (ball.ballType === 'danger') {
                clearInterval(safeInterval);
                clearInterval(dangerInterval);
                window.cancelAnimationFrame(anmiRequest);
                if(score>$highScore.textContent)
                {
                    newRecord(score);
                }

            }
        }

        ball.draw(cxt);
    }


    var safeInterval = setInterval(function() {
        createSafeBall(8);
    }, 3000);

    var dangerInterval = setInterval(function() {
        createangerBall(5);
    }, 8000);



    (function drawFrame() {
        anmiRequest = window.requestAnimationFrame(drawFrame, canvas);
        cxt.clearRect(0, 0, canvas.width, canvas.height);

        balls.forEach(move);
        balls.forEach(draw);

        centerBall.draw(cxt);
    }());

    function newRecord(score) {
        //判显示是新纪录、上传成绩
        alert("新纪录！");
        uploadScore(score);
        //显示前5用户排名
    }
    function uploadScore(score) {
        //postData={"userName":,password:,status:(login,refreshScore)}
        var username=getCookie("username");

        postData={"username":username,status:"refreshScore","score":score.toString()};
        $.ajax({
            type: "POST",
            url: urlstring,
            contentType: "application/json;charset=UTF-8",
            dataType: "json",
            data:JSON.stringify(postData),
            success(data) {
                if(data.status === "ok") {
                    //返回数据格式{"status":"ok","users":[n1,n2,n3,n4,n5],"scores":[s1,s2,s3,s4,s5]}
                    showTop5(data.users,data.scores);
                }else if(data.status === "fail") {
                    alert("get scoreRecord wong");
                }
            }
        });
    }
    function showTop5(names,scores) {
       $(".scoreLine").each(function (index) {
           $(this).children(".scoreUser").text(names[index]);
           $(this).children(".scoreScore").text(scores[index]);
       });
        $("#rank").css("display","block");
    }

    function getCookie(name)
    {
        var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");

        if(arr=document.cookie.match(reg))

            return unescape(arr[2]);
        else
            return false;
    }
    function getScoreRecord() {
        var username=getCookie("username");

        getScoreRecordData={"username":username,"status":"getRecord"};
        $.ajax({
            type: "POST",
            url: urlstring,
            contentType: "application/json;charset=UTF-8",
            dataType: "json",
            data:JSON.stringify(getScoreRecordData),
            success(data) {
                if(data.status === "ok") {
                    console.log(data);
                    $highScore.textContent=parseInt(data.scoreRecord);
                }else if(data.status === "fail") {
                    alert("get scoreRecord wong");
                }
            }
        });
    }
};