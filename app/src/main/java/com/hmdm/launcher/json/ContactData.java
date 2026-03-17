/*
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC
 *
 * Licensed under the Apache License, Version 2.0
 */

package com.hmdm.launcher.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * DTO for contact data.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactData {

    private List<ContactEntry> contacts;

    public ContactData() {}

    public ContactData(List<ContactEntry> contacts) {
        this.contacts = contacts;
    }

    public List<ContactEntry> getContacts() {
        return contacts;
    }

    public void setContacts(List<ContactEntry> contacts) {
        this.contacts = contacts;
    }

    /**
     * Single contact entry.
     */
    public static class ContactEntry {
        private String name;
        private String phone;
        private String email;

        public ContactEntry() {}

        public ContactEntry(String name, String phone, String email) {
            this.name = name;
            this.phone = phone;
            this.email = email;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}