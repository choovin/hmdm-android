/*
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC
 *
 * Licensed under the Apache License, Version 2.0
 */

package com.hmdm.launcher;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Const class - constants used across enterprise features.
 */
public class ConstTest {

    // Task Result Constants

    @Test
    public void testTaskSuccessConstant() {
        assertEquals(0, Const.TASK_SUCCESS);
    }

    @Test
    public void testTaskErrorConstant() {
        assertEquals(1, Const.TASK_ERROR);
    }

    @Test
    public void testTaskNetworkErrorConstant() {
        assertEquals(2, Const.TASK_NETWORK_ERROR);
    }

    // Action Constants

    @Test
    public void testActionServiceStop() {
        assertEquals("SERVICE_STOP", Const.ACTION_SERVICE_STOP);
    }

    @Test
    public void testActionShowLauncher() {
        assertEquals("SHOW_LAUNCHER", Const.ACTION_SHOW_LAUNCHER);
    }

    @Test
    public void testActionEnableSettings() {
        assertEquals("ENABLE_SETTINGS", Const.ACTION_ENABLE_SETTINGS);
    }

    @Test
    public void testActionPermissiveMode() {
        assertEquals("PERMISSIVE_MODE", Const.ACTION_PERMISSIVE_MODE);
    }

    @Test
    public void testActionTogglePermissive() {
        assertEquals("TOGGLE_PERMISSIVE", Const.ACTION_TOGGLE_PERMISSIVE);
    }

    @Test
    public void testActionExitKiosk() {
        assertEquals("EXIT_KIOSK", Const.ACTION_EXIT_KIOSK);
    }

    @Test
    public void testActionAdminPanel() {
        assertEquals("ADMIN_PANEL", Const.ACTION_ADMIN_PANEL);
    }

    @Test
    public void testActionStopControl() {
        assertEquals("STOP_CONTROL", Const.ACTION_STOP_CONTROL);
    }

    @Test
    public void testActionExit() {
        assertEquals("EXIT", Const.ACTION_EXIT);
    }

    @Test
    public void testActionHideScreen() {
        assertEquals("HIDE_SCREEN", Const.ACTION_HIDE_SCREEN);
    }

    @Test
    public void testActionUpdateConfiguration() {
        assertEquals("UPDATE_CONFIGURATION", Const.ACTION_UPDATE_CONFIGURATION);
    }

    @Test
    public void testActionPolicyViolation() {
        assertEquals("ACTION_POLICY_VIOLATION", Const.ACTION_POLICY_VIOLATION);
    }

    @Test
    public void testActionAdmin() {
        assertEquals("ADMIN", Const.ACTION_ADMIN);
    }

    @Test
    public void testActionInstallComplete() {
        assertEquals("INSTALL_COMPLETE", Const.ACTION_INSTALL_COMPLETE);
    }

    @Test
    public void testActionDisableBlockWindow() {
        assertEquals("DISABLE_BLOCK_WINDOW", Const.ACTION_DISABLE_BLOCK_WINDOW);
    }

    // Extra Constants

    @Test
    public void testExtraEnabled() {
        assertEquals("ENABLED", Const.EXTRA_ENABLED);
    }

    // Status Constants

    @Test
    public void testStatusOk() {
        assertEquals("OK", Const.STATUS_OK);
    }

    // Preferences Constants

    @Test
    public void testPreferences() {
        assertEquals("PREFERENCES", Const.PREFERENCES);
    }

    @Test
    public void testPreferencesOn() {
        assertEquals(1, Const.PREFERENCES_ON);
    }

    @Test
    public void testPreferencesOff() {
        assertEquals(0, Const.PREFERENCES_OFF);
    }

    @Test
    public void testPreferencesAdministrator() {
        assertEquals("PREFERENCES_ADMINISTRATOR", Const.PREFERENCES_ADMINISTRATOR);
    }

    @Test
    public void testPreferencesOverlay() {
        assertEquals("PREFERENCES_OVERLAY", Const.PREFERENCES_OVERLAY);
    }

    @Test
    public void testPreferencesUsageStatistics() {
        assertEquals("PREFERENCES_USAGE_STATISTICS", Const.PREFERENCES_USAGE_STATISTICS);
    }

    @Test
    public void testPreferencesManageStorage() {
        assertEquals("PREFERENCES_MANAGE_STORAGE", Const.PREFERENCES_MANAGE_STORAGE);
    }

    @Test
    public void testPreferencesAccessibilityService() {
        assertEquals("PREFERENCES_ACCESSIBILITY_SERVICE", Const.PREFERENCES_ACCESSIBILITY_SERVICE);
    }

    @Test
    public void testPreferencesDeviceOwner() {
        assertEquals("PREFERENCES_DEVICE_OWNER", Const.PREFERENCES_DEVICE_OWNER);
    }

    // Log Constants

    @Test
    public void testLogTag() {
        assertEquals("HeadwindMDM", Const.LOG_TAG);
    }

    @Test
    public void testLogError() {
        assertEquals(1, Const.LOG_ERROR);
    }

    @Test
    public void testLogWarn() {
        assertEquals(2, Const.LOG_WARN);
    }

    @Test
    public void testLogInfo() {
        assertEquals(3, Const.LOG_INFO);
    }

    @Test
    public void testLogDebug() {
        assertEquals(4, Const.LOG_DEBUG);
    }

    @Test
    public void testLogVerbose() {
        assertEquals(5, Const.LOG_VERBOSE);
    }

    // Timeout Constants

    @Test
    public void testSettingsUnblockTime() {
        assertEquals(180000, Const.SETTINGS_UNBLOCK_TIME);
    }

    @Test
    public void testPermissiveModeTime() {
        assertEquals(180000, Const.PERMISSIVE_MODE_TIME);
    }

    // Connection Constants

    @Test
    public void testConnectionTimeout() {
        assertEquals(10000, Const.CONNECTION_TIMEOUT);
    }

    @Test
    public void testLongPollingReadTimeout() {
        assertEquals(300000, Const.LONG_POLLING_READ_TIMEOUT);
    }

    // System Package Constants

    @Test
    public void testSettingsPackageName() {
        assertEquals("com.android.settings", Const.SETTINGS_PACKAGE_NAME);
    }

    @Test
    public void testGsfPackageName() {
        assertEquals("com.google.android.gsf", Const.GSF_PACKAGE_NAME);
    }

    @Test
    public void testSystemUiPackageName() {
        assertEquals("com.android.systemui", Const.SYSTEM_UI_PACKAGE_NAME);
    }

    @Test
    public void testKioskBrowserPackageName() {
        assertEquals("com.hmdm.kiosk", Const.KIOSK_BROWSER_PACKAGE_NAME);
    }

    @Test
    public void testApuppetPackageName() {
        assertEquals("com.hmdm.control", Const.APUPPET_PACKAGE_NAME);
    }

    // QR Code Constants

    @Test
    public void testQrBaseUrlAttr() {
        assertEquals("com.hmdm.BASE_URL", Const.QR_BASE_URL_ATTR);
    }

    @Test
    public void testQrSecondaryBaseUrlAttr() {
        assertEquals("com.hmdm.SECONDARY_BASE_URL", Const.QR_SECONDARY_BASE_URL_ATTR);
    }

    @Test
    public void testQrServerProjectAttr() {
        assertEquals("com.hmdm.SERVER_PROJECT", Const.QR_SERVER_PROJECT_ATTR);
    }

    @Test
    public void testQrDeviceIdAttr() {
        assertEquals("com.hmdm.DEVICE_ID", Const.QR_DEVICE_ID_ATTR);
    }

    @Test
    public void testQrLegacyDeviceIdAttr() {
        assertEquals("ru.headwind.kiosk.DEVICE_ID", Const.QR_LEGACY_DEVICE_ID_ATTR);
    }

    @Test
    public void testQrDeviceIdUseAttr() {
        assertEquals("com.hmdm.DEVICE_ID_USE", Const.QR_DEVICE_ID_USE_ATTR);
    }

    @Test
    public void testQrCustomerAttr() {
        assertEquals("com.hmdm.CUSTOMER", Const.QR_CUSTOMER_ATTR);
    }

    @Test
    public void testQrConfigAttr() {
        assertEquals("com.hmdm.CONFIG", Const.QR_CONFIG_ATTR);
    }

    @Test
    public void testQrGroupAttr() {
        assertEquals("com.hmdm.GROUP", Const.QR_GROUP_ATTR);
    }

    // Kiosk Unlock Constants

    @Test
    public void testKioskUnlockClickCount() {
        assertEquals(4, Const.KIOSK_UNLOCK_CLICK_COUNT);
    }

    // WiFi State Constants

    @Test
    public void testWifiStateFailed() {
        assertEquals("failed", Const.WIFI_STATE_FAILED);
    }

    @Test
    public void testWifiStateInactive() {
        assertEquals("inactive", Const.WIFI_STATE_INACTIVE);
    }

    @Test
    public void testWifiStateScanning() {
        assertEquals("scanning", Const.WIFI_STATE_SCANNING);
    }

    @Test
    public void testWifiStateDisconnected() {
        assertEquals("disconnected", Const.WIFI_STATE_DISCONNECTED);
    }

    @Test
    public void testWifiStateConnecting() {
        assertEquals("connecting", Const.WIFI_STATE_CONNECTING);
    }

    @Test
    public void testWifiStateConnected() {
        assertEquals("connected", Const.WIFI_STATE_CONNECTED);
    }

    // GPS State Constants

    @Test
    public void testGpsStateInactive() {
        assertEquals("inactive", Const.GPS_STATE_INACTIVE);
    }

    @Test
    public void testGpsStateLost() {
        assertEquals("lost", Const.GPS_STATE_LOST);
    }

    @Test
    public void testGpsStateActive() {
        assertEquals("active", Const.GPS_STATE_ACTIVE);
    }

    // Mobile State Constants

    @Test
    public void testMobileStateInactive() {
        assertEquals("inactive", Const.MOBILE_STATE_INACTIVE);
    }

    @Test
    public void testMobileStateDisconnected() {
        assertEquals("disconnected", Const.MOBILE_STATE_DISCONNECTED);
    }

    @Test
    public void testMobileStateConnected() {
        assertEquals("connected", Const.MOBILE_STATE_CONNECTED);
    }

    // Mobile SIM State Constants

    @Test
    public void testMobileSimStateUnknown() {
        assertEquals("unknown", Const.MOBILE_SIMSTATE_UNKNOWN);
    }

    @Test
    public void testMobileSimStateAbsent() {
        assertEquals("absent", Const.MOBILE_SIMSTATE_ABSENT);
    }

    @Test
    public void testMobileSimStateReady() {
        assertEquals("ready", Const.MOBILE_SIMSTATE_READY);
    }

    @Test
    public void testMobileSimStateError() {
        assertEquals("error", Const.MOBILE_SIMSTATE_ERROR);
    }

    // Battery Charging Constants

    @Test
    public void testDeviceChargingUsb() {
        assertEquals("usb", Const.DEVICE_CHARGING_USB);
    }

    @Test
    public void testDeviceChargingAc() {
        assertEquals("ac", Const.DEVICE_CHARGING_AC);
    }

    // Password Quality Constants

    @Test
    public void testPasswordQualityPresent() {
        assertEquals("present", Const.PASSWORD_QUALITY_PRESENT);
    }

    @Test
    public void testPasswordQualityEasy() {
        assertEquals("easy", Const.PASSWORD_QUALITY_EASY);
    }

    @Test
    public void testPasswordQualityModerate() {
        assertEquals("moderate", Const.PASSWORD_QUALITY_MODERATE);
    }

    @Test
    public void testPasswordQualityStrong() {
        assertEquals("strong", Const.PASSWORD_QUALITY_STRONG);
    }

    // Header Constants

    @Test
    public void testHeaderIpAddress() {
        assertEquals("X-IP-Address", Const.HEADER_IP_ADDRESS);
    }

    @Test
    public void testHeaderResponseSignature() {
        assertEquals("X-Response-Signature", Const.HEADER_RESPONSE_SIGNATURE);
    }

    // Orientation Constants

    @Test
    public void testScreenOrientationPortrait() {
        assertEquals(1, Const.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Test
    public void testScreenOrientationLandscape() {
        assertEquals(2, Const.SCREEN_ORIENTATION_LANDSCAPE);
    }

    // Direction Constants

    @Test
    public void testDirectionLeft() {
        assertEquals(0, Const.DIRECTION_LEFT);
    }

    @Test
    public void testDirectionRight() {
        assertEquals(1, Const.DIRECTION_RIGHT);
    }

    @Test
    public void testDirectionUp() {
        assertEquals(2, Const.DIRECTION_UP);
    }

    @Test
    public void testDirectionDown() {
        assertEquals(3, Const.DIRECTION_DOWN);
    }

    // Push Keepalive Constants

    @Test
    public void testDefaultPushAlarmKeepaliveTime() {
        assertEquals(300, Const.DEFAULT_PUSH_ALARM_KEEPALIVE_TIME_SEC);
    }

    @Test
    public void testDefaultPushWorkerKeepaliveTime() {
        assertEquals(900, Const.DEFAULT_PUSH_WORKER_KEEPALIVE_TIME_SEC);
    }

    // Work Tag Constants

    @Test
    public void testWorkTagCommon() {
        assertEquals("com.hmdm.launcher", Const.WORK_TAG_COMMON);
    }
}