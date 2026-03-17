# 远程重启功能实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现远程重启功能，服务器发送 MQTT 消息触发设备重启

**Architecture:** PushMessage TYPE_REBOOT → PushNotificationProcessor → ProUtils.reboot() → DevicePolicyManager.reboot()

**Tech Stack:** Android, DevicePolicyManager, MQTT Push

---

## 文件结构

| 文件路径 | 操作 | 说明 |
|----------|------|------|
| `app/src/main/java/com/hmdm/launcher/pro/ProUtils.java` | 修改 | 实现 reboot() 方法 |
| `app/src/main/java/com/hmdm/launcher/json/PushMessage.java` | 修改 | 添加 TYPE_REBOOT 消息处理 |
| `app/build.gradle` | 修改 | 版本号 +1 |

---

## Task 1: 实现 ProUtils.reboot()

**Files:**
- Modify: `app/src/main/java/com/hmdm/launcher/pro/ProUtils.java`

- [ ] **Step 1: 添加 import**

```java
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
```

- [ ] **Step 2: 实现 reboot 方法**

在 ProUtils.java 中替换现有的 reboot 存根：

```java
public static boolean reboot(Context context) {
    try {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(context, com.hmdm.launcher.AdminReceiver.class);

        if (!dpm.isAdminActive(adminComponent)) {
            RemoteLogger.log(context, Const.LOG_ERROR, "Device admin not active, cannot reboot");
            return false;
        }

        dpm.reboot(new android.content.ComponentName(context, com.hmdm.launcher.AdminReceiver.class));
        RemoteLogger.log(context, Const.LOG_INFO, "Device reboot initiated");
        return true;
    } catch (Exception e) {
        RemoteLogger.log(context, Const.LOG_ERROR, "Failed to reboot: " + e.getMessage());
        return false;
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/hmdm/launcher/pro/ProUtils.java
git commit -m "feat: implement reboot in ProUtils"
```

---

## Task 2: 注册 TYPE_REBOOT 消息处理器

**Files:**
- Modify: 需要找到处理 PushMessage 的地方

- [ ] **Step 1: 查找消息处理器**

搜索现有消息类型（如 TYPE_LOCK, TYPE_FACTORY_RESET）的处理位置。

- [ ] **Step 2: 添加 TYPE_REBOOT 处理逻辑**

参考现有的 TYPE_LOCK 和 TYPE_FACTORY_RESET 处理方式。

- [ ] **Step 3: Commit**

---

## Task 3: 版本号更新

- [ ] **Step 1: 更新版本号**

```groovy
versionCode 15312 → 15313
versionName "6.33" → "6.34"
```

- [ ] **Step 2: Commit**

---

## Task 4: 构建验证

- [ ] **Step 1: 运行 Gradle 构建**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: 提交所有更改**

---

## 实现完成检查清单

- [ ] ProUtils.reboot() - 实现重启逻辑
- [ ] Push 消息处理 - 注册 TYPE_REBOOT
- [ ] build.gradle - 版本号更新
- [ ] 构建验证成功