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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for ControlSignal DTO class.
 */
public class ControlSignalTest {

    @Test
    public void testDefaultConstructor() {
        ControlSignal signal = new ControlSignal();
        assertNull(signal.getType());
        assertNull(signal.getData());
    }

    @Test
    public void testParameterizedConstructor() {
        Object data = new Object();
        ControlSignal signal = new ControlSignal("screenshot", data);

        assertEquals("screenshot", signal.getType());
        assertEquals(data, signal.getData());
    }

    @Test
    public void testSetterGetter() {
        ControlSignal signal = new ControlSignal();

        signal.setType("reboot");
        signal.setData("immediate");

        assertEquals("reboot", signal.getType());
        assertEquals("immediate", signal.getData());
    }

    @Test
    public void testTypeCanBeNull() {
        ControlSignal signal = new ControlSignal(null, "data");
        assertNull(signal.getType());
    }

    @Test
    public void testDataCanBeNull() {
        ControlSignal signal = new ControlSignal("type", null);
        assertNull(signal.getData());
    }

    @Test
    public void testDataCanBeString() {
        ControlSignal signal = new ControlSignal();
        signal.setType("lock");
        signal.setData("1234");

        assertEquals("lock", signal.getType());
        assertEquals("1234", signal.getData());
    }

    @Test
    public void testDataCanBeObject() {
        ControlSignal signal = new ControlSignal();
        signal.setType("config");

        Object configData = new Object();
        signal.setData(configData);

        assertEquals(configData, signal.getData());
    }
}