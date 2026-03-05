# TeamMatching - 团队匹配平台

一个基于 Spring Boot 3.2 的校园团队组建与人才匹配平台，帮助学生找到合适的团队成员和项目机会。

## 📋 项目简介

TeamMatching 是一个面向大学生的团队协作平台，提供以下核心功能：

- **身份认证系统**：校园身份验证（学生证、身份证、校园卡、学信网证明）
- **用户管理**：注册登录、微信一键登录、密码管理
- **人才卡片**：展示个人技能、项目经历、获奖情况
- **项目管理**：发布项目、招募成员、进度跟踪
- **社区互动**：帖子发布、评论、点赞、关注
- **即时通讯**：WebSocket 实时消息推送
- **文件管理**：阿里云 OSS 文件存储

## 🛠️ 技术栈

### 后端框架
- **Spring Boot 3.2.1** - 核心框架
- **Spring Web** - RESTful API
- **Spring WebSocket** - 实时通信
- **Spring Validation** - 参数校验

### 数据存储
- **MySQL 8.0** - 关系型数据库
- **MyBatis-Plus 3.5.9** - ORM 框架
- **Redis** - 缓存与会话管理

### 安全与认证
- **JWT (io.jsonwebtoken 0.11.5)** - Token 认证
- **BCrypt** - 密码加密
- **自定义拦截器** - 权限控制

### 第三方服务
- **阿里云 OSS** - 文件存储
- **SMTP** - 邮件发送
- **微信小程序** - 一键登录（可选）

### 开发工具
- **Lombok** - 代码简化
- **Swagger** - API 文档
- **Maven** - 依赖管理

## 🚀 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 安装步骤

#### 1. 克隆项目

```bash
git clone <repository-url>
cd TeamMatching
```

#### 2. 配置环境变量

复制 `.env.example` 为 `.env`，并配置以下环境变量：

```bash
# 数据库配置
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# 邮件配置
MAIL_USERNAME=your_email@qq.com
MAIL_PASSWORD=your_email_smtp_password

# 阿里云 OSS 配置
OSS_ACCESS_KEY_ID=your_oss_access_key_id
OSS_ACCESS_KEY_SECRET=your_oss_access_key_secret
```

#### 3. 修改配置文件

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/team_matching?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
  
  data:
    redis:
      host: localhost
      port: 6379

wechat:
  miniapp:
    appid: your_wechat_appid
    secret: your_wechat_secret
```

#### 4. 初始化数据库

创建数据库并导入 SQL 脚本：

```sql
CREATE DATABASE team_matching DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE team_matching;
-- 导入表结构（需要创建 SQL 脚本）
```

#### 5. 编译运行

```bash
mvn clean install
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动

## 📦 项目结构

```
TeamMatching/
├── src/main/java/club/boyuan/official/teammatching/
│   ├── common/              # 公共模块
│   │   ├── annotation/      # 自定义注解
│   │   ├── constants/       # 常量定义
│   │   ├── enums/           # 枚举类
│   │   └── utils/           # 工具类
│   ├── config/              # 配置类
│   ├── controller/          # 控制器层
│   ├── dto/                 # 数据传输对象
│   │   ├── request/         # 请求 DTO
│   │   └── response/        # 响应 DTO
│   ├── entity/              # 实体类
│   ├── exception/           # 异常处理
│   ├── interceptor/         # 拦截器
│   ├── mapper/              # 数据访问层
│   ├── mq/producer/         # 消息队列生产者
│   ├── service/             # 业务逻辑层
│   │   └── impl/            # 服务实现
│   ├── task/                # 定时任务
│   └── websocket/           # WebSocket 相关
├── src/main/resources/
│   ├── application.yml      # 主配置文件
│   ├── application-dev.yml  # 开发环境配置
│   └── application-prod.yml # 生产环境配置
└── pom.xml                  # Maven 配置
```

## 🔌 API 接口

### 认证相关

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 用户注册 | POST | `/auth/register` | 邮箱/手机号注册 |
| 发送验证码 | POST | `/auth/send-code` | 发送验证码 |
| 微信登录 | POST | `/auth/wx-login` | 微信一键登录 |
| 密码登录 | POST | `/auth/login` | 密码登录 |
| 找回密码 | POST | `/auth/forgot-password` | 通过验证码找回密码 |
| 修改密码 | PUT | `/auth/password` | 登录后修改密码 |
| **提交身份认证** | **POST** | **`/auth/verify`** | **提交校园身份认证信息** |
| **查询认证状态** | **GET** | **`/auth/status`** | **查询认证状态及材料** |

### 用户相关

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 获取用户信息 | GET | `/user/profile` | 获取当前用户信息 |
| 更新用户信息 | PUT | `/user/profile` | 更新用户资料 |
| 上传头像 | POST | `/user/avatar` | 上传用户头像 |

### 项目相关

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 创建项目 | POST | `/project/create` | 发布新项目 |
| 项目列表 | GET | `/project/list` | 获取项目列表 |
| 项目详情 | GET | `/project/{id}` | 获取项目详情 |
| 申请加入 | POST | `/project/{id}/apply` | 申请加入项目 |

### 团队相关

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 创建团队 | POST | `/team/create` | 创建新团队 |
| 团队列表 | GET | `/team/list` | 获取团队列表 |
| 邀请成员 | POST | `/team/{id}/invite` | 邀请用户加入 |

### 社区相关

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 发布帖子 | POST | `/community/post/create` | 发布新帖子 |
| 帖子列表 | GET | `/community/post/list` | 获取帖子列表 |
| 发表评论 | POST | `/community/post/{id}/comment` | 发表评论 |

### 文件管理

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 上传文件 | POST | `/file/upload` | 上传文件到 OSS |
| 删除文件 | DELETE | `/file/delete/{id}` | 删除文件 |

## 📊 数据库设计

### 核心表结构

- `user` - 用户表
- `auth_material` - 身份认证材料表
- `project` - 项目表
- `project_role_requirements` - 项目角色需求表
- `team_member` - 团队成员表
- `talent_card` - 人才卡片表
- `skill_tag` - 技能标签表
- `community_post` - 社区帖子表
- `message` - 消息表
- `file_resource` - 文件资源表

## 🔐 认证说明

### JWT Token 使用

所有需要登录的接口都需要在请求头中携带 Token：

```
Authorization: Bearer <your_jwt_token>
```

### 认证状态

- **0** - 待审核
- **1** - 已通过
- **2** - 已驳回

### 材料类型

- **1** - 学生证
- **2** - 身份证
- **3** - 校园卡
- **4** - 学信网证明

## ⚙️ 配置说明

### 开发环境配置

编辑 `application-dev.yml`：

```yaml
server:
  port: 8080

logging:
  level:
    club.boyuan.official.teammatching: debug
```

### 生产环境配置

编辑 `application-prod.yml`：

```yaml
server:
  port: 80

logging:
  level:
    club.boyuan.official.teammatching: info
```

## 🧪 测试

运行所有测试：

```bash
mvn test
```

运行特定测试类：

```bash
mvn test -Dtest=AuthControllerValidationTest
```

## 📝 API 文档

项目使用 Swagger 生成 API 文档，启动应用后访问：

```
http://localhost:8080/swagger-ui.html
```

或使用 Apifox 导入 `默认模块.openapi.yaml` 查看完整接口文档。

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 📄 许可证

本项目采用 MIT 许可证

## 👥 开发团队

- 开发者：TeamMatch Team
- 组织：Boyuan Official

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- Email: 2368691796@qq.com
- Issues: GitHub Issues

## 🗺️ 路线图

### v1.0 (已完成)
- ✅ 用户注册登录
- ✅ 身份认证系统
- ✅ 文件上传管理
- ✅ 基础用户信息

### v1.1 (计划中)
- 🔄 人才卡片功能
- 🔄 项目发布与管理
- 🔄 团队组建功能
- 🔄 社区论坛

### v1.2 (未来规划)
- 📋 智能匹配算法
- 📋 在线聊天室
- 📋 活动发布
- 📋 数据分析看板

---

<div align="center">
  <p>Made with ❤️ by TeamMatch Team</p>
</div>