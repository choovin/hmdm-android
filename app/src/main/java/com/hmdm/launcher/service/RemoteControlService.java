/*
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC
 *
 * Licensed under the Apache License, Version 2.0
 */

package com.hmdm.launcher.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.hmdm.launcher.Const;
import com.hmdm.launcher.R;
import com.hmdm.launcher.helper.SettingsHelper;
import com.hmdm.launcher.json.ServerConfig;
import com.hmdm.launcher.server.ServerService;
import com.hmdm.launcher.server.ServerServiceKeeper;
import com.hmdm.launcher.util.RemoteLogger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Service for remote control functionality.
 * Handles screen capture and streaming to the server.
 */
public class RemoteControlService extends Service {

    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "remote_control_channel";

    public static final String ACTION_START = "com.hmdm.launcher.action.START_REMOTE_CONTROL";
    public static final String ACTION_STOP = "com.hmdm.launcher.action.STOP_REMOTE_CONTROL";

    private ExecutorService executor;
    private volatile boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newSingleThreadExecutor();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                startRemoteControl();
            } else if (ACTION_STOP.equals(action)) {
                stopRemoteControl();
            }
        }
        return START_STICKY;
    }

    private void startRemoteControl() {
        if (isRunning) {
            RemoteLogger.log(this, Const.LOG_INFO, "Remote control already running");
            return;
        }

        // Check if remote control is enabled in config
        ServerConfig config = SettingsHelper.getInstance(this).getConfig();
        // For now, we'll just register with the server
        // The actual WebRTC streaming would require additional native libraries

        isRunning = true;

        // Start foreground service
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);

        // Register with server
        executor.execute(() -> {
            try {
                SettingsHelper settingsHelper = SettingsHelper.getInstance(this);
                String deviceNumber = settingsHelper.getDeviceId();
                String project = settingsHelper.getServerProject();

                if (deviceNumber != null && !deviceNumber.isEmpty()) {
                    ServerService serverService = ServerServiceKeeper.getServerServiceInstance(this);
                    Response<ResponseBody> response = serverService.getControlSession(project, deviceNumber).execute();
                    if (response.isSuccessful()) {
                        RemoteLogger.log(this, Const.LOG_INFO, "Remote control session registered");
                    }
                }
            } catch (Exception e) {
                RemoteLogger.log(this, Const.LOG_ERROR, "Failed to register remote control: " + e.getMessage());
            }
        });

        RemoteLogger.log(this, Const.LOG_INFO, "Remote control service started");
    }

    private void stopRemoteControl() {
        isRunning = false;
        stopForeground(true);
        stopSelf();
        RemoteLogger.log(this, Const.LOG_INFO, "Remote control service stopped");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.remote_control_channel),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Remote control notification channel");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, com.hmdm.launcher.ui.MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, RemoteControlService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        return builder.setContentTitle(getString(R.string.remote_control_notification_title))
                .setContentText(getString(R.string.remote_control_notification_text))
                .setSmallIcon(R.drawable.ic_mqtt_service)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        getString(R.string.stop), stopPendingIntent)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (executor != null) {
            executor.shutdown();
        }
    }

    /**
     * Start remote control service.
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, RemoteControlService.class);
        intent.setAction(ACTION_START);
        context.startService(intent);
    }

    /**
     * Stop remote control service.
     */
    public static void stop(Context context) {
        Intent intent = new Intent(context, RemoteControlService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }
}