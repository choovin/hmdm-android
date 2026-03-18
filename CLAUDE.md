# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is Headwind MDM, an open-source Mobile Device Management (MDM) launcher for Android. It consists of:
- **app**: The main MDM launcher application (`com.hmdm.launcher`)
- **lib**: A library module providing an API for third-party apps to integrate with Headwind MDM

## Build Commands

### Building the APK
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build all variants
./gradlew build
```

Output location: `app/build/outputs/apk/`

### Building the Library (AAR)
```bash
./gradlew :lib:assembleRelease
```
Output location: `lib/build/outputs/aar/`

### Running Tests
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

### Code Quality
```bash
# Run lint checks
./gradlew lint
```

Lint configuration is in `lint.xml` (currently ignores `ExpiredTargetSdkVersion`).

### Clean Build
```bash
./gradlew clean
```

## Docker Build Environment

A Docker build environment is provided for consistent builds:

```bash
# Build the Docker image
docker build -f Dockerfile.build -t android-builder .

# Build APK using Docker
docker run --rm -v $(pwd):/workspace android-builder ./gradlew assembleRelease
```

## Project Configuration

### Critical Build Config Fields (app/build.gradle)

The `buildConfigField` values in `app/build.gradle` define compile-time constants:

| Field | Description |
|-------|-------------|
| `BASE_URL` | Default server URL (scheme + host) |
| `SECONDARY_BASE_URL` | Fallback server URL |
| `SERVER_PROJECT` | Relative path on server (empty if root) |
| `DEVICE_ID_CHOICE` | Device ID setup: `user`, `suggest`, `imei`, `serial`, `mac` |
| `ENABLE_PUSH` | Enable MQTT push notifications |
| `MQTT_PORT` | MQTT port for push notifications |
| `SYSTEM_PRIVILEGES` | Set to `true` if signing with system keys |
| `TRUST_ANY_CERTIFICATE` | Allow self-signed certs (security risk) |
| `REQUEST_SIGNATURE` | Shared secret for signing requests |
| `CHECK_SIGNATURE` | Verify server signature |
| `LIBRARY_API_KEY` | API key for library authorization |

### Signing Configuration

Release builds use a configured keystore in `app/build.gradle`:
- Keystore path, password, key alias, and key password are defined in `signingConfigs.release`

### SDK Versions

- `compileSdkVersion`: 34
- `minSdkVersion`: 16 (app), 14 (lib)
- `targetSdkVersion`: 34

## Architecture Overview

### Module Structure

```
app/src/main/java/com/hmdm/launcher/
├── ui/              # Activities and UI components
├── service/         # Background services (MQTT, location, status control)
├── receiver/        # Broadcast receivers (boot, shutdown, SIM change)
├── task/            # Async tasks for server communication
├── server/          # Server API interface (Retrofit)
├── json/            # JSON model classes for API
├── helper/          # Configuration and settings helpers
├── db/              # SQLite database helpers
├── util/            # Utility classes
├── pro/             # Pro features (accessibility services, workers)
└── worker/          # WorkManager background workers

lib/src/main/java/com/hmdm/
├── HeadwindMDM.java     # Main API class for third-party apps
├── MDMService.java      # AIDL service interface
└── MDM* classes         # Supporting classes for the library API
```

### Key Components

**MainActivity** (`ui/MainActivity.java`): The launcher home screen. Handles:
- App grid display and management
- Kiosk mode enforcement
- Permission requests (overlay, device admin, etc.)
- QR code scanning for provisioning

**AdminReceiver** (`AdminReceiver.java`): Device admin receiver handling device policy management.

**ConfigUpdater** (`helper/ConfigUpdater.java`): Fetches and applies server configuration.

**ServerService** (`server/ServerService.java`): Retrofit interface for MDM server API.

**Push Services**: MQTT-based push notifications via `MqttService` and `PushNotificationProcessor`.

### Library API

The `lib` module provides an API for third-party apps to:
- Connect to Headwind MDM service
- Query device info (server host, device ID, custom fields)
- Force configuration updates
- Send push messages to server

Usage pattern:
```java
HeadwindMDM mdm = HeadwindMDM.getInstance();
mdm.setApiKey("your-api-key");
mdm.connect(context, eventHandler);
```

### Device Provisioning

The app supports Android Enterprise provisioning:
- `MdmChoiceSetupActivity`: Handles `GET_PROVISIONING_MODE` intent
- `InitialSetupActivity`: Handles `ADMIN_POLICY_COMPLIANCE` intent
- QR code provisioning with embedded server URL and device ID

## Development Notes

### Device Owner Setup (Development)

To set device owner for testing:
```bash
adb shell dpm set-device-owner com.hmdm.launcher/.AdminReceiver
```

### Shared User ID

The app uses `android:sharedUserId="com.hmdm"` to allow integration with companion apps using the same shared user ID.

### Foreground Services

Multiple foreground services are used:
- MQTT service (`MqttService`) - push notifications
- Location service (`LocationService`) - GPS tracking
- Push long polling service (`PushLongPollingService`) - fallback push mechanism

### Security Considerations

- The app requests extensive device policy permissions for MDM functionality
- Request signatures can be enabled for server communication verification
- Self-signed certificate support is available but disabled by default
- Library API requires API key for privileged operations

### Localization

The app supports 15+ languages defined in `res/values-*/` directories.

## Project Structure

This repository is part of a larger Headwind MDM ecosystem:

```
jubensha-hmdm-ws/
├── hmdm-android/          # Android client (this repo)
├── hmdm-server/           # MDM server (Java/JAX-RS)
└── hmdm-docker/           # Docker deployment
```

## Documentation

Key documentation in `knowledge/` folder:
- `20260317-*/HeadwindMDM企业功能对接评估方案*.md` - Enterprise feature integration guides
- `20260318-*/HeadwindMDM-Server代码审核指南.md` - Server code review guide
- `20260318-*/HeadwindMDM-E2E端到端测试指南.md` - E2E test procedures for 10 enterprise features
- `20260318-*/HeadwindMDM-项目记忆.md` - Project memory and completed work

## Enterprise Features

The MDM supports 10 enterprise features:
1. **Kiosk Mode** - Lock device to specific apps (ProUtils.java)
2. **Location Tracking** - GPS tracking (LocationUploadWorker, LocationService)
3. **Remote Control** - Screen capture, lock, reboot (RemoteControlService)
4. **Network Filter** - VPN-based traffic filtering (NetworkFilterService)
5. **Contact Sync** - Bidirectional contact sync (ContactSyncWorker)
6. **Photo Upload** - Device photo upload (PhotoUploadWorker)
7. **Remote Lock** - Remote device lock
8. **Remote Reboot** - Remote device reboot
9. **Factory Reset** - Remote factory reset
10. **LDAP Integration** - Server-side LDAP authentication

## Server API Endpoints

Server provides REST APIs at `/plugins/*` paths:
- `/plugins/devicelocations/public` - Location tracking
- `/plugins/devicephoto/public` - Photo upload
- `/plugins/devicecontrol` - Remote control
- `/plugins/networkfilter` - Network filtering
- `/plugins/contacts` - Contact sync
