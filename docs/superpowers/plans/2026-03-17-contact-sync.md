# 联系人同步功能实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现从服务器同步联系人到设备，以及从设备上传联系人到服务器

**Architecture:** ServerService.getContacts() → 写入本地 ContactsProvider ← 设备联系人上传

**Tech Stack:** Android, ContentResolver, ContactsContract, WorkManager

---

## 文件结构

| 文件路径 | 操作 | 说明 |
|----------|------|------|
| `app/src/main/java/com/hmdm/launcher/pro/ProUtils.java` | 修改 | 添加联系人同步方法 |
| `app/src/main/java/com/hmdm/launcher/worker/ContactSyncWorker.java` | 新增 | 联系人同步 Worker |
| `app/src/main/java/com/hmdm/launcher/server/ServerService.java` | 修改 | 添加联系人 API |
| `app/build.gradle` | 修改 | 版本号 +1 |

---

## Task 1: 创建 ContactSyncWorker

**Files:**
- Create: `app/src/main/java/com/hmdm/launcher/worker/ContactSyncWorker.java`

- [ ] **Step 1: 创建 Worker 类**

```java
public class ContactSyncWorker extends Worker {
    // 1. 从服务器获取联系人列表
    // 2. 写入设备通讯录
    // 3. 或上传设备联系人到服务器
}
```

- [ ] **Step 2: Commit**

---

## Task 2: 添加 ServerService 联系人 API

- [ ] **Step 1: 添加联系人接口**

```java
@GET("{project}/rest/plugins/contacts/{number}")
Call<List<Contact>> getContacts(@Path("project") String project, @Path("number") String number);

@PUT("{project}/rest/plugins/contacts/{number}")
Call<Void> uploadContacts(@Path("project") String project, @Path("number") String number, @Body List<Contact> contacts);
```

- [ ] **Step 2: Commit**

---

## Task 3: 版本号更新

- [ ] **Step 1: 更新版本号**

```groovy
versionCode 15317 → 15318
versionName "6.38" → "6.39"
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

- [ ] ContactSyncWorker - 联系人同步 Worker
- [ ] ServerService - 联系人 API
- [ ] ProUtils - 联系人同步方法
- [ ] build.gradle - 版本号更新
- [ ] 构建验证成功