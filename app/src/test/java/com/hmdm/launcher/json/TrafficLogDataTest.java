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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Unit tests for TrafficLogData DTO class.
 */
public class TrafficLogDataTest {

    @Test
    public void testDefaultConstructor() {
        TrafficLogData data = new TrafficLogData();
        assertNull(data.getLogs());
    }

    @Test
    public void testParameterizedConstructor() {
        List<TrafficLogData.TrafficLogEntry> logs = new ArrayList<>();
        TrafficLogData data = new TrafficLogData(logs);

        assertEquals(logs, data.getLogs());
    }

    @Test
    public void testSetterGetter() {
        TrafficLogData data = new TrafficLogData();
        List<TrafficLogData.TrafficLogEntry> logs = new ArrayList<>();
        data.setLogs(logs);

        assertEquals(logs, data.getLogs());
    }

    @Test
    public void testTrafficLogEntryDefaultConstructor() {
        TrafficLogData.TrafficLogEntry entry = new TrafficLogData.TrafficLogEntry();
        assertEquals(0, entry.getTimestamp());
        assertNull(entry.getPackageName());
        assertNull(entry.getHost());
        assertEquals(0, entry.getPort());
        assertEquals(0, entry.getBytesIn());
        assertEquals(0, entry.getBytesOut());
    }

    @Test
    public void testTrafficLogEntryParameterizedConstructor() {
        TrafficLogData.TrafficLogEntry entry = new TrafficLogData.TrafficLogEntry(
            1700000000000L, "com.example.app", "example.com", 443, 1024, 2048
        );

        assertEquals(1700000000000L, entry.getTimestamp());
        assertEquals("com.example.app", entry.getPackageName());
        assertEquals("example.com", entry.getHost());
        assertEquals(443, entry.getPort());
        assertEquals(1024, entry.getBytesIn());
        assertEquals(2048, entry.getBytesOut());
    }

    @Test
    public void testTrafficLogEntrySetterGetter() {
        TrafficLogData.TrafficLogEntry entry = new TrafficLogData.TrafficLogEntry();

        entry.setTimestamp(1700001000000L);
        entry.setPackageName("com.test.app");
        entry.setHost("test.com");
        entry.setPort(80);
        entry.setBytesIn(512);
        entry.setBytesOut(256);

        assertEquals(1700001000000L, entry.getTimestamp());
        assertEquals("com.test.app", entry.getPackageName());
        assertEquals("test.com", entry.getHost());
        assertEquals(80, entry.getPort());
        assertEquals(512, entry.getBytesIn());
        assertEquals(256, entry.getBytesOut());
    }

    @Test
    public void testTrafficLogDataWithMultipleEntries() {
        List<TrafficLogData.TrafficLogEntry> logs = new ArrayList<>();
        logs.add(new TrafficLogData.TrafficLogEntry(1700000000000L, "com.app1", "app1.com", 443, 100, 200));
        logs.add(new TrafficLogData.TrafficLogEntry(1700000001000L, "com.app2", "app2.com", 80, 300, 400));
        logs.add(new TrafficLogData.TrafficLogEntry(1700000002000L, "com.app3", "app3.com", 8080, 500, 600));

        TrafficLogData data = new TrafficLogData(logs);

        assertEquals(3, data.getLogs().size());
        assertEquals("com.app1", data.getLogs().get(0).getPackageName());
        assertEquals("com.app2", data.getLogs().get(1).getPackageName());
        assertEquals("com.app3", data.getLogs().get(2).getPackageName());
    }

    @Test
    public void testEmptyLogsList() {
        TrafficLogData data = new TrafficLogData(new ArrayList<>());
        assertTrue(data.getLogs().isEmpty());
    }
}