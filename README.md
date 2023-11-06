# Bin API

> 一个丰富的API开放调用平台，为开发者提供便捷、实用的API调用体验
>
> Java + React 全栈项目，包括网站前台+管理员后台，感谢[鱼厂创始人](https://github.com/liyupi)提供的项目基础框架，我也是在此基础上作拓展
>
> 项目后端开源代码：https://github.com/ishuaige/binapi
>
> 项目前端开源代码：https://github.com/ishuaige/binapi-frontend

## 项目介绍

Bin API 平台初衷是尽可能地帮助和服务更多的用户和开发者， 为开发者提供API接口，提高开发者开发效率的平台。我们可以为用户提供各种类型的接口服务，使他们能够更高效地完成需求，例如：获取今日天气、获取金句、随机头像等服务。

项目后端使用语言为Java，包括现在市面上主流技术栈，采用微服务架构开发，解耦业务模块，前端使用React，Ant Design Pro + Ant Design组件库，使用现成组件库快速开发项目。

## 项目背景

本人大学牲一枚，作为一个菜鸡程序员，身边的前端朋友也常来问我是否可以开发一些接口可以供他们调用，或是自己难免在开发中会遇到一些简单地想法，但是可能又要花费不少时间去实现，于是乎自然就会想到有没有一些现成的API可以调用。

在搜罗了大多API网站后，发现有的接口质量参差不齐，或是收费不合理，或是速度慢，或是不符合预期，在获取之前有可能还需要进行关注公众号等操作，甚是繁琐，虽然知道有些操作合理的，但却让我萌生了一个自己写一个接口平台的想法，于是该项目诞生了。

Bin API 平台在开发者注册后，只需要找到需要的接口，获取接口后，使用我们提供的SDK配置密钥后就可以很方便地调用我们为您提供的服务！

## 技术栈

### 前端技术栈

- 开发框架：React、Umi
- 脚手架：Ant Design Pro
- 组件库：Ant Design、Ant Design Components
- 语法扩展：TypeScript、Less
- 打包工具：Webpack
- 代码规范：ESLint、StyleLint、Prettier

### 后端技术栈

* 主语言：Java
* 框架：SpringBoot 2.7.0、Mybatis-plus、Spring Cloud
* 数据库：Mysql8.0、Redis
* 中间件：RabbitMq
* 注册中心：Nacos
* 服务调用：Dubbo
* 网关：Spring Cloud  Gateway
* 负载均衡：Spring cloud Loadbalancer

## 快速上手

### 后端

0. 如果你在最开始不使用Nacos配置中心，~~那么可以将binapi-config中的配置复制到对应模块使用~~ 各个模块配置已经放在对应目录下，只需要把注释去掉即可

1. 将各模块配置修改成你自己本地的端口、账号、密码

2. 启动Nacos、Mysql、Redis、RabbitMq、（**支付宝沙箱服务需要配置内网穿透，非必要**）

服务启动顺序参考：

1. binapi-backend （ 一定要先启动，创建队列，否则后面的项目可能启动失败 ）
2. binapi-interface
3. binapi-order
4. binapi-third-party
5. binapi-gateway

## 功能模块

> 🚀 未来计划（画饼）

* 用户、管理员
  * 登录注册
    * 短信验证：[短信验证码—Java实现](https://blog.csdn.net/idogbin/article/details/130444691)
  * 个人主页
  * 设置个人信息（🚀，因为用户信息模块并不是本项目重点，优先级较后）
  * 管理员：用户管理
  * 管理员：接口管理
  * 管理员：接口分析、订单分析
* 接口
  * 浏览接口信息
  * 在线调用接口
  * 接口搜索
  * 购买接口
  * 下载SDK调用接口
  * 用户上传自己的接口（🚀）
* 订单
  * 创建订单
  * 支付宝沙箱支付

### 后端模块

* binapi-backend：后端服务，提供用户、接口等基本操作
* binapi-common：项目公共模块，包含一些公用的实体类，远程调用接口
* binapi-gateway：api网关，整个后端的入口，作服务转发、用户鉴权、统一日志、服务接口调用计数
* binapi-interface：平台提供的接口服务，目前只有简单的几个接口，大家可以自行拓展
* binapi-order：订单服务，提供对订单的操作
* binapi-third-party：第三方服务，包含阿里云oss、支付宝沙箱支付、腾讯短信服务
* binapi-client-sdk：提供给开发者的SDK
* binapi-config：配置文件

![image-20230513211723627](https://niumapicgo.oss-cn-beijing.aliyuncs.com/images/image-20230513211723627.png)

## 系统架构

> 仅供参考

![image-20230513161820131](https://niumapicgo.oss-cn-beijing.aliyuncs.com/images/image-20230513161820131.png)

## 项目展示

* 登陆注册

![image-20230513163417755](https://niumapicgo.oss-cn-beijing.aliyuncs.com/images/image-20230513163417755.png)

![image-20230513163549224](https://niumapicgo.oss-cn-beijing.aliyuncs.com/images/image-20230513163549224.png)

* 主页

![image-20230513225733026](https://niumapicgo.oss-cn-beijing.aliyuncs.com/images/image-20230513225733026.png)

* 接口详情以及在线调用

![image-20230513225745495](https://niumapicgo.oss-cn-beijing.aliyuncs.com/images/image-20230513225745495.png)



![image-20230513225757002](https://niumapicgo.oss-cn-beijing.aliyuncs.com/images/image-20230513225757002.png)

* 购买接口（创建订单）

![image-20230513225812582](https://niumapicgo.oss-cn-beijing.aliyuncs.com/images/image-20230513225812582.png)

* 订单页

![image-20230513230326046](https://niumapicgo.oss-cn-beijing.aliyuncs.com/images/image-20230513230326046.png)

* 支付弹窗

![image-20230513230345813](https://niumapicgo.oss-cn-beijing.aliyuncs.com/images/image-20230513230345813.png)

* 我已经拥有的接口页

![image-20230513230400078](https://niumapicgo.oss-cn-beijing.aliyuncs.com/images/image-20230513230400078.png)

* 用户、接口管理页

![image-20230513230411559](https://niumapicgo.oss-cn-beijing.aliyuncs.com/images/image-20230513230411559.png)

![image-20230513230426027](https://niumapicgo.oss-cn-beijing.aliyuncs.com/images/image-20230513230426027.png)

* 分析页

![image-20230513204432307](https://niumapicgo.oss-cn-beijing.aliyuncs.com/images/image-20230513204432307.png)

* 个人信息页

![image-20230513230441943](https://niumapicgo.oss-cn-beijing.aliyuncs.com/images/image-20230513230441943.png)

## 欢迎贡献

项目需要大家的支持，期待更多小伙伴的贡献，你可以：

* 对于项目中的Bug和建议，能够在Issues区提出建议，我会积极响应