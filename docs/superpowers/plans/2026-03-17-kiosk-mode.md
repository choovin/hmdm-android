# Kiosk 模式功能实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现完整的 Kiosk 模式功能，锁定设备到指定应用

**Architecture:** ServerConfig.kioskMode → ProUtils.startCosuKioskMode() → DevicePolicyManager.setLockTaskPackages()

**Tech Stack:** Android, DevicePolicyManager, AccessibilityService, UsageStatsManager

---

## 文件结构

| 文件路径 | 操作 | 说明 |
|----------|------|------|
| `app/src/main/java/com/hmdm/launcher/pro/ProUtils.java` | 修改 | 实现 Kiosk 模式方法 |
| `app/build.gradle` | 修改 | 版本号 +1 |

---

## Task 1: 实现 Kiosk 模式核心方法

**Files:**
- Modify: `app/src/main/java/com/hmdm/launcher/pro/ProUtils.java`

- [ ] **Step 1: 添加 import**

```java
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.app.usage.UsageStatsManager;
import android.view.accessibility.AccessibilityManager;
```

- [ ] **Step 2: 实现 kioskModeRequired 方法**

```java
public static boolean kioskModeRequired(Context context) {
    ServerConfig config = SettingsHelper.getInstance(context).getConfig();
    return config != null && config.isKioskMode();
}
```

- [ ] **Step 3: 实现 startCosuKioskMode 方法**

```java
public static boolean startCosuKioskMode(String kioskApp, Activity activity, boolean enableSettings) {
    try {
        DevicePolicyManager dpm = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(activity, AdminReceiver.class);

        if (!dpm.isAdminActive(adminComponent)) {
            RemoteLogger.log(activity, Const.LOG_ERROR, "Device admin not active, cannot start kiosk");
            return false;
        }

        // 获取主应用包名
        String packageName = kioskApp;
        if (kioskApp == null || kioskApp.isEmpty()) {
            ServerConfig config = SettingsHelper.getInstance(activity).getConfig();
            if (config != null && config.getMainApp() != null) {
                packageName = config.getMainApp();
            }
        }

        if (packageName == null || packageName.isEmpty()) {
            RemoteLogger.log(activity, Const.LOG_ERROR, "No kiosk app specified");
            return false;
        }

        // 设置锁定的包
        dpm.setLockTaskPackages(adminComponent, new String[]{packageName});

        // 启用锁屏任务模式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.startLockTask();
        }

        RemoteLogger.log(activity, Const.LOG_INFO, "Kiosk mode started for: " + packageName);
        return true;
    } catch (Exception e) {
        RemoteLogger.log(activity, Const.LOG_ERROR, "Failed to start kiosk: " + e.getMessage());
        return false;
    }
}
```

- [ ] **Step 4: 实现其他 Kiosk 相关方法**

实现：isKioskModeRunning, updateKioskOptions, updateKioskAllowedApps, unlockKiosk

- [ ] **Step 5: Commit**

---

## Task 2: 版本号更新

- [ ] **Step 1: 更新版本号**

```groovy
versionCode 15313 → 15314
versionName "6.34" → "6.35"
```

- [ ] **Step 2: Commit**

---

## Task 3: 构建验证

- [ ] **Step 1: 运行 Gradle 构建**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: 提交所有更改**

---

## 实现完成检查清单

- [ ] ProUtils - 实现所有 Kiosk 方法
- [ ] build.gradle - 版本号更新
- [ ] 构建验证成功