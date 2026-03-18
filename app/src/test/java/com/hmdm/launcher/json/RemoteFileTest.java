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
 * Unit tests for RemoteFile class - used by file management and remote operations.
 */
public class RemoteFileTest {

    @Test
    public void testDefaultConstructor() {
        RemoteFile file = new RemoteFile();
        assertNotNull(file);
    }

    @Test
    public void testFileId() {
        RemoteFile file = new RemoteFile();
        file.setId(1);
        assertEquals(1, file.getId());
    }

    @Test
    public void testFileIdDefault() {
        RemoteFile file = new RemoteFile();
        assertEquals(0, file.getId());
    }

    @Test
    public void testFileUrl() {
        RemoteFile file = new RemoteFile();
        file.setUrl("https://example.com/files/example.apk");
        assertEquals("https://example.com/files/example.apk", file.getUrl());
    }

    @Test
    public void testFileUrlNull() {
        RemoteFile file = new RemoteFile();
        assertNull(file.getUrl());
    }

    @Test
    public void testFileChecksum() {
        RemoteFile file = new RemoteFile();
        file.setChecksum("abc123def456");
        assertEquals("abc123def456", file.getChecksum());
    }

    @Test
    public void testFileChecksumNull() {
        RemoteFile file = new RemoteFile();
        assertNull(file.getChecksum());
    }

    @Test
    public void testFileLastUpdate() {
        RemoteFile file = new RemoteFile();
        file.setLastUpdate(1700000000000L);
        assertEquals(1700000000000L, file.getLastUpdate());
    }

    @Test
    public void testFileLastUpdateDefault() {
        RemoteFile file = new RemoteFile();
        assertEquals(0, file.getLastUpdate());
    }

    @Test
    public void testFilePath() {
        RemoteFile file = new RemoteFile();
        file.setPath("/sdcard/Download/example.apk");
        assertEquals("/sdcard/Download/example.apk", file.getPath());
    }

    @Test
    public void testFilePathNull() {
        RemoteFile file = new RemoteFile();
        assertNull(file.getPath());
    }

    @Test
    public void testFileDescription() {
        RemoteFile file = new RemoteFile();
        file.setDescription("Example application");
        assertEquals("Example application", file.getDescription());
    }

    @Test
    public void testFileDescriptionNull() {
        RemoteFile file = new RemoteFile();
        assertNull(file.getDescription());
    }

    @Test
    public void testFileRemove() {
        RemoteFile file = new RemoteFile();
        file.setRemove(true);
        // This is a boolean primitive, check that setter doesn't throw
        assertNotNull(file);
    }

    @Test
    public void testFullRemoteFile() {
        RemoteFile file = new RemoteFile();
        file.setId(100);
        file.setUrl("https://example.com/test.apk");
        file.setChecksum("sha256hash123");
        file.setLastUpdate(1700000000000L);
        file.setPath("/sdcard/Download/test.apk");
        file.setDescription("Test application");

        assertEquals(100, file.getId());
        assertEquals("https://example.com/test.apk", file.getUrl());
        assertEquals("sha256hash123", file.getChecksum());
        assertEquals(1700000000000L, file.getLastUpdate());
        assertEquals("/sdcard/Download/test.apk", file.getPath());
        assertEquals("Test application", file.getDescription());
    }
}