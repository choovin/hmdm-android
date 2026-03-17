# 照片上传功能实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现从设备上传照片到服务器的功能

**Architecture:** 设备拍照/截图 → 本地存储 → Worker 批量上传 → ServerService.uploadPhoto()

**Tech Stack:** Android, WorkManager, Retrofit, Camera/File Provider

---

## 文件结构

| 文件路径 | 操作 | 说明 |
|----------|------|------|
| `app/src/main/java/com/hmdm/launcher/pro/ProUtils.java` | 修改 | 添加照片上传方法 |
| `app/src/main/java/com/hmdm/launcher/worker/PhotoUploadWorker.java` | 新增 | 照片上传 Worker |
| `app/src/main/java/com/hmdm/launcher/server/ServerService.java` | 修改 | 添加上传 API |
| `app/build.gradle` | 修改 | 版本号 +1 |

---

## Task 1: 创建 PhotoUploadWorker

**Files:**
- Create: `app/src/main/java/com/hmdm/launcher/worker/PhotoUploadWorker.java`

- [ ] **Step 1: 创建 Worker 类**

```java
public class PhotoUploadWorker extends Worker {
    public static final String WORK_NAME = "photo_upload";

    @Override
    public Result doWork() {
        // 1. 检查是否有待上传的照片
        // 2. 调用 API 上传照片
        // 3. 上传成功后删除本地文件
    }

    public static void scheduleUpload(Context context) {
        // 使用 WorkManager 调度定期上传
    }
}
```

- [ ] **Step 2: Commit**

---

## Task 2: 添加 ProUtils 照片方法

- [ ] **Step 1: 添加 capturePhoto 方法**

```java
public static void capturePhoto(Context context, String fileName) {
    // 使用相机或截图功能
    // 保存到本地
    // 触发上传
}
```

- [ ] **Step 2: Commit**

---

## Task 3: 版本号更新

- [ ] **Step 1: 更新版本号**

```groovy
versionCode 15314 → 15315
versionName "6.35" → "6.36"
```

- [ ] **Step 2: Commit**

---

## Task 4: 构建验证

- [ ] **Step 1: 运行 Gradle 构建**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL

---

## 实现完成检查清单

- [ ] PhotoUploadWorker - 创建照片上传 Worker
- [ ] ProUtils - 添加照片相关方法
- [ ] ServerService - 添加上传 API
- [ ] build.gradle - 版本号更新
- [ ] 构建验证成功