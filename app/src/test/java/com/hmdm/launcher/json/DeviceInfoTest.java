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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for DeviceInfo class.
 */
public class DeviceInfoTest {

    @Test
    public void testDefaultConstructor() {
        DeviceInfo info = new DeviceInfo();

        assertNull(info.getModel());
        assertNull(info.getDeviceId());
        assertNull(info.getPhone());
        assertNull(info.getImei());
        assertFalse(info.isMdmMode());
        assertFalse(info.isKioskMode());
        assertEquals(0, info.getBatteryLevel());
        assertNull(info.isBatteryCharging());
        assertNull(info.getAndroidVersion());
    }

    @Test
    public void testSetterGetter() {
        DeviceInfo info = new DeviceInfo();

        info.setModel("Pixel 7");
        info.setDeviceId("device123");
        info.setPhone("+1234567890");
        info.setImei("123456789012345");
        info.setMdmMode(true);
        info.setKioskMode(true);
        info.setBatteryLevel(85);
        info.setBatteryCharging("USB");
        info.setAndroidVersion("14");

        assertEquals("Pixel 7", info.getModel());
        assertEquals("device123", info.getDeviceId());
        assertEquals("+1234567890", info.getPhone());
        assertEquals("123456789012345", info.getImei());
        assertTrue(info.isMdmMode());
        assertTrue(info.isKioskMode());
        assertEquals(85, info.getBatteryLevel());
        assertEquals("USB", info.isBatteryCharging());
        assertEquals("14", info.getAndroidVersion());
    }

    @Test
    public void testPermissionsList() {
        DeviceInfo info = new DeviceInfo();

        List<Integer> permissions = new ArrayList<>();
        permissions.add(1);
        permissions.add(2);
        permissions.add(3);

        info.setPermissions(permissions);

        assertEquals(3, info.getPermissions().size());
        assertEquals(1, (int) info.getPermissions().get(0));
    }

    @Test
    public void testApplicationsList() {
        DeviceInfo info = new DeviceInfo();

        List<Application> apps = new ArrayList<>();
        Application app1 = new Application();
        app1.setPkg("com.example.app1");
        Application app2 = new Application();
        app2.setPkg("com.example.app2");
        apps.add(app1);
        apps.add(app2);

        info.setApplications(apps);

        assertEquals(2, info.getApplications().size());
        assertEquals("com.example.app1", info.getApplications().get(0).getPkg());
    }

    @Test
    public void testSimCardFields() {
        DeviceInfo info = new DeviceInfo();

        info.setImsi("460001234567890");
        info.setIccid("89860012345678901");
        info.setPhone2("+0987654321");
        info.setImei2("987654321098765");
        info.setImsi2("460009876543210");
        info.setIccid2("89860098765432109");

        assertEquals("460001234567890", info.getImsi());
        assertEquals("89860012345678901", info.getIccid());
        assertEquals("+0987654321", info.getPhone2());
        assertEquals("987654321098765", info.getImei2());
        assertEquals("460009876543210", info.getImsi2());
        assertEquals("89860098765432109", info.getIccid2());
    }

    @Test
    public void testDeviceIdentifiers() {
        DeviceInfo info = new DeviceInfo();

        info.setCpu("arm64-v8a");
        info.setSerial("ABC123456789");
        info.setCustom1("customValue1");
        info.setCustom2("customValue2");

        assertEquals("arm64-v8a", info.getCpu());
        assertEquals("ABC123456789", info.getSerial());
        assertEquals("customValue1", info.getCustom1());
        assertEquals("customValue2", info.getCustom2());
    }

    @Test
    public void testLocation() {
        DeviceInfo info = new DeviceInfo();

        DeviceInfo.Location location = new DeviceInfo.Location();
        location.setTs(1700000000000L);
        location.setLat(22.54321);
        location.setLon(114.12345);

        info.setLocation(location);

        assertEquals(1700000000000L, location.getTs());
        assertEquals(22.54321, location.getLat(), 0.0001);
        assertEquals(114.12345, location.getLon(), 0.0001);
    }

    @Test
    public void testLauncherInfo() {
        DeviceInfo info = new DeviceInfo();

        info.setLauncherType("launcher");
        info.setLauncherPackage("com.hmdm.launcher");
        info.setDefaultLauncher(true);

        assertEquals("launcher", info.getLauncherType());
        assertEquals("com.hmdm.launcher", info.getLauncherPackage());
        assertTrue(info.isDefaultLauncher());
    }

    @Test
    public void testFactoryReset() {
        DeviceInfo info = new DeviceInfo();

        info.setFactoryReset(true);
        assertTrue(info.getFactoryReset());

        info.setFactoryReset(false);
        assertFalse(info.getFactoryReset());

        info.setFactoryReset(null);
        assertNull(info.getFactoryReset());
    }

    @Test
    public void testEmptyListsAreInitialized() {
        DeviceInfo info = new DeviceInfo();

        // Verify lists are not null when accessed
        assertTrue(info.getPermissions() != null);
        assertTrue(info.getApplications() != null);
        assertTrue(info.getFiles() != null);
    }

    @Test
    public void testBatteryLevelRange() {
        DeviceInfo info = new DeviceInfo();

        info.setBatteryLevel(0);
        assertEquals(0, info.getBatteryLevel());

        info.setBatteryLevel(50);
        assertEquals(50, info.getBatteryLevel());

        info.setBatteryLevel(100);
        assertEquals(100, info.getBatteryLevel());
    }
}