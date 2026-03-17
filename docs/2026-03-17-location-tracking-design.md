# Headwind MDM 定位追踪功能设计

## 1. 功能概述

本文档描述 Headwind MDM Android 客户端定位追踪功能的企业级实现方案。

### 1.1 功能需求

| 项目 | 要求 |
|------|------|
| 发送策略 | 批量发送（本地缓存 + 定时上传） |
| 数据内容 | 完整信息（坐标、速度、海拔、精度等） |

### 1.2 设计目标

- 实现完整的定位追踪功能
- 支持批量上传减少网络请求
- 存储完整的位置信息（坐标、速度、海拔、精度等）
- 失败重试机制确保数据不丢失

---

## 2. 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                    Android 客户端                            │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  LocationService (现有框架)                          │   │
│  │  - GPS/Network 位置监听                              │   │
│  │  - 调用 ProUtils.processLocation()                  │   │
│  └──────────────────┬──────────────────────────────────┘   │
│                     ▼                                        │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ProUtils.processLocation() (需实现)                │   │
│  │  - 提取完整位置信息                                   │   │
│  │  - 存储到 SQLite                                     │   │
│  │  - 触发批量上传 WorkManager                          │   │
│  └──────────────────┬──────────────────────────────────┘   │
│                     ▼                                        │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  LocationTable (SQLite)                             │   │
│  │  - 本地缓存位置数据                                   │   │
│  └──────────────────┬──────────────────────────────────┘   │
│                     ▼                                        │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  WorkManager 定时任务                                │   │
│  │  - 每5分钟批量上传                                    │   │
│  └──────────────────┬──────────────────────────────────┘   │
│                     ▼                                        │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ServerService.sendLocations()                      │   │
│  │  → REST API: PUT /rest/plugins/devicelocations/...  │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. 数据结构

### 3.1 LocationTable.Location 字段

```java
public static class Location {
    long time;           // 时间戳 (毫秒)
    double lat;          // 纬度
    double lon;          // 经度
    float speed;         // 速度 (m/s)
    float altitude;      // 海拔 (m)
    float accuracy;      // 精度 (m)
    float bearing;       // 方向/航向 (度)
    String provider;     // "gps" 或 "network"
}
```

### 3.2 后端 API

**接口**: `PUT /rest/plugins/devicelocations/public/update/{number}`

**请求体**:
```json
[
    {
        "time": 1700000000000,
        "lat": 22.54321,
        "lon": 114.12345,
        "speed": 15.5,
        "altitude": 125.3,
        "accuracy": 5.0,
        "bearing": 90.0,
        "provider": "gps"
    },
    ...
]
```

---

## 4. 核心实现逻辑

### 4.1 ProUtils.processLocation()

```java
public static void processLocation(Context context, Location location, String provider) {
    // 1. 检查是否启用定位追踪
    if (!isLocationTrackingEnabled(context)) {
        return;
    }

    // 2. 提取完整位置信息
    LocationData data = new LocationData();
    data.setTime(System.currentTimeMillis());
    data.setLat(location.getLatitude());
    data.setLon(location.getLongitude());
    data.setSpeed(location.getSpeed());
    data.setAltitude(location.getAltitude());
    data.setAccuracy(location.getAccuracy());
    data.setBearing(location.getBearing());
    data.setProvider(provider);

    // 3. 存储到本地 SQLite
    LocationTable.insert(context, data);

    // 4. 触发批量上传检查
    LocationUploadWorker.scheduleUpload(context);
}
```

### 4.2 批量上传流程

```
WorkManager 定时任务 (每5分钟)
    │
    ├── 检查是否有未上传的位置数据
    │
    ├── 调用 ServerService.sendLocations()
    │
    ├── 上传成功 → 删除本地已上传记录
    │
    └── 上传失败 → 保留数据，下次重试
```

---

## 5. 需要修改的文件

| 文件路径 | 操作 | 说明 |
|----------|------|------|
| `app/src/main/java/com/hmdm/launcher/pro/ProUtils.java` | 修改 | 实现 processLocation() |
| `app/src/main/java/com/hmdm/launcher/db/LocationTable.java` | 修改 | 添加完整字段 |
| `app/src/main/java/com/hmdm/launcher/worker/LocationUploadWorker.java` | 新增 | 批量上传 Worker |

---

## 6. 配置项

定位追踪功能由服务器配置控制，通过 ServerConfig 获取：

- `locationTrackingEnabled`: 是否启用定位追踪
- `locationUpdateInterval`: 位置更新间隔（毫秒）
- `locationBatchInterval`: 批量上传间隔（毫秒）

---

## 7. 错误处理

| 场景 | 处理方式 |
|------|---------|
| 无网络 | 本地缓存，下次重试 |
| 上传失败 | 保留数据，WorkManager 重试 |
| 数据库满 | 保留最新1000条，删除旧数据 |
| GPS 关闭 | 使用网络定位作为备选 |

---

## 8. 权限要求

```xml
<!-- AndroidManifest.xml 已有 -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
```

---

## 9. 后端兼容性

后端已有定位追踪插件，无需修改：
- REST API: `/rest/plugins/devicelocations/public/update/{number}`
- 数据库表: `device_locations`
- 插件: `device-locations`

客户端只需实现数据上报功能。