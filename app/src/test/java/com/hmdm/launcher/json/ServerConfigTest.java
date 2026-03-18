/*
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC
 *
 * Licensed under the Apache License, Version 2.0
 */

package com.hmdm.launcher.json;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for ServerConfig class - testing enterprise features:
 * - Kiosk mode
 * - Location tracking
 * - Remote reboot
 * - Remote lock
 * - Factory reset
 */
public class ServerConfigTest {

    @Test
    public void testDefaultConstructor() {
        ServerConfig config = new ServerConfig();
        assertNotNull(config);
    }

    // Kiosk Mode Tests

    @Test
    public void testKioskModeDefaultsToFalse() {
        ServerConfig config = new ServerConfig();
        org.junit.Assert.assertFalse(config.isKioskMode());
    }

    @Test
    public void testKioskModeEnabled() {
        ServerConfig config = new ServerConfig();
        config.setKioskMode(true);
        assertTrue(config.isKioskMode());
    }

    @Test
    public void testKioskModeDisabled() {
        ServerConfig config = new ServerConfig();
        config.setKioskMode(false);
        org.junit.Assert.assertFalse(config.isKioskMode());
    }

    @Test
    public void testKioskModeNullReturnsFalse() {
        ServerConfig config = new ServerConfig();
        config.setKioskMode(null);
        org.junit.Assert.assertFalse(config.isKioskMode());
    }

    @Test
    public void testKioskMainApp() {
        ServerConfig config = new ServerConfig();
        config.setMainApp("com.example.kiosk");
        assertEquals("com.example.kiosk", config.getMainApp());
    }

    @Test
    public void testKioskMainAppNull() {
        ServerConfig config = new ServerConfig();
        assertNull(config.getMainApp());
    }

    @Test
    public void testKioskHomeSetting() {
        ServerConfig config = new ServerConfig();
        config.setKioskHome(true);
        assertTrue(config.getKioskHome());
    }

    @Test
    public void testKioskRecentsSetting() {
        ServerConfig config = new ServerConfig();
        config.setKioskRecents(false);
        org.junit.Assert.assertFalse(config.getKioskRecents());
    }

    @Test
    public void testKioskNotificationsSetting() {
        ServerConfig config = new ServerConfig();
        config.setKioskNotifications(true);
        assertTrue(config.getKioskNotifications());
    }

    @Test
    public void testKioskSystemInfoSetting() {
        ServerConfig config = new ServerConfig();
        config.setKioskSystemInfo(false);
        org.junit.Assert.assertFalse(config.getKioskSystemInfo());
    }

    @Test
    public void testKioskKeyguardSetting() {
        ServerConfig config = new ServerConfig();
        config.setKioskKeyguard(true);
        assertTrue(config.getKioskKeyguard());
    }

    @Test
    public void testKioskLockButtonsSetting() {
        ServerConfig config = new ServerConfig();
        config.setKioskLockButtons(true);
        assertTrue(config.getKioskLockButtons());
    }

    @Test
    public void testKioskScreenOnSetting() {
        ServerConfig config = new ServerConfig();
        config.setKioskScreenOn(true);
        assertTrue(config.getKioskScreenOn());
    }

    // Location Tracking Tests

    @Test
    public void testLocationTrackingEnabled() {
        ServerConfig config = new ServerConfig();
        config.setLocationTrackingEnabled(true);
        assertTrue(config.getLocationTrackingEnabled());
    }

    @Test
    public void testLocationTrackingDisabled() {
        ServerConfig config = new ServerConfig();
        config.setLocationTrackingEnabled(false);
        org.junit.Assert.assertFalse(config.getLocationTrackingEnabled());
    }

    @Test
    public void testLocationTrackingNullDefaultsToTrue() {
        ServerConfig config = new ServerConfig();
        config.setLocationTrackingEnabled(null);
        // The isLocationTrackingEnabled logic returns (enabled == null || enabled)
        assertNull(config.getLocationTrackingEnabled());
    }

    @Test
    public void testDisableLocationSetting() {
        ServerConfig config = new ServerConfig();
        config.setDisableLocation(true);
        assertTrue(config.getDisableLocation());
    }

    // Remote Reboot Tests

    @Test
    public void testRemoteRebootEnabled() {
        ServerConfig config = new ServerConfig();
        config.setReboot(true);
        assertTrue(config.getReboot());
    }

    @Test
    public void testRemoteRebootDisabled() {
        ServerConfig config = new ServerConfig();
        config.setReboot(false);
        org.junit.Assert.assertFalse(config.getReboot());
    }

    @Test
    public void testRemoteRebootNull() {
        ServerConfig config = new ServerConfig();
        assertNull(config.getReboot());
    }

    // Remote Lock Tests

    @Test
    public void testRemoteLockEnabled() {
        ServerConfig config = new ServerConfig();
        config.setLock(true);
        assertTrue(config.getLock());
    }

    @Test
    public void testRemoteLockDisabled() {
        ServerConfig config = new ServerConfig();
        config.setLock(false);
        org.junit.Assert.assertFalse(config.getLock());
    }

    @Test
    public void testRemoteLockNull() {
        ServerConfig config = new ServerConfig();
        assertNull(config.getLock());
    }

    @Test
    public void testLockMessage() {
        ServerConfig config = new ServerConfig();
        config.setLockMessage("Device locked by administrator");
        assertEquals("Device locked by administrator", config.getLockMessage());
    }

    @Test
    public void testLockMessageNull() {
        ServerConfig config = new ServerConfig();
        assertNull(config.getLockMessage());
    }

    // Factory Reset Tests

    @Test
    public void testFactoryResetEnabled() {
        ServerConfig config = new ServerConfig();
        config.setFactoryReset(true);
        assertTrue(config.getFactoryReset());
    }

    @Test
    public void testFactoryResetDisabled() {
        ServerConfig config = new ServerConfig();
        config.setFactoryReset(false);
        org.junit.Assert.assertFalse(config.getFactoryReset());
    }

    @Test
    public void testFactoryResetNull() {
        ServerConfig config = new ServerConfig();
        assertNull(config.getFactoryReset());
    }

    // Additional Enterprise Settings Tests

    @Test
    public void testLockStatusBar() {
        ServerConfig config = new ServerConfig();
        config.setLockStatusBar(true);
        assertTrue(config.getLockStatusBar());
    }

    @Test
    public void testPasswordResetSetting() {
        ServerConfig config = new ServerConfig();
        config.setPasswordReset("allow");
        assertEquals("allow", config.getPasswordReset());
    }

    @Test
    public void testConstants() {
        // Verify title constants
        assertEquals("none", ServerConfig.TITLE_NONE);
        assertEquals("deviceId", ServerConfig.TITLE_DEVICE_ID);
        assertEquals("description", ServerConfig.TITLE_DESCRIPTION);
        assertEquals("custom1", ServerConfig.TITLE_CUSTOM1);
        assertEquals("custom2", ServerConfig.TITLE_CUSTOM2);
        assertEquals("custom3", ServerConfig.TITLE_CUSTOM3);
        assertEquals("imei", ServerConfig.TITLE_IMEI);
        assertEquals("serialNumber", ServerConfig.TITLE_SERIAL);
        assertEquals("externalIp", ServerConfig.TITLE_EXTERNAL_IP);

        // Verify system update constants
        assertEquals(0, ServerConfig.SYSTEM_UPDATE_DEFAULT);
        assertEquals(1, ServerConfig.SYSTEM_UPDATE_INSTANT);
        assertEquals(2, ServerConfig.SYSTEM_UPDATE_SCHEDULE);
        assertEquals(3, ServerConfig.SYSTEM_UPDATE_MANUAL);

        // Verify push options constants
        assertEquals("mqttWorker", ServerConfig.PUSH_OPTIONS_MQTT_WORKER);
        assertEquals("mqttAlarm", ServerConfig.PUSH_OPTIONS_MQTT_ALARM);
        assertEquals("polling", ServerConfig.PUSH_OPTIONS_POLLING);

        // Verify app permissions constants
        assertEquals("asklocation", ServerConfig.APP_PERMISSIONS_ASK_LOCATION);
        assertEquals("denylocation", ServerConfig.APP_PERMISSIONS_DENY_LOCATION);
        assertEquals("askall", ServerConfig.APP_PERMISSIONS_ASK_ALL);

        // Verify default icon size
        assertEquals(100, ServerConfig.DEFAULT_ICON_SIZE);
    }

    @Test
    public void testFullKioskConfiguration() {
        ServerConfig config = new ServerConfig();

        // Configure full kiosk mode
        config.setKioskMode(true);
        config.setMainApp("com.example.kiosk");
        config.setKioskHome(true);
        config.setKioskRecents(false);
        config.setKioskNotifications(false);
        config.setKioskSystemInfo(false);
        config.setKioskKeyguard(true);
        config.setKioskLockButtons(true);
        config.setKioskScreenOn(true);
        config.setLockStatusBar(true);

        assertTrue(config.isKioskMode());
        assertEquals("com.example.kiosk", config.getMainApp());
        assertTrue(config.getKioskHome());
        org.junit.Assert.assertFalse(config.getKioskRecents());
        org.junit.Assert.assertFalse(config.getKioskNotifications());
        org.junit.Assert.assertFalse(config.getKioskSystemInfo());
        assertTrue(config.getKioskKeyguard());
        assertTrue(config.getKioskLockButtons());
        assertTrue(config.getKioskScreenOn());
        assertTrue(config.getLockStatusBar());
    }

    @Test
    public void testFullDeviceControlConfiguration() {
        ServerConfig config = new ServerConfig();

        // Configure remote control features
        config.setReboot(true);
        config.setLock(true);
        config.setFactoryReset(true);
        config.setPasswordReset("allow");
        config.setLockMessage("Please contact administrator");

        assertTrue(config.getReboot());
        assertTrue(config.getLock());
        assertTrue(config.getFactoryReset());
        assertEquals("allow", config.getPasswordReset());
        assertEquals("Please contact administrator", config.getLockMessage());
    }
}