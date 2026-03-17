# 网络过滤功能实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现网络流量监控和过滤功能

**Architecture:** VpnService → 本地 VPN 拦截流量 → 规则匹配 → 上传日志到服务器

**Tech Stack:** Android, VpnService, iptables, WorkManager

---

## 文件结构

| 文件路径 | 操作 | 说明 |
|----------|------|------|
| `app/src/main/java/com/hmdm/launcher/service/NetworkFilterService.java` | 新增 | VPN 网络过滤服务 |
| `app/src/main/java/com/hmdm/launcher/pro/ProUtils.java` | 修改 | 添加网络过滤方法 |
| `app/src/main/java/com/hmdm/launcher/server/ServerService.java` | 修改 | 添加规则下载/日志上传 API |
| `app/build.gradle` | 修改 | 版本号 +1 |

---

## Task 1: 创建 NetworkFilterService

**Files:**
- Create: `app/src/main/java/com/hmdm/launcher/service/NetworkFilterService.java`

- [ ] **Step 1: 创建 VPN 服务**

```java
public class NetworkFilterService extends VpnService {
    // 1. 创建本地 VPN
    // 2. 拦截网络流量
    // 3. 根据规则过滤/放行
    // 4. 记录流量日志
}
```

- [ ] **Step 2: Commit**

---

## Task 2: 添加 ServerService API

- [ ] **Step 1: 添加规则和日志接口**

```java
@GET("{project}/rest/plugins/networkfilter/rules/{number}")
Call<List<NetworkRule>> getRules(@Path("project") String project, @Path("number") String number);

@POST("{project}/rest/plugins/networkfilter/logs/{number}")
Call<Void> uploadTrafficLogs(@Path("project") String project, @Path("number") String number, @Body List<TrafficLog> logs);
```

- [ ] **Step 2: Commit**

---

## Task 3: 版本号更新

- [ ] **Step 1: 更新版本号**

```groovy
versionCode 15316 → 15317
versionName "6.37" → "6.38"
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

- [ ] NetworkFilterService - VPN 网络过滤服务
- [ ] ServerService - 规则和日志 API
- [ ] ProUtils - 网络过滤控制方法
- [ ] build.gradle - 版本号更新
- [ ] 构建验证成功