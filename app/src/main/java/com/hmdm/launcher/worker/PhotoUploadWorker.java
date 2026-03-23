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
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.hmdm.launcher.Const;
import com.hmdm.launcher.helper.SettingsHelper;
import com.hmdm.launcher.server.ServerService;
import com.hmdm.launcher.server.ServerServiceKeeper;
import com.hmdm.launcher.util.RemoteLogger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Worker for uploading photos to server.
 */
public class PhotoUploadWorker extends Worker {

    public static final String WORK_NAME = "photo_upload";
    private static final String PHOTO_DIR = "photos";

    public PhotoUploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        try {
            SettingsHelper settingsHelper = SettingsHelper.getInstance(context);
            String deviceNumber = settingsHelper.getDeviceId();
            String project = settingsHelper.getServerProject();

            if (deviceNumber == null || deviceNumber.isEmpty()) {
                return Result.success();
            }

            // Get photo directory
            File photoDir = new File(context.getFilesDir(), PHOTO_DIR);
            if (!photoDir.exists() || !photoDir.isDirectory()) {
                return Result.success();
            }

            // Get all photo files
            File[] photos = photoDir.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
            if (photos == null || photos.length == 0) {
                return Result.success();
            }

            // Upload each photo
            ServerService serverService = ServerServiceKeeper.getServerServiceInstance(context);

            for (File photo : photos) {
                try {
                    boolean uploaded = uploadPhoto(serverService, project, deviceNumber, photo);
                    if (uploaded) {
                        // Delete photo after successful upload
                        photo.delete();
                        RemoteLogger.log(context, Const.LOG_INFO, "Photo uploaded: " + photo.getName());
                    }
                } catch (Exception e) {
                    RemoteLogger.log(context, Const.LOG_WARN, "Failed to upload photo: " + e.getMessage());
                }
            }

            return Result.success();
        } catch (Exception e) {
            RemoteLogger.log(context, Const.LOG_ERROR, "Photo upload error: " + e.getMessage());
            return Result.retry();
        }
    }

    private boolean uploadPhoto(ServerService serverService, String project, String deviceNumber, File photo) throws IOException {
        // Use OkHttp's streaming RequestBody - reads file in chunks without loading
        // entire content into memory, preventing OOM for large photos
        RequestBody photoBody = RequestBody.create(MediaType.parse("application/octet-stream"), photo);
        RequestBody filenameBody = RequestBody.create(MediaType.parse("text/plain"), photo.getName());

        // Upload
        Response<ResponseBody> response = serverService.uploadPhoto(project, deviceNumber, photoBody, filenameBody).execute();
        return response.isSuccessful();
    }

    /**
     * Schedule photo upload.
     */
    public static void scheduleUpload(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest uploadRequest = new OneTimeWorkRequest.Builder(PhotoUploadWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                uploadRequest
        );
    }

    /**
     * Save photo to local storage for later upload.
     */
    public static File savePhoto(Context context, byte[] photoData, String filename) {
        try {
            File photoDir = new File(context.getFilesDir(), PHOTO_DIR);
            if (!photoDir.exists()) {
                photoDir.mkdirs();
            }

            File photoFile = new File(photoDir, filename);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(photoFile);
            fos.write(photoData);
            fos.close();

            return photoFile;
        } catch (Exception e) {
            RemoteLogger.log(context, Const.LOG_ERROR, "Failed to save photo: " + e.getMessage());
            return null;
        }
    }

    /**
     * Cancel scheduled upload.
     */
    public static void cancelUpload(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
}