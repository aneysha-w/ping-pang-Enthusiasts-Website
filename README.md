# 乒乓球交流平台

一个面向乒乓球爱好者的交流平台，支持积分排名、赛事管理、俱乐部交流和用户管理。

## 技术栈

- **后端**: Spring Boot 3.2 + JPA + H2 数据库（可切换 MySQL）
- **前端**: Vue3 + Vite + Element Plus
- **认证**: Token-based（内存会话管理，可切换 Redis）

## 功能模块

### 1. 用户管理系统
- 用户注册（手机号 + 验证码 + 密码）
- 用户登录
- 个人资料管理（昵称、真实姓名、性别、城市、技术水平等级）
- 头像管理
- 附近球友推荐

### 2. 积分排名系统
- ELO 积分算法（含赛事级别权重：锦标赛1.5/公开赛1.2/俱乐部赛1.0/友谊赛0.5）
- 全站排行榜
- 俱乐部内排行榜
- 按技术水平等级分组排行榜（业余初级/业余中级/业余高级/专业级）
- 个人积分变动历史

### 3. 赛事管理系统
- 赛事创建（支持淘汰赛/循环赛）
- 赛事报名与取消（防超卖）
- 赛程自动生成（淘汰赛种子选手 + 循环赛 Berger 表算法）
- 比赛结果录入与积分更新
- 赛事状态流转

### 4. 俱乐部交流模块
- 俱乐部创建与查询
- 申请加入俱乐部（需管理员审批）
- 成员管理（移除、管理员转让）
- 帖子与评论交流
- 俱乐部赛事与解散

### 5. 首页导航
- 登录后首页展示积分排名、赛事中心、俱乐部、个人中心入口
- 各入口概览摘要

### 6. 管理后台
- 系统管理员独立登录
- 俱乐部加入申请审批（通过/拒绝）
- 审批结果通知

## 快速启动

### 环境要求
- JDK 17+
- Maven 3.6+
- Node.js 18+

### Windows 启动
```bash
# 双击 start.bat 或在命令行执行
start.bat
```

### 手动启动

**启动后端:**
```bash
cd backend
mvn spring-boot:run
```

**启动前端:**
```bash
cd frontend
npm install
npm run dev
```

### 访问地址
- 球友前台: http://localhost:5173
- 管理后台: http://localhost:5173/admin/login
- H2 控制台: http://localhost:8080/api/v1/h2-console
- API 基础路径: http://localhost:8080/api/v1

### 默认账号
- 系统管理员: admin / admin123

## API 接口

共 39 个 RESTful 接口，分为七大类：
1. 用户管理接口 (7个)
2. 积分排名接口 (5个)
3. 赛事管理接口 (9个)
4. 俱乐部接口 (13个)
5. 通知接口 (1个)
6. 首页导航接口 (1个)
7. 管理后台接口 (3个)

## 项目结构

```
cordarts-progect/
├── backend/                    # 后端 Spring Boot 项目
│   ├── src/main/java/com/pingpong/
│   │   ├── PingPongApplication.java
│   │   ├── common/             # 通用工具（响应封装、异常、错误码）
│   │   ├── config/             # 配置类
│   │   ├── controller/         # REST 控制器
│   │   ├── entity/             # JPA 实体
│   │   ├── repository/         # 数据仓库
│   │   └── service/            # 业务服务
│   └── src/main/resources/
│       └── application.yml     # 应用配置
├── frontend/                   # 前端 Vue3 项目
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   ├── router/             # 路由配置
│   │   └── utils/              # 工具类
│   └── package.json
├── start.bat                   # 启动脚本
├── stop.bat                    # 停止脚本
└── README.md
```

## 切换到 MySQL

修改 `backend/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pingpong
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: your_password
```

并在 `pom.xml` 中添加 MySQL 驱动依赖。