# 定位追踪功能实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现完整的定位追踪功能，支持批量上传完整位置信息（坐标、速度、海拔、精度等）

**Architecture:** LocationService 调用 ProUtils.processLocation() → 存储到 LocationTable SQLite → WorkManager 定时批量上传 → ServerService.sendLocations()

**Tech Stack:** Android, SQLite, WorkManager, Retrofit, DevicePolicyManager

---

## 文件结构

| 文件路径 | 操作 | 说明 |
|----------|------|------|
| `app/src/main/java/com/hmdm/launcher/db/LocationTable.java` | 修改 | 添加 speed, altitude, accuracy, bearing 字段 |
| `app/src/main/java/com/hmdm/launcher/pro/ProUtils.java` | 修改 | 实现 processLocation() 方法 |
| `app/src/main/java/com/hmdm/launcher/worker/LocationUploadWorker.java` | 新增 | 批量上传 Worker |
| `app/build.gradle` | 修改 | 版本号 +1 |

---

## Chunk 1: 扩展 LocationTable 数据库

### Task 1: 添加完整位置字段

**Files:**
- Modify: `app/src/main/java/com/hmdm/launcher/db/LocationTable.java`

- [ ] **Step 1: 更新数据库表结构**

修改 `CREATE_TABLE` 添加新字段：
```java
private static final String CREATE_TABLE =
        "CREATE TABLE locations (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "ts INTEGER, " +
                "lat REAL, " +
                "lon REAL, " +
                "speed REAL, " +      // 新增
                "altitude REAL, " +   // 新增
                "accuracy REAL, " +   // 新增
                "bearing REAL, " +    // 新增
                "provider TEXT" +     // 新增
                ")";
```

- [ ] **Step 2: 更新 INSERT 语句**

```java
private static final String INSERT_LOCATIONS =
        "INSERT OR IGNORE INTO locations(ts, lat, lon, speed, altitude, accuracy, bearing, provider) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
```

- [ ] **Step 3: 更新 Location 类**

在 `Location` 类中添加字段：
```java
public static class Location {
    private long _id;
    private long ts;
    private double lat;
    private double lon;
    private float speed;      // 新增
    private float altitude;   // 新增
    private float accuracy;   // 新增
    private float bearing;    // 新增
    private String provider;  // 新增

    // getters and setters...
}
```

- [ ] **Step 4: 更新 insert 方法**

```java
public static void insert(SQLiteDatabase db, Location location) {
    try {
        db.execSQL(INSERT_LOCATIONS, new String[]{
                Long.toString(location.getTs()),
                Double.toString(location.getLat()),
                Double.toString(location.getLon()),
                Float.toString(location.getSpeed()),
                Float.toString(location.getAltitude()),
                Float.toString(location.getAccuracy()),
                Float.toString(location.getBearing()),
                location.getProvider()
        });
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/hmdm/launcher/db/LocationTable.java
git commit -m "feat: add complete location fields to LocationTable"
```

---

## Chunk 2: 实现 ProUtils.processLocation()

### Task 2: 实现位置处理逻辑

**Files:**
- Modify: `app/src/main/java/com/hmdm/launcher/pro/ProUtils.java`

- [ ] **Step 1: 添加必要 import**

```java
import com.hmdm.launcher.db.LocationTable;
import com.hmdm.launcher.helper.SettingsHelper;
import com.hmdm.launcher.worker.LocationUploadWorker;
```

- [ ] **Step 2: 实现 processLocation 方法**

在 ProUtils.java 中找到现有的 processLocation 存根，替换为：

```java
public static void processLocation(Context context, Location location, String provider) {
    // 检查定位追踪是否启用
    if (!isLocationTrackingEnabled(context)) {
        return;
    }

    // 创建位置数据对象
    LocationTable.Location locData = new LocationTable.Location();
    locData.setTs(System.currentTimeMillis());
    locData.setLat(location.getLatitude());
    locData.setLon(location.getLongitude());
    locData.setSpeed(location.getSpeed());
    locData.setAltitude(location.getAltitude());
    locData.setAccuracy(location.getAccuracy());
    locData.setBearing(location.getBearing());
    locData.setProvider(provider);

    // 存储到数据库
    try {
        SQLiteDatabase db = context.openOrCreateDatabase("hmdm.db", Context.MODE_PRIVATE, null);
        LocationTable.insert(db, locData);
        db.close();
    } catch (Exception e) {
        RemoteLogger.log(context, Const.LOG_ERROR, "Failed to save location: " + e.getMessage());
    }

    // 触发批量上传
    LocationUploadWorker.scheduleUpload(context);
}

private static boolean isLocationTrackingEnabled(Context context) {
    // 从配置读取，默认启用
    return SettingsHelper.getSettings(context).getServerConfig() != null &&
           SettingsHelper.getSettings(context).getServerConfig().getLocationTrackingEnabled() != null &&
           SettingsHelper.getSettings(context).getServerConfig().getLocationTrackingEnabled();
}
```

- [ ] **Step 3: 添加 Settings 字段检查**

在 ServerConfig 中确认有 locationTrackingEnabled 字段（如果不存在需要添加）。

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/hmdm/launcher/pro/ProUtils.java
git commit -m "feat: implement processLocation in ProUtils"
```

---

## Chunk 3: 创建 LocationUploadWorker

### Task 3: 创建批量上传 Worker

**Files:**
- Create: `app/src/main/java/com/hmdm/launcher/worker/LocationUploadWorker.java`

- [ ] **Step 1: 创建 LocationUploadWorker.java**

```java
/*
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC
 *
 * Licensed under the Apache License, Version 2.0
 */

package com.hmdm.launcher.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.hmdm.launcher.Const;
import com.hmdm.launcher.db.LocationTable;
import com.hmdm.launcher.helper.SettingsHelper;
import com.hmdm.launcher.json.ServerConfig;
import com.hmdm.launcher.server.ServerService;
import com.hmdm.launcher.util.RemoteLogger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Response;

/**
 * Worker for batch uploading location data to server.
 */
public class LocationUploadWorker extends Worker {

    public static final String WORK_NAME = "location_upload";
    private static final long UPLOAD_INTERVAL = 5; // minutes

    public LocationUploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        try {
            // 获取设备配置
            ServerConfig config = SettingsHelper.getSettings(context).getServerConfig();
            if (config == null) {
                return Result.success();
            }

            String deviceNumber = config.getDeviceId();
            String project = config.getProject();

            if (deviceNumber == null || deviceNumber.isEmpty()) {
                return Result.success();
            }

            // 读取未上传的位置数据
            android.database.sqlite.SQLiteDatabase db = context.openOrCreateDatabase("hmdm.db", Context.MODE_PRIVATE, null);
            List<LocationTable.Location> locations = LocationTable.select(db, 100);
            db.close();

            if (locations.isEmpty()) {
                return Result.success();
            }

            // 调用 API 上传
            ServerService serverService = ServerService.getInstance(context);
            Response<Void> response = serverService.sendLocations(project, deviceNumber, locations).execute();

            if (response.isSuccessful()) {
                // 上传成功，删除已上传的数据
                db = context.openOrCreateDatabase("hmdm.db", Context.MODE_PRIVATE, null);
                LocationTable.delete(db, locations);
                db.close();
                RemoteLogger.log(context, Const.LOG_INFO, "Uploaded " + locations.size() + " locations");
            } else {
                RemoteLogger.log(context, Const.LOG_WARN, "Location upload failed: " + response.code());
            }

            return Result.success();
        } catch (IOException e) {
            RemoteLogger.log(context, Const.LOG_ERROR, "Location upload error: " + e.getMessage());
            return Result.retry();
        }
    }

    /**
     * Schedule periodic location upload.
     */
    public static void scheduleUpload(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest uploadRequest = new PeriodicWorkRequest.Builder(
                LocationUploadWorker.class,
                UPLOAD_INTERVAL, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                uploadRequest
        );
    }

    /**
     * Cancel scheduled upload.
     */
    public static void cancelUpload(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
}
```

- [ ] **Step 2: 添加 ServerConfig 字段**

在 ServerConfig.java 中添加 locationTrackingEnabled 字段（如果需要）。

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/hmdm/launcher/worker/LocationUploadWorker.java
git commit -m "feat: add LocationUploadWorker for batch location upload"
```

---

## Chunk 4: 版本号更新

### Task 4: 更新版本号

**Files:**
- Modify: `app/build.gradle`

- [ ] **Step 1: 更新版本号**

```groovy
versionCode 15311 → 15312
versionName "6.32" → "6.33"
```

- [ ] **Step 2: Commit**

```bash
git add app/build.gradle
git commit -m "feat: bump version to 6.33 for location tracking"
```

---

## Chunk 5: 构建验证

### Task 5: 构建和测试

- [ ] **Step 1: 运行 Gradle 构建**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: 检查 APK**

```bash
ls -la app/build/outputs/apk/debug/
```

- [ ] **Step 3: 提交所有更改**

```bash
git add -A
git commit -m "feat: implement complete location tracking with batch upload

- Add complete location fields (speed, altitude, accuracy, bearing, provider)
- Implement ProUtils.processLocation() for location storage
- Add LocationUploadWorker for batch upload to server
- Update version to 6.33"
```

---

## 实现完成检查清单

- [ ] LocationTable.java - 添加完整字段
- [ ] ProUtils.java - 实现 processLocation()
- [ ] LocationUploadWorker.java - 创建批量上传
- [ ] build.gradle - 版本号更新
- [ ] 构建验证成功