# Headwind MDM Server 代码审核文档

## 目录
1. [项目概述](#1-项目概述)
2. [技术栈](#2-技术栈)
3. [项目结构](#3-项目结构)
4. [REST API架构](#4-rest-api架构)
5. [新增API端点详解](#5-新增api端点详解)
6. [数据库层](#6-数据库层)
7. [安全机制](#7-安全机制)
8. [代码审核要点](#8-代码审核要点)
9. [常见问题与建议](#9-常见问题与建议)

---

## 1. 项目概述

**Headwind MDM** 是一个开源的安卓设备管理(移动设备管理/MDM)服务器系统。该系统为企业管理安卓设备提供完整的解决方案，包括：

- 设备配置与应用管理
- 远程控制（锁屏、重启、恢复出厂设置）
- 位置跟踪
- 网络过滤
- 联系人同步
- 照片上传
- Kiosk模式

---

## 2. 技术栈

| 组件 | 技术/版本 |
|------|----------|
| **应用服务器** | Jersey (Glassfish) 2.25.1 |
| **数据库** | PostgreSQL |
| **ORM框架** | MyBatis 3.5.3 + MyBatis-Guice 3.10 |
| **数据库迁移** | Liquibase 3.6.3 |
| **JSON处理** | Jackson 2.10.0 |
| **安全框架** | JWT (自定义实现) |
| **依赖注入** | Google Guice |
| **API文档** | Swagger |
| **日志框架** | SLF4J 2.0.16 |
| **Java版本** | Java 8+ |

---

## 3. 项目结构

```
hmdm-server/
├── common/                    # 共享模块：持久化层和领域模型
│   └── src/main/java/com/hmdm/
│       ├── persistence/       # DAO层和数据访问
│       │   ├── AbstractDAO.java
│       │   ├── DeviceDAO.java
│       │   ├── ConfigurationDAO.java
│       │   └── mapper/        # MyBatis XML映射器
│       ├── persistence/domain/ # 领域对象
│       ├── event/             # 事件系统
│       ├── guice/             # Guice模块配置
│       └── rest/              # REST过滤器
├── jwt/                       # JWT认证模块
│   └── src/main/java/com/hmdm/security/jwt/
│       ├── JWTFilter.java
│       └── TokenProvider.java
├── notification/              # 推送通知服务
│   └── src/main/java/com/hmdm/notification/
├── plugins/                   # 插件系统
│   ├── audit/                 # 审计日志插件
│   ├── deviceinfo/            # 设备信息插件
│   ├── devicelog/             # 设备日志插件
│   ├── messaging/             # 消息推送插件
│   ├── platform/              # 插件管理
│   ├── push/                  # 推送通知插件
│   └── xtra/                  # 扩展功能插件
├── server/                    # 主服务器模块
│   └── src/main/java/com/hmdm/
│       ├── guice/module/      # 主应用Guice模块
│       └── rest/
│           ├── resource/      # REST API资源
│           ├── json/          # JSON请求/响应模型
│           └── filter/        # 请求过滤器
└── swagger/ui/                # API文档前端

```

---

## 4. REST API架构

### 4.1 API组织方式

API遵循 **JAX-RS (Jersey)** 标准，使用 **Swagger注解** 进行文档化。

**端点分类：**
- 公开端点：`/public/auth`, `/public/signup`, `/public/files`
- 私有端点：`/private/devices`, `/private/configurations`, `/private/users`
- 插件端点：基于已启用插件动态生成

### 4.2 资源类模式

```java
@Api(tags = {"设备管理"}, authorizations = {@Authorization("apiKey")})
@Path("/plugins/xxx")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class XxxResource {

    private final XxxDAO xxxDAO;
    private final DeviceDAO deviceDAO;

    @Inject
    public XxxResource(XxxDAO xxxDAO, DeviceDAO deviceDAO) {
        this.xxxDAO = xxxDAO;
        this.deviceDAO = deviceDAO;
    }

    /**
     * 根据设备编号获取设备ID
     */
    private Integer getDeviceIdByNumber(String number) {
        Device device = deviceDAO.getDeviceByNumber(number);
        return device != null ? device.getId() : null;
    }
}
```

### 4.3 响应格式

```java
// 成功响应
Response.ok(data).build();
Response.OK("操作成功");

// 错误响应
Response.ERROR("error.code", "错误消息");
Response.OBJECT_NOT_FOUND_ERROR();
```

---

## 5. 新增API端点详解

### 5.1 DeviceLocationResource.java

**路径**: `/plugins/devicelocations/public`

**功能**: 设备位置数据上传与获取

| 方法 | 路径 | 功能 |
|-----|------|------|
| PUT | `/update/{number}` | 批量上传设备位置 |
| GET | `/latest/{number}` | 获取设备最新位置 |

**代码位置**: `server/src/main/java/com/hmdm/rest/resource/DeviceLocationResource.java`

**关键代码**:
```java
@PUT
@Path("/update/{number}")
public Response updateDeviceLocation(
        @PathParam("number") String number,
        @ApiParam("位置数据列表") List<DeviceLocation> locations) {
    Integer deviceId = getDeviceIdByNumber(number);
    if (deviceId == null) {
        return Response.ERROR("device.not.found", "设备不存在");
    }
    for (DeviceLocation location : locations) {
        location.setDeviceId(deviceId);
        deviceLocationDAO.insertLocation(location);
    }
    return Response.OK();
}
```

---

### 5.2 DevicePhotoUploadResource.java

**路径**: `/plugins/devicephoto/public`

**功能**: 设备照片上传与管理

| 方法 | 路径 | 功能 |
|-----|------|------|
| POST | `/upload/{number}` | 上传设备照片 |
| GET | `/list/{number}` | 获取照片列表 |
| DELETE | `/{number}/{photoId}` | 删除照片 |

**代码位置**: `server/src/main/java/com/hmdm/rest/resource/DevicePhotoUploadResource.java`

---

### 5.3 DeviceControlResource.java

**路径**: `/plugins/devicecontrol`

**功能**: 远程控制（截屏、锁屏、重启、恢复出厂设置）

| 方法 | 路径 | 功能 |
|-----|------|------|
| GET | `/session/{number}` | 获取远程控制会话 |
| POST | `/signal/{number}` | 发送远程控制信号 |
| PUT | `/session/{number}/{sessionId}/status` | 更新会话状态 |
| DELETE | `/session/{number}/{sessionId}` | 结束会话 |

**支持的信号类型**:
- `screenshot` - 截屏
- `lock` - 锁屏
- `reboot` - 重启
- `factory_reset` - 恢复出厂设置

**代码位置**: `server/src/main/java/com/hmdm/rest/resource/DeviceControlResource.java`

---

### 5.4 NetworkFilterResource.java

**路径**: `/plugins/networkfilter`

**功能**: 网络过滤规则与流量统计

| 方法 | 路径 | 功能 |
|-----|------|------|
| GET | `/rules/{number}` | 获取网络过滤规则 |
| POST | `/logs/{number}` | 上传流量日志 |
| GET | `/settings/{number}` | 获取网络设置 |
| PUT | `/settings/{number}` | 更新网络设置 |
| GET | `/stats/{number}` | 获取流量统计 |

**代码位置**: `server/src/main/java/com/hmdm/rest/resource/NetworkFilterResource.java`

---

### 5.5 ContactsSyncResource.java

**路径**: `/plugins/contacts`

**功能**: 联系人同步与管理

| 方法 | 路径 | 功能 |
|-----|------|------|
| GET | `/{number}` | 获取设备联系人列表 |
| PUT | `/{number}` | 上传联系人到服务器 |
| DELETE | `/{number}` | 删除设备联系人 |
| GET | `/search/{number}` | 搜索联系人 |
| POST | `/sync/{number}` | 触发同步 |

**代码位置**: `server/src/main/java/com/hmdm/rest/resource/ContactsSyncResource.java`

---

## 6. 数据库层

### 6.1 DAO模式

使用 **AbstractDAO** 作为基类，提供安全上下文集成：

```java
public abstract class AbstractDAO<T> {
    protected List<T> getList(Function<Integer, List<T>> listRetrievalLogic)
    protected T getSingleRecord(Function<Integer, T> searchLogic)
    protected void insertRecord(T record, Consumer<T> recordInsertionLogic)
    protected void updateRecord(T record, Consumer<T> recordUpdateLogic)
}
```

### 6.2 关键DAO类

| DAO类 | 功能 |
|-------|------|
| DeviceDAO | 设备管理 |
| ConfigurationDAO | 配置管理 |
| ApplicationDAO | 应用管理 |
| DeviceLocationDAO | 位置数据 |
| DevicePhotoDAO | 照片管理 |
| DeviceContactDAO | 联系人管理 |
| NetworkTrafficDAO | 网络流量 |
| RemoteControlDAO | 远程控制会话 |

### 6.3 MyBatis映射

XML映射文件位于: `common/src/main/java/com/hmdm/persistence/mapper/`

**示例**:
```xml
<mapper namespace="com.hmdm.persistence.mapper.DeviceMapper">
    <resultMap id="deviceResult" type="Device">
        <result property="id" column="deviceId"/>
        <collection property="groups" ofType="LookupItem"/>
    </resultMap>
</mapper>
```

---

## 7. 安全机制

### 7.1 JWT认证流程

```
客户端请求 → JWTFilter → TokenProvider验证 → SecurityContext设置 → 业务处理
```

**核心组件**:
- `JWTFilter`: 请求拦截器，解析Authorization头
- `TokenProvider`: Token生成与验证
- `SecurityContext`: 线程本地存储的用户上下文

### 7.2 权限控制

```java
// 检查权限
boolean hasPermission = securityContext.hasPermission("DEVICE_VIEW");

// 检查超级管理员
boolean isSuperAdmin = securityContext.isSuperAdmin();
```

### 7.3 公开vs私有资源

- **公开资源**: AuthResource, SignupResource, 设备端点(带apiKey)
- **私有资源**: 所有`/private/*`路径，需要JWT认证

---

## 8. 代码审核要点

### 8.1 新增文件审核

#### DeviceLocationResource.java
- [ ] 是否正确获取设备ID（使用getDeviceIdByNumber方法）
- [ ] 是否处理空列表情况
- [ ] 是否正确设置时间戳
- [ ] 异常处理是否完整

#### DevicePhotoUploadResource.java
- [ ] 文件名验证是否存在
- [ ] 文件大小限制是否有
- [ ] 存储路径是否安全
- [ ] 是否存在SQL注入风险

#### DeviceControlResource.java
- [ ] 信号类型是否都有对应处理
- [ ] 危险操作(factory_reset)是否有额外验证
- [ ] 会话状态管理是否正确
- [ ] PushService是否正确使用

#### NetworkFilterResource.java
- [ ] 规则启用状态是否正确检查
- [ ] 批量操作是否有限流
- [ ] 统计数据计算是否正确

#### ContactsSyncResource.java
- [ ] JSON序列化是否正确处理特殊字符
- [ ] 批量插入是否高效
- [ ] 搜索功能是否防注入

### 8.2 通用审核项

- [ ] 所有public方法都有Swagger文档注释
- [ ] 所有API都有中文注释说明功能
- [ ] 异常消息是否友好（不暴露内部信息）
- [ ] 敏感操作是否有日志记录
- [ ] 参数验证是否完整
- [ ] 资源是否正确关闭

### 8.3 安全审核项

- [ ] 设备认证机制是否健全
- [ ] 是否有越权风险
- [ ] 输入验证是否充分
- [ ] 错误处理是否安全
- [ ] 日志是否记录敏感操作

---

## 9. 常见问题与建议

### 9.1 DAO方法调用问题

**问题**: 直接调用`deviceContactDAO.getDeviceIdByNumber()`可能导致方法不存在

**解决**: 使用私有辅助方法统一处理
```java
private Integer getDeviceIdByNumber(String number) {
    Device device = deviceDAO.getDeviceByNumber(number);
    return device != null ? device.getId() : null;
}
```

### 9.2 JSON序列化问题

**问题**: 返回JSON字符串需要手动转义

**解决**: 使用Jackson库或手动实现转义方法
```java
private String escapeJson(String value) {
    if (value == null) return "";
    return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
}
```

### 9.3 批量操作优化

**建议**: 大批量数据使用批量插入，避免循环单条插入

### 9.4 文件上传安全

**建议**:
- 验证文件类型
- 限制文件大小
- 使用唯一文件名（UUID）
- 存储在Web根目录外

---

## 附录：文件清单

### 新增/修改的服务端资源文件

| 文件路径 | 功能 | 状态 |
|---------|------|------|
| `server/.../DeviceLocationResource.java` | 位置跟踪 | 新增 |
| `server/.../DevicePhotoUploadResource.java` | 照片上传 | 新增 |
| `server/.../DeviceControlResource.java` | 远程控制 | 新增 |
| `server/.../NetworkFilterResource.java` | 网络过滤 | 新增 |
| `server/.../ContactsSyncResource.java` | 联系人同步 | 新增 |

---

**文档版本**: 1.0
**创建日期**: 2026-03-18
**适用项目**: Headwind MDM Server