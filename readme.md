# 问题
电路板失去同server的连接时，应该怎么提示用户？

# 配置

   - 项目地址

    http://localhost:8080
   - 接口文档地址：

    http://localhost:8080/swagger-ui.html
   - 用户名密码

    admin admin
相关包功能简介com.yunfd：
     
- config
    相关框架的配置文件，druid、日志、shiro、swagger还有跨域等相关配置
- domain
    所有和数据映射的实体都需集成BaseModel，BaseModel中为数据库表基础字段。
    项目使用了lombok插件，实体不需要写set和get方法。
    实体包，其中enums包下为常用的约定好不会变更的枚举对象,以下为使用方式。
    
    > 首先建好枚举类
```java
public enum UserStatus implements IEnum {
    able(1, "启用"),
    disable(0, "禁用");

    private int value;
    private String desc;

    UserStatus(final int value, final String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public Serializable getValue() {
        return this.value;
    }
    @JsonValue
    public String getDesc(){
        return this.desc;
    }

    @Override
    public String toString() {
        return this.desc;
    }
}

```
   > 在需要使用的字段，直接用枚举类作为字段类型。注意需要添加的注解。

```java
    private String email;
    private String mobile;
    @JSONField(serialzeFeatures= SerializerFeature.WriteEnumUsingToString)
    private UserStatus status;
```

 -  service 、mapper、util为常规包，不做说明 
   
 -  web

    VO是视图对象，用来方便根据业务或页面进行封装，方便展示和复用。几个重要的基础对象为分页（Smart*VO）、数据返回（ResultVO）。
    所有controller继承BaseController，封装基础通用操作。   
# 交互

## ServerHandler和板子交互的msg
```java
// 电路板客户端登录
    if (msg.contains("Login")) {// 首次信息 mod,,,#XXXX#
      String long_id = (msg.split("#"))[1];
    }

    // 心跳包
    if (msg.contains("Heartbeat")) {// heart...#XXXX#
      String long_id = (msg.split("#"))[1];
    }

    // 烧录过程
    if (msg.contains("OK")) {
      String long_id = (msg.split("#"))[1];
    }

    // 烧录成功  置 map 中 isrecorded 为 1
    if (msg.contains("END")) {
      String long_id = (msg.split("#"))[1];
    }

    // 收到关闭链路消息
    if (msg.contains("bye")) {
      log.info("bye!");
    }

    if (msg.contains("NICE")) {

    }

    if (msg.contains("STAT")) {
      String[] str = msg.split("#");
      // keypoint 旧板子
      if (str[1].length() == 8) {
        ...
      }
      // keypoint 新板子 2021
      else if (str[1].length() == 18) {
        ...
      }
    }
```

## Instructions

烧录文件

> 首次：
>
> ```
> ctx.channel().writeAndFlush("NNN");
> ctx.channel().writeAndFlush("CALL 1234 #");
> ```
>
> 第二次：（传输文件信息）
>
> ```
> ctx.channel().writeAndFlush("SIZE#" + sizeString + "#");
> ```
>
> 后续：（正常传输数据片段）
>
> ```
> ctx.channel().writeAndFlush("FIL" + "FF" + stringList.get(count - 2));
> ```

置空：ctx.channel().writeAndFlush("NNN");

传输按键：ctx.channel().writeAndFlush("CTR #" + BUTTON_STRING + "#");