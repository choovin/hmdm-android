/*
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hmdm.launcher.db;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedList;
import java.util.List;

public class LocationTable {
    private static final String CREATE_TABLE =
            "CREATE TABLE locations (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "ts INTEGER, " +
                    "lat REAL, " +
                    "lon REAL, " +
                    "speed REAL, " +
                    "altitude REAL, " +
                    "accuracy REAL, " +
                    "bearing REAL, " +
                    "provider TEXT" +
                    ")";
    private static final String SELECT_LAST_LOCATION =
            "SELECT * FROM locations ORDER BY ts LIMIT ?";
    private static final String INSERT_LOCATIONS =
            "INSERT OR IGNORE INTO locations(ts, lat, lon, speed, altitude, accuracy, bearing, provider) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String DELETE_FROM_LOCATION =
            "DELETE FROM locations WHERE _id=?";
    private static final String DELETE_OLD_ITEMS =
            "DELETE FROM locations WHERE ts < ?";

    public static class Location {
        private long _id;
        private long ts;
        private double lat;
        private double lon;
        private float speed;
        private float altitude;
        private float accuracy;
        private float bearing;
        private String provider;

        public Location() {}

        public Location(android.location.Location location) {
            this.ts = location.getTime();
            this.lat = location.getLatitude();
            this.lon = location.getLongitude();
            this.speed = location.getSpeed();
            this.altitude = location.getAltitude();
            this.accuracy = location.getAccuracy();
            this.bearing = location.getBearing();
            this.provider = location.getProvider();
        }

        @SuppressLint("Range")
        public Location(Cursor cursor) {
            _id = cursor.getLong(cursor.getColumnIndex("_id"));
            ts = cursor.getLong(cursor.getColumnIndex("ts"));
            lat = cursor.getDouble(cursor.getColumnIndex("lat"));
            lon = cursor.getDouble(cursor.getColumnIndex("lon"));
            speed = cursor.getFloat(cursor.getColumnIndex("speed"));
            altitude = cursor.getFloat(cursor.getColumnIndex("altitude"));
            accuracy = cursor.getFloat(cursor.getColumnIndex("accuracy"));
            bearing = cursor.getFloat(cursor.getColumnIndex("bearing"));
            provider = cursor.getString(cursor.getColumnIndex("provider"));
        }

        public long getId() {
            return _id;
        }

        public void setId(int id) {
            this._id = id;
        }

        public long getTs() {
            return ts;
        }

        public void setTs(long ts) {
            this.ts = ts;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        public float getSpeed() {
            return speed;
        }

        public void setSpeed(float speed) {
            this.speed = speed;
        }

        public float getAltitude() {
            return altitude;
        }

        public void setAltitude(float altitude) {
            this.altitude = altitude;
        }

        public float getAccuracy() {
            return accuracy;
        }

        public void setAccuracy(float accuracy) {
            this.accuracy = accuracy;
        }

        public float getBearing() {
            return bearing;
        }

        public void setBearing(float bearing) {
            this.bearing = bearing;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }
    }

    public static String getCreateTableSql() {
        return CREATE_TABLE;
    }

    public static void insert(SQLiteDatabase db, Location location) {
        try {
            db.execSQL(INSERT_LOCATIONS, new String[]{
                    Long.toString(location.getTs()),
                    Double.toString(location.getLat()),
                    Double.toString(location.getLon()),
                    Float.toString(location.getSpeed()),
                    Float.toString(location.getAltitude()),
                    Float.toString(location.getAccuracy()),
                    Float.toString(location.getBearing()),
                    location.getProvider() != null ? location.getProvider() : ""
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteOldItems(SQLiteDatabase db) {
        long oldTs = System.currentTimeMillis() - 24 * 60 * 60 * 1000L;
        try {
            db.execSQL(DELETE_OLD_ITEMS, new String[]{
                    Long.toString(oldTs)
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void delete(SQLiteDatabase db, List<Location> items) {
        db.beginTransaction();
        try {
            for (Location item : items) {
                db.execSQL(DELETE_FROM_LOCATION, new String[]{
                        Long.toString(item.getId())
                });
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public static List<Location> select(SQLiteDatabase db, int limit) {
        Cursor cursor = db.rawQuery( SELECT_LAST_LOCATION, new String[] {
                Integer.toString(limit)
        });
        List<Location> result = new LinkedList<>();

        boolean isDataNotEmpty = cursor.moveToFirst();
        while (isDataNotEmpty) {
            Location item = new Location(cursor);
            result.add(item);
            isDataNotEmpty = cursor.moveToNext();
        }
        cursor.close();

        return result;
    }
}
