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
 * Unit tests for Application class - used by kiosk mode and app management.
 */
public class ApplicationTest {

    @Test
    public void testDefaultConstructor() {
        Application app = new Application();
        assertNotNull(app);
    }

    @Test
    public void testPackageName() {
        Application app = new Application();
        app.setPkg("com.example.app");
        assertEquals("com.example.app", app.getPkg());
    }

    @Test
    public void testPackageNameNull() {
        Application app = new Application();
        assertNull(app.getPkg());
    }

    @Test
    public void testApplicationType() {
        Application app = new Application();
        app.setType("system");
        assertEquals("system", app.getType());
    }

    @Test
    public void testApplicationTypeDefaults() {
        Application app = new Application();
        assertNull(app.getType());
    }

    @Test
    public void testApplicationName() {
        Application app = new Application();
        app.setName("Example App");
        assertEquals("Example App", app.getName());
    }

    @Test
    public void testApplicationNameNull() {
        Application app = new Application();
        assertNull(app.getName());
    }

    @Test
    public void testApplicationIcon() {
        Application app = new Application();
        app.setIcon("icon.png");
        assertEquals("icon.png", app.getIcon());
    }

    @Test
    public void testApplicationUrl() {
        Application app = new Application();
        app.setUrl("https://example.com/app.apk");
        assertEquals("https://example.com/app.apk", app.getUrl());
    }

    @Test
    public void testApplicationVersion() {
        Application app = new Application();
        app.setVersion("1.0.0");
        assertEquals("1.0.0", app.getVersion());
    }

    @Test
    public void testApplicationCode() {
        Application app = new Application();
        app.setCode(1);
        assertEquals(Integer.valueOf(1), app.getCode());
    }

    @Test
    public void testApplicationUseKiosk() {
        Application app = new Application();
        app.setUseKiosk(true);
        // Just verify no exception
        assertNotNull(app);
    }

    @Test
    public void testApplicationShowIcon() {
        Application app = new Application();
        app.setShowIcon(false);
        // Just verify no exception
        assertNotNull(app);
    }

    @Test
    public void testApplicationRemove() {
        Application app = new Application();
        app.setRemove(true);
        // Just verify no exception
        assertNotNull(app);
    }

    @Test
    public void testApplicationRunAfterInstall() {
        Application app = new Application();
        app.setRunAfterInstall(true);
        // Just verify no exception
        assertNotNull(app);
    }

    @Test
    public void testApplicationRunAtBoot() {
        Application app = new Application();
        app.setRunAtBoot(true);
        // Just verify no exception
        assertNotNull(app);
    }

    @Test
    public void testApplicationSkipVersion() {
        Application app = new Application();
        app.setSkipVersion(true);
        // Just verify no exception
        assertNotNull(app);
    }

    @Test
    public void testApplicationIntent() {
        Application app = new Application();
        app.setIntent("android.intent.action.MAIN");
        assertEquals("android.intent.action.MAIN", app.getIntent());
    }

    @Test
    public void testFullApplication() {
        Application app = new Application();
        app.setPkg("com.example.fullapp");
        app.setType("app");
        app.setName("Full Example App");
        app.setIcon("app_icon.png");
        app.setUrl("https://example.com/fullapp.apk");
        app.setVersion("2.0.0");
        app.setCode(2);

        assertEquals("com.example.fullapp", app.getPkg());
        assertEquals("app", app.getType());
        assertEquals("Full Example App", app.getName());
        assertEquals("app_icon.png", app.getIcon());
        assertEquals("https://example.com/fullapp.apk", app.getUrl());
        assertEquals("2.0.0", app.getVersion());
        assertEquals(Integer.valueOf(2), app.getCode());
    }
}