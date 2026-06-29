# RuoYi-Vue v3.9.2 完整项目技术吃透文档

> **阅读者身份**：资深前端技术经理，精通 Vue/Element 全家桶，无 Java 后端深度开发经验
> **文档目标**：吃透 SpringBoot 后端整套架构、业务模块、三层权限底层、安全校验、代码生成逻辑，实现独立完成后端 CRUD、权限开发、定时任务、接口开发
> **生成日期**：2026-06-29
> **仓库地址**：yangzongzhuan/RuoYi-Vue（master 分支）

---

## 目录

- [第一章 项目整体概述](#第一章-项目整体概述)
- [第二章 后端完整技术栈清单](#第二章-后端完整技术栈清单)
- [第三章 前端完整技术栈与前后端联动映射](#第三章-前端完整技术栈与前后端联动映射)
- [第四章 后端多模块分层详解](#第四章-后端多模块分层详解)
- [第五章 系统全部内置功能完整拆解](#第五章-系统全部内置功能完整拆解)
- [第六章 核心权限体系底层原理](#第六章-核心权限体系底层原理)
- [第七章 数据库完整设计](#第七章-数据库完整设计)
- [第八章 代码生成器完整底层实现](#第八章-代码生成器完整底层实现)
- [第九章 安全专项](#第九章-安全专项)
- [第十章 本地开发、启动、部署全流程](#第十章-本地开发启动部署全流程)
- [第十一章 专项功能底层实现](#第十一章-专项功能底层实现)
- [第十二章 后端统一开发规范](#第十二章-后端统一开发规范)
- [第十三章 定制化学习路线](#第十三章-定制化学习路线)

---

## 第一章 项目整体概述

### 1.1 项目基本信息

| 项目属性 | 说明 |
|---------|------|
| **项目名称** | RuoYi-Vue（若依管理系统） |
| **版本号** | v3.9.2 |
| **仓库地址** | https://gitee.com/y_project/RuoYi-Vue |
| **作者仓库** | yangzongzhuan/RuoYi-Vue |
| **开源协议** | MIT License（Copyright (c) 2018 RuoYi） |
| **项目定位** | 基于 SpringBoot + Vue 前后端分离的 Java 快速开发框架 |
| **核心理念** | 全部开源、毫无保留、个人及企业免费使用 |

#### MIT 协议商用约束说明

MIT 是最宽松的开源协议之一，核心约束如下：
- **允许**：商用、修改、分发、再授权、出售
- **唯一义务**：在所有副本或重要部分中包含版权声明和许可声明
- **免责条款**：软件按"现状"提供，不承担任何担保责任
- **前端视角类比**：比 Apache 2.0 更宽松，不需要声明修改内容，不涉及专利授权条款

### 1.2 Maven 多模块工程拆分架构

RuoYi 采用 Maven 多模块（Multi-Module）工程组织代码。父 `pom.xml` 统一管理版本号，子模块按职责拆分。

```
ruoyi (父POM, packaging=pom)
├── ruoyi-admin       → 启动模块，打包为可执行 jar，包含 Controller 层
├── ruoyi-framework   → 框架核心，安全认证、权限拦截、AOP切面
├── ruoyi-system      → 系统业务，Service + Mapper + Domain
├── ruoyi-quartz      → 定时任务模块
├── ruoyi-generator   → 代码生成器模块
└── ruoyi-common      → 公共工具，常量、异常、工具类、注解
```

**模块依赖关系图：**

```
ruoyi-admin
  ├── ruoyi-framework   → 依赖 ruoyi-system
  │                        └── ruoyi-common
  ├── ruoyi-quartz      → 依赖 ruoyi-common
  ├── ruoyi-generator   → 依赖 ruoyi-common
  └── (直接依赖) ruoyi-system, ruoyi-common
```

**依赖层次说明（前端类比）：**
- `ruoyi-common` 相当于前端的 `utils/` 工具库，被所有模块引用
- `ruoyi-system` 相当于前端的 `api/` 数据层，提供 Service 接口和 Mapper
- `ruoyi-framework` 相当于前端的 `router guard` + `axios interceptor`，处理认证和权限
- `ruoyi-admin` 相当于前端的 `App.vue` + `main.js`，是启动入口

#### 父 POM 统一版本管控机制

父 `pom.xml` 的 `<properties>` 标签统一定义所有依赖版本号：

```xml
<properties>
    <ruoyi.version>3.9.2</ruoyi.version>
    <spring-boot.version>2.5.15</spring-boot.version>
    <druid.version>1.2.28</druid.version>
    <fastjson.version>2.0.62</fastjson.version>
    <poi.version>4.1.2</poi.version>
    <velocity.version>2.3</velocity.version>
    <jwt.version>0.9.1</jwt.version>
    <spring-security.version>5.7.14</spring-security.version>
    <spring-framework.version>5.3.39</spring-framework.version>
    <!-- ... -->
</properties>
```

通过 `<dependencyManagement>` 声明依赖（不实际引入），子模块只需写 `groupId` + `artifactId`，无需写 `version`，保证全项目版本一致。这类似于前端 `package.json` 中的统一版本管理。

> **重要说明**：当前本地仓库的 pom.xml 配置为 Spring Boot 2.5.15 + JDK 1.8。根据 README 说明，master 分支应为 Spring Boot 4.x (JDK 17+)，springboot3 分支为 Spring Boot 3.x (JDK 17+)，springboot2 分支为 Spring Boot 2.x (JDK 8+)。本仓库可能是 springboot2 分支或过渡版本。后续技术分析基于实际代码配置。

### 1.3 三大后端分支完整对比

| 对比维度 | master (SpringBoot 4.x) | springboot3 (SpringBoot 3.x) | springboot2 (SpringBoot 2.x) |
|---------|------------------------|------------------------------|------------------------------|
| **JDK 要求** | JDK 17+ | JDK 17+ | JDK 8+ |
| **Spring Boot** | 4.x | 3.x | 2.5.15 |
| **Spring Framework** | 6.x+ | 6.x | 5.3.x |
| **Spring Security** | 6.x+ | 6.x | 5.7.x |
| **javax → jakarta** | 全面 jakarta 命名空间 | 全面 jakarta 命名名空间 | 保留 javax 命名空间 |
| **MyBatis 兼容** | 需 mybatis-spring-boot-starter 3.x+ | 需 mybatis-spring-boot-starter 3.x | mybatis-spring-boot-starter 2.x |
| **接口兼容** | JDK17 强制、jakarta 包名 | JDK17 强制、jakarta 包名 | 传统 javax 包名 |
| **适用场景** | 最新技术栈、未来维护 | 过渡版本、逐步迁移 | 稳定成熟、社区资料丰富 |

**前端视角理解 javax → jakarta 迁移**：相当于前端的 CommonJS → ESM 迁移。`javax.servlet.*` 变成 `jakarta.servlet.*`，所有 import 路径都要改。

### 1.4 三套前端工程完整对比

| 对比维度 | RuoYi-Vue (当前) | RuoYi-Vue3 | RuoYi-Vue3-TypeScript |
|---------|-----------------|------------|----------------------|
| **前端框架** | Vue 2 | Vue 3 | Vue 3 |
| **脚本语言** | JavaScript | JavaScript | TypeScript |
| **构建工具** | Vue CLI (Webpack) | Vite | Vite |
| **UI 组件库** | Element UI | Element Plus | Element Plus |
| **状态管理** | Vuex | Pinia | Pinia |
| **路由管理** | Vue Router 3 | Vue Router 4 | Vue Router 4 |
| **核心特点** | 经典稳定、社区资料丰富、维护重心已转移 | 现代技术栈、性能更优、官方主推 | 类型加持、企业级协作 |
| **适用场景** | 存量项目维护 | 新项目首选 | 大型团队协作 |

**三个前端工程均可与任意后端分支搭配使用**，前后端通过 RESTful API + JWT 通信，完全解耦。

### 1.5 官方配套资源

| 资源 | 地址 |
|------|------|
| **演示地址** | http://vue.ruoyi.vip |
| **文档地址** | http://doc.ruoyi.vip |
| **测试账号** | admin / admin123 |
| **Gitee 仓库** | https://gitee.com/y_project/RuoYi-Vue |
| **Vue3 前端** | https://gitcode.com/yangzongzhuan/RuoYi-Vue3 |
| **Vue3-TS 前端** | https://gitcode.com/yangzongzhuan/RuoYi-Vue3/tree/typescript |
| **交流群** | QQ群 127358632（最后一个未满群） |

---

## 第二章 后端完整技术栈清单

### 2.1 核心容器与 Web 层

| 技术 | 版本 | 作用详解 |
|------|------|---------|
| **Spring Boot** | 2.5.15 | 自动配置框架，内嵌 Tomcat，简化 Spring 应用开发。相当于后端的 Vite，一键启动无需部署 WAR 包 |
| **Spring Core** | 5.3.39 | IoC 容器 + DI 依赖注入。管理所有 Bean 的生命周期，相当于前端的依赖注入机制 |
| **Spring MVC** | 5.3.39 | Web MVC 框架，处理 HTTP 请求路由、参数绑定、响应输出。`@RestController`、`@GetMapping` 等注解都来自此框架 |
| **内嵌 Tomcat** | 9.0.112 | Servlet 容器，无需独立安装 Tomcat，`java -jar` 即可启动 Web 服务 |

### 2.2 安全认证

| 技术 | 版本 | 作用详解 |
|------|------|---------|
| **Spring Security** | 5.7.14 | 企业级安全框架，提供认证（你是谁）、授权（你能做什么）两大核心能力。相当于后端的路由守卫 + 权限指令 |
| **JWT (jjwt)** | 0.9.1 | 无状态令牌方案，Token 中携带用户 UUID，服务端不存 Session，通过 Redis 存储在线用户信息 |
| **Redis** | - | 存储登录用户缓存（`login_tokens:` 前缀）、验证码、密码错误次数、配置参数、字典数据 |
| **BCrypt** | - | 密码加密算法，Spring Security 内置。数据库中密码形如 `$2a$10$xxxx`，不可逆加密 |

**登录全链路校验流程：**

```
前端发送 POST /login (username, password, code, uuid)
    ↓
SysLoginController.login()
    ↓
SysLoginService.login()
    ├── 1. validateCaptcha()     → 验证码校验（Redis 取验证码对比）
    ├── 2. loginPreCheck()        → 前置校验（用户名/密码长度、IP 黑名单）
    ├── 3. authenticationManager.authenticate()  → Spring Security 认证
    │       └── UserDetailsServiceImpl.loadUserByUsername()
    │           ├── 查询数据库用户
    │           ├── 检查用户状态（删除/停用）
    │           └── SysPasswordService.validate()
    │               ├── 检查密码错误次数（Redis，超过 maxRetryCount=5 锁定 10 分钟）
    │               └── BCrypt 密码匹配
    ├── 4. recordLoginInfo()      → 更新最后登录 IP 和时间
    └── 5. tokenService.createToken()  → 生成 JWT + Redis 缓存 LoginUser
```

### 2.3 数据库层

| 技术 | 版本 | 作用详解 |
|------|------|---------|
| **MyBatis** | - | ORM 框架，XML 映射 SQL。相当于前端的 axios 封装层，但直接操作 SQL |
| **PageHelper** | 1.4.7 | MyBatis 分页插件，`startPage()` 自动拦截 SQL 追加 LIMIT |
| **MySQL** | - | 关系型数据库，存储所有业务数据 |
| **Druid** | 1.2.28 | 阿里数据库连接池，提供监控面板（/druid）、慢 SQL 记录、SQL 防火墙 |

**Druid 关键配置（application-druid.yml）：**
- 初始连接数：5，最小空闲：10，最大活跃：20
- 慢 SQL 阈值：1000ms（自动记录到日志）
- 监控面板地址：`/druid/*`，账号 `ruoyi/123456`
- SQL 防火墙：允许多语句执行（`multi-statement-allow: true`）

### 2.4 缓存中间件 Redis

Redis 在 RuoYi 中承担多种缓存角色，所有 Key 前缀定义在 `CacheConstants.java`：

| Redis Key 前缀 | 常量名 | 存储内容 | TTL |
|---------------|--------|---------|-----|
| `login_tokens:` | `LOGIN_TOKEN_KEY` | 登录用户完整信息（LoginUser 对象） | 30 分钟（可配置） |
| `captcha_codes:` | `CAPTCHA_CODE_KEY` | 验证码文本 | 一次性（用后即删） |
| `sys_config:` | `SYS_CONFIG_KEY` | 系统参数配置 | 永久（参数变更时刷新） |
| `sys_dict:` | `SYS_DICT_KEY` | 字典数据 | 永久（字典变更时刷新） |
| `repeat_submit:` | `REPEAT_SUBMIT_KEY` | 防重复提交 | 短期 |
| `rate_limit:` | `RATE_LIMIT_KEY` | 接口限流计数 | 按配置 |
| `pwd_err_cnt:` | `PWD_ERR_CNT_KEY` | 密码错误次数 | 10 分钟（锁定时间） |

**角色权限热更新缓存方案**：当管理员修改角色权限后，系统扫描 Redis 中所有 `login_tokens:*` Key，找到拥有该角色的在线用户，重新查询其权限并刷新缓存，实现**无需用户重新登录即可生效新权限**。

### 2.5 工具生态

| 技术 | 版本 | 作用详解 |
|------|------|---------|
| **Fastjson2** | 2.0.62 | 阿里 JSON 解析器，用于 JSON 序列化/反序列化。替代 Jackson |
| **Apache POI** | 4.1.2 | Excel 导入导出，通过 `ExcelUtil<T>` 封装，注解驱动（`@Excel` 注解） |
| **Kaptcha** | 2.3.3 | 验证码生成，支持数学计算（`math`）和字符（`char`）两种类型 |
| **Velocity** | 2.3 | 模板引擎，代码生成器使用 `.vm` 模板文件生成 Java/Vue/SQL 代码 |
| **Commons IO** | 2.22.0 | Apache 文件操作工具，文件上传、复制、删除 |
| **OSHI** | 7.3.0 | 系统信息获取，用于服务监控（CPU、内存、磁盘） |
| **Yauaa** | 7.32.0 | User-Agent 解析，识别浏览器和操作系统 |

### 2.6 定时任务 Quartz

| 特性 | 说明 |
|------|------|
| **框架** | Quartz Scheduler（持久化到数据库） |
| **任务存储** | QRTZ_* 系列 11 张表（job_details、triggers、cron_triggers 等） |
| **业务表** | `sys_job`（任务调度表）+ `sys_job_log`（任务日志表） |
| **动态管理** | 支持运行时增删改任务，通过反射调用指定 Bean 方法 |
| **安全限制** | 白名单机制：只允许调用 `com.ruoyi.quartz.task` 包下的方法；黑名单禁止调用 `java.net.URL`、`javax.naming.InitialContext` 等危险类 |

### 2.7 接口文档

| 特性 | 说明 |
|------|------|
| **框架** | Springfox Swagger 3.0.0 |
| **访问地址** | `/swagger-ui/index.html` 或前端代理到 `/tool/swagger` |
| **配置** | `swagger.enabled: true`，`pathMapping: /dev-api` |
| **注解驱动** | `@Api`、`@ApiOperation`、`@ApiImplicitParam` 自动生成接口文档 |

### 2.8 底层配套

| 特性 | 实现类 | 说明 |
|------|--------|------|
| **全局异常处理** | `GlobalExceptionHandler` | `@RestControllerAdvice` 统一捕获异常，返回标准 AjaxResult |
| **跨域处理** | `CorsFilter` | Spring Security 配置中注册，允许跨域请求 |
| **日志框架** | Logback | `logback.xml` 配置，输出到 `logs/sys-info.log`、`sys-error.log`、`sys-user.log` |
| **线程池** | `ThreadPoolConfig` | 异步任务执行（如登录日志异步记录） |
| **数据脱敏** | `@Sensitive` + `DesensitizedUtil` | 注解驱动的敏感数据脱敏（手机号、邮箱等） |
| **IP 解析** | `AddressUtils` | 基于 IP 归属地查询 |
| **XSS 防护** | `XssFilter` + `XssHttpServletRequestWrapper` | 请求参数 XSS 过滤，可配置排除路径 |
| **防盗链** | `RefererFilter` | Referer 头校验，防止资源盗链 |
| **防重复提交** | `@RepeatSubmit` 注解 | 基于自定义 Token 的防重提交机制 |

---

## 第三章 前端完整技术栈与前后端联动映射

### 3.1 前端技术栈全景

| 技术 | 版本/说明 |
|------|----------|
| **Vue 2** | 前端核心框架（Options API） |
| **Element UI** | UI 组件库（表格、表单、弹窗等） |
| **Vue CLI** | 构建工具（基于 Webpack） |
| **Vuex** | 全局状态管理 |
| **Vue Router 3** | 前端路由 |
| **Axios** | HTTP 请求库 |
| **Vue Quill** | 富文本编辑器（公告内容编辑） |
| **jsencrypt** | 前端加密 |
| **file-saver** | 文件下载 |
| **NProgress** | 路由切换进度条 |
| **ECharts** | 图表组件（服务监控可视化） |
| **Clipboard** | 剪贴板复制 |
| **screenfull** | 全屏切换 |

**环境变量配置（.env.development）：**
- `VUE_APP_TITLE`：网页标题
- `VUE_APP_BASE_API`：API 基础路径（开发环境 `/dev-api`，用于代理）

### 3.2 Axios 拦截封装详解

请求封装位于 `src/utils/request.js`，是前后端通信的核心枢纽：

**请求拦截器（Request Interceptor）：**
```javascript
// 1. 自动携带 Token
if (getToken() && !isToken) {
  config.headers['Authorization'] = 'Bearer ' + getToken()
}

// 2. GET 参数拼接到 URL
if (config.method === 'get' && config.params) {
  // 将 params 对象转为 URL Query String
}

// 3. POST/PUT 防重复提交（基于 sessionStorage 时间间隔判断）
if (!isRepeatSubmit && (config.method === 'post' || config.method === 'put')) {
  // 与上次请求对比 URL + 数据 + 时间间隔，相同则拒绝提交
}
```

**响应拦截器（Response Interceptor）：**
```javascript
// 统一处理业务状态码
const code = res.data.code || 200

if (code === 401) {
  // 登录过期 → 弹出重新登录提示 → 跳转登录页
  MessageBox.confirm('登录状态已过期...')
  store.dispatch('LogOut')
} else if (code === 500) {
  // 服务器错误 → Message.error 提示
} else if (code === 601) {
  // 警告信息 → Message.warning
} else if (code !== 200) {
  // 其他错误 → Notification.error
} else {
  return res.data  // 正常返回业务数据
}
```

### 3.3 权限前后端映射：v-hasPermi ↔ @PreAuthorize

这是前后端权限体系的核心映射关系：

**后端（Controller 层）：**
```java
@PreAuthorize("@ss.hasPermi('system:user:list')")  // 权限标识
@GetMapping("/list")
public TableDataInfo list(SysUser user) { ... }
```

- `@PreAuthorize` 是 Spring Security 注解，在方法执行前校验权限
- `@ss` 是自定义的 `PermissionService` Bean（`@Service("ss")`）
- `hasPermi('system:user:list')` 从当前登录用户的 permissions 集合中查找是否包含该权限字符串

**前端（Vue 模板层）：**
```html
<el-button v-hasPermi="['system:user:add']" @click="handleAdd">新增</el-button>
```

- `v-hasPermi` 是自定义指令（`src/directive/permission/hasPermi.js`）
- 从 Vuex 的 `store.getters.permissions` 中查找是否包含该权限
- 如果没有权限，直接从 DOM 中移除该元素（`el.parentNode.removeChild(el)`）

**完整链路：**
```
数据库 sys_menu 表 (perms 字段: 'system:user:list')
    ↓
后端 getInfo 接口返回 permissions 集合
    ↓
前端 Vuex store.commit('SET_PERMISSIONS', permissions)
    ↓
v-hasPermi 指令检查 permissions → 控制 UI 元素显示/隐藏
```

### 3.4 动态路由流程

**完整流程：**

```
1. 用户登录成功 → 存储 Token
2. 路由守卫 (permission.js) 检测 Token
3. 如果 roles 为空（首次进入）：
   ├── dispatch('GetInfo')  → 调用后端 /getInfo 获取用户信息和权限
   └── dispatch('GenerateRoutes') → 调用后端 /getRouters 获取菜单树
4. 后端 /getRouters 返回菜单树 JSON：
   SysLoginController.getRouters()
   → menuService.selectMenuTreeByUserId(userId)  // 按用户查询有权限的菜单
   → menuService.buildMenus(menus)               // 构建前端路由格式
5. 前端 filterAsyncRouter() 转换：
   ├── component === 'Layout'     → 映射为 Layout 组件
   ├── component === 'ParentView' → 映射为 ParentView 组件
   ├── component === 'InnerLink'  → 映射为 InnerLink 组件
   └── 其他 → loadView(component)  → 动态 import 对应 .vue 文件
6. router.addRoutes(accessRoutes)  → 动态注册路由
```

### 3.5 页签滚动层级优化（迭代新增特性）

**问题背景**：多标签页（TagsView）场景下，滚动条层级冲突导致内容溢出。

**底层 CSS/组件逻辑（`AppMain.vue`）：**

```scss
.app-main {
  overflow: hidden;              // 主容器隐藏溢出
  position: relative;
}

.fixed-header + .app-main {
  overflow-y: auto;              // 固定头部时，主区域独立滚动
  scrollbar-gutter: auto;        // 预留滚动条空间，防止布局抖动
  height: calc(100vh - 50px);    // 精确计算可视区域高度
}

.hasTagsView {
  .app-main {
    min-height: calc(100vh - 84px);  // 84 = navbar(50) + tagsView(34)
  }
}

// 移动端安全区域适配
@media screen and (max-width: 991px) {
  .fixed-header + .app-main {
    padding-bottom: max(60px, calc(env(safe-area-inset-bottom) + 40px));
    overscroll-behavior-y: none;    // 防止滚动链（scroll chaining）
  }
}

// iOS Safari 100vh 问题修复
@supports (-webkit-touch-callout: none) {
  .fixed-header + .app-main {
    height: calc(100svh - 50px);    // small viewport height
    height: calc(100dvh - 50px);    // dynamic viewport height
  }
}
```

**ScrollPane 组件优化（`TagsView/ScrollPane.vue`）：**
- 实现平滑滚动（`requestAnimationFrame` + 缓动函数 `ease`）
- 滚轮事件拦截（`@wheel.native.prevent`）
- 滚动完成后触发 `updateArrows` 事件更新左右箭头状态

### 3.6 在线表单构建器

位于 `src/views/tool/build/index.vue`，是一个**拖拽式 HTML 表单代码生成器**：

- **核心库**：基于 `vue-form-making` 或自研拖拽组件
- **功能**：拖拽表单元素（输入框、选择器、日期等）到画布，生成对应的 HTML 源码
- **输出**：可复制的 HTML 代码，直接粘贴到页面使用
- **权限**：`tool:build:list`

---

## 第四章 后端多模块分层详解

### 4.1 ruoyi-admin（启动模块）

**定位**：系统启动入口，包含所有 Controller 和启动配置。

**核心文件：**

| 文件 | 作用 |
|------|------|
| `RuoYiApplication.java` | Spring Boot 启动类，`@SpringBootApplication` |
| `RuoYiServletInitializer.java` | WAR 部署初始化器 |
| `application.yml` | 主配置文件 |
| `application-druid.yml` | 数据源配置 |
| `logback.xml` | 日志配置 |

**Controller 层完整清单（src/main/java/com/ruoyi/web/controller/）：**

| Controller | 路径前缀 | 职责 |
|-----------|---------|------|
| `SysLoginController` | `/` (login, getInfo, getRouters) | 登录、获取用户信息和路由 |
| `SysUserController` | `/system/user` | 用户管理 CRUD |
| `SysRoleController` | `/system/role` | 角色管理 CRUD（含权限热刷新） |
| `SysMenuController` | `/system/menu` | 菜单管理 CRUD |
| `SysDeptController` | `/system/dept` | 部门管理 CRUD |
| `SysPostController` | `/system/post` | 岗位管理 CRUD |
| `SysDictTypeController` | `/system/dict/type` | 字典类型管理 |
| `SysDictDataController` | `/system/dict/data` | 字典数据管理 |
| `SysConfigController` | `/system/config` | 参数配置管理 |
| `SysNoticeController` | `/system/notice` | 通知公告管理（含阅读记录功能） |
| `SysProfileController` | `/system/user/profile` | 个人信息、修改密码、头像上传 |
| `SysOperlogController` | `/monitor/operlog` | 操作日志查询 |
| `SysLogininforController` | `/monitor/logininfor` | 登录日志查询 |
| `SysUserOnlineController` | `/monitor/online` | 在线用户管理 |
| `SysJobController` | `/monitor/job` | 定时任务管理 |
| `GenController` | `/tool/gen` | 代码生成器 |
| `TestController` | `/test/user` | Swagger 接口示例 |
| `CaptChaController` | `/captchaImage` | 验证码生成 |
| `CommonController` | `/common` | 通用文件上传/下载 |
| `IndexController` | `/` | 首页 |

### 4.2 ruoyi-framework（框架核心模块）

**定位**：Spring Security 安全认证、权限拦截、AOP 切面、数据源配置。

**核心类清单：**

| 类名 | 作用详解 |
|------|---------|
| `SecurityConfig` | Spring Security 配置：禁用 CSRF、禁用 Session、注册 JWT 过滤器、配置匿名访问地址 |
| `TokenService` | Token 核心服务：创建/验证/刷新/删除令牌，**权限热刷新方法 `refreshPermissionByRoleId()`** |
| `SysLoginService` | 登录核心逻辑：验证码校验、前置校验、Spring Security 认证、生成 Token |
| `SysPasswordService` | 密码校验：错误次数计数、账号锁定、BCrypt 匹配 |
| `UserDetailsServiceImpl` | 用户详情加载：查数据库、检查状态、加载权限、返回 LoginUser |
| `SysPermissionService` | 权限获取：`getRolePermission()` 获取角色、`getMenuPermission()` 获取菜单权限 |
| `PermissionService` | `@Service("ss")` 权限校验 Bean：`hasPermi()`、`hasRole()` 供 `@PreAuthorize` 调用 |
| `JwtAuthenticationTokenFilter` | JWT 过滤器：每个请求解析 Token → 获取 LoginUser → 设置 SecurityContext |
| `AuthenticationEntryPointImpl` | 认证失败处理：返回 401 JSON |
| `LogoutSuccessHandlerImpl` | 退出处理：删除 Redis 中的 Token |
| `DataScopeAspect` | 数据权限切面：拦截 `@DataScope` 注解，自动拼接数据范围 SQL |
| `GlobalExceptionHandler` | 全局异常处理：统一捕获异常返回标准错误格式 |

**权限热刷新核心代码（`TokenService.refreshPermissionByRoleId()`）：**

```java
public void refreshPermissionByRoleId(Long roleId, SysPermissionService permissionService) {
    // 1. 扫描 Redis 中所有在线 Token（login_tokens:* 模式匹配）
    String pattern = CacheConstants.LOGIN_TOKEN_KEY + "*";
    Collection<String> keys = redisCache.keys(pattern);
    if (keys == null || keys.isEmpty()) return;

    for (String key : keys) {
        LoginUser loginUser = redisCache.getCacheObject(key);
        // 2. 跳过管理员（管理员拥有所有权限）
        if (loginUser == null || loginUser.getUser() == null || loginUser.getUser().isAdmin())
            continue;
        // 3. 判断该用户是否拥有被修改的角色
        boolean hasRole = loginUser.getUser().getRoles() != null
                && loginUser.getUser().getRoles().stream()
                    .anyMatch(r -> roleId.equals(r.getRoleId()));
        if (!hasRole) continue;
        // 4. 重新查询权限并刷新 Redis 缓存
        loginUser.setPermissions(permissionService.getMenuPermission(loginUser.getUser()));
        refreshToken(loginUser);
        log.info("角色[{}]权限变更，已刷新在线用户[{}]的权限缓存", roleId, loginUser.getUsername());
    }
}
```

**调用时机**：`SysRoleController.edit()` 方法中，角色修改成功后立即调用。

### 4.3 ruoyi-common（公共工具模块）

**定位**：常量、异常、工具类、注解、过滤器，被所有模块依赖。

**核心包结构：**

```
com.ruoyi.common
├── annotation/      → 自定义注解（@DataScope, @Log, @Excel, @RateLimiter, @RepeatSubmit, @Sensitive）
├── config/          → RuoYiConfig（读取 application.yml 配置）
├── constant/        → 常量定义（CacheConstants, Constants, UserConstants, HttpStatus）
├── core/            → 核心基础类（BaseController, BaseEntity, AjaxResult, RedisCache, LoginUser）
├── enums/           → 枚举（BusinessType, BusinessStatus, DataSourceType, UserStatus）
├── exception/       → 自定义异常体系
├── filter/          → 过滤器（XssFilter, RefererFilter, RepeatableFilter）
├── utils/           → 工具类集合（SecurityUtils, StringUtils, DateUtils, FileUploadUtils...）
└── xss/             → XSS 校验（XssValidator）
```

**关键工具类解读：**

| 工具类 | 作用 | 前端类比 |
|--------|------|---------|
| `SecurityUtils` | 获取当前登录用户、加密密码、校验权限 | 类似 Vuex 中的 getters |
| `FileUploadUtils` | 文件上传核心：大小校验、**后缀白名单校验**、路径生成 | 类似前端的上传组件封装 |
| `MimeTypeUtils` | **文件后缀白名单定义**（不含 html/htm/js 等危险文件） | 类似 MIME 类型配置 |
| `ExcelUtil<T>` | Excel 导入导出，注解驱动 | 类似前端的 xlsx 插件 |
| `DictUtils` | 字典缓存工具，从 Redis 读取字典 | 类似前端的本地缓存 |
| `ServletUtils` | 获取 Request/Response 对象 | 类似前端的 window.location |

### 4.4 ruoyi-system（系统业务模块）

**定位**：系统核心业务的 Service + Mapper + Domain 实现。

**Service 层完整清单：**

| Service 接口 | 实现类 | 职责 |
|-------------|--------|------|
| `ISysUserService` | `SysUserServiceImpl` | 用户管理（含数据权限） |
| `ISysRoleService` | `SysRoleServiceImpl` | 角色管理（含角色菜单关联） |
| `ISysMenuService` | `SysMenuServiceImpl` | 菜单管理 + 路由树构建 |
| `ISysDeptService` | `SysDeptServiceImpl` | 部门管理（树结构 + 数据权限） |
| `ISysPostService` | `SysPostServiceImpl` | 岗位管理 |
| `ISysDictTypeService` | `SysDictTypeServiceImpl` | 字典类型管理 |
| `ISysDictDataService` | `SysDictDataServiceImpl` | 字典数据管理 |
| `ISysConfigService` | `SysConfigServiceImpl` | 参数配置管理（含 Redis 缓存） |
| `ISysNoticeService` | `SysNoticeServiceImpl` | 通知公告管理 |
| `ISysNoticeReadService` | `SysNoticeReadServiceImpl` | **公告已读记录管理（新增功能）** |
| `ISysOperLogService` | `SysOperLogServiceImpl` | 操作日志管理 |
| `ISysLogininforService` | `SysLogininforServiceImpl` | 登录日志管理 |

### 4.5 ruoyi-quartz（定时任务模块）

**定位**：基于 Quartz 的动态定时任务管理。

**核心类：**

| 类名 | 作用 |
|------|------|
| `SysJobController` | 任务管理 Controller（增删改查、执行、暂停） |
| `SysJobLogController` | 任务日志查询 |
| `ScheduleConfig` | Quartz 调度器配置（JDBC 持久化） |
| `ScheduleJob` | 任务调度执行入口，通过反射调用目标方法 |
| `QuartzJobExecution` | 允许并发的任务执行 |
| `QuartzDisallowConcurrentExecution` | 禁止并发的任务执行 |
| `RyTask` | 示例任务类（`ryTask.ryNoParams()` 等） |

### 4.6 ruoyi-generator（代码生成器模块）

**定位**：读取数据库表结构，使用 Velocity 模板生成前后端全套代码。

**核心类：**

| 类名 | 作用 |
|------|------|
| `GenController` | 代码生成 API（查询表、导入、预览、下载） |
| `GenTableServiceImpl` | 表管理业务逻辑 |
| `GenTableColumnServiceImpl` | 字段管理业务逻辑 |
| `GenUtils` | 代码生成工具（表名转换、字段映射） |
| `VelocityUtils` | Velocity 模板上下文准备 |
| `VelocityInitializer` | Velocity 引擎初始化 |

---

## 第五章 系统全部内置功能完整拆解

### 5.1 用户管理

| 维度 | 详情 |
|------|------|
| **数据库表** | `sys_user`（用户信息表）、`sys_user_role`（用户-角色关联）、`sys_user_post`（用户-岗位关联） |
| **后端接口** | `/system/user/list`（查询）、`/system/user`（新增）、`/system/user`（修改）、`/system/user/{ids}`（删除）、`/system/user/export`（导出）、`/system/user/importData`（导入）、`/system/user/resetPwd`（重置密码）、`/system/user/changeStatus`（状态修改）、`/system/user/authRole`（授权角色） |
| **权限校验** | `system:user:list`、`system:user:query`、`system:user:add`、`system:user:edit`、`system:user:remove`、`system:user:export`、`system:user:import`、`system:user:resetPwd` |
| **前端路由** | `/system/user` → `system/user/index.vue` |
| **数据权限** | `@DataScope(deptAlias = "d", userAlias = "u")` 拦截用户查询，按角色数据范围过滤 |
| **密码加密** | `SecurityUtils.encryptPassword()` → BCrypt 加密存储 |

### 5.2 部门管理

| 维度 | 详情 |
|------|------|
| **数据库表** | `sys_dept`（部门表，树结构：parent_id + ancestors 祖级列表） |
| **后端接口** | `/system/dept/list`、`/system/dept`、`/system/dept/{deptId}` 等 |
| **数据权限** | `@DataScope(deptAlias = "d")` 拦截部门查询 |
| **树结构** | `ancestors` 字段存储祖级路径（如 `0,100,101`），配合 `find_in_set()` 实现层级查询 |
| **前端路由** | `/system/dept` → `system/dept/index.vue` |

### 5.3 岗位管理

| 维度 | 详情 |
|------|------|
| **数据库表** | `sys_post`（岗位信息表） |
| **后端接口** | `/system/post/list`、`/system/post` 等 |
| **权限校验** | `system:post:list`、`system:post:add`、`system:post:edit`、`system:post:remove`、`system:post:export` |
| **前端路由** | `/system/post` → `system/post/index.vue` |

### 5.4 菜单管理

| 维度 | 详情 |
|------|------|
| **数据库表** | `sys_menu`（菜单权限表：menu_type M目录/C菜单/F按钮） |
| **后端接口** | `/system/menu/list`、`/system/menu` 等 |
| **核心方法** | `selectMenuTreeByUserId()` 按用户权限查询菜单树、`buildMenus()` 构建前端路由格式 |
| **权限标识** | `perms` 字段（如 `system:user:list`）与 `@PreAuthorize` 注解一一对应 |
| **前端路由** | `/system/menu` → `system/menu/index.vue` |

### 5.5 角色管理（含权限热刷新）

| 维度 | 详情 |
|------|------|
| **数据库表** | `sys_role`（角色表，含 data_scope 数据范围字段）、`sys_role_menu`（角色-菜单关联）、`sys_role_dept`（角色-部门关联，用于自定义数据权限） |
| **后端接口** | `/system/role/list`、`/system/role`、`/system/role/dataScope`（数据权限配置）、`/system/role/changeStatus`、`/system/role/authUser/*`（用户授权） |
| **权限热刷新** | `SysRoleController.edit()` 修改角色后调用 `tokenService.refreshPermissionByRoleId()` |
| **数据范围** | `data_scope` 字段：1全部、2自定义、3本部门、4本部门及以下、5仅本人 |
| **前端路由** | `/system/role` → `system/role/index.vue` |

### 5.6 字典管理

| 维度 | 详情 |
|------|------|
| **数据库表** | `sys_dict_type`（字典类型）、`sys_dict_data`（字典数据） |
| **缓存机制** | `DictUtils` 从 Redis 读取（`sys_dict:` 前缀），字典变更时自动刷新缓存 |
| **前端使用** | `<el-select v-for="dict in dict.type.sys_user_sex">` 动态渲染下拉选项 |
| **前端路由** | `/system/dict` → `system/dict/index.vue` |

### 5.7 参数管理

| 维度 | 详情 |
|------|------|
| **数据库表** | `sys_config`（参数配置表） |
| **缓存机制** | `sys_config:` 前缀，参数变更时刷新 Redis |
| **关键参数** | `sys.user.initPassword`（初始密码）、`sys.account.captchaEnabled`（验证码开关）、`sys.account.chrtype`（**密码字符范围**）、`sys.account.initPasswordModify`（初始密码修改策略）、`sys.account.passwordValidateDays`（密码更新周期）、`sys.login.blackIPList`（登录 IP 黑名单） |
| **前端路由** | `/system/config` → `system/config/index.vue` |

### 5.8 通知公告（含阅读记录统计 - 迭代新增特性）

这是本次版本的重要新增功能，完整实现了公告阅读用户统计。

| 维度 | 详情 |
|------|------|
| **数据库表** | `sys_notice`（公告表）+ **`sys_notice_read`（公告已读记录表 - 新增）** |
| **新增表结构** | `sys_notice_read`：read_id(主键)、notice_id(公告ID)、user_id(用户ID)、read_time(阅读时间)，唯一索引 `uk_user_notice` 防止重复记录 |
| **后端接口** | `/system/notice/list`（列表）、`/system/notice/{noticeId}`（详情）、`/system/notice`（新增/修改）、`/system/notice/{noticeIds}`（删除）、**`/system/notice/listTop`（首页公告+未读数）**、**`/system/notice/markRead`（标记已读）**、**`/system/notice/markReadAll`（批量已读）**、**`/system/notice/readUsers/list`（已读用户列表）** |
| **权限校验** | `system:notice:list`、`system:notice:add`、`system:notice:edit`、`system:notice:remove` |
| **前端路由** | `/system/notice` → `system/notice/index.vue` |
| **核心逻辑** | `INSERT IGNORE INTO sys_notice_read` 实现幂等标记已读；LEFT JOIN 查询带已读状态的公告列表；INNER JOIN 查询已读用户列表 |

**完整源码链路：**

```
Controller: SysNoticeController
  ├── listTop() → noticeReadService.selectNoticeListWithReadStatus(userId, 5)
  │     → SQL: SELECT n.*, CASE WHEN r.notice_id IS NOT NULL THEN true ELSE false END AS isRead
  │            FROM sys_notice n LEFT JOIN sys_notice_read r ON ...
  ├── markRead(noticeId) → noticeReadService.markRead(noticeId, userId)
  │     → SQL: INSERT IGNORE INTO sys_notice_read (notice_id, user_id, read_time) VALUES (...)
  ├── markReadAll(ids) → noticeReadService.markReadBatch(userId, noticeIds)
  │     → SQL: INSERT IGNORE INTO sys_notice_read ... VALUES (批量)
  ├── readUsersList(noticeId) → noticeReadService.selectReadUsersByNoticeId(noticeId, searchValue)
  │     → SQL: SELECT u.user_name, u.nick_name, d.dept_name, r.read_time
  │            FROM sys_notice_read r INNER JOIN sys_user u ON ... LEFT JOIN sys_dept d ON ...
  └── remove(noticeIds) → noticeReadService.deleteByNoticeIds(noticeIds)  // 删除公告时清理已读记录
```

### 5.9 操作日志

| 维度 | 详情 |
|------|------|
| **数据库表** | `sys_oper_log`（操作日志表，含请求参数、返回结果、消耗时间） |
| **实现机制** | `@Log` 注解 + AOP 切面（`LogAspect`），自动记录操作 |
| **后端接口** | `/monitor/operlog/list`、`/monitor/operlog/{operIds}`（删除）、`/monitor/operlog/clean`（清空）、`/monitor/operlog/export`（导出） |
| **前端路由** | `/monitor/operlog` → `monitor/operlog/index.vue` |

### 5.10 登录日志

| 维度 | 详情 |
|------|------|
| **数据库表** | `sys_logininfor`（系统访问记录表） |
| **记录方式** | 异步线程池记录（`AsyncManager.me().execute(AsyncFactory.recordLogininfor(...))`） |
| **后端接口** | `/monitor/logininfor/list`、`/monitor/logininfor/{infoIds}`（删除）、`/monitor/logininfor/unlock/{userName}`（账户解锁）、`/monitor/logininfor/clean`（清空） |
| **前端路由** | `/monitor/logininfor` → `monitor/logininfor/index.vue` |

### 5.11 在线用户

| 维度 | 详情 |
|------|------|
| **数据来源** | Redis（`login_tokens:*` 所有在线 Token） |
| **后端接口** | `/monitor/online/list`（查询）、`/monitor/online/{tokenId}`（强退） |
| **强退逻辑** | `redisCache.deleteObject(CacheConstants.LOGIN_TOKEN_KEY + tokenId)` |
| **前端路由** | `/monitor/online` → `monitor/online/index.vue` |

### 5.12 定时任务

| 维度 | 详情 |
|------|------|
| **数据库表** | `sys_job`（任务表）+ `sys_job_log`（日志表）+ QRTZ_*（Quartz 内部表） |
| **后端接口** | `/monitor/job/list`、`/monitor/job`（增删改）、`/monitor/job/changeStatus`（暂停/恢复）、`/monitor/job/run`（立即执行）、`/monitor/job/export` |
| **任务调用** | 反射机制：`invokeTarget` 字段（如 `ryTask.ryNoParams`）→ 查找 Spring Bean → 反射调用方法 |
| **安全限制** | 白名单 `com.ruoyi.quartz.task`，黑名单禁止 `java.net.URL`、`org.springframework` 等 |
| **前端路由** | `/monitor/job` → `monitor/job/index.vue` |

### 5.13 代码生成

| 维度 | 详情 |
|------|------|
| **数据库表** | `gen_table`（代码生成业务表）+ `gen_table_column`（字段表） |
| **后端接口** | `/tool/gen/list`、`/tool/gen/importTable`（导入表）、`/tool/gen/preview/{tableId}`（预览）、`/tool/gen/download/{tableName}`（下载）、`/tool/gen/genCode/{tableName}`（生成）、`/tool/gen/synchDb/{tableName}`（同步） |
| **前端路由** | `/tool/gen` → `tool/gen/index.vue` |

### 5.14 服务监控

| 维度 | 详情 |
|------|------|
| **实现** | OSHI 库获取 CPU、内存、磁盘、JVM 信息 |
| **后端接口** | `/monitor/server` |
| **前端路由** | `/monitor/server` → `monitor/server/index.vue` |

### 5.15 缓存监控

| 维度 | 详情 |
|------|------|
| **实现** | Redis 命令统计（info、dbsize、commandStats） |
| **后端接口** | `/monitor/cache`、`/monitor/cache/getNames`、`/monitor/cache/getKeys/{cacheName}`、`/monitor/cache/getValue/{cacheName}/{cacheKey}`、`/monitor/cache/clearCacheKey`、`/monitor/cache/clearCacheName`、`/monitor/cache/clearCacheAll` |
| **前端路由** | `/monitor/cache` → `monitor/cache/index.vue`、`/monitor/cacheList` → `monitor/cache/list.vue` |

### 5.16 连接池监控（Druid）

| 维度 | 详情 |
|------|------|
| **实现** | Druid 内置 StatViewServlet，地址 `/druid/*` |
| **账号** | `ruoyi / 123456`（application-druid.yml 配置） |
| **功能** | SQL 监控、慢 SQL 分析、URI 监控、Session 监控、连接池状态 |
| **前端路由** | `/monitor/druid` → iframe 内嵌 Druid 页面 |

### 5.17 在线表单构建器

| 维度 | 详情 |
|------|------|
| **前端路由** | `/tool/build` → `tool/build/index.vue` |
| **权限** | `tool:build:list` |
| **功能** | 拖拽表单元素 → 生成 HTML 源码 → 复制使用 |

---

## 第六章 核心权限体系底层原理

### 6.1 完整登录流程

```
1. 前端：用户输入 用户名 + 密码 + 验证码
2. 前端：POST /login { username, password, code, uuid }
3. 后端 SysLoginController.login()
   → SysLoginService.login()
     ├── validateCaptcha(): Redis 取验证码（captcha_codes:{uuid}），对比后删除
     ├── loginPreCheck(): 用户名/密码长度校验 + IP 黑名单校验
     └── authenticationManager.authenticate()
         → UserDetailsServiceImpl.loadUserByUsername()
           ├── userService.selectUserByUserName(): 查数据库
           ├── 检查用户状态（删除/停用 → 抛异常）
           └── SysPasswordService.validate()
               ├── Redis 取错误次数（pwd_err_cnt:{username}）
               ├── 超过 5 次 → 抛 UserPasswordRetryLimitExceedException（锁定 10 分钟）
               ├── BCrypt.matches(原始密码, 数据库密码) → 不匹配则错误次数+1
               └── 匹配成功 → 清除错误计数
4. 后端：tokenService.createToken(loginUser)
   ├── 生成 UUID 作为 Token 标识
   ├── setUserAgent(): 设置 IP、浏览器、操作系统
   ├── refreshToken(): 设置过期时间（30分钟）→ 存入 Redis（login_tokens:{uuid}）
   └── 生成 JWT（包含 UUID + 用户名，HS512 签名）
5. 返回前端：{ code: 200, token: "eyJhbG..." }
6. 前端：setToken(res.token) → 存入 Cookie + Vuex
```

### 6.2 请求鉴权链路

```
每个 HTTP 请求：
  ↓
JwtAuthenticationTokenFilter.doFilterInternal()
  ├── tokenService.getLoginUser(request)
  │   ├── 从 Header 取 Authorization: Bearer xxx
  │   ├── 解析 JWT → 获取 UUID
  │   └── Redis 取 LoginUser（login_tokens:{uuid}）
  ├── tokenService.verifyToken(loginUser)
  │   └── 如果距过期不足 20 分钟 → refreshToken() 自动续期
  └── SecurityContextHolder.setAuthentication(authenticationToken)
  ↓
到达 Controller
  ├── @PreAuthorize("@ss.hasPermi('xxx')")
  │   → PermissionService.hasPermi()
  │     → 从 SecurityContext 获取 LoginUser
  │     → 检查 loginUser.getPermissions() 是否包含权限字符串
  └── 方法执行
```

**Token 过期/无效处理：**
- Token 过期 → Redis 中 LoginUser 被自动清除 → `getLoginUser()` 返回 null → 未认证 → `AuthenticationEntryPointImpl` 返回 401
- 前端收到 401 → `request.js` 响应拦截器 → 弹出"登录状态已过期" → 跳转登录页

### 6.3 三层权限完整源码

#### 第一层：菜单权限（路由级）

**后端**：`SysLoginController.getRouters()` → 按用户角色查询有权限的菜单树
**前端**：路由守卫动态注册路由（`router.addRoutes()`），无权限的菜单不出现在侧边栏

```java
// SysMenuServiceImpl
public List<SysMenu> selectMenuTreeByUserId(Long userId) {
    if (SecurityUtils.isAdmin(userId)) {
        // 管理员返回所有菜单
        return menuMapper.selectMenuTreeAll();
    } else {
        // 普通用户按角色查询
        return menuMapper.selectMenuTreeByUserId(userId);
    }
}
```

#### 第二层：按钮权限（操作级）

**后端**：`@PreAuthorize("@ss.hasPermi('system:user:add')")` 注解
**前端**：`v-hasPermi="['system:user:add']"` 指令

```java
// PermissionService（@Service("ss")）
public boolean hasPermi(String permission) {
    LoginUser loginUser = SecurityUtils.getLoginUser();
    if (StringUtils.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getPermissions()))
        return false;
    PermissionContextHolder.setContext(permission);
    return hasPermissions(loginUser.getPermissions(), permission);
}

private boolean hasPermissions(Set<String> permissions, String permission) {
    return permissions.contains(Constants.ALL_PERMISSION) ||  // *:*:* 超级权限
           permissions.contains(StringUtils.trim(permission));
}
```

#### 第三层：数据范围权限（数据级）

**注解**：`@DataScope(deptAlias = "d", userAlias = "u")`
**切面**：`DataScopeAspect`（AOP `@Before` 拦截）

```java
// 五种数据范围
Constants.Dept.DATA_SCOPE_ALL = "1"            // 全部数据：不加条件
Constants.Dept.DATA_SCOPE_CUSTOM = "2"          // 自定义：OR d.dept_id IN (SELECT dept_id FROM sys_role_dept WHERE role_id = ?)
Constants.Dept.DATA_SCOPE_DEPT = "3"            // 本部门：OR d.dept_id = 用户部门ID
Constants.Dept.DATA_SCOPE_DEPT_AND_CHILD = "4" // 本部门及以下：OR d.dept_id IN (SELECT dept_id FROM sys_dept WHERE dept_id = ? OR find_in_set(?, ancestors))
Constants.Dept.DATA_SCOPE_SELF = "5"            // 仅本人：OR u.user_id = 当前用户ID
```

**数据范围 SQL 拼接结果示例**（本部门及以下）：
```sql
SELECT * FROM sys_user u LEFT JOIN sys_dept d ON u.dept_id = d.dept_id
WHERE u.del_flag = '0'
AND (d.dept_id IN (SELECT dept_id FROM sys_dept WHERE dept_id = 103 OR find_in_set(103, ancestors)))
```

### 6.4 角色权限变更后实时刷新（关键新增逻辑）

**完整实现链路：**

```
管理员修改角色权限
  ↓
SysRoleController.edit()
  ├── roleService.updateRole(role)  → 更新 sys_role + sys_role_menu 关联表
  └── tokenService.refreshPermissionByRoleId(role.getRoleId(), permissionService)
      ├── 遍历 Redis 所有 login_tokens:* Key
      ├── 获取每个 LoginUser
      ├── 跳过管理员（拥有所有权限）
      ├── 判断用户是否拥有该角色（检查 loginUser.getUser().getRoles()）
      ├── 如果有 → 重新查询权限：permissionService.getMenuPermission(loginUser.getUser())
      ├── 更新 loginUser.setPermissions(newPermissions)
      └── refreshToken(loginUser) → 重新写入 Redis
  ↓
在线用户下次请求时自动使用新权限，无需重新登录
```

**补充机制**：`getInfo` 接口也有权限刷新逻辑：
```java
// SysLoginController.getInfo()
if (!loginUser.getPermissions().equals(permissions)) {
    loginUser.setPermissions(permissions);
    tokenService.refreshToken(loginUser);
}
```
这确保即使用户当前 Token 的权限缓存未被角色变更触发刷新，下次调用 `getInfo` 时也会自动同步。

---

## 第七章 数据库完整设计

### 7.1 核心业务表清单

| 序号 | 表名 | 说明 | 关键字段 |
|------|------|------|---------|
| 1 | `sys_dept` | 部门表 | dept_id, parent_id, ancestors（祖级列表） |
| 2 | `sys_user` | 用户信息表 | user_id, dept_id, password（BCrypt）, pwd_update_date |
| 3 | `sys_post` | 岗位信息表 | post_id, post_code |
| 4 | `sys_role` | 角色信息表 | role_id, role_key, **data_scope**（数据范围） |
| 5 | `sys_menu` | 菜单权限表 | menu_id, **menu_type**（M目录/C菜单/F按钮）, **perms**（权限标识） |
| 6 | `sys_user_role` | 用户-角色关联表 | user_id, role_id（联合主键） |
| 7 | `sys_role_menu` | 角色-菜单关联表 | role_id, menu_id（联合主键） |
| 8 | `sys_role_dept` | 角色-部门关联表 | role_id, dept_id（自定义数据权限） |
| 9 | `sys_user_post` | 用户-岗位关联表 | user_id, post_id |
| 10 | `sys_oper_log` | 操作日志表 | oper_id, method, oper_param, json_result, cost_time |
| 11 | `sys_dict_type` | 字典类型表 | dict_id, dict_type（唯一） |
| 12 | `sys_dict_data` | 字典数据表 | dict_code, dict_type, dict_label, dict_value |
| 13 | `sys_config` | 参数配置表 | config_id, config_key, config_value |
| 14 | `sys_logininfor` | 系统访问记录 | info_id, user_name, ipaddr, status |
| 15 | `sys_job` | 定时任务调度表 | job_id, invoke_target, cron_expression |
| 16 | `sys_job_log` | 定时任务日志表 | job_log_id, job_message, exception_info |
| 17 | `sys_notice` | 通知公告表 | notice_id, notice_type, notice_content（longblob） |
| **18** | **`sys_notice_read`** | **公告已读记录表（新增）** | read_id, notice_id, user_id, read_time |
| 19 | `gen_table` | 代码生成业务表 | table_id, table_name, class_name, tpl_category |
| 20 | `gen_table_column` | 代码生成字段表 | column_id, table_id, java_field, html_type |

### 7.2 权限五联表关联图

```
sys_user ──N:1── sys_user_role ──1:N── sys_role
    │                                           │
    │                                      sys_role_menu ──1:N── sys_menu
    │                                           │
    └──N:1── sys_dept                     sys_role_dept ──1:N── sys_dept
                    ↑                                           ↑
              sys_user_post                              数据范围过滤
              ──1:N── sys_post
```

**关联关系详解：**
- 用户 → 角色：多对多（`sys_user_role`）
- 角色 → 菜单：多对多（`sys_role_menu`），菜单包含目录(M)、菜单(C)、按钮(F)三种类型
- 角色 → 部门：多对多（`sys_role_dept`），仅当 `data_scope=2`（自定义）时使用
- 用户 → 岗位：多对多（`sys_user_post`）
- 用户 → 部门：多对一（`sys_user.dept_id`）

### 7.3 新增专属数据表：sys_notice_read

```sql
CREATE TABLE sys_notice_read (
  read_id     bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '已读主键',
  notice_id   int(4)      NOT NULL                 COMMENT '公告id',
  user_id     bigint(20)   NOT NULL                 COMMENT '用户id',
  read_time   datetime     NOT NULL                 COMMENT '阅读时间',
  PRIMARY KEY (read_id),
  UNIQUE KEY uk_user_notice (user_id, notice_id)     -- 同一用户同一公告只记录一次
) ENGINE=InnoDB COMMENT='公告已读记录表';
```

**设计要点：**
- `UNIQUE KEY uk_user_notice`：联合唯一索引，配合 `INSERT IGNORE` 实现幂等标记已读
- 查询未读数：`SELECT COUNT(*) FROM sys_notice n WHERE n.status='0' AND NOT EXISTS (SELECT 1 FROM sys_notice_read r WHERE r.notice_id=n.notice_id AND r.user_id=#{userId})`
- 查询已读用户列表：`INNER JOIN sys_user` + `LEFT JOIN sys_dept` 获取用户详情

### 7.4 Quartz 定时任务系列表

| 表名 | 说明 |
|------|------|
| `QRTZ_JOB_DETAILS` | 任务详细信息（job_class_name、is_nonconcurrent） |
| `QRTZ_TRIGGERS` | 触发器信息（trigger_state、trigger_type） |
| `QRTZ_SIMPLE_TRIGGERS` | 简单触发器（repeat_count、repeat_interval） |
| `QRTZ_CRON_TRIGGERS` | Cron 触发器（cron_expression、time_zone_id） |
| `QRTZ_BLOB_TRIGGERS` | Blob 类型触发器 |
| `QRTZ_CALENDARS` | 日历信息 |
| `QRTZ_PAUSED_TRIGGER_GRPS` | 暂停的触发器组 |
| `QRTZ_FIRED_TRIGGERS` | 已触发的触发器 |
| `QRTZ_SCHEDULER_STATE` | 调度器状态（集群用） |
| `QRTZ_LOCKS` | 悲观锁信息 |
| `QRTZ_SIMPROP_TRIGGERS` | 同步机制行锁表 |

### 7.5 安全相关配置字段

**sys_config 表中的安全参数：**

| config_key | config_value | 说明 |
|-----------|-------------|------|
| `sys.user.initPassword` | `123456` | 新用户初始密码 |
| `sys.account.captchaEnabled` | `true` | 验证码开关 |
| `sys.account.registerUser` | `false` | 注册功能开关 |
| `sys.login.blackIPList` | （空） | 登录 IP 黑名单（支持通配符和网段） |
| `sys.account.initPasswordModify` | `1` | 初始密码修改策略（0关闭/1提醒） |
| `sys.account.passwordValidateDays` | `0` | 密码更新周期（0不限制，否则天数） |
| `sys.account.chrtype` | `0` | **密码字符范围**（0任意/1数字/2字母/3字母+数字/4字母+数字+特殊字符） |

### 7.6 SQL 脚本执行顺序

1. **先执行** `sql/ry_20260417.sql`：创建全部业务表（20张表）+ 初始化数据
2. **再执行** `sql/quartz.sql`：创建 Quartz 定时任务表（11张表）
3. 数据库名默认 `ry-vue`，字符集 UTF-8

---

## 第八章 代码生成器完整底层实现

### 8.1 读取数据库表结构流程

```
1. GenController.importTable(tables)  → 导入表
   → GenTableServiceImpl.importGenTable(tables)
     ├── 查询 information_schema.tables（获取表注释）
     ├── 查询 information_schema.columns（获取字段名、类型、注释）
     ├── 创建 gen_table 记录（表信息）
     ├── 创建 gen_table_column 记录（字段信息，含 Java 类型映射）
     └── GenUtils.initColumnField() → 自动推断：
         ├── is_pk（是否主键）
         ├── is_increment（是否自增）
         ├── java_type（Java 类型映射：varchar→String, bigint→Long...）
         ├── java_field（驼峰命名转换）
         ├── html_type（显示类型推断：varchar→input, text→textarea, datetime→datetime...）
         └── query_type（查询方式：EQ/NE/GT/LT/LIKE/BETWEEN）
```

### 8.2 模板引擎生成

使用 **Apache Velocity** 模板引擎，21 个 `.vm` 模板文件：

**Java 模板（vm/java/）：**
| 模板文件 | 生成内容 |
|---------|---------|
| `controller.java.vm` | Controller 层（含 CRUD 接口 + @PreAuthorize 权限注解） |
| `domain.java.vm` | 实体类（含字段、Getter/Setter、toString） |
| `mapper.java.vm` | Mapper 接口 |
| `service.java.vm` | Service 接口 |
| `serviceImpl.java.vm` | Service 实现 |
| `sub-domain.java.vm` | 子表实体类 |
| `xml/mapper.xml.vm` | MyBatis XML 映射文件 |

**前端模板（vm/vue/）：**
| 模板文件 | 生成内容 |
|---------|---------|
| `index.vue.vm` | Vue2 + Element UI 列表页面（含增删改查弹窗） |
| `index-tree.vue.vm` | 树表页面 |
| `view.vue.vm` | 详情查看页面 |

**Vue3 模板（vm/vue/v3/、vm/vue/v3ts/）：**
- 支持 Element Plus 版本
- 支持 TypeScript 版本

**其他模板：**
| 模板文件 | 生成内容 |
|---------|---------|
| `js/api.js.vm` | 前端 API 请求封装 |
| `sql/sql.vm` | 菜单权限 SQL 脚本（INSERT INTO sys_menu...） |
| `ts/api.ts.vm` | TypeScript API 封装 |
| `ts/type.ts.vm` | TypeScript 类型定义 |

### 8.3 自定义模板修改步骤

1. 模板文件位于 `ruoyi-generator/src/main/resources/vm/` 目录
2. 直接编辑 `.vm` 文件，使用 Velocity 语法（`${变量}`、`#if`、`#foreach`）
3. 可用变量由 `VelocityUtils.getContext()` 提供（表信息、字段列表、包路径等）
4. 修改后重新编译 ruoyi-generator 模块即可生效

### 8.4 代码打包下载

```
GenController.download(tableName)
  → GenTableServiceImpl.generatorCode(tableName)
    ├── 查询 gen_table + gen_table_column
    ├── VelocityUtils.prepareContext() → 准备模板上下文
    ├── 遍历模板列表，Velocity 合并生成代码字符串
    ├── 写入 ZipOutputStream（按目录结构：java/、vue/、sql/、js/）
    └── 返回 ZIP 文件流
```

**输出目录规则：**
- Java 文件：`主包路径/模块名/`（如 `com/ruoyi/system/`）
- Vue 文件：`vue/模块名/`（如 `vue/system/`）
- SQL 文件：`sql/`
- JS 文件：`js/`

---

## 第九章 安全专项

### 9.1 文件上传安全：后缀白名单拦截 html 等危险文件

**核心机制**：白名单制，只允许上传指定类型文件，html/htm/js/jsp/php 等均被拦截。

**白名单定义（`MimeTypeUtils.java`）：**

```java
public static final String[] DEFAULT_ALLOWED_EXTENSION = {
    // 图片
    "bmp", "gif", "jpg", "jpeg", "png",
    // 文档
    "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt",
    // 压缩文件
    "rar", "zip", "gz", "bz2",
    // 视频格式
    "mp4", "avi", "rmvb",
    // PDF
    "pdf"
};
// 注意：不包含 html, htm, js, jsp, php, sh, bat 等危险文件类型
```

**校验流程（`FileUploadUtils.assertAllowed()`）：**

```java
public static final void assertAllowed(MultipartFile file, String[] allowedExtension)
        throws FileSizeLimitExceededException, InvalidExtensionException {
    // 1. 文件大小校验（默认 50MB）
    long size = file.getSize();
    if (size > DEFAULT_MAX_SIZE) { throw new FileSizeLimitExceededException(...); }

    // 2. 后缀白名单校验
    String extension = getExtension(file);  // 先取文件名后缀，取不到则从 ContentType 推断
    if (allowedExtension != null && !isAllowedExtension(extension, allowedExtension)) {
        // 根据上传类型抛出对应异常（图片/Flash/媒体/视频/通用）
        throw new InvalidExtensionException(...);
    }
}
```

**文件下载安全（`FileUtils.checkAllowDownload()`）：**
- 禁止目录跳转：`if (StringUtils.contains(resource, ".."))` → 返回 false
- 仅允许白名单后缀下载：与上传白名单一致

**头像上传特殊限制**：使用 `MimeTypeUtils.IMAGE_EXTENSION`（仅 bmp/gif/jpg/jpeg/png）

### 9.2 用户密码安全：自定义密码复杂度配置

**配置方案**：通过 `sys_config` 表的 `sys.account.chrtype` 参数动态配置密码规则。

**密码字符范围（chrtype）取值：**

| chrtype 值 | 规则 | 正则表达式 |
|-----------|------|-----------|
| 0（默认） | 任意字符（排除危险符号） | `/^[^<>"'\|\\]+$/` |
| 1 | 纯数字 | `/^[0-9]+$/` |
| 2 | 纯字母 | `/^[a-zA-Z]+$/` |
| 3 | 字母 + 数字（必须同时包含） | `/^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$/` |
| 4 | 字母 + 数字 + 特殊字符 | `/^(?=.*[A-Za-z])(?=.*\d)(?=.*[~!@#$%^&*()\-=_+])[...]+$/` |

**后端传递 chrtype 到前端：**

```java
// SysLoginController.getInfo()
ajax.put("pwdChrtype", getSysAccountChrtype());

public String getSysAccountChrtype() {
    return Convert.toStr(configService.selectConfigByKey("sys.account.chrtype"), "0");
}
```

**前端动态校验（`passwordRule.js` mixin）：**
- `GetInfo` 时存储到 sessionStorage：`cache.session.set('pwrChrtype', res.pwdChrtype)`
- `passwordRule.js` mixin 根据 chrtype 动态生成表单校验规则
- 在 `resetPwd.vue`、`register.vue`、`userForm.vue` 等页面使用

**密码过期策略：**

| 参数 | 说明 |
|------|------|
| `sys.account.initPasswordModify` | 0：关闭提示；1：未修改初始密码时登录提醒 |
| `sys.account.passwordValidateDays` | 0：不限制；>0：超过天数登录提醒修改 |

```java
// SysLoginController.getInfo()
ajax.put("isDefaultModifyPwd", initPasswordIsModify(user.getPwdUpdateDate()));
ajax.put("isPasswordExpired", passwordIsExpiration(user.getPwdUpdateDate()));
```

**密码错误锁定：**
- 最大错误次数：`user.password.maxRetryCount`（默认 5 次）
- 锁定时间：`user.password.lockTime`（默认 10 分钟）
- 实现：`SysPasswordService.validate()` → Redis 计数 `pwd_err_cnt:{username}`

### 9.3 在线用户权限热更新缓存机制

**核心实现**：`TokenService.refreshPermissionByRoleId()`

**工作原理：**
1. 管理员修改角色权限（菜单分配）
2. `SysRoleController.edit()` 调用 `refreshPermissionByRoleId(roleId, permissionService)`
3. 遍历 Redis 中所有 `login_tokens:*` Key
4. 对每个在线用户：
   - 跳过管理员（拥有所有权限）
   - 检查是否拥有该角色
   - 如果有 → 重新查询最新权限 → 更新 `loginUser.setPermissions()` → 重新写入 Redis
5. 用户下次请求时，`PermissionService.hasPermi()` 从更新后的 permissions 集合中查找

**优势**：无需用户重新登录，权限变更实时生效。

---

## 第十章 本地开发、启动、部署全流程

### 10.1 环境依赖

| 依赖 | 版本要求 | 说明 |
|------|---------|------|
| **JDK** | 8+（当前代码）/ 17+（master 分支） | `java -version` 验证 |
| **Maven** | 3.6+ | `mvn -version` 验证 |
| **MySQL** | 5.7+ / 8.0 | 数据库引擎 InnoDB |
| **Redis** | 5.0+ | 缓存中间件 |
| **Node.js** | 12+ | 前端构建 |
| **npm** | 6+ | 前端包管理 |

### 10.2 application.yml 全参数解读

```yaml
ruoyi:
  name: RuoYi                    # 项目名称
  version: 3.9.2                 # 版本号
  copyrightYear: 2026            # 版权年份
  profile: D:/ruoyi/uploadPath   # 文件上传根路径（Linux 改为 /home/ruoyi/uploadPath）
  addressEnabled: false          # IP 地址解析开关
  captchaType: math              # 验证码类型：math（数学计算）/ char（字符）

server:
  port: 8080                    # 后端端口
  tomcat:
    uri-encoding: UTF-8
    accept-count: 1000           # 排队数
    threads:
      max: 800                   # 最大线程数
      min-spare: 100             # 最小空闲线程

user:
  password:
    maxRetryCount: 5             # 密码最大错误次数
    lockTime: 10                 # 锁定时间（分钟）

spring:
  redis:
    host: localhost              # Redis 地址
    port: 6379                   # Redis 端口
    database: 0                  # 数据库索引
    password:                    # Redis 密码
    timeout: 10s                 # 连接超时
  servlet:
    multipart:
      max-file-size: 10MB        # 单文件最大大小
      max-request-size: 20MB     # 总上传最大大小

token:
  header: Authorization          # Token 请求头名称
  secret: abcdefghijklmnopqrstuvwxyz  # JWT 签名密钥（生产环境必须修改！）
  expireTime: 30                # Token 有效期（分钟）

xss:
  enabled: true                  # XSS 过滤开关
  excludes: /system/notice       # 排除路径（公告内容含 HTML）
  urlPatterns: /system/*,/monitor/*,/tool/*  # 匹配路径

referer:
  enabled: false                 # 防盗链开关
  allowed-domains: localhost,127.0.0.1,ruoyi.vip  # 允许的域名
```

### 10.3 前端启动流程

```bash
# 1. 进入前端目录
cd ruoyi-ui

# 2. 安装依赖（推荐使用淘宝镜像）
npm install --registry=https://registry.npmmirror.com

# 3. 启动开发服务器（默认 80 端口）
npm run dev

# 4. 构建生产包
npm run build:prod
```

**代理跨域配置（vue.config.js）：**
```javascript
devServer: {
  proxy: {
    '/dev-api': {                        // 前缀（.env.development 中 VUE_APP_BASE_API）
      target: 'http://localhost:8080',    // 后端地址
      changeOrigin: true,
      pathRewrite: {
        '^/dev-api': ''                  // 去掉前缀，转发到后端根路径
      }
    }
  }
}
```

### 10.4 ry.bat / ry.sh 启动脚本

**ry.sh（Linux）执行逻辑：**
1. 接收参数：`start` / `stop` / `restart` / `status`
2. JVM 参数配置：`-Xms512m -Xmx1024m -XX:MetaspaceSize=128m` 等
3. `start`：`nohup java $JVM_OPTS -jar ruoyi-admin.jar > /dev/null 2>&1 &`
4. `stop`：`kill -TERM $PID`，循环等待进程退出
5. `status`：检查 java 进程是否存活
6. 日志输出到 `logs/ruoyi-admin.jar.log`

**ry.bat（Windows）执行逻辑：**
1. 交互式菜单选择操作
2. `start`：`javaw` 后台启动（`jps -l` 检查是否已运行）
3. `stop`：`taskkill /f /pid` 强制终止
4. 使用 `jps` 命令查找 Java 进程

### 10.5 跨版本兼容踩坑

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| Boot2 → Boot3/4 启动报错 | javax → jakarta 包名变更 | 全局替换 `javax.servlet` → `jakarta.servlet` |
| Vue2 → Vue3 联调报错 | Element UI → Element Plus API 差异 | 前端需对应替换组件 |
| JWT 库不兼容 | jjwt 0.9.1 使用 javax | 升级到 jjwt 0.11.x+ 或使用 spring-security-oauth2 |
| MyBatis 版本冲突 | Boot3 需要 mybatis-spring-boot-starter 3.x | 升级 MyBatis Starter 版本 |
| 前端代理 404 | 后端 context-path 配置 | 检查 `server.servlet.context-path` |

---

## 第十一章 专项功能底层实现

### 11.1 Redis 缓存工具与权限缓存刷新

**RedisCache 封装类**（`ruoyi-common/core/redis/RedisCache.java`）：
- 基于 Spring Data Redis 的 `RedisTemplate`
- 提供 `setCacheObject`、`getCacheObject`、`deleteObject`、`keys` 等方法
- 序列化配置：`RedisConfig` 中配置 `Jackson2JsonRedisSerializer`

**权限缓存刷新三种触发方式：**

| 触发方式 | 代码位置 | 场景 |
|---------|---------|------|
| 角色权限变更 | `SysRoleController.edit()` → `refreshPermissionByRoleId()` | 管理员修改角色菜单 |
| getInfo 接口 | `SysLoginController.getInfo()` → 比对 permissions | 用户刷新页面/重新加载 |
| Token 自动续期 | `JwtAuthenticationTokenFilter` → `verifyToken()` | 每次请求检查过期时间 |

### 11.2 Quartz 动态定时任务开发示例

**开发步骤：**

1. 在 `com.ruoyi.quartz.task` 包下创建任务类：
```java
@Component("myTask")
public class MyTask {
    public void execute(String param) {
        System.out.println("定时任务执行，参数：" + param);
    }
}
```

2. 在管理页面新增任务：
- 任务名称：我的任务
- 调用目标字符串：`myTask.execute('hello')`
- Cron 表达式：`0/30 * * * * ?`（每 30 秒）

3. 系统通过反射调用：
```java
// ScheduleJob.invokeMethod()
Object bean = SpringUtils.getBean(beanName);  // 获取 Spring Bean
Method method = bean.getClass().getMethod(methodName, paramTypes);  // 反射获取方法
method.invoke(bean, args);  // 执行
```

### 11.3 文件上传路径、大小、类型限制配置

| 配置项 | 位置 | 默认值 |
|--------|------|--------|
| 上传根路径 | `application.yml: ruoyi.profile` | `D:/ruoyi/uploadPath` |
| 单文件大小 | `application.yml: spring.servlet.multipart.max-file-size` | 10MB |
| 总上传大小 | `application.yml: spring.servlet.multipart.max-request-size` | 20MB |
| 文件名最大长度 | `FileUploadUtils.DEFAULT_FILE_NAME_LENGTH` | 100 字符 |
| 文件最大大小（工具类） | `FileUploadUtils.DEFAULT_MAX_SIZE` | 50MB |
| 允许的后缀 | `MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION` | 白名单（不含 html 等） |
| 头像允许后缀 | `MimeTypeUtils.IMAGE_EXTENSION` | bmp/gif/jpg/jpeg/png |

**文件存储路径规则：**
- 普通上传：`{profile}/upload/{yyyy/MM/dd}/{原文件名}_{序列号}.{后缀}`
- 头像上传：`{profile}/avatar/{yyyy/MM/dd}/{UUID}.{后缀}`
- 导入文件：`{profile}/import/`

### 11.4 Excel 导入导出 POI 工具封装

**注解驱动**（`@Excel` 注解）：
```java
@Excel(name = "用户名", sort = 1, width = 20)
private String userName;

@Excel(name = "性别", readConverterExp = "0=男,1=女")
private String sex;

@Excel(name = "创建时间", dateFormat = "yyyy-MM-dd")
private Date createTime;
```

**导出流程：**
```java
// Controller
ExcelUtil<SysUser> util = new ExcelUtil<>(SysUser.class);
util.exportExcel(response, list, "用户数据");
```

**导入流程：**
```java
// Controller
ExcelUtil<SysUser> util = new ExcelUtil<>(SysUser.class);
List<SysUser> userList = util.importExcel(file.getInputStream());
```

### 11.5 在线拖拽表单构建器原理

- 前端组件：`src/views/tool/build/index.vue`
- 核心机制：基于拖拽 API（HTML5 Drag and Drop）
- 左侧面板：表单元素列表（输入框、文本域、下拉框、单选、复选、日期等）
- 中间画布：拖放区域，实时渲染表单
- 右侧属性：编辑元素属性（标签、名称、默认值、校验规则）
- 输出：生成 HTML 源码（`<el-form>` + `<el-form-item>` 结构），可复制使用

---

## 第十二章 后端统一开发规范

### 12.1 Controller-Service-Mapper 分层开发规范

```
Controller 层（ruoyi-admin）
├── 继承 BaseController
├── @RestController + @RequestMapping
├── @PreAuthorize 权限注解
├── @Log 操作日志注解
├── 调用 Service 层
└── 返回 AjaxResult / TableDataInfo

Service 层（ruoyi-system）
├── 接口 ISysXxxService
├── 实现 SysXxxServiceImpl
├── @Service 注解
├── @DataScope 数据权限注解（需要时）
├── 调用 Mapper 层
└── 业务逻辑处理

Mapper 层（ruoyi-system）
├── 接口 SysXxxMapper（MyBatis 注解或 XML）
├── XML 文件：resources/mapper/system/SysXxxMapper.xml
└── 纯 SQL 操作，不含业务逻辑
```

### 12.2 统一返回体

**普通接口返回 AjaxResult：**
```json
{
  "code": 200,        // 200成功，500失败，401未授权，601警告
  "msg": "操作成功",
  "data": {}          // 业务数据
}
```

**分页接口返回 TableDataInfo：**
```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [],         // 数据列表
  "total": 100         // 总记录数
}
```

**分页使用方式：**
```java
// Controller
startPage();                          // 开始分页（PageHelper）
List<SysUser> list = userService.selectUserList(user);
return getDataTable(list);            // 包装为 TableDataInfo
```

### 12.3 参数校验、全局异常、错误码体系

**参数校验**：JSR-303 `@Validated` 注解
```java
public AjaxResult add(@Validated @RequestBody SysUser user) { ... }
```

**全局异常处理（`GlobalExceptionHandler`）：**

| 异常类型 | 处理方式 |
|---------|---------|
| `RuntimeException` | `AjaxResult.error(e.getMessage())` |
| `Exception` | `AjaxResult.error(e.getMessage())` |
| `BindException` | 取第一个错误消息返回 |
| `MethodArgumentNotValidException` | 取第一个字段错误返回 |
| `DemoModeException` | "演示模式，不允许操作" |
| `AccessDeniedException` | "没有权限，请联系管理员授权" |
| `HttpRequestMethodNotSupportedException` | "请求方法不支持" |

### 12.4 新增业务模块完整实操步骤

```
1. 建表
   ├── 创建数据库表（如 sys_product）
   └── 执行 SQL 脚本

2. 代码生成
   ├── 访问 代码生成 → 导入表 sys_product
   ├── 配置生成信息（包路径、模块名、业务名、功能名、作者）
   ├── 预览/下载生成代码
   ├── Java 代码放到 ruoyi-system/src/main/java/
   ├── Mapper XML 放到 resources/mapper/
   └── Vue 代码放到 ruoyi-ui/src/views/

3. 执行菜单 SQL
   └── 执行生成器输出的 SQL（INSERT INTO sys_menu...）

4. 微调后端逻辑
   ├── 添加 @DataScope 数据权限（如需要）
   ├── 添加 @Log 操作日志
   └── 业务逻辑调整

5. 前端调试
   ├── 确认 API 路径与后端一致
   ├── 确认权限标识与菜单一致
   └── 联调增删改查
```

---

## 第十三章 定制化学习路线

### 13.1 循序渐进学习顺序

```
阶段一：启动项目（1天）
├── 安装 JDK、Maven、MySQL、Redis、Node.js
├── 导入 SQL 脚本（ry_20260417.sql + quartz.sql）
├── 修改 application.yml（数据库、Redis 配置）
├── 后端启动：RuoYiApplication.java
└── 前端启动：npm run dev

阶段二：吃透三层权限（3天）
├── 阅读登录流程：SysLoginService → UserDetailsServiceImpl → SysPasswordService
├── 阅读权限校验：PermissionService.hasPermi() + @PreAuthorize
├── 阅读数据权限：DataScopeAspect + @DataScope
└── 阅读权限热刷新：TokenService.refreshPermissionByRoleId()

阶段三：掌握安全校验（2天）
├── 文件上传安全：FileUploadUtils.assertAllowed() + MimeTypeUtils
├── 密码规则：SysPasswordService + passwordRule.js + sys.account.chrtype
├── XSS 防护：XssFilter + XssValidator
└── 防盗链、防重复提交：RefererFilter + @RepeatSubmit

阶段四：熟悉代码生成器（2天）
├── 使用代码生成器导入表、生成代码
├── 阅读 Velocity 模板语法
├── 尝试修改模板
└── 理解生成文件的目录结构

阶段五：独立开发业务模块（3天）
├── 新建业务表
├── 代码生成 + 微调
├── 添加数据权限、文件上传、密码校验
└── 前后端联调
```

### 13.2 必须吃透的核心代码点位清单

| 序号 | 类名/文件 | 路径 | 核心理解点 |
|------|----------|------|-----------|
| 1 | `TokenService` | framework/web/service/ | JWT 创建/验证/刷新/权限热刷新 |
| 2 | `SysLoginService` | framework/web/service/ | 登录全流程 |
| 3 | `SecurityConfig` | framework/config/ | Spring Security 配置 |
| 4 | `PermissionService` | framework/web/service/ | `@ss.hasPermi()` 权限校验 |
| 5 | `DataScopeAspect` | framework/aspectj/ | 数据权限 SQL 拼接 |
| 6 | `JwtAuthenticationTokenFilter` | framework/security/filter/ | 请求拦截 + Token 续期 |
| 7 | `FileUploadUtils` | common/utils/file/ | 文件上传白名单校验 |
| 8 | `MimeTypeUtils` | common/utils/file/ | 允许的文件后缀定义 |
| 9 | `SysPasswordService` | framework/web/service/ | 密码错误锁定 |
| 10 | `SysLoginController` | admin/web/controller/system/ | getInfo + getRouters + 密码策略 |
| 11 | `SysRoleController` | admin/web/controller/system/ | 角色修改 + 权限热刷新调用 |
| 12 | `SysNoticeController` | admin/web/controller/system/ | 公告阅读记录功能 |
| 13 | `SysNoticeReadServiceImpl` | system/service/impl/ | 公告已读完整逻辑 |
| 14 | `passwordRule.js` | ui/utils/ | 前端密码校验规则 |
| 15 | `permission.js` | ui/permission.js | 前端路由守卫 |
| 16 | `request.js` | ui/utils/request.js | Axios 拦截封装 |
| 17 | `hasPermi.js` | ui/directive/permission/ | v-hasPermi 指令 |
| 18 | `permission.js` (store) | ui/store/modules/ | 动态路由生成 |
| 19 | `user.js` (store) | ui/store/modules/ | 用户状态管理 |
| 20 | `RuoYiConfig` | common/config/ | 项目配置读取 |

### 13.3 实操练习任务

**任务：新建一个「资产管理」业务模块，包含以下能力：**

1. **建表**：`sys_asset`（资产表），字段：asset_id、asset_name、asset_type（字典）、amount、dept_id（关联部门）、create_by、create_time 等
2. **代码生成**：使用代码生成器生成前后端全套代码
3. **数据权限**：添加 `@DataScope(deptAlias = "d")` 实现按部门数据范围过滤
4. **文件上传**：资产附件上传功能，限制为图片 + PDF
5. **密码校验**：资产操作需要验证当前用户密码（使用 chrtype=4 规则）
6. **权限配置**：配置菜单权限 `system:asset:list/add/edit/remove/export`
7. **前端联调**：确认 v-hasPermi 指令、API 路径、路由配置

**验收标准：**
- 不同角色用户登录只能看到各自数据范围的资产
- 上传 html 文件被拦截
- 修改角色权限后在线用户无需重登即可生效
- 操作日志自动记录
- Excel 导入导出正常

---

> **文档结束**
> 本文档基于 RuoYi-Vue v3.9.2 全量代码扫描生成，覆盖全部 6 个 Maven 模块、前端 ruoyi-ui 工程、SQL 脚本、启动脚本、根目录配置，重点拆解了角色权限热刷新、自定义密码规则、html 文件上传拦截、公告阅读用户统计、页签层级优化等迭代新增特性。
