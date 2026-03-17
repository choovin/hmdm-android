# 远程锁定与恢复出厂设置功能实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现远程锁定和恢复出厂设置两个企业级功能，包括消息常量定义、消息处理器、确认对话框 UI

**Architecture:** 基于现有的 PushNotificationProcessor 消息分发机制，新增 TYPE_LOCK 和 TYPE_FACTORY_RESET 消息类型，通过 ConfirmDialogActivity 确认后调用 DevicePolicyManager 执行实际操作

**Tech Stack:** Android, DevicePolicyManager, Push Notification, MQTT/Polling

---

## 文件结构

| 文件路径 | 操作 | 说明 |
|----------|------|------|
| `app/src/main/java/com/hmdm/launcher/json/PushMessage.java` | 修改 | 添加 TYPE_LOCK, TYPE_FACTORY_RESET 常量 |
| `app/src/main/java/com/hmdm/launcher/worker/PushNotificationProcessor.java` | 修改 | 添加消息分发到确认对话框 |
| `app/src/main/java/com/hmdm/launcher/ui/ConfirmDialogActivity.java` | 新增 | 确认对话框 Activity |
| `app/src/main/res/layout/activity_confirm_dialog.xml` | 新增 | 布局文件 |
| `app/src/main/res/values/strings.xml` | 修改 | 添加确认对话框字符串 |
| `app/src/main/AndroidManifest.xml` | 修改 | 注册 ConfirmDialogActivity |

---

## Chunk 1: 添加消息类型常量

### Task 1: 添加 PushMessage 常量

**Files:**
- Modify: `app/src/main/java/com/hmdm/launcher/json/PushMessage.java`

- [ ] **Step 1: 添加 TYPE_LOCK 和 TYPE_FACTORY_RESET 常量**

打开文件 `app/src/main/java/com/hmdm/launcher/json/PushMessage.java`，在第 47 行 `TYPE_CLEAR_APP_DATA` 之后添加：

```java
// Remote command types (新增)
public static final String TYPE_LOCK = "lock";
public static final String TYPE_FACTORY_RESET = "factoryReset";
```

- [ ] **Step 2: 验证修改**

检查文件确认常量已添加成功。

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/hmdm/launcher/json/PushMessage.java
git commit -m "feat: add TYPE_LOCK and TYPE_FACTORY_RESET message type constants"
```

---

## Chunk 2: 创建确认对话框 Activity

### Task 2: 创建 ConfirmDialogActivity.java

**Files:**
- Create: `app/src/main/java/com/hmdm/launcher/ui/ConfirmDialogActivity.java`

- [ ] **Step 1: 创建 ConfirmDialogActivity.java**

创建文件 `app/src/main/java/com/hmdm/launcher/ui/ConfirmDialogActivity.java`，内容如下：

```java
/*
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hmdm.launcher.ui;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hmdm.launcher.Const;
import com.hmdm.launcher.R;
import com.hmdm.launcher.util.RemoteLogger;

/**
 * Activity for confirming dangerous operations like remote lock and factory reset.
 * Displays operation details and requires user confirmation before executing.
 */
public class ConfirmDialogActivity extends AppCompatActivity {

    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_COUNTDOWN = "countdown";
    public static final String EXTRA_WIPE_EXTERNAL = "wipeExternal";

    public static final String ACTION_LOCK = "lock";
    public static final String ACTION_FACTORY_RESET = "factoryReset";

    private String action;
    private String adminMessage;
    private int countdown;
    private boolean wipeExternal;
    private boolean countdownRunning = true;
    private CountDownTimer countDownTimer;

    private TextView countdownText;
    private Button confirmButton;
    private Button cancelButton;
    private EditText confirmInput;
    private View inputContainer;

    private static final int DEFAULT_COUNTDOWN_LOCK = 30;
    private static final int DEFAULT_COUNTDOWN_RESET = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on and show over lock screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_confirm_dialog);

        // Get parameters from intent
        action = getIntent().getStringExtra(EXTRA_ACTION);
        adminMessage = getIntent().getStringExtra(EXTRA_MESSAGE);
        countdown = getIntent().getIntExtra(EXTRA_COUNTDOWN, 0);
        wipeExternal = getIntent().getBooleanExtra(EXTRA_WIPE_EXTERNAL, true);

        // Set default countdown if not provided
        if (countdown <= 0) {
            countdown = ACTION_FACTORY_RESET.equals(action) ? DEFAULT_COUNTDOWN_RESET : DEFAULT_COUNTDOWN_LOCK;
        }

        initViews();
        setupUI();
        startCountdown();
    }

    private void initViews() {
        countdownText = findViewById(R.id.countdown_text);
        confirmButton = findViewById(R.id.confirm_button);
        cancelButton = findViewById(R.id.cancel_button);
        confirmInput = findViewById(R.id.confirm_input);
        inputContainer = findViewById(R.id.input_container);
    }

    private void setupUI() {
        TextView titleText = findViewById(R.id.dialog_title);
        TextView messageText = findViewById(R.id.dialog_message);
        View lockInfoContainer = findViewById(R.id.lock_info_container);
        View factoryResetInfoContainer = findViewById(R.id.factory_reset_info_container);

        if (ACTION_LOCK.equals(action)) {
            titleText.setText(R.string.remote_lock_title);
            messageText.setText(R.string.remote_lock_message);
            lockInfoContainer.setVisibility(View.VISIBLE);
            factoryResetInfoContainer.setVisibility(View.GONE);
            inputContainer.setVisibility(View.GONE);
            confirmButton.setText(R.string.confirm_lock);
        } else if (ACTION_FACTORY_RESET.equals(action)) {
            titleText.setText(R.string.factory_reset_title);
            messageText.setText(R.string.factory_reset_message);
            lockInfoContainer.setVisibility(View.GONE);
            factoryResetInfoContainer.setVisibility(View.VISIBLE);
            inputContainer.setVisibility(View.VISIBLE);
            confirmButton.setText(R.string.confirm_factory_reset);
        }

        // Display admin message if provided
        TextView adminMessageText = findViewById(R.id.admin_message);
        if (adminMessage != null && !adminMessage.isEmpty()) {
            adminMessageText.setText(adminMessage);
            adminMessageText.setVisibility(View.VISIBLE);
        } else {
            adminMessageText.setVisibility(View.GONE);
        }

        // Set up button listeners
        confirmButton.setOnClickListener(v -> onConfirmClick());
        cancelButton.setOnClickListener(v -> onCancelClick());

        // Initially disable confirm button
        confirmButton.setEnabled(false);
    }

    private void startCountdown() {
        countdownRunning = true;

        countDownTimer = new CountDownTimer(countdown * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                countdownText.setText(String.format("%d", secondsRemaining));
                cancelButton.setText(getString(R.string.cancel_with_countdown, secondsRemaining));
            }

            @Override
            public void onFinish() {
                countdownRunning = false;
                countdownText.setText("0");
                cancelButton.setText(R.string.cancel);
                confirmButton.setEnabled(true);
                confirmButton.setText(R.string.execute_now);
            }
        };

        countDownTimer.start();
    }

    private void onConfirmClick() {
        if (ACTION_FACTORY_RESET.equals(action)) {
            // Factory reset requires typing "确认"
            String input = confirmInput.getText().toString();
            if (!"确认".equals(input)) {
                Toast.makeText(this, R.string.confirm_input_hint, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!countdownRunning) {
            executeAction();
        }
    }

    private void onCancelClick() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        finish();
    }

    private void executeAction() {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = new ComponentName(this, com.hmdm.launcher.AdminReceiver.class);

            // Check if admin is enabled
            if (!dpm.isAdminActive(adminComponent)) {
                RemoteLogger.log(this, Const.LOG_ERROR, "Device admin not active, cannot execute remote command");
                Toast.makeText(this, R.string.admin_not_active, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            if (ACTION_LOCK.equals(action)) {
                dpm.lockNow();
                RemoteLogger.log(this, Const.LOG_INFO, "Device locked by user confirmation");
                Toast.makeText(this, R.string.device_locked, Toast.LENGTH_SHORT).show();
            } else if (ACTION_FACTORY_RESET.equals(action)) {
                int flags = wipeExternal ? DevicePolicyManager.WIPE_EXTERNAL_STORAGE : 0;
                dpm.wipeData(flags);
                RemoteLogger.log(this, Const.LOG_INFO, "Factory reset initiated by user confirmation");
            }
        } catch (Exception e) {
            RemoteLogger.log(this, Const.LOG_ERROR, "Failed to execute action: " + e.getMessage());
            Toast.makeText(this, getString(R.string.operation_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        } finally {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/hmdm/launcher/ui/ConfirmDialogActivity.java
git commit -m "feat: add ConfirmDialogActivity for remote lock and factory reset confirmation"
```

---

## Chunk 3: 创建布局文件

### Task 3: 创建 activity_confirm_dialog.xml

**Files:**
- Create: `app/src/main/res/layout/activity_confirm_dialog.xml`

- [ ] **Step 1: 创建布局文件**

创建文件 `app/src/main/res/layout/activity_confirm_dialog.xml`：

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/background_dark"
    android:padding="24dp"
    android:gravity="center">

    <!-- Warning Icon -->
    <ImageView
        android:layout_width="64dp"
        android:layout_height="64dp"
        android:src="@android:drawable/ic_dialog_alert"
        android:tint="@color/warning_red"
        android:layout_marginBottom="16dp" />

    <!-- Title -->
    <TextView
        android:id="@+id/dialog_title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="24sp"
        android:textStyle="bold"
        android:textColor="@color/text_primary"
        android:layout_marginBottom="8dp" />

    <!-- Description -->
    <TextView
        android:id="@+id/dialog_message"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="16sp"
        android:textColor="@color/text_secondary"
        android:gravity="center"
        android:layout_marginBottom="16dp" />

    <!-- Admin Message (optional) -->
    <TextView
        android:id="@+id/admin_message"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textSize="14sp"
        android:textColor="@color/text_hint"
        android:background="@color/info_background"
        android:padding="12dp"
        android:layout_marginBottom="16dp"
        android:visibility="gone" />

    <!-- Lock Info Container -->
    <LinearLayout
        android:id="@+id/lock_info_container"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:background="@color/info_background"
        android:padding="12dp"
        android:layout_marginBottom="16dp"
        android:visibility="gone">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/lock_info_title"
            android:textStyle="bold"
            android:textColor="@color/text_primary"
            android:layout_marginBottom="8dp" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/lock_info_content"
            android:textColor="@color/text_secondary" />
    </LinearLayout>

    <!-- Factory Reset Info Container -->
    <LinearLayout
        android:id="@+id/factory_reset_info_container"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:background="@color/warning_background"
        android:padding="12dp"
        android:layout_marginBottom="16dp"
        android:visibility="gone">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/factory_reset_warning"
            android:textStyle="bold"
            android:textColor="@color/warning_red"
            android:layout_marginBottom="8dp" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/factory_reset_info"
            android:textColor="@color/text_primary" />
    </LinearLayout>

    <!-- Input for Factory Reset Confirmation -->
    <LinearLayout
        android:id="@+id/input_container"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:layout_marginBottom="16dp"
        android:visibility="gone">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/confirm_input_label"
            android:textColor="@color/text_secondary"
            android:layout_marginBottom="8dp" />

        <EditText
            android:id="@+id/confirm_input"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="@string/confirm_input_hint"
            android:inputType="text"
            android:maxLength="10"
            android:gravity="center" />
    </LinearLayout>

    <!-- Countdown Display -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center"
        android:layout_marginBottom="24dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/countdown_label"
            android:textColor="@color/text_secondary" />

        <TextView
            android:id="@+id/countdown_text"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textStyle="bold"
            android:textSize="20sp"
            android:textColor="@color/accent_color"
            android:layout_marginStart="8dp" />
    </LinearLayout>

    <!-- Action Buttons -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center">

        <Button
            android:id="@+id/cancel_button"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="@string/cancel"
            android:layout_marginEnd="8dp"
            style="@style/Widget.MaterialComponents.Button.OutlinedButton" />

        <Button
            android:id="@+id/confirm_button"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="@string/confirm"
            android:enabled="false"
            android:layout_marginStart="8dp" />
    </LinearLayout>

</LinearLayout>
```

- [ ] **Step 2: 创建缺少的颜色资源**

检查 `res/values/colors.xml` 是否有所需颜色，如没有则添加：

```xml
<!-- Add to res/values/colors.xml if not exists -->
<color name="background_dark">#1A1A1A</color>
<color name="warning_red">#FF5722</color>
<color name="info_background">#2D3748</color>
<color name="warning_background">#4A1A1A</color>
<color name="text_primary">#FFFFFF</color>
<color name="text_secondary">#B0B0B0</color>
<color name="text_hint">#808080</color>
<color name="accent_color">#4CAF50</color>
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/layout/activity_confirm_dialog.xml
git commit -m "feat: add layout for ConfirmDialogActivity"
```

---

## Chunk 4: 添加字符串资源

### Task 4: 修改 strings.xml

**Files:**
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: 添加字符串资源**

打开 `app/src/main/res/values/strings.xml`，在文件末尾添加：

```xml
<!-- Remote Lock and Factory Reset Confirmation -->
<string name="remote_lock_title">远程锁定确认</string>
<string name="remote_lock_message">管理员正在远程锁定此设备</string>
<string name="lock_info_title">ℹ️ 锁定后:</string>
<string name="lock_info_content">屏幕将关闭，输入密码可恢复正常使用</string>

<string name="factory_reset_title">恢复出厂设置确认</string>
<string name="factory_reset_message">管理员正在将此设备恢复出厂设置</string>
<string name="factory_reset_warning">⚠️ 此操作不可逆！</string>
<string name="factory_reset_info">将清除: 所有应用和数据、所有设置、SD卡存储</string>

<string name="confirm_input_label">请输入 \"确认\" 以继续:</string>
<string name="confirm_input_hint">确认</string>

<string name="countdown_label">剩余确认时间:</string>
<string name="cancel_with_countdown">取消 (%ds)</string>

<string name="confirm_lock">确认锁定</string>
<string name="confirm_factory_reset">确认重置</string>
<string name="execute_now">立即执行</string>

<string name="admin_not_active">设备管理员未激活，无法执行操作</string>
<string name="device_locked">设备已锁定</string>
<string name="operation_failed">操作失败: %s</string>
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/res/values/strings.xml
git commit -m "feat: add strings for remote lock and factory reset confirmation"
```

---

## Chunk 5: 注册 Activity 和修改消息处理器

### Task 5: 修改 AndroidManifest.xml

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: 注册 ConfirmDialogActivity**

在 `</activity>` 标签（ErrorDetailsActivity）之后添加：

```xml
<activity
    android:name=".ui.ConfirmDialogActivity"
    android:excludeFromRecents="true"
    android:exported="false"
    android:showOnLockScreen="true"
    android:turnScreenOn="true"
    android:theme="@style/SetupWizardTheme" />
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/AndroidManifest.xml
git commit -m "feat: register ConfirmDialogActivity in AndroidManifest"
```

### Task 6: 修改 PushNotificationProcessor 添加消息分发

**Files:**
- Modify: `app/src/main/java/com/hmdm/launcher/worker/PushNotificationProcessor.java`

- [ ] **Step 1: 添加消息处理逻辑**

打开 `app/src/main/java/com/hmdm/launcher/worker/PushNotificationProcessor.java`，在现有的消息处理分支后（`TYPE_CLEAR_APP_DATA` 处理之后，大约第 127 行）添加：

```java
} else if (message.getMessageType().equals(PushMessage.TYPE_LOCK)) {
    RemoteLogger.log(context, Const.LOG_INFO, "Received remote lock command");
    Intent intent = new Intent(context, ConfirmDialogActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // Parse optional payload
    String countdownStr = message.getPayloadJSON() != null ?
        message.getPayloadJSON().optString("countdown", "30") : "30";
    String adminMessage = message.getPayloadJSON() != null ?
        message.getPayloadJSON().optString("message", "") : "";
    intent.putExtra(ConfirmDialogActivity.EXTRA_ACTION, ConfirmDialogActivity.ACTION_LOCK);
    intent.putExtra(ConfirmDialogActivity.EXTRA_COUNTDOWN, Integer.parseInt(countdownStr));
    if (!adminMessage.isEmpty()) {
        intent.putExtra(ConfirmDialogActivity.EXTRA_MESSAGE, adminMessage);
    }
    context.startActivity(intent);
    return;
} else if (message.getMessageType().equals(PushMessage.TYPE_FACTORY_RESET)) {
    RemoteLogger.log(context, Const.LOG_INFO, "Received factory reset command");
    Intent intent = new Intent(context, ConfirmDialogActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // Parse optional payload
    String countdownStr = message.getPayloadJSON() != null ?
        message.getPayloadJSON().optString("countdown", "60") : "60";
    String adminMessage = message.getPayloadJSON() != null ?
        message.getPayloadJSON().optString("message", "") : "";
    boolean wipeExternal = message.getPayloadJSON() != null ?
        message.getPayloadJSON().optBoolean("wipeExternal", true) : true;
    intent.putExtra(ConfirmDialogActivity.EXTRA_ACTION, ConfirmDialogActivity.ACTION_FACTORY_RESET);
    intent.putExtra(ConfirmDialogActivity.EXTRA_COUNTDOWN, Integer.parseInt(countdownStr));
    intent.putExtra(ConfirmDialogActivity.EXTRA_WIPE_EXTERNAL, wipeExternal);
    if (!adminMessage.isEmpty()) {
        intent.putExtra(ConfirmDialogActivity.EXTRA_MESSAGE, adminMessage);
    }
    context.startActivity(intent);
    return;
```

- [ ] **Step 2: 添加必要的 import**

确保文件顶部有以下 import：

```java
import android.content.Intent;
import com.hmdm.launcher.ui.ConfirmDialogActivity;
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/hmdm/launcher/worker/PushNotificationProcessor.java
git commit -m "feat: add message handlers for TYPE_LOCK and TYPE_FACTORY_RESET"
```

---

## Chunk 6: 构建验证

### Task 7: 构建和验证

**Files:**
- Test: `app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 1: 运行 Gradle 构建**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: 检查 APK 输出**

```bash
ls -la app/build/outputs/apk/debug/
```

- [ ] **Step 3: Commit 完整功能**

```bash
git add -A
git commit -m "feat: implement remote lock and factory reset with user confirmation

- Add TYPE_LOCK and TYPE_FACTORY_RESET message constants
- Add ConfirmDialogActivity with countdown and confirmation
- Add message handlers in PushNotificationProcessor
- Support optional admin message and countdown from payload
- Factory reset requires typing '确认' for extra security"
```

---

## 实现完成检查清单

- [ ] PushMessage.java - 添加常量
- [ ] ConfirmDialogActivity.java - 创建确认对话框
- [ ] activity_confirm_dialog.xml - 创建布局
- [ ] strings.xml - 添加字符串资源
- [ ] AndroidManifest.xml - 注册 Activity
- [ ] PushNotificationProcessor.java - 添加消息处理
- [ ] 构建验证成功