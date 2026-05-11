# 毕设说明

### 功能

* 增删改查
* 文件上传
* 数据库配置
* 日志管理
* 权限控制
* AI交互
### 后端部署流程

1. 配置端口 位于application.yml
2. 配置DB_NAME 位于application.yml
3. 配置BASE_LOCATION 位于application.yml
4. 修改logback-spring.xml下的LOG_HOME的value值
5. maven clean -> maven package
6. 将jar包复制到服务器
7. 将upload文件夹复制到服务器
8. 迁移mysql数据库
9. 运行启动jar包命令

### 运行jar命令（或双击start.sh）

title xxxx
java -jar -Xms64m -Xmx128m -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=64m xxxxx.jar

### 项目访问路径

服务启动后，常用访问路径如下：

* 前台页面：http://localhost:8004/#/index
* 后台页面：http://localhost:8004/#/admin
* 后端接口文档（如已开启）：http://localhost:8009/swagger-ui.html
* Druid监控页面：http://localhost:8009/druid/index.html

### 后台管理登录信息

* 用户名：汤弘正
* 密码：Thz20031001


### 数据库相关

删除数据库命令：

drop database if exists book;

创建数据库命令：

CREATE DATABASE IF NOT EXISTS book DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

数据库备份命令:

mysqldump -u root -p --databases 数据库名称 > xxx.sql

数据库恢复命令：

source D:\\xxx.sql


### 常见问题

#### 日志路径

修改logback-spring.xml下的LOG_HOME的value值

#### 配置数据库

application.yml中

#### Druid连接池配置

* application.yml中配置druid
* 参考链接：https://blog.csdn.net/nothingavenger/article/details/114119585
* 监控地址：http://localhost:8009/druid/index.html

#### mysql主键id过长

https://blog.csdn.net/qq_46728644/article/details/120772577

#### yml不起作用

需要maven clean一下

#### 注意实体字段最好是String类型

实体字段最好是String类型，mybatis-plus的update的时候，null的不更新

#### 打包步骤

maven clean -> maven package

https://blog.csdn.net/weixin_42822484/article/details/107893586

#### 配置文件上传大小

application.yml中multipart下

#### 静态资源路径配置

https://blog.csdn.net/cylcjsg/article/details/128102776?

#### 跨域配置

见CorsConfig.java


#### 访问路径

用户端入口：http://localhost:3000/#/index
管理员端入口：http://localhost:3000/#/admin
 

### 运行步骤
以后的运行步骤

第一步:
打开IDEA，点击运行按钮

第二步:
打开cmd终端窗口，控制台然后执行:
D：
cd D:\毕业设计\java_shop-master\java_shop-master\web
npm run dev

第三步:
浏览器访问即可

### 接口文档访问
http://localhost:9100/api/swagger-ui/index.html


------------------------------------------------------------------------------------------------------------------------

## 接口文档

### 一、通用说明

#### 1. 统一响应格式

所有接口均返回 `APIResponse` 结构：

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {},
  "timestamp": 1713001234567
}
```

#### 2. 状态码 (ResponeCode)

| code | 说明 |
|------|------|
| 200 | 成功 |
| 500 | 系统错误 |
| 401 | 未授权（需登录） |
| 403 | 无权限（需管理员） |
| 400 | 参数错误 / 业务错误 |

#### 3. 权限说明

| 注解 | 说明 |
|------|------|
| `@Access(level = AccessLevel.ADMIN)` | 需管理员登录 |
| `@Access(level = AccessLevel.LOGIN)` | 需普通用户登录 |
| 无注解 | 公开接口 |

#### 4. 文件上传

文件上传接口使用 `multipart/form-data`，文件字段名为 `imageFile`。

---

### 二、用户管理 `/api/user`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /user/list | 用户列表（关键词搜索） | 公开 |
| GET | /user/detail | 用户详情 | 公开 |
| POST | /user/login | 管理员登录 | 公开 |
| POST | /user/userLogin | 普通用户登录 | 公开 |
| POST | /user/userRegister | 普通用户注册 | 公开 |
| POST | /user/create | 新增用户 | ADMIN |
| POST | /user/delete | 批量删除用户 | ADMIN |
| POST | /user/update | 更新用户 | ADMIN |
| POST | /user/updateUserInfo | 更新当前用户资料 | LOGIN |
| POST | /user/updateUsername | 修改用户名（需验证密码） | LOGIN |
| POST | /user/updatePwd | 修改密码 | LOGIN |

**请求参数示例：**

`POST /api/user/userRegister`
```json
{
  "username": "testuser",
  "password": "123456",
  "rePassword": "123456",
  "avatarFile": "<file>"
}
```

**登录响应示例：**
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "token": "md5hash...",
    "role": "1",
    "status": "0"
  }
}
```

---

### 三、商品管理 `/api/thing`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /thing/list | 商品列表（关键词/排序/分类/标签） | 公开 |
| GET | /thing/detail | 商品详情 | 公开 |
| POST | /thing/create | 新增商品 | ADMIN |
| POST | /thing/delete | 批量删除商品 | ADMIN |
| POST | /thing/update | 更新商品 | ADMIN |

**请求参数示例：**

`GET /api/thing/list?keyword=手机&sort=price_asc&c=1&tag=热销`
```json
{
  "keyword": "商品关键词",
  "sort": "price_asc | price_desc | sales",
  "c": "分类ID",
  "tag": "标签名"
}
```

`POST /api/thing/create`
```json
{
  "title": "商品标题",
  "price": 2999.00,
  "description": "商品描述",
  "repertory": 100,
  "classificationId": 1,
  "imageFile": "<file>"
}
```

---

### 四、订单管理 `/api/order`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /order/list | 订单列表（全部） | 公开 |
| GET | /order/userOrderList | 用户订单列表（按状态） | 公开 |
| POST | /order/create | 创建订单 | 公开 |
| POST | /order/delete | 批量删除订单 | ADMIN |
| POST | /order/update | 更新订单 | 公开 |
| POST | /order/cancelOrder | 取消订单 | ADMIN |
| POST | /order/cancelUserOrder | 取消用户订单 | LOGIN |

**订单状态说明：**
- `1` — 未支付
- `2` — 已支付
- `7` — 已取消

**请求参数示例：**

`POST /api/order/create`
```json
{
  "userId": "1",
  "thingId": "1",
  "count": 2,
  "addressId": "1",
  "redeemPoints": 100
}
```

---

### 五、购物车 `/api/cart`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /cart/add | 加入购物车 | LOGIN |
| POST | /cart/updateCount | 修改数量 | LOGIN |
| POST | /cart/remove | 删除购物车项 | LOGIN |
| GET | /cart/list | 购物车列表 | LOGIN |
| POST | /cart/clear | 清空购物车 | LOGIN |
| GET | /cart/count | 购物车商品件数合计 | LOGIN |

**请求参数示例：**

`POST /api/cart/add`
```json
{
  "userId": "1",
  "thingId": "1",
  "itemCount": 2
}
```

---

### 六、商品收藏 `/api/thingCollect`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /thingCollect/collect | 收藏商品 | LOGIN |
| POST | /thingCollect/unCollect | 取消收藏 | LOGIN |
| GET | /thingCollect/getUserCollectList | 用户收藏列表 | 公开 |

**请求参数示例：**

`POST /api/thingCollect/collect`
```json
{
  "userId": "1",
  "thingId": "1"
}
```

---

### 七、商品心愿单 `/api/thingWish`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /thingWish/wish | 加入心愿单 | LOGIN |
| POST | /thingWish/unWish | 取消心愿单 | LOGIN |
| GET | /thingWish/getUserWishList | 用户心愿单列表 | 公开 |

---

### 八、评论管理 `/api/comment`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /comment/list | 评论列表（全部） | 公开 |
| GET | /comment/listThingComments | 商品评论列表 | 公开 |
| GET | /comment/listUserComments | 用户评论列表 | 公开 |
| POST | /comment/create | 发表评论 | 公开 |
| POST | /comment/delete | 批量删除评论 | ADMIN |
| POST | /comment/update | 更新评论 | ADMIN |
| POST | /comment/like | 评论点赞 | 公开 |

**请求参数示例：**

`GET /api/comment/listThingComments?thingId=1&order=like`
```json
{
  "thingId": "商品ID",
  "order": "like | time"
}
```

`POST /api/comment/create`
```json
{
  "thingId": "1",
  "userId": "1",
  "content": "商品很好用！"
}
```

---

### 九、会员权益 `/api/member`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /member/calcPrice | 计算商品会员折扣价 | LOGIN |
| GET | /member/info | 获取会员信息 | LOGIN |

**请求参数示例：**

`GET /api/member/calcPrice?thingId=1&count=2&userId=1`

**响应示例：**
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "title": "商品标题",
    "price": 2999.00,
    "count": 2,
    "subtotal": 5998.00,
    "discountRate": 0.95,
    "discountAmount": 299.90,
    "finalPrice": 5698.10,
    "earnedPoints": 5698,
    "memberLevel": 2,
    "memberLevelName": "黄金会员",
    "userPoints": 5000,
    "maxRedeemPoints": 5000,
    "maxRedeemMoney": 50.00,
    "nextLevelThreshold": 10000,
    "canFreeShipping": true
  }
}
```

**会员信息响应示例：**
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "memberLevel": 2,
    "memberLevelName": "白银会员",
    "discountRate": 0.95,
    "nextLevelThreshold": 5000,
    "canFreeShipping": false,
    "userPoints": 5000
  }
}
```

**会员等级说明：**
| 等级 | 折扣率 | 免运费 | 累计消费门槛 |
|------|--------|--------|------------|
| 1-普通会员 | 9.8折 | 否 | 0元 |
| 2-白银会员 | 9.5折 | 否 | 1000元 |
| 3-黄金会员 | 9.0折 | 是 | 5000元 |
| 4-钻石会员 | 8.5折 | 是 | 20000元 |

---

### 十、积分管理 `/api/points`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /points/get | 获取当前积分 | LOGIN |
| POST | /points/sign | 每日签到 | LOGIN |
| POST | /points/redeem | 积分抵扣 | LOGIN |
| GET | /points/log | 获取积分记录明细 | LOGIN |
| GET | /points/signed | 检查是否已签到 | LOGIN |

**签到响应示例：**
```json
{
  "code": 200,
  "msg": "签到成功",
  "data": {
    "earnedPoints": 10,
    "totalPoints": 5010
  }
}
```

**积分记录类型 (type 字段)：**
- `ORDER` — 订单获得
- `EVAL` — 评价奖励
- `SIGN` — 签到获得
- `REDEEM` — 积分抵扣

---

### 十一、收货地址 `/api/address`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /address/list | 用户收货地址列表 | 公开 |
| POST | /address/create | 新增收货地址 | 公开 |
| POST | /address/delete | 批量删除收货地址 | 公开 |
| POST | /address/update | 更新收货地址 | 公开 |

**请求参数示例：**

`POST /api/address/create`
```json
{
  "userId": "1",
  "name": "张三",
  "mobile": "13800138000",
  "description": "北京市朝阳区xxx",
  "def": "1"
}
```

`def=1` 表示设为默认地址

---

### 十二、商品分类 `/api/classification`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /classification/list | 分类列表 | 公开 |
| POST | /classification/create | 新增分类 | ADMIN |
| POST | /classification/delete | 批量删除分类 | ADMIN |
| POST | /classification/update | 更新分类 | ADMIN |

---

### 十三、标签管理 `/api/tag`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /tag/list | 标签列表 | 公开 |
| POST | /tag/create | 新增标签 | ADMIN |
| POST | /tag/delete | 批量删除标签 | ADMIN |
| POST | /tag/update | 更新标签 | ADMIN |

---

### 十四、轮播图 `/api/banner`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /banner/list | 轮播图列表 | 公开 |
| POST | /banner/create | 新增轮播图 | ADMIN |
| POST | /banner/delete | 批量删除轮播图 | ADMIN |
| POST | /banner/update | 更新轮播图 | ADMIN |

`POST /api/banner/create` 支持 `imageFile` 字段上传图片，请求参数：
```json
{
  "thingId": 1,
  "imageFile": "<file>"
}
```

---

### 十五、广告管理 `/api/ad`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /ad/list | 广告列表 | 公开 |
| POST | /ad/create | 新增广告 | ADMIN |
| POST | /ad/delete | 批量删除广告 | ADMIN |
| POST | /ad/update | 更新广告 | ADMIN |

**广告创建参数：**
```json
{
  "slogan": "广告标语",
  "image": "图片路径",
  "link": "https://xxx.com",
  "thingId": 1,
  "imageFile": "<file>"
}
```

---

### 十六、系统公告 `/api/notice`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /notice/list | 公告列表（全部） | 公开 |
| GET | /notice/userList | 我的消息列表 | LOGIN |
| POST | /notice/userDelete | 删除我的消息 | LOGIN |
| POST | /notice/create | 新增公告 | ADMIN |
| POST | /notice/delete | 批量删除公告 | ADMIN |
| POST | /notice/update | 更新公告 | ADMIN |

---

### 十七、AI客服 `/api/ai/customer-service`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /ai/customer-service/stream | 客服对话 SSE 流式（多轮记忆） | 公开 |
| GET | /ai/customer-service/rag/stream | RAG 客服 SSE 流式（知识库） | 公开 |

**请求参数：**
```
GET /api/ai/customer-service/stream?message=怎么申请退款？&userId=1
```

- `message` — 用户消息（必填）
- `userId` — 用户ID（可选，未传为 guest 共享会话）

**响应：** SSE 流式输出

---

### 十八、数据统计 `/api/overview`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /overview/sysInfo | 服务器与 JVM 信息 | 公开 |
| GET | /overview/count | 后台统计数据（商品/订单/流量等） | 公开 |

**统计数据响应示例：**
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "spzs": 100,
    "qrxz": 10,
    "wfdd": 5,
    "wfddrs": 3,
    "yfdd": 50,
    "yfddrs": 40,
    "qxdd": 2,
    "qxddrs": 2,
    "popularThings": [  ],
    "popularClassification": [  ],
    "visitList": [  ]
  }
}
```

---

### 十九、操作日志 `/api/opLog`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /opLog/list | 操作日志列表 | 公开 |
| GET | /opLog/loginLogList | 登录日志列表 | 公开 |
| POST | /opLog/create | 新增操作日志 | 公开 |
| POST | /opLog/delete | 批量删除操作日志 | 公开 |
| POST | /opLog/update | 更新操作日志 | 公开 |

**操作日志字段说明：**
| 字段 | 说明 |
|------|------|
| reUrl | 请求URL |
| reMethod | 请求方法 (GET/POST) |
| reContent | 操作内容 |
| reIp | IP地址 |
| reTime | 响应时间 (ms) |
| reUa | User-Agent |
| accessTime | 访问时间 |

---

### 二十、错误日志 `/api/errorLog`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /errorLog/list | 错误日志列表 | 公开 |
| POST | /errorLog/create | 新增错误日志 | 公开 |
| POST | /errorLog/delete | 批量删除错误日志 | 公开 |
| POST | /errorLog/update | 更新错误日志 | 公开 |

**错误日志字段说明：**
| 字段 | 说明 |
|------|------|
| ip | 请求IP地址 |
| url | 请求URL |
| method | 请求方法 |
| content | 错误内容 |
| logTime | 记录时间 |

---

### 二十一、在线文档

项目集成了 Knife4j（Swagger3）在线接口文档，启动服务后访问：

```
http://localhost:9100/api/swagger-ui/index.html
```

该文档与本接口文档内容一致，可用于在线调试接口。