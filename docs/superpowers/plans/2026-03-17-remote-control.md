# 远程控制功能实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现远程查看和控制设备屏幕的功能

**Architecture:** 设备端 WebRTC → ServerService signaling → Web 端远程查看/控制

**Tech Stack:** Android, WebRTC (libwebrtc), Retrofit Signaling

---

## 文件结构

| 文件路径 | 操作 | 说明 |
|----------|------|------|
| `app/src/main/java/com/hmdm/launcher/service/RemoteControlService.java` | 新增 | 远程控制服务 |
| `app/src/main/java/com/hmdm/launcher/server/ServerService.java` | 修改 | 添加 WebRTC 信令 API |
| `app/src/main/java/com/hmdm/launcher/pro/ProUtils.java` | 修改 | 添加远程控制方法 |
| `app/build.gradle` | 修改 | 版本号 +1 |

---

## Task 1: 创建 RemoteControlService

**Files:**
- Create: `app/src/main/java/com/hmdm/launcher/service/RemoteControlService.java`

- [ ] **Step 1: 创建前台服务**

```java
public class RemoteControlService extends Service {
    // 1. 创建 WebRTC PeerConnection
    // 2. 处理信令消息
    // 3. 捕获屏幕并发送
    // 4. 接收远程控制指令
}
```

- [ ] **Step 2: Commit**

---

## Task 2: 添加 ServerService 信令 API

- [ ] **Step 1: 添加信令相关接口**

```java
@POST("{project}/rest/plugins/devicecontrol/signal/{number}")
Call<ResponseBody> sendSignal(@Path("project") String project, @Path("number") String number, @Body SignalMessage message);

@WebSocket("{project}/rest/plugins/devicecontrol/ws/{number}")
RetrofitWebSocketCall webSocket(@Path("project") String project, @Path("number") String number);
```

- [ ] **Step 2: Commit**

---

## Task 3: 版本号更新

- [ ] **Step 1: 更新版本号**

```groovy
versionCode 15315 → 15316
versionName "6.36" → "6.37"
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

- [ ] RemoteControlService - 远程控制服务
- [ ] ServerService - WebRTC 信令 API
- [ ] ProUtils - 远程控制方法
- [ ] build.gradle - 版本号更新
- [ ] 构建验证成功