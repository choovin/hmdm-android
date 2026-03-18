/*
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC
 *
 * Licensed under the Apache License, Version 2.0
 */

package com.hmdm.launcher.db;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for LocationTable.Location class.
 */
public class LocationTableTest {

    @Test
    public void testLocationCreation() {
        LocationTable.Location location = new LocationTable.Location();
        assertNotNull(location);
    }

    @Test
    public void testLocationSettersAndGetters() {
        LocationTable.Location location = new LocationTable.Location();

        location.setId(1);
        location.setTs(1700000000000L);
        location.setLat(22.54321);
        location.setLon(114.12345);
        location.setSpeed(5.5f);
        location.setAltitude(100.0f);
        location.setAccuracy(10.0f);
        location.setBearing(180.0f);
        location.setProvider("gps");

        assertEquals(1, location.getId());
        assertEquals(1700000000000L, location.getTs());
        assertEquals(22.54321, location.getLat(), 0.0001);
        assertEquals(114.12345, location.getLon(), 0.0001);
        assertEquals(5.5f, location.getSpeed(), 0.001);
        assertEquals(100.0f, location.getAltitude(), 0.001);
        assertEquals(10.0f, location.getAccuracy(), 0.001);
        assertEquals(180.0f, location.getBearing(), 0.001);
        assertEquals("gps", location.getProvider());
    }

    @Test
    public void testLocationDefaultValues() {
        LocationTable.Location location = new LocationTable.Location();

        assertEquals(0, location.getId());
        assertEquals(0, location.getTs());
        assertEquals(0.0, location.getLat(), 0.0001);
        assertEquals(0.0, location.getLon(), 0.0001);
        assertEquals(0.0f, location.getSpeed(), 0.001);
        assertEquals(0.0f, location.getAltitude(), 0.001);
        assertEquals(0.0f, location.getAccuracy(), 0.001);
        assertEquals(0.0f, location.getBearing(), 0.001);
        assertNull(location.getProvider());
    }

    @Test
    public void testLocationSetNullProvider() {
        LocationTable.Location location = new LocationTable.Location();
        location.setProvider(null);
        assertNull(location.getProvider());
    }

    @Test
    public void testLocationWithAllFields() {
        LocationTable.Location location = new LocationTable.Location();

        long timestamp = System.currentTimeMillis();
        location.setId(100);
        location.setTs(timestamp);
        location.setLat(39.9042);
        location.setLon(116.4074);
        location.setSpeed(15.5f);
        location.setAltitude(50.0f);
        location.setAccuracy(5.0f);
        location.setBearing(90.0f);
        location.setProvider("network");

        assertEquals(100, location.getId());
        assertEquals(timestamp, location.getTs());
        assertEquals(39.9042, location.getLat(), 0.0001);
        assertEquals(116.4074, location.getLon(), 0.0001);
        assertEquals(15.5f, location.getSpeed(), 0.001);
        assertEquals(50.0f, location.getAltitude(), 0.001);
        assertEquals(5.0f, location.getAccuracy(), 0.001);
        assertEquals(90.0f, location.getBearing(), 0.001);
        assertEquals("network", location.getProvider());
    }

    @Test
    public void testGetCreateTableSql() {
        String sql = LocationTable.getCreateTableSql();
        assertNotNull(sql);
        assertTrue(sql.contains("CREATE TABLE"));
        assertTrue(sql.contains("locations"));
        assertTrue(sql.contains("ts INTEGER"));
        assertTrue(sql.contains("lat REAL"));
        assertTrue(sql.contains("lon REAL"));
        assertTrue(sql.contains("provider TEXT"));
    }

    @Test
    public void testLocationCoordinatesBoundary() {
        LocationTable.Location location = new LocationTable.Location();

        // Test max latitude
        location.setLat(90.0);
        assertEquals(90.0, location.getLat(), 0.0001);

        // Test min latitude
        location.setLat(-90.0);
        assertEquals(-90.0, location.getLat(), 0.0001);

        // Test max longitude
        location.setLon(180.0);
        assertEquals(180.0, location.getLon(), 0.0001);

        // Test min longitude
        location.setLon(-180.0);
        assertEquals(-180.0, location.getLon(), 0.0001);
    }

    @Test
    public void testLocationNegativeValues() {
        LocationTable.Location location = new LocationTable.Location();

        location.setLat(-33.8688);
        location.setLon(151.2093);
        location.setSpeed(-1.0f); // Negative speed should be allowed in some cases
        location.setAltitude(-10.0f); // Below sea level

        assertEquals(-33.8688, location.getLat(), 0.0001);
        assertEquals(151.2093, location.getLon(), 0.0001);
        assertEquals(-1.0f, location.getSpeed(), 0.001);
        assertEquals(-10.0f, location.getAltitude(), 0.001);
    }

    @Test
    public void testLocationZeroValues() {
        LocationTable.Location location = new LocationTable.Location();

        location.setLat(0.0);
        location.setLon(0.0);
        location.setSpeed(0.0f);
        location.setAltitude(0.0f);
        location.setAccuracy(0.0f);
        location.setBearing(0.0f);

        assertEquals(0.0, location.getLat(), 0.0001);
        assertEquals(0.0, location.getLon(), 0.0001);
        assertEquals(0.0f, location.getSpeed(), 0.001);
        assertEquals(0.0f, location.getAltitude(), 0.001);
        assertEquals(0.0f, location.getAccuracy(), 0.001);
        assertEquals(0.0f, location.getBearing(), 0.001);
    }
}