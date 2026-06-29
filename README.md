# 解忧杂货铺 - Servlet Web 电商平台

## 项目概述

解忧杂货铺是一个基于 Java Servlet 的 Web 电商平台，采用典型的 MVC 架构，为用户提供温暖的治愈系商品购物体验。系统支持用户注册登录、店铺浏览、商品管理、愿望单收藏和系统消息通知等功能。

## 技术栈

| 类别 | 技术 |
|------|------|
| 后端框架 | Jakarta Servlet 6.0 |
| 前端 | HTML5 + CSS3 + JavaScript |
| 数据库 | MySQL 8.0 |
| 密码加密 | BCrypt |
| JSON处理 | Google Gson |
| 构建工具 | Maven |
| 服务器 | Tomcat 10+ |

## 项目结构

```
servletLogin/
├── src/main/java/com/wang/servlet/
│   ├── controller/           # Servlet 控制器
│   │   ├── LoginServlet.java         # 用户登录
│   │   ├── RegisterServlet.java      # 用户注册
│   │   ├── LogoutServlet.java        # 用户登出
│   │   ├── CurrentUserServlet.java   # 当前用户信息
│   │   ├── ShopListServlet.java      # 店铺列表
│   │   ├── ShopDetailServlet.java    # 店铺详情
│   │   ├── ShopOwnerProductServlet.java  # 店主商品管理
│   │   ├── AdminAuditServlet.java    # 管理员商品审核
│   │   ├── WishlistServlet.java      # 愿望单管理
│   │   ├── SystemMessageServlet.java  # 系统消息
│   │   └── InitDatabaseServlet.java   # 数据库初始化
│   ├── entity/               # 实体类
│   │   ├── SysUser.java             # 用户实体
│   │   ├── UserProfile.java         # 用户资料实体
│   │   ├── Shop.java                # 店铺实体
│   │   ├── Product.java             # 商品实体
│   │   ├── Wishlist.java            # 愿望单实体
│   │   └── SystemMessage.java       # 系统消息实体
│   ├── filter/               # 过滤器
│   │   └── AuthFilter.java          # 认证授权过滤器
│   └── util/                 # 工具类
│       └── DBUtil.java               # 数据库连接工具
├── src/main/webapp/
│   ├── home.html             # 首页（登录/注册）
│   ├── shopDetail.html       # 店铺详情页
│   ├── personal.html         # 个人中心页
│   ├── css/                  # 样式文件
│   ├── js/                   # 前端脚本
│   └── images/               # 图片资源
└── pom.xml                  # Maven 配置
```

## 角色权限

系统分为三种用户角色：

| 角色 | 标识 | 权限说明 |
|------|------|----------|
| 普通用户 | `user` | 浏览店铺/商品、加入愿望单、管理个人信息 |
| 店主 | `shop_owner` | 管理自有店铺商品、查看系统消息 |
| 管理员 | `admin` | 审核店铺商品、管理平台 |

## 核心功能

### 1. 用户模块

#### 注册 `/register`
- 用户名唯一性检查
- BCrypt 密码加密存储
- 默认角色为 `user`

#### 登录 `/login`
- 用户名密码验证
- Session 会话管理（30分钟超时）
- Cookie 记录登录信息

#### 登出 `/logout`
- Session 失效

### 2. 店铺模块

#### 店铺列表 `/shopList`
- 分页展示所有营业中的店铺
- 显示店铺名称、描述、图片、店主

#### 店铺详情 `/shopDetail`
- 查看店铺信息及已审核通过的商品
- 店主可查看全部商品（含待审核/已拒绝）

### 3. 商品模块

#### 店主商品管理 `/shopOwner/product`
- **GET**: 获取店主店铺及商品列表
- **POST action=add**: 添加商品（自动进入待审核状态）
- **POST action=update**: 更新商品信息
- **POST action=remove**: 下架商品

#### 管理员审核 `/admin/audit`
- **GET**: 获取所有待审核商品列表
- **POST action=approve**: 审核通过，发送系统消息通知店主
- **POST action=reject**: 审核拒绝，附带拒绝原因

### 4. 愿望单模块 `/wishlist`

- **GET**: 查看当前用户的愿望单
- **POST**: 添加商品到愿望单（同时扣减库存）
- **DELETE**: 从愿望单移除（库存退回）

### 5. 系统消息 `/messages`

- 查看当前用户收到的系统通知
- 包含商品审核结果等消息

## 数据库设计

### ER图说明

系统包含 6 个核心表：

```
sys_user (用户表)
    ├── user_id (PK)
    ├── username (UNIQUE)
    ├── password
    ├── user_role
    └── create_time
           │
           │ 1:1
           ▼
user_profile (用户资料表)
    ├── profile_id (PK)
    ├── user_id (FK)
    ├── avatar_url
    ├── bio
    └── update_time

    │
    │ 1:N (店主可拥有多个店铺，实际代码限制为1个)
    ▼
shop (店铺表)
    ├── shop_id (PK)
    ├── owner_id (FK → sys_user)
    ├── shop_name
    ├── description
    ├── shop_img
    ├── status
    └── create_time
           │
           │ 1:N
           ▼
product (商品表)
    ├── product_id (PK)
    ├── shop_id (FK → shop)
    ├── name
    ├── price
    ├── stock
    ├── description
    ├── preview_url
    ├── audit_status
    ├── reject_reason
    └── upload_time

sys_user (用户表)
    │
    │ 1:N
    ▼
wishlist (愿望单表)
    ├── id (PK)
    ├── user_id (FK → sys_user)
    ├── product_id (FK → product)
    └── add_time

    │
    │ 1:N
    ▼
system_message (系统消息表)
    ├── msg_id (PK)
    ├── receiver_id (FK → sys_user)
    ├── content
    ├── is_read
    └── create_time
```

详细表结构请参见下方 E-R 图文档。

## API 接口

### 认证相关

| 接口 | 方法 | 说明 | 需登录 |
|------|------|------|--------|
| `/login` | POST | 用户登录 | 否 |
| `/register` | POST | 用户注册 | 否 |
| `/logout` | POST/GET | 用户登出 | 是 |
| `/currentUser` | GET | 获取当前用户信息 | 是 |

### 业务接口

| 接口 | 方法 | 说明 | 需登录 | 角色限制 |
|------|------|------|--------|----------|
| `/shopList` | GET | 获取店铺列表 | 否 | - |
| `/shopDetail?shopId=` | GET | 获取店铺详情 | 否 | - |
| `/shopOwner/product` | GET | 获取店主商品 | 是 | shop_owner |
| `/shopOwner/product` | POST | 添加/更新/删除商品 | 是 | shop_owner |
| `/admin/audit` | GET | 获取待审核商品 | 是 | admin |
| `/admin/audit` | POST | 审核商品 | 是 | admin |
| `/wishlist` | GET | 获取愿望单 | 是 | - |
| `/wishlist` | POST | 添加愿望单 | 是 | - |
| `/wishlist` | DELETE | 移除愿望单 | 是 | - |
| `/messages` | GET | 获取系统消息 | 是 | - |
| `/initDb` | GET | 初始化数据库 | 否 | - |

## 测试账号

初始化数据库后可使用以下账号登录：

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 店主1 | shopowner1 | owner123 |
| 店主2 | shopowner2 | owner123 |
| 普通用户 | testuser | user123 |

## 部署说明

1. **数据库配置**
   - 修改各 Servlet 中的 DB_URL、USER、PASS
   - 或修改 `DBUtil.java` 中的数据库连接配置
   - 执行 `/initDb` 初始化数据库表和测试数据

2. **构建部署**
   ```bash
   mvn clean package
   ```
   生成 `target/servletLogin.war` 部署到 Tomcat

3. **访问地址**
   - 项目根路径: `http://localhost:8080/servletLogin/`
   - 初始化数据库: `http://localhost:8080/servletLogin/initDb`

## 安全特性

- 密码 BCrypt 加密存储
- Session 超时验证（30分钟）
- 基于角色的访问控制（RBAC）
- SQL PreparedStatement 防注入
- 敏感路径登录验证过滤
