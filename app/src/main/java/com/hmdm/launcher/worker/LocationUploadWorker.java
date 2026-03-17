/*
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC
 *
 * Licensed under the Apache License, Version 2.0
 */

package com.hmdm.launcher.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.hmdm.launcher.Const;
import com.hmdm.launcher.db.DatabaseHelper;
import com.hmdm.launcher.db.LocationTable;
import com.hmdm.launcher.helper.SettingsHelper;
import com.hmdm.launcher.json.ServerConfig;
import com.hmdm.launcher.server.ServerService;
import com.hmdm.launcher.server.ServerServiceKeeper;
import com.hmdm.launcher.util.RemoteLogger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Worker for batch uploading location data to server.
 */
public class LocationUploadWorker extends Worker {

    public static final String WORK_NAME = "location_upload";
    private static final long UPLOAD_INTERVAL = 5; // minutes

    public LocationUploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        try {
            // Get device config
            SettingsHelper settingsHelper = SettingsHelper.getInstance(context);
            ServerConfig config = settingsHelper.getConfig();
            if (config == null) {
                return Result.success();
            }

            String deviceNumber = settingsHelper.getDeviceId();
            String project = settingsHelper.getServerProject();

            if (deviceNumber == null || deviceNumber.isEmpty()) {
                return Result.success();
            }

            // Read pending location data
            DatabaseHelper dbHelper = DatabaseHelper.instance(context);
            android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
            List<LocationTable.Location> locations = LocationTable.select(db, 100);
            db.close();

            if (locations.isEmpty()) {
                return Result.success();
            }

            // Call API to upload
            ServerService serverService = ServerServiceKeeper.getServerServiceInstance(context);
            Response<ResponseBody> response = serverService.sendLocations(project, deviceNumber, locations).execute();

            if (response.isSuccessful()) {
                // Upload successful, delete uploaded data
                db = dbHelper.getWritableDatabase();
                LocationTable.delete(db, locations);
                db.close();
                RemoteLogger.log(context, Const.LOG_INFO, "Uploaded " + locations.size() + " locations");
            } else {
                RemoteLogger.log(context, Const.LOG_WARN, "Location upload failed: " + response.code());
            }

            return Result.success();
        } catch (IOException e) {
            RemoteLogger.log(context, Const.LOG_ERROR, "Location upload error: " + e.getMessage());
            return Result.retry();
        }
    }

    /**
     * Schedule periodic location upload.
     */
    public static void scheduleUpload(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest uploadRequest = new PeriodicWorkRequest.Builder(
                LocationUploadWorker.class,
                UPLOAD_INTERVAL, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                uploadRequest
        );
    }

    /**
     * Cancel scheduled upload.
     */
    public static void cancelUpload(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
}