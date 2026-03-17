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
 * DTO for network traffic log data.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrafficLogData {

    private List<TrafficLogEntry> logs;

    public TrafficLogData() {}

    public TrafficLogData(List<TrafficLogEntry> logs) {
        this.logs = logs;
    }

    public List<TrafficLogEntry> getLogs() {
        return logs;
    }

    public void setLogs(List<TrafficLogEntry> logs) {
        this.logs = logs;
    }

    /**
     * Single traffic log entry.
     */
    public static class TrafficLogEntry {
        private long timestamp;
        private String packageName;
        private String host;
        private int port;
        private long bytesIn;
        private long bytesOut;

        public TrafficLogEntry() {}

        public TrafficLogEntry(long timestamp, String packageName, String host, int port, long bytesIn, long bytesOut) {
            this.timestamp = timestamp;
            this.packageName = packageName;
            this.host = host;
            this.port = port;
            this.bytesIn = bytesIn;
            this.bytesOut = bytesOut;
        }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public long getBytesIn() { return bytesIn; }
        public void setBytesIn(long bytesIn) { this.bytesIn = bytesIn; }
        public long getBytesOut() { return bytesOut; }
        public void setBytesOut(long bytesOut) { this.bytesOut = bytesOut; }
    }
}