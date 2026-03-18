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
 * Unit tests for Action class - used by kiosk mode and remote commands.
 */
public class ActionTest {

    @Test
    public void testDefaultConstructor() {
        Action action = new Action();
        assertNotNull(action);
    }

    @Test
    public void testActionAction() {
        Action action = new Action();
        action.setAction("install");
        assertEquals("install", action.getAction());
    }

    @Test
    public void testActionActionNull() {
        Action action = new Action();
        assertNull(action.getAction());
    }

    @Test
    public void testCategories() {
        Action action = new Action();
        action.setCategories("category1,category2");
        assertEquals("category1,category2", action.getCategories());
    }

    @Test
    public void testCategoriesNull() {
        Action action = new Action();
        assertNull(action.getCategories());
    }

    @Test
    public void testPackageId() {
        Action action = new Action();
        action.setPackageId("com.example.app");
        assertEquals("com.example.app", action.getPackageId());
    }

    @Test
    public void testPackageIdNull() {
        Action action = new Action();
        assertNull(action.getPackageId());
    }

    @Test
    public void testActivity() {
        Action action = new Action();
        action.setActivity(".MainActivity");
        assertEquals(".MainActivity", action.getActivity());
    }

    @Test
    public void testActivityNull() {
        Action action = new Action();
        assertNull(action.getActivity());
    }

    @Test
    public void testSchemes() {
        Action action = new Action();
        action.setSchemes("http,https");
        assertEquals("http,https", action.getSchemes());
    }

    @Test
    public void testSchemesNull() {
        Action action = new Action();
        assertNull(action.getSchemes());
    }

    @Test
    public void testHosts() {
        Action action = new Action();
        action.setHosts("example.com");
        assertEquals("example.com", action.getHosts());
    }

    @Test
    public void testHostsNull() {
        Action action = new Action();
        assertNull(action.getHosts());
    }

    @Test
    public void testMimeTypes() {
        Action action = new Action();
        action.setMimeTypes("application/json");
        assertEquals("application/json", action.getMimeTypes());
    }

    @Test
    public void testMimeTypesNull() {
        Action action = new Action();
        assertNull(action.getMimeTypes());
    }

    @Test
    public void testFullAction() {
        Action action = new Action();
        action.setAction("view");
        action.setCategories("category1");
        action.setPackageId("com.example.newapp");
        action.setActivity(".MainActivity");
        action.setSchemes("https");
        action.setHosts("example.com");
        action.setMimeTypes("text/html");

        assertEquals("view", action.getAction());
        assertEquals("category1", action.getCategories());
        assertEquals("com.example.newapp", action.getPackageId());
        assertEquals(".MainActivity", action.getActivity());
        assertEquals("https", action.getSchemes());
        assertEquals("example.com", action.getHosts());
        assertEquals("text/html", action.getMimeTypes());
    }
}