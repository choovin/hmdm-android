# Headwind MDM 远程锁定与恢复出厂设置功能设计

## 1. 功能概述

本文档描述 Headwind MDM Android 客户端实现远程锁定和恢复出厂设置两个企业级功能的设计方案。

### 1.1 功能需求

| 功能 | 用户确认 | 锁定范围 | 清除选项 |
|------|---------|---------|---------|
| 远程锁定 | ✅ 需要确认 | 仅锁屏 | N/A |
| 恢复出厂设置 | ✅ 需要确认 | N/A | 包含SD卡 |

### 1.2 设计目标

- 实现完整的远程锁定功能（需用户确认）
- 实现完整的恢复出厂设置功能（需用户确认，包含SD卡）
- 确保操作可逆性（确认对话框 + 倒计时）
- 与现有架构无缝集成

---

## 2. 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                      后端 (hmdm-server)                      │
│  PushService.sendRemoteCommand(deviceId, "lock")            │
│  PushService.sendRemoteCommand(deviceId, "factoryReset")    │
└──────────────────────┬──────────────────────────────────────┘
                       │ MQTT / Polling Push
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    Android 客户端                            │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  PushNotificationProcessor                          │   │
│  │  - TYPE_LOCK (新增)                                  │   │
│  │  - TYPE_FACTORY_RESET (新增)                         │   │
│  └──────────────────┬──────────────────────────────────┘   │
│                     ▼                                        │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ConfirmDialogActivity (新增)                        │   │
│  │  - 显示操作说明                                       │   │
│  │  - 用户确认后执行                                     │   │
│  │  - 支持倒计时取消                                     │   │
│  └──────────────────┬──────────────────────────────────┘   │
│                     ▼                                        │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  DevicePolicyManager (系统API)                       │   │
│  │  - lockNow() / wipeData()                           │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. 消息类型定义

### 3.1 客户端 PushMessage 常量

**文件**: `app/src/main/java/com/hmdm/launcher/json/PushMessage.java`

```java
// Remote command types (新增)
public static final String TYPE_LOCK = "lock";
public static final String TYPE_FACTORY_RESET = "factoryReset";
```

### 3.2 消息 Payload 格式（可选）

后端可通过 payload 传递附加参数：

```json
// 远程锁定
{
    "message": "管理员留言内容",
    "countdown": 30
}

// 恢复出厂设置
{
    "message": "管理员留言内容",
    "countdown": 60,
    "wipeExternal": true
}
```

---

## 4. 确认对话框设计

### 4.1 远程锁定确认界面

```
┌─────────────────────────────────────┐
│  ⚠️ 远程锁定确认                     │
├─────────────────────────────────────┤
│                                     │
│  管理员正在远程锁定此设备            │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  ℹ️ 锁定后屏幕将关闭          │   │
│  │  输入密码可恢复正常使用       │   │
│  └─────────────────────────────┘   │
│                                     │
│  管理员留言: (可选显示)              │
│  "请立即保存工作进度"               │
│                                     │
│  ┌── 30 ──────────────────────┐    │
│  │   取消 (29s)      确认锁定  │    │
│  └───┬───────────────────────┘    │
│      │                              │
│      │  [按钮倒计时，点击立即执行]   │
│                                     │
└─────────────────────────────────────┘
```

### 4.2 恢复出厂设置确认界面

```
┌─────────────────────────────────────┐
│  ⚠️ 恢复出厂设置确认                 │
├─────────────────────────────────────┤
│                                     │
│  ⚠️ 此操作不可逆！                   │
│                                     │
│  管理员正在将此设备恢复出厂设置      │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  ℹ️ 将清除:                   │   │
│  │  - 所有应用和数据             │   │
│  │  - 所有设置                   │   │
│  │  - 外部存储 (SD卡)            │   │
│  │  ⚠️ 所有数据将被永久删除       │   │
│  └─────────────────────────────┘   │
│                                     │
│  请输入 "确认" 以继续:              │
│  ┌─────────────────────────────┐   │
│  │ [          ]                 │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌── 60 ──────────────────────┐    │
│  │   取消 (59s)      确认重置  │    │
│  └───┬───────────────────────┘    │
│                                     │
└─────────────────────────────────────┘
```

---

## 5. 消息处理流程

```
PushNotificationProcessor.process()
    │
    ├── 消息类型 = TYPE_LOCK
    │   │
    │   1. 解析 payload (可选的倒计时、留言)
    │   2. 启动 ConfirmDialogActivity
    │   3. Intent: new Intent(context, ConfirmDialogActivity.class)
    │   4. putExtra("action", "lock")
    │   5. putExtra("message", "管理员留言")
    │   6. putExtra("countdown", 30)
    │   7. startActivity(intent)
    │
    └── 消息类型 = TYPE_FACTORY_RESET
        │
        1. 解析 payload
        2. 启动 ConfirmDialogActivity
        3. Intent: new Intent(context, ConfirmDialogActivity.class)
        4. putExtra("action", "factoryReset")
        5. putExtra("wipeExternal", true)  // 包含SD卡
        6. putExtra("countdown", 60)
        7. startActivity(intent)
```

---

## 6. ConfirmDialogActivity 实现

### 6.1 核心代码逻辑

```java
public class ConfirmDialogActivity extends AppCompatActivity {

    private String action;
    private int countdown;
    private boolean countdownRunning = true;
    private EditText confirmInput;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_dialog);

        // 获取参数
        action = getIntent().getStringExtra("action");
        countdown = getIntent().getIntExtra("countdown", 30);
        boolean wipeExternal = getIntent().getBooleanExtra("wipeExternal", true);
        String message = getIntent().getStringExtra("message");

        // 根据 action 显示不同 UI
        if ("lock".equals(action)) {
            showLockConfirmation(message);
        } else if ("factoryReset".equals(action)) {
            showFactoryResetConfirmation(message, wipeExternal);
        }

        // 启动倒计时
        startCountdown();
    }

    private void startCountdown() {
        final TextView countdownView = findViewById(R.id.countdown_text);
        confirmButton = findViewById(R.id.confirm_button);

        new Handler().postDelayed(() -> {
            countdownRunning = false;
            confirmButton.setEnabled(true);
            confirmButton.setText("确认执行");
        }, countdown * 1000);

        // 更新倒计时显示
        // ... (计时器逻辑)
    }

    public void onConfirmClick(View v) {
        if ("factoryReset".equals(action)) {
            // 恢复出厂设置需要输入 "确认"
            String input = confirmInput.getText().toString();
            if (!"确认".equals(input)) {
                Toast.makeText(this, "请输入 '确认'", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!countdownRunning) {
            executeAction();
        }
    }

    public void onCancelClick(View v) {
        finish();  // 用户取消，关闭对话框
    }

    private void executeAction() {
        try {
            DevicePolicyManager dpm = getSystemService(DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = new ComponentName(this, AdminReceiver.class);

            if ("lock".equals(action)) {
                dpm.lockNow();
                RemoteLogger.log(this, Const.LOG_INFO, "Device locked by user confirmation");
            } else if ("factoryReset".equals(action)) {
                boolean wipeExternal = getIntent().getBooleanExtra("wipeExternal", true);
                int flags = wipeExternal ? DevicePolicyManager.WIPE_EXTERNAL_STORAGE : 0;
                dpm.wipeData(flags);
                RemoteLogger.log(this, Const.LOG_INFO, "Factory reset initiated by user confirmation");
            }
        } catch (Exception e) {
            RemoteLogger.log(this, Const.LOG_ERROR, "Failed to execute action: " + e.getMessage());
            Toast.makeText(this, "操作失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            finish();
        }
    }
}
```

### 6.2 布局说明

- `res/layout/activity_confirm_dialog.xml` - 共用布局
- 通过 `ViewStub` 动态切换锁定/重置的提示内容

---

## 7. 需要修改/新增的文件

| 文件路径 | 操作 | 说明 |
|----------|------|------|
| `app/src/main/java/com/hmdm/launcher/json/PushMessage.java` | 修改 | 添加 TYPE_LOCK, TYPE_FACTORY_RESET 常量 |
| `app/src/main/java/com/hmdm/launcher/worker/PushNotificationProcessor.java` | 修改 | 添加消息分发到确认对话框 |
| `app/src/main/java/com/hmdm/launcher/ui/ConfirmDialogActivity.java` | 新增 | 确认对话框 Activity |
| `app/src/main/res/layout/activity_confirm_dialog.xml` | 新增 | 布局文件 |
| `app/src/main/res/values/strings.xml` | 修改 | 添加确认对话框字符串 |
| `app/src/main/AndroidManifest.xml` | 修改 | 注册 ConfirmDialogActivity |

---

## 8. 权限要求

```xml
<!-- AndroidManifest.xml -->
<!-- 设备管理员权限由 AdminReceiver 配置，此处无需额外声明 -->

<!-- 系统应用需要此权限 -->
<uses-permission android:name="android.permission.MASTER_CLEAR" />
<!-- 注：只有系统应用才能请求此权限，普通应用无法获取 -->
```

设备管理员权限已在 `AdminReceiver` 中配置，包含以下权限：
- `android.app.action.DEVICE_ADMIN_ENABLED`
- `android.app.action.DEVICE_ADMIN_DISABLED`

---

## 9. 后端兼容性

后端无需修改，已有的消息类型和 PushService 可直接使用。

### 9.1 后端 PushService (已存在)

```java
// hmdm-server/notification/src/main/java/com/hmdm/notification/PushService.java
@Transactional
public void sendRemoteCommand(Integer deviceId, String commandType) {
    final Device dbDevice = this.deviceDAO.getDeviceById(deviceId);
    if (dbDevice != null) {
        PushMessage message = new PushMessage();
        message.setDeviceId(dbDevice.getId());
        message.setMessageType(commandType);
        // commandType: "lock" 或 "factoryReset"
        this.send(message);
    }
}
```

### 9.2 调用方式

管理员在后台点击"远程锁定"或"恢复出厂设置"按钮时，后端调用：
```java
pushService.sendRemoteCommand(deviceId, "lock");
pushService.sendRemoteCommand(deviceId, "factoryReset");
```

---

## 10. 错误处理

| 场景 | 处理方式 |
|------|---------|
| 无设备管理员权限 | 显示 Toast 提示 "需要设备管理员权限"，记录日志 |
| 设备未激活 Admin | 跳过确认步骤，直接执行，记录警告日志 |
| 恢复出厂设置失败 | 显示错误 Toast，记录详细日志 |
| 用户超时未确认 | 倒计时结束后自动关闭对话框，不执行操作 |

---

## 11. 安全考虑

1. **确认机制**: 用户必须明确确认才能执行危险操作
2. **倒计时**: 防止误触，提供取消机会
3. **输入验证**: 恢复出厂设置需要输入"确认"二次验证
4. **日志记录**: 所有操作均记录到 RemoteLogger
5. **不可逆提示**: 明确告知用户数据将被永久删除