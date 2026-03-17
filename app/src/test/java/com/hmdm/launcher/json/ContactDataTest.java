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

/**
 * Unit tests for ContactData DTO class.
 */
public class ContactDataTest {

    @Test
    public void testDefaultConstructor() {
        ContactData data = new ContactData();
        assertNull(data.getContacts());
    }

    @Test
    public void testParameterizedConstructor() {
        List<ContactData.ContactEntry> contacts = new ArrayList<>();
        ContactData data = new ContactData(contacts);

        assertEquals(contacts, data.getContacts());
    }

    @Test
    public void testSetterGetter() {
        ContactData data = new ContactData();
        List<ContactData.ContactEntry> contacts = new ArrayList<>();
        data.setContacts(contacts);

        assertEquals(contacts, data.getContacts());
    }

    @Test
    public void testContactEntryDefaultConstructor() {
        ContactData.ContactEntry entry = new ContactData.ContactEntry();
        assertNull(entry.getName());
        assertNull(entry.getPhone());
        assertNull(entry.getEmail());
    }

    @Test
    public void testContactEntryParameterizedConstructor() {
        ContactData.ContactEntry entry = new ContactData.ContactEntry("John Doe", "+1234567890", "john@example.com");

        assertEquals("John Doe", entry.getName());
        assertEquals("+1234567890", entry.getPhone());
        assertEquals("john@example.com", entry.getEmail());
    }

    @Test
    public void testContactEntrySetterGetter() {
        ContactData.ContactEntry entry = new ContactData.ContactEntry();

        entry.setName("Jane Doe");
        entry.setPhone("+0987654321");
        entry.setEmail("jane@example.com");

        assertEquals("Jane Doe", entry.getName());
        assertEquals("+0987654321", entry.getPhone());
        assertEquals("jane@example.com", entry.getEmail());
    }

    @Test
    public void testContactDataWithMultipleEntries() {
        List<ContactData.ContactEntry> contacts = new ArrayList<>();
        contacts.add(new ContactData.ContactEntry("Alice", "+1111111111", "alice@test.com"));
        contacts.add(new ContactData.ContactEntry("Bob", "+2222222222", "bob@test.com"));
        contacts.add(new ContactData.ContactEntry("Charlie", "+3333333333", null));

        ContactData data = new ContactData(contacts);

        assertEquals(3, data.getContacts().size());
        assertEquals("Alice", data.getContacts().get(0).getName());
        assertEquals("Bob", data.getContacts().get(1).getName());
        assertEquals("Charlie", data.getContacts().get(2).getName());
        assertNull(data.getContacts().get(2).getEmail());
    }

    @Test
    public void testEmptyContactsList() {
        ContactData data = new ContactData(new ArrayList<>());
        assertTrue(data.getContacts().isEmpty());
    }

    @Test
    public void testContactEntryWithOnlyName() {
        ContactData.ContactEntry entry = new ContactData.ContactEntry("Only Name", null, null);
        assertEquals("Only Name", entry.getName());
        assertNull(entry.getPhone());
        assertNull(entry.getEmail());
    }

    @Test
    public void testContactEntryWithOnlyPhone() {
        ContactData.ContactEntry entry = new ContactData.ContactEntry(null, "+1234567890", null);
        assertNull(entry.getName());
        assertEquals("+1234567890", entry.getPhone());
        assertNull(entry.getEmail());
    }

    @Test
    public void testContactEntryWithOnlyEmail() {
        ContactData.ContactEntry entry = new ContactData.ContactEntry(null, null, "test@example.com");
        assertNull(entry.getName());
        assertNull(entry.getPhone());
        assertEquals("test@example.com", entry.getEmail());
    }
}