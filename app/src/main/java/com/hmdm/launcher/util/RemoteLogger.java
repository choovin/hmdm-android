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

package com.hmdm.launcher.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hmdm.launcher.Const;
import com.hmdm.launcher.db.DatabaseHelper;
import com.hmdm.launcher.db.LogConfigTable;
import com.hmdm.launcher.db.LogTable;
import com.hmdm.launcher.json.RemoteLogConfig;
import com.hmdm.launcher.json.RemoteLogItem;
import com.hmdm.launcher.worker.RemoteLogWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Remote logging engine which uses SQLite for configuration
 * and storing unsent logs
 */
public class RemoteLogger {
    public static long lastLogRemoval = 0;

    // Log batching: accumulate logs in buffer and flush periodically to reduce I/O
    // Each log entry is still matched against config rules before buffering
    private static final int LOG_BUFFER_SIZE = 10;
    private static final long LOG_FLUSH_INTERVAL_MS = 5000; // 5 seconds
    private static final List<RemoteLogItem> sLogBuffer = new ArrayList<>();
    private static final AtomicBoolean sFlushScheduled = new AtomicBoolean(false);
    private static ScheduledExecutorService sFlushExecutor = null;

    private static ScheduledExecutorService getFlushExecutor() {
        if (sFlushExecutor == null) {
            sFlushExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        return sFlushExecutor;
    }

    /**
     * Flush buffered logs to the database and schedule upload.
     * Called when buffer is full or flush timer expires.
     */
    private static synchronized void flushLogBuffer(Context context) {
        if (sLogBuffer.isEmpty()) {
            sFlushScheduled.set(false);
            return;
        }

        List<RemoteLogItem> itemsToFlush = new ArrayList<>(sLogBuffer);
        sLogBuffer.clear();
        sFlushScheduled.set(false);

        // Write all buffered logs to database in a single transaction
        DatabaseHelper dbHelper = DatabaseHelper.instance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (RemoteLogItem item : itemsToFlush) {
                LogTable.insert(db, item);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        // Schedule upload once for the batch
        sendLogsToServer(context);
    }

    /**
     * Schedule a timed flush if not already scheduled.
     * Uses AtomicBoolean to prevent scheduling multiple timers.
     */
    private static void scheduleTimedFlush(Context context) {
        if (sFlushScheduled.compareAndSet(false, true)) {
            getFlushExecutor().schedule(
                    () -> flushLogBuffer(context),
                    LOG_FLUSH_INTERVAL_MS,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public static void updateConfig(Context context, List<RemoteLogConfig> rules) {
        LogConfigTable.replaceAll(DatabaseHelper.instance(context).getWritableDatabase(), rules);
    }

    public static void log(Context context, int level, String message) {
        switch (level) {
            case Const.LOG_VERBOSE:
                Log.v(Const.LOG_TAG, message);
                break;
            case Const.LOG_DEBUG:
                Log.d(Const.LOG_TAG, message);
                break;
            case Const.LOG_INFO:
                Log.i(Const.LOG_TAG, message);
                break;
            case Const.LOG_WARN:
                Log.w(Const.LOG_TAG, message);
                break;
            case Const.LOG_ERROR:
                Log.e(Const.LOG_TAG, message);
                break;
        }

        RemoteLogItem item = new RemoteLogItem();
        item.setTimestamp(System.currentTimeMillis());
        item.setLogLevel(level);
        item.setPackageId(context.getPackageName());
        item.setMessage(message);
        postLog(context, item);
    }

    public static void postLog(Context context, RemoteLogItem item) {
        DatabaseHelper dbHelper = DatabaseHelper.instance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        if (!LogConfigTable.match(db, item)) {
            // Log does not match any rule, skip
            return;
        }

        // Remove old logs once per hour (check without blocking on the critical path)
        long now = System.currentTimeMillis();
        if (now > lastLogRemoval + 3600000L) {
            lastLogRemoval = now;
            // Perform cleanup in background
            final long cutoffTime = now;
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    DatabaseHelper helper = DatabaseHelper.instance(context);
                    LogTable.deleteOldItems(helper.getWritableDatabase());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Buffer the log entry: write to DB when buffer is full or timer expires
        synchronized (sLogBuffer) {
            sLogBuffer.add(item);
            if (sLogBuffer.size() >= LOG_BUFFER_SIZE) {
                // Buffer full: flush immediately outside the sync block
                final Context ctx = context.getApplicationContext();
                List<RemoteLogItem> toFlush = new ArrayList<>(sLogBuffer);
                sLogBuffer.clear();
                Executors.newSingleThreadExecutor().execute(() -> {
                    DatabaseHelper dbHelper2 = DatabaseHelper.instance(ctx);
                    SQLiteDatabase db2 = dbHelper2.getWritableDatabase();
                    db2.beginTransaction();
                    try {
                        for (RemoteLogItem i : toFlush) {
                            LogTable.insert(db2, i);
                        }
                        db2.setTransactionSuccessful();
                    } finally {
                        db2.endTransaction();
                    }
                    sendLogsToServer(ctx);
                });
            } else {
                // Schedule timed flush
                scheduleTimedFlush(context.getApplicationContext());
            }
        }
    }

    public static void resetState() {
        RemoteLogWorker.resetState();
    }

    public static void sendLogsToServer(Context context) {
        RemoteLogWorker.scheduleUpload(context);
    }
}
