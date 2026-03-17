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
 * Unit tests for ServerResponse class.
 */
public class ServerResponseTest {

    @Test
    public void testDefaultConstructor() {
        ServerResponse response = new ServerResponse();
        assertNull(response.getStatus());
        assertNull(response.getMessage());
    }

    @Test
    public void testSetterGetter() {
        ServerResponse response = new ServerResponse();

        response.setStatus("OK");
        response.setStatus("ERROR");
        response.setMessage("Something went wrong");

        assertEquals("ERROR", response.getStatus());
        assertEquals("Something went wrong", response.getMessage());
    }

    @Test
    public void testOkStatus() {
        ServerResponse response = new ServerResponse();
        response.setStatus("OK");
        response.setMessage("Success");

        assertEquals("OK", response.getStatus());
        assertEquals("Success", response.getMessage());
    }

    @Test
    public void testErrorStatus() {
        ServerResponse response = new ServerResponse();
        response.setStatus("ERROR");
        response.setMessage("Device not found");

        assertEquals("ERROR", response.getStatus());
        assertEquals("Device not found", response.getMessage());
    }

    @Test
    public void testNullValues() {
        ServerResponse response = new ServerResponse();
        response.setStatus(null);
        response.setMessage(null);

        assertNull(response.getStatus());
        assertNull(response.getMessage());
    }

    @Test
    public void testEmptyMessage() {
        ServerResponse response = new ServerResponse();
        response.setStatus("OK");
        response.setMessage("");

        assertEquals("OK", response.getStatus());
        assertEquals("", response.getMessage());
    }
}