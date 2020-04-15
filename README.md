# Spring Security + JWT + Mybatis完整示例

## 设计特点
- JWT和Spring Security结合进行授权验证。
- 接口支持Pojo传参，在Pojo上使用注解进行参数校验，依赖Spring的Validation。
- 使用统一返回的Body对象，支持返回Pojo对象或者Map封装的数据集合。
- mybatis文件放置在源代码目录，按照模块打包controller、pojo、mybatis mapper文件。每个业务包互相独立，不允许做横向调用，公共模块做抽象组装，做接口调用。
- 使用`SqlSession`调用mapper配置文件，避免编写额外的接口。
- 使用IDEA提供的HTTP Request功能进行接口测试，测试脚本放在test目录。
- 所有后端接口请求配置统一的前缀，方便与前端使用同一PORT进行部署，后端请求转发到本应用进行处理。
  - 在`application.yml`里配置`server.servlet.context-path`属性为`/api`，所有请求默认带`api`前缀。
- 遵循Java的驼峰命名和HTTP的下划线命名，对POJO启用Jackson的`property-naming-strategy = SNAKE_CASE`配置。
  - `application.yml`中配置`spring.jackson.property-naming-strategy`的属性为`SNAKE_CASE`，将Java属性的驼峰命名转换为http的标准下划线命名。
- 使用时间单位配置yml文件，增加yml文件中时间配置属性可读性，Spring支持时间和数据大小两种单位。
  - 使用Duration配置yml文件，允许设置时间单位或者数据单位。参考：[Spring Boot官方手册4.2.8 Properties Conversion](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/reference/htmlsingle/#boot-features-external-config-conversion)
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
  
## 工程目录结构说明
```
spring-jwt-example/                        * 工程目录名，可以根据实际项目情况进行修改
  |- resources
     |- application.yml                    * 主配置文件，包含激活配置文件，jackson，servlet，jwt，日志配置等
     |- application-dev.yml                * 数据库配置 
     |- application-druid.xml              * alibaba druid数据库连接池配置
     |- application-mybatis.xml            * mybatis配置，包含pagehelper配置
     |- log4j2.xml                         * log4j2配置文件，默认打开console配置，可以配置异步输出；打印SQL的日志级别可以配置好
  |- src/main/java
     |- Body.java                          * controller返回org.springframework.http.ResponseEntity，Body是ResponseEntity的数据结构体，兼容@Valid返回的数据结构
     |- SpringJwtExampleApplication.java   * 启动类，根据项目情况进行修改
     |- examples.spring.project            * 源码包结构，根据实际项目进行重修改
        |- config                          * 框架类的各种配置
           |- CorsConfig.java              * 跨域请求（Cross-Origin Resource Sharing）配置，已经配置成允许所有请求所有参数
           |- WebSecurityConfig.java       * JWT安全配置，有需要排除不做登录请求过滤的uri在这里面配置
        |- exception
           |- CustomExceptionHandlerjava   * 使用@ControllerAdvice处理所有Controller抛出的异常并返回给前端，避免前端收到500错误页面
        |- security
           JwtUserDetailsService.java      * 查询数据库进行登录验证，每次接口请求都会访问对应的类，如果对性能有要求，可以对登录校验加缓存
           |- jwt
              |- InvalidJwtAuthenticationException.java * JWT验证自定义异常类
              |- JwtAuthenticationEntryPoint.java       * JWT登录失败时调用的类
              |- JwtSecurityConfigurer.java             * 配置AuthenticationEntryPoint和JwtTokenAuthenticationFilter的类
              |- JwtTokenAuthenticationFilter.java      * JWT过滤器，验证每次请求的Token是否有效
              |- JwtTokenProvider.java      * JwtToken计算工具类
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

## 接口清单
### 登录验证（/api/authenticate）

- 请求header

  *无*

- 请求参数

|参数名|参数值|说明|
| ---- | ---- | ---- |
| user_name | admin | 登录账号 |
| password | 123456 | 登录密码 |

- 请求示例

```shell script
curl -d "user_name=admin&password=123456" http://localhost:8080/api/authenticate
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
| user_name | 登录用户名 |
| token_expiration | token过期时间，需要在过期前调用refresh_token刷新token |
| token | token本身，注意此token没有包含`Bearer `前缀，设置到http header时需要增加`Bearer `前缀 |

- 响应示例
```json
{
  "status": 200,
  "message": "登录成功",
  "data": {
    "user_name": "admin",
    "token_expiration": "2020-04-14 14:31:42",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbXSwiaWF0IjoxNTg2ODQ1ODk3LCJleHAiOjE1ODY4NDU5MDJ9.-JiaVq4HlcIceojaa2SxgpZYA_MqhHezvAganke7OyA"
  }
}
```

### 刷新token（/api/refresh_token）

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

### 获取当前用户信息（/api/current_user）
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
    "nickname": "系统管理员",
    "account_non_expired": true,
    "account_non_locked": true,
    "credentials_non_expired": true,
    "enabled": true
  }
}
```

## 数据库表结构
```sql
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for login_user
-- ----------------------------
DROP TABLE IF EXISTS `login_user`;
CREATE TABLE `login_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(50) DEFAULT NULL COMMENT '登录用户名',
  `nick_name` varchar(200) DEFAULT NULL COMMENT '用户昵称',
  `bcrypt_passwd` char(68) DEFAULT NULL COMMENT '加密密码',
  `status` char(1) DEFAULT 'Y' COMMENT '用户状态\nY：正常 \nL：锁定\nD：逻辑删除',
  `create_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '账号创建日期',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_name` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='用户登录信息表';

-- ----------------------------
-- Records of login_user
-- ----------------------------
BEGIN;
-- 当前默认密码是123456
INSERT INTO `login_user` VALUES (1, 'admin', '系统管理员', '{bcrypt}$2a$10$XMj0iNiUZNf5Dec5QL4.WOVa92tSY6xcRH6SjY.LajLZvVMVxb8Vy', 'Y', '2020-03-22 00:36:54', '2020-03-22 20:20:34');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
```

## 生成密码的方法
调用`PasswordTools`可以生成密码，填入数据库即可

## 安装成`systemd`服务
- 在`/etc/systemd/system`目录下配置`spring-jw-example.service`文件
```shell script
[Unit]
Description=spring-jwt-example
After=syslog.target

[Service]
User=myapp
ExecStart=/var/spring-jwt-example/spring-jwt-example.1.0.0.RELEASE.jar /var/spring-jwt-example/config
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

- 配置成自动启动
```shell script
systemctl enable spring-jw-example.service
```

- 启动服务
```shell script
systemctl start spring-jw-example.service
```