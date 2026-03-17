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
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.hmdm.launcher.Const;
import com.hmdm.launcher.R;
import com.hmdm.launcher.helper.SettingsHelper;
import com.hmdm.launcher.server.ServerService;
import com.hmdm.launcher.server.ServerServiceKeeper;
import com.hmdm.launcher.util.RemoteLogger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * VPN-based network filter service.
 * Intercepts network traffic and applies filtering rules.
 */
public class NetworkFilterService extends VpnService {

    private static final int NOTIFICATION_ID = 1002;
    private static final String CHANNEL_ID = "network_filter_channel";

    public static final String ACTION_START = "com.hmdm.launcher.action.START_NETWORK_FILTER";
    public static final String ACTION_STOP = "com.hmdm.launcher.action.STOP_NETWORK_FILTER";

    public static final String VPN_ADDRESS = "10.0.0.2";
    public static final String VPN_ROUTE = "0.0.0.0";

    private ParcelFileDescriptor vpnInterface;
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
                startNetworkFilter();
            } else if (ACTION_STOP.equals(action)) {
                stopNetworkFilter();
            }
        }
        return START_STICKY;
    }

    private void startNetworkFilter() {
        if (isRunning) {
            RemoteLogger.log(this, Const.LOG_INFO, "Network filter already running");
            return;
        }

        try {
            // Create VPN interface
            Builder builder = new Builder();
            builder.setSession("Network Filter")
                    .addAddress(VPN_ADDRESS, 32)
                    .addRoute(VPN_ROUTE, 0)
                    .addDnsServer("8.8.8.8")
                    .addDnsServer("8.8.4.4");

            vpnInterface = builder.establish();

            if (vpnInterface == null) {
                RemoteLogger.log(this, Const.LOG_ERROR, "Failed to establish VPN");
                return;
            }

            isRunning = true;

            // Start foreground service
            Notification notification = createNotification();
            startForeground(NOTIFICATION_ID, notification);

            // Start packet processing
            executor.execute(this::processPackets);

            // Fetch rules from server
            fetchRulesFromServer();

            RemoteLogger.log(this, Const.LOG_INFO, "Network filter service started");
        } catch (Exception e) {
            RemoteLogger.log(this, Const.LOG_ERROR, "Failed to start network filter: " + e.getMessage());
            stopNetworkFilter();
        }
    }

    private void processPackets() {
        FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
        FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());
        ByteBuffer packet = ByteBuffer.allocate(32767);

        try {
            while (isRunning) {
                packet.clear();
                int length = in.read(packet.array());

                if (length > 0) {
                    packet.limit(length);

                    // Process packet - here you would apply filtering rules
                    // For now, just pass through
                    out.write(packet.array(), 0, length);
                }

                Thread.sleep(10);
            }
        } catch (IOException | InterruptedException e) {
            RemoteLogger.log(this, Const.LOG_WARN, "Packet processing interrupted: " + e.getMessage());
        }
    }

    private void fetchRulesFromServer() {
        executor.execute(() -> {
            try {
                SettingsHelper settingsHelper = SettingsHelper.getInstance(this);
                String deviceNumber = settingsHelper.getDeviceId();
                String project = settingsHelper.getServerProject();

                if (deviceNumber != null && !deviceNumber.isEmpty()) {
                    ServerService serverService = ServerServiceKeeper.getServerServiceInstance(this);
                    // For now, we just check if rules are available
                    // Actual rule processing would be done here
                    RemoteLogger.log(this, Const.LOG_INFO, "Fetching network filter rules from server");
                }
            } catch (Exception e) {
                RemoteLogger.log(this, Const.LOG_ERROR, "Failed to fetch network rules: " + e.getMessage());
            }
        });
    }

    private void stopNetworkFilter() {
        isRunning = false;

        try {
            if (vpnInterface != null) {
                vpnInterface.close();
                vpnInterface = null;
            }
        } catch (IOException e) {
            // Ignore
        }

        stopForeground(true);
        stopSelf();
        RemoteLogger.log(this, Const.LOG_INFO, "Network filter service stopped");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.network_filter_channel),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Network filter notification channel");

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

        Intent stopIntent = new Intent(this, NetworkFilterService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        return builder.setContentTitle(getString(R.string.network_filter_notification_title))
                .setContentText(getString(R.string.network_filter_notification_text))
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
     * Start network filter service.
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, NetworkFilterService.class);
        intent.setAction(ACTION_START);
        context.startService(intent);
    }

    /**
     * Stop network filter service.
     */
    public static void stop(Context context) {
        Intent intent = new Intent(context, NetworkFilterService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }

    /**
     * Check if VPN permission is granted.
     * Returns null if already prepared, otherwise returns Intent to request permission.
     */
    public static Intent prepare(Context context) {
        return VpnService.prepare(context);
    }
}