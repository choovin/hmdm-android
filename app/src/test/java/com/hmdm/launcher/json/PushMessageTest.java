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
 * Unit tests for PushMessage class.
 */
public class PushMessageTest {

    @Test
    public void testDefaultConstructor() {
        PushMessage message = new PushMessage();
        assertNull(message.getMessageType());
        assertNull(message.getPayload());
    }

    @Test
    public void testSetterGetter() {
        PushMessage message = new PushMessage();

        message.setMessageType(PushMessage.TYPE_REBOOT);
        message.setPayload("immediate");

        assertEquals(PushMessage.TYPE_REBOOT, message.getMessageType());
        assertEquals("immediate", message.getPayload());
    }

    @Test
    public void testTypeConstantsExist() {
        // Verify all type constants are defined
        assertEquals("configUpdating", PushMessage.TYPE_CONFIG_UPDATING);
        assertEquals("configUpdated", PushMessage.TYPE_CONFIG_UPDATED);
        assertEquals("runApp", PushMessage.TYPE_RUN_APP);
        assertEquals("broadcast", PushMessage.TYPE_BROADCAST);
        assertEquals("uninstallApp", PushMessage.TYPE_UNINSTALL_APP);
        assertEquals("deleteFile", PushMessage.TYPE_DELETE_FILE);
        assertEquals("purgeDir", PushMessage.TYPE_PURGE_DIR);
        assertEquals("deleteDir", PushMessage.TYPE_DELETE_DIR);
        assertEquals("permissiveMode", PushMessage.TYPE_PERMISSIVE_MODE);
        assertEquals("runCommand", PushMessage.TYPE_RUN_COMMAND);
        assertEquals("reboot", PushMessage.TYPE_REBOOT);
        assertEquals("exitKiosk", PushMessage.TYPE_EXIT_KIOSK);
        assertEquals("clearDownloadHistory", PushMessage.TYPE_CLEAR_DOWNLOADS);
        assertEquals("intent", PushMessage.TYPE_INTENT);
        assertEquals("grantPermissions", PushMessage.TYPE_GRANT_PERMISSIONS);
        assertEquals("adminPanel", PushMessage.TYPE_ADMIN_PANEL);
        assertEquals("clearAppData", PushMessage.TYPE_CLEAR_APP_DATA);
        assertEquals("lock", PushMessage.TYPE_LOCK);
    }

    @Test
    public void testMessageTypeCanBeNull() {
        PushMessage message = new PushMessage();
        message.setMessageType(null);
        assertNull(message.getMessageType());
    }

    @Test
    public void testPayloadCanBeNull() {
        PushMessage message = new PushMessage();
        message.setPayload(null);
        assertNull(message.getPayload());
    }

    @Test
    public void testPayloadWithJsonString() {
        PushMessage message = new PushMessage();
        message.setMessageType(PushMessage.TYPE_RUN_APP);
        message.setPayload("{\"package\":\"com.example.app\",\"action\":\"start\"}");

        assertEquals(PushMessage.TYPE_RUN_APP, message.getMessageType());
        assertEquals("{\"package\":\"com.example.app\",\"action\":\"start\"}", message.getPayload());
    }

    @Test
    public void testDifferentMessageTypes() {
        PushMessage msg1 = new PushMessage();
        msg1.setMessageType(PushMessage.TYPE_REBOOT);

        PushMessage msg2 = new PushMessage();
        msg2.setMessageType(PushMessage.TYPE_LOCK);

        PushMessage msg3 = new PushMessage();
        msg3.setMessageType(PushMessage.TYPE_RUN_COMMAND);

        assertEquals(PushMessage.TYPE_REBOOT, msg1.getMessageType());
        assertEquals(PushMessage.TYPE_LOCK, msg2.getMessageType());
        assertEquals(PushMessage.TYPE_RUN_COMMAND, msg3.getMessageType());
    }
}