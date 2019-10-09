# 部署方法
* 主目录创建config、logs两个文件夹
* 主目录放置start.sh和stop.sh两个脚本文件，并添加可执行权限
* config目录下放置application.properties和log4j2.xml文件
* 修改log4j2.xml文件，增加日志文件输出

# 启动脚本start.sh
```shell
#!/bin/bash
APP_HOME=$(cd "$(dirname "$0")";pwd)
APP_EXEC_JAR="spring-activemq-example-0.0.1.RELEASE.jar"
PIDFILE="$APP_HOME/pid"
checkRunning(){
    if [ -f "$PIDFILE" ]; then
       if  [ -z "`cat $PIDFILE`" ];then
        echo "ERROR: Pidfile '$PIDFILE' exists but contains no pid"
        return 2
       fi
       PID="`cat $PIDFILE`"
       RET="`ps -p $PID|grep java`"
       if [ -n "$RET" ];then
         return 0;
       else
         return 1;
       fi
    else
         return 1;
    fi
}

if ( checkRunning );then 
    PID=`cat $PIDFILE`
    echo "INFO: Process with pid '$PID' is already running"
    exit 0
fi

java -server -Xmx256m -XX:+UseG1GC \
     -Dspring.config=$APP_HOME/config/application.properties \
     -Dlogging.config=$APP_HOME/config/log4j2.xml \
     -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector \
     -jar $APP_HOME/$APP_EXEC_JAR > $APP_HOME/nohup 2>&1 &

echo $! > "$APP_HOME/pid";
```

# 停止脚本stop.sh
```shell
#!/bin/sh
APP_HOME=$(cd "$(dirname "$0")";pwd)
PID=`cat $APP_HOME/pid`
echo "INFO: Sending SIGKILL to process with pid '$PID'"
i=1
while [ "$i" != "3" ]; do
    kill -KILL $PID
    i=`expr $i + 1`
done
```