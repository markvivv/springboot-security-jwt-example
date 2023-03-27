# Spring Security 6 + JWT + Mybatis完整示例

## 1. 设计特点
- JWT和Spring Security结合进行授权验证。
- 接口支持Pojo传参，在Pojo上使用注解进行参数校验，依赖Spring的Validation。
- 使用统一返回的Body对象，支持返回Pojo对象或者Map封装的数据集合，Body中统一处理分页查询返回。
- mybatis文件放置在源代码目录，按照模块打包controller、pojo、mybatis mapper文件。每个业务包互相独立，不允许做横向调用，公共模块做抽象组装，做接口调用。
- 使用`SqlSession`调用mapper配置文件，避免编写额外的接口。
- 使用IDEA提供的HTTP Request功能进行接口测试，测试脚本放在test目录。
- 所有后端接口请求配置统一的前缀，方便与前端使用同一PORT进行部署，后端请求转发到本应用进行处理。
  - 在`application.yml`里配置`server.servlet.context-path`属性为`/api`，所有请求默认带`api`前缀。
- 遵循Java的驼峰命名和HTTP的下划线命名，对POJO启用Jackson的`property-naming-strategy = SNAKE_CASE`配置。
  - `application.yml`中配置`spring.jackson.property-naming-strategy`的属性为`SNAKE_CASE`，将Java属性的驼峰命名转换为http的标准下划线命名。
- 使用时间单位配置yml文件，增加yml文件中时间配置属性可读性，Spring支持时间和数据大小两种单位。
  - 使用Duration配置yml文件，允许设置时间单位或者数据单位。参考：[Spring Boot官方手册3.0.4 Properties Conversion](https://docs.spring.io/spring-boot/docs/3.0.4/reference/htmlsingle/#features.external-config.typesafe-configuration-properties.conversion)
  - 配置样例参考配置文件`application.yml`的`jwt.validtiy`属性
      ```yaml
      jwt:
        # 24 hours
        validtiy: 24h
      ```
    
  - 代码样例参考`JwtTokenPrivider`的`validtiyInMs`属性
      ```java
      @Value("${jwt.validtiy}")
      @DurationUnit(ChronoUnit.MILLIS)
      private Duration validityInMs;
      ```
- 配置.gitlab-ci.yml，启用GitLab基于Maven的自动持续集成（Auto CI）
- 类关系图
![SpringSecurity3 vpd](https://user-images.githubusercontent.com/447704/227861428-a478ad7c-dde9-4b5d-b30d-f147188f9722.jpg)

  
## 2. 工程目录结构说明
```
spring3-jwt-example/                        * 工程目录名，可以根据实际项目情况进行修改
  |- resources
     |- application.yml                    * 主配置文件，包含激活配置文件，jackson，servlet，jwt，日志配置等
     |- application-dev.yml                * 数据库配置 
     |- application-druid.xml              * alibaba druid数据库连接池配置
     |- application-mybatis.xml            * mybatis配置，包含pagehelper配置
     |- log4j2.xml                         * log4j2配置文件，默认打开console配置，可以配置异步输出；打印SQL的日志级别可以配置好
  |- src/main/java
     |- Body.java                          * controller返回org.springframework.http.ResponseEntity，Body是ResponseEntity的数据结构体，兼容@Valid返回的数据结构，包含分页参数的处理
     |- SpringJwtExampleApplication.java   * 启动类，根据项目情况进行修改
     |- examples.spring.project            * 源码包结构，根据实际项目进行重修改
        |- config                          * 框架类的各种配置
           |- CorsConfig.java                            * 跨域请求（Cross-Origin Resource Sharing）配置，已经配置成允许所有请求所有参数
           |- SecurityConfig.java                        * JWT安全配置，有需要排除不做登录请求过滤的uri在这里面配置
           |- SecurityUserDetailsService.java            * 查询数据库进行登录验证
           |- SecurityUserDetails.java                   * Security UserDetails的实现类
           |- InvalidJwtAuthenticationException.java     * JWT验证自定义异常类
           |- InvalidAuthenticationEntryPoint.java       * JWT登录失败时调用的类
           |- jwt
              |- JwtTokenOncePerRequestFilter.java       * JWT过滤器，验证每次请求的Token是否有效
              |- JwtTokenProvider.java                   * JwtToken计算工具类
        |- exception
           |- CustomExceptionHandlerjava   * 使用@ControllerAdvice处理所有Controller抛出的异常并返回给前端，避免前端收到500错误页面
        |- users
           |- mapper                        * mybatis文件夹，mybatis mapper文件必须在这里才会被自动扫描到，在application-mybatis.xml文件中配置
              |- Login.xml                  * 登录mybatis mapper文件
              |- User.xml                   * 用户管理mybatis mapper文件
           |- AuthenticationController.java * 登录验证，刷新token，获取当前用户的controller类
           |- UserController.java           * 用户管理，根据项目实际情况的进行修改
           |- UserPojo.java                 * 用户管理模块配套的传值对象，增加了@Valid注解，启用校验，校验错误提示会返回给前端。根据项目实际情况修改字段属性
        |- model1                           * 样例模块，根据实际的情况进行命名编写，每个业务功能模块一个独立的包
           |- mapper                        * 样例模块对应的mybatis mapper文件
              |- Model1.xml                 * 样例mybatis mapper文件
           |- Model1Controller.java         * 样例controller类
           |- Model1Pojo.java               * 样例pojo类
  |- src/test/java                          * 测试用例存放文件夹
     |- examples.spring.project             * 测试用例包，保持和源码包相同
     |- model1                              
        |- model1.http                      * model1所有接口的IDEA http client测试用例（测试脚本可以使用JUnit/Postman测试脚本/IDEA http client三者中的任意一种）
     |- passwdtools
        |- PasswordTools.java               * 生成密码的工具类，默认使用bcrypt加密方式
     |- users
        |- users.http                       * users模块的测试用例
     |- .gitignore                          * git提交忽略文件的配置项
     |- pom.xml                             * pom配置。当前jwt依赖jsonwebtoken.jjwt
```

## 3. 接口清单
### 3.1. 登录验证（/api/authenticate）

- 请求header

  *无*

- 请求参数

|参数名|参数值|说明|
| ---- | ---- | ---- |
| username | admin | 登录账号 |
| password | 123456 | 登录密码 |

- 请求示例

```shell
curl -d "username=admin&password=123456" http://localhost:8080/api/authenticate
```

- 响应参数

|参数名|说明|
| ---- | ---- |
| status | 响应状态，遵循w3c http code命名规约，200表示正常 |
| message | 响应值中文描述，可用于提示用户 |
| data | 响应数据结构，封装返回的业务数据结构 |

- data响应参数

|参数名|说明|
| ---- | ---- |
| username | 登录用户名 |
| token_expiration | token过期时间，需要在过期前调用refresh_token刷新token |
| token | token本身，注意此token没有包含`Bearer `前缀，设置到http header时需要增加`Bearer `前缀 |

- 响应示例
```json
{
  "status": 200,
  "message": "登录成功",
  "data": {
    "user_name": "admin",
    "token_expiration": "2023-03-28 13:34:06",
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbXSwibmlja25hbWUiOiIiLCJpYXQiOjE2Nzk4OTUyNDYsImV4cCI6MTY3OTk4MTY0Nn0.ddZyCbnHAykshhjfTMftsfr4n5iuYX25TZNb8miZeR3XjwE-Sle3lfS4JHYIKcnjx9aAIURoZhq7u64R125Nfg"
  }
}
```

### 3.2. 刷新token（/api/refresh_token）

- 请求Header

|参数名|参数值|说明|
| ---- | ---- | ---- |
| Accept | application/json |  |
| Authorization | Bearer [token] | [token]填写登录获得的实际值 |

- 请求Body

  *无*

- 请求示例

  *无*
  
- 响应参数

  *参考登录验证接口*

- 响应示例

  *参考登录验证接口*

### 3.3. 获取当前用户信息（/api/current_user）
通过还在有效期的token，获取当前的用户信息，如果token已经失效，接口返回http code 403。
- 请求Header

|参数名|参数值|说明|
| ---- | ---- | ---- |
| Accept | application/json |  |
| Authorization | Bearer [token] | [token]填写登录获得的实际值 |

- 请求Body

  *无*

- 请求示例

  *无*

- 响应参数

|参数名|说明|
| ---- | ---- |
| status | 响应状态，遵循w3c http code命名规约，200表示正常 |
| message | 响应值中文描述，可用于提示用户 |
| data | 响应数据结构，封装返回的业务数据结构 |

- data响应参数

|参数名|说明|
| ---- | ---- |
| username | 登录用户名（遵循SpringSecurity的UserDetail的命名规范，没有下划线分隔） |
| nickname | 用户昵称（遵循SpringSecurity的UserDetail的命名规范，没有下划线分隔） |
| account_non_expired | 账号非过期标识 |
| account_non_locked | 账号非锁定标识 |
| credentials_non_expired | 密码过期标识（不是token过期标识） |
| enabled |  账号是否启用 |

- 响应示例

```json
{
  "status": 200,
  "message": "成功获取当前用户信息",
  "data": {
    "username": "admin",
    "nickname": "",
    "authorities": [],
    "account_non_expired": true,
    "account_non_locked": true,
    "credentials_non_expired": true,
    "enabled": true
  }
}
```

# 4. 测试环境数据库环境初始化
## 4.1. docker环境拉取Mysql官方镜像

```shell
markvivv@MBP ~ % docker pull mysql
Using default tag: latest
'latest: Pulling from library/mysql
ab8798141d46: Pull complete 
75508f0dccd7: Pull complete 
b2a1f5f86172: Pull complete 
5ccc774632f3: Pull complete 
df7d86490565: Pull complete 
32480f1416f7: Pull complete 
0b89229d2472: Pull complete 
229bb5ff022d: Pull complete 
a972d41dd67e: Pull complete 
c8283d390a92: Pull complete 
ddba158ba540: Pull complete 
Digest: sha256:ca114710bb35b862062fd51733a7dba1ba3e93be33e4eede442b0ce15c77b718
Status: Downloaded newer image for mysql:latest
docker.io/library/mysql:latest
```

## 4.2. 启动Mysql
设置root账号默认密码1@34qWer，服务器本地3306端口映射容器3306端口启动

```shell
markvivv@MBP ~ % docker run --name mysql  -e MYSQL_ROOT_PASSWORD=1@34qWer -d mysql:latest
b210f38ea50ccd379e3d45933b89d464b0fabb5f545254ca6f461a5531bfbca9
```

## 4.3. 测试3306端口是否正常

```shell
markvivv@MBP ~ % telnet 127.0.0.1 3306
Trying 127.0.0.1...
Connected to localhost.
Escape character is '^]'.
J
8.0.3b+F13gG?1!b%F8-}Vcaching_sha2_password^CConnection closed by foreign host.
markvivv@MBP ~ % 
```

## 4.4. 进入mysql正在运行容器的命令行环境

```bash
markvivv@MBP ~ % docker exec -it mysql bash
```

## 4.5. 创建数据库实例以及数据库用户
```bash
bash-4.4# mysql -u root -p1@34qWer
mysql: [Warning] Using a password on the command line interface can be insecure.
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 12
Server version: 8.0.32 MySQL Community Server - GPL

Copyright (c) 2000, 2023, Oracle and/or its affiliates.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql> CREATE USER 'example'@'%' IDENTIFIED BY '1@34qWer';
Query OK, 0 rows affected (0.04 sec)

mysql> create database if not exists example DEFAULT CHARACTER SET = 'UTF8mb4';
Query OK, 1 row affected (0.02 sec)

mysql> GRANT ALL ON example.* TO 'example'@'%';
Query OK, 0 rows affected (0.01 sec)

mysql> flush privileges;
Query OK, 0 rows affected (0.01 sec)

mysql> use example
```

## 4.6. 执行数据库表初始化

```shell
mysql> CREATE TABLE `login_user` (
    ->   `id` int(11) NOT NULL AUTO_INCREMENT,
    ->   `user_name` varchar(50) DEFAULT NULL COMMENT '',
    ->   `nick_name` varchar(200) DEFAULT NULL COMMENT '',
    ->   `bcrypt_passwd` char(68) DEFAULT NULL COMMENT '',
    ->   `status` char(1) DEFAULT 'Y' COMMENT '\nY \nL\nD',
    ->   `create_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '',
    ->   `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ->   PRIMARY KEY (`id`),
    ->   UNIQUE KEY `idx_user_name` (`user_name`)
    -> ) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='';
Query OK, 0 rows affected, 1 warning (0.06 sec)

# 加密串对应的默认密码是123456
mysql> INSERT INTO `login_user` VALUES (1, 'admin', '', '{bcrypt}$2a$10$XMj0iNiUZNf5Dec5QL4.WOVa92tSY6xcRH6SjY.LajLZvVMVxb8Vy', 'Y', '2020-03-22 00:36:54', '2020-03-22 20:20:34');
Query OK, 1 row affected (0.03 sec)

mysql>
```

- 建表语句

```sql
CREATE TABLE `login_user` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `user_name` varchar(50) DEFAULT NULL COMMENT '',
    `nick_name` varchar(200) DEFAULT NULL COMMENT '',
    `bcrypt_passwd` char(68) DEFAULT NULL COMMENT '',
    `status` char(1) DEFAULT 'Y' COMMENT '\nY \nL\nD',
    `create_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_name` (`user_name`)
    ) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='';
```

## 4.7. 生成密码的方法
调用`PasswordTools`可以生成密码，填入数据库即可

# 5. 示例项目部署方法
- 主目录创建config、logs两个文件夹
- 主目录放置start.sh和stop.sh两个脚本文件，并添加可执行权限
- config目录下放置application.properties或application*.yml和log4j2.xml文件
- 修改log4j2.xml文件，增加日志文件输出

### 5.1. 示例项目启动脚本start.sh

```shell
#!/bin/bash
APP_HOME=$(cd "$(dirname "$0")";pwd)
APP_EXEC_JAR="spring3-security6-jwt-example-0.0.1.SNAPSHOT.jar"
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
     -jar $APP_HOME/$APP_EXEC_JAR $APP_HOME/config > $APP_HOME/nohup 2>&1 &

echo $! > "$APP_HOME/pid";
```

### 5.2. 示例项目停止脚本stop.sh
当前采用直接kill进程的方式，未来计划提供安全停止的方法

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

### 5.3. 安装成`systemd`服务
- 在`/etc/systemd/system`目录下配置`spring-jw-example.service`文件

```shell
[Unit]
Description=spring-jw-example
After=syslog.target

[Service]
User=myapp
ExecStart=/var/spring3-security6-jwt-example/start.sh
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

- 配置成自动启动
```shell
systemctl enable spring-jw-example.service
```

- 启动服务
```shell
systemctl start spring-jw-example.service
```

# 6. 基于GitLab的CI
参考[How to deploy Maven projects to Artifactory with GitLab CI/CD](https://docs.gitlab.com/ee/ci/examples/artifactory_and_gitlab/index.html)添加.gitlab-ci.yml文件，只做build，test，所以文件配置如下
```yaml
image: maven:latest

stages:
  - build
  - test

variables:
  MAVEN_CLI_OPTS: "-s .m2/settings.xml --batch-mode"
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"

cache:
  paths:
    - .m2/repository/
    - target/

build:
  stage: build
  script:
    - mvn $MAVEN_CLI_OPTS compile

test:
  stage: test
  script:
    - mvn $MAVEN_CLI_OPTS test
```

为了加速maven仓库访问，在工程中配置`.m2/settings.xml`文件，并且在该文件中的仓库镜像配置阿里云maven加速。

```xml
<mirror>
    <id>alimaven</id>
    <name>aliyun maven</name>
    <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
    <mirrorOf>central</mirrorOf>
</mirror>
```

# 7. SpringBoot tomcat 性能测试情况

先说结论：
- 服务器操作系统`CentOS Linux release 7.6.1810`，仅仅调整session的openfile至10万，未做任何其他参数调整
- 安装`Open JDK11.0.8`，Java进程启动参数`-server -Xss256k -Xms8g -Xmx8g`
- 服务器配置：VMWare虚拟化16vCPU，32G，稳定运行在并发3000个请求/秒左右，国外有人测试不调整任何参数能够稳定在5000个并发以内

## 7.1. 测试命令准备

### 7.2. 安装ab test命令

```shell
[root@node-kubeadm-251 ~]# yum -y install httpd-tools
```
### 7.3. 准备json文件

```json
{
  "id": "452FFCA6-F394-4AA1-9447-F6B839836F50",
  "servId": "E0619A20-7D33-4D48-874F-45C0D9314F5E",
  "severName": "汉十孝感服务区",
  "areaName": "北区",
  "devName": "西区卡口入口摄像机",
  "reportTime": "2017-08-25 11:32",
  "plate": "吉AK2222",
  "plateColor": "黄色",
  "carColor": "红色",
  "leaveOrEnter": "进入",
  "carSize": "大型车",
  "area": "北区",
  "createDate": "2017-08-25 11:35:00"
}
```

### 7.4. 调用/api/authenticate获取token后开始测试

```shell
[root@node-kubeadm-251 ~]# ab -n300000 -c200 -T application/json -H "Content-Type: application/json" -H  "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbXSwiaWF0IjoxNjAwODI0NTI2LCJleHAiOjE2MDA5MTA5MjZ9.mJAB0Z6iEUdtyXBobB0GTvAjLKPCiW9lbIws68nEzZI" -p test_post.json http://127.0.0.1:8080/api/benchmarks/post_dev_info
This is ApacheBench, Version 2.3 <$Revision: 1430300 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking 127.0.0.1 (be patient)
Completed 30000 requests
Completed 60000 requests
Completed 90000 requests
Completed 120000 requests
Completed 150000 requests
Completed 180000 requests
Completed 210000 requests
Completed 240000 requests
Completed 270000 requests
Completed 300000 requests
Finished 300000 requests


Server Software:        
Server Hostname:        127.0.0.1
Server Port:            8080

Document Path:          /api/benchmarks/post_dev_info
Document Length:        552 bytes

Concurrency Level:      200
Time taken for tests:   90.035 seconds
Complete requests:      300000
Failed requests:        0
Write errors:           0
Total transferred:      278400000 bytes
Total body sent:        239700000
HTML transferred:       165600000 bytes
Requests per second:    3332.05 [#/sec] (mean)
Time per request:       60.023 [ms] (mean)
Time per request:       0.300 [ms] (mean, across all concurrent requests)
Transfer rate:          3019.67 [Kbytes/sec] received
                        2599.91 kb/s sent
                        5619.58 kb/s total

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    0   0.4      0      17
Processing:     1   60  78.5     45    1317
Waiting:        0   60  78.5     44    1317
Total:          1   60  78.5     45    1317

Percentage of the requests served within a certain time (ms)
  50%     45
  66%     58
  75%     86
  80%     96
  90%    145
  95%    201
  98%    289
  99%    370
 100%   1317 (longest request)
```
