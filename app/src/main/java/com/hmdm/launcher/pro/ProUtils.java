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

package com.hmdm.launcher.pro;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Build;
import android.view.View;

import com.hmdm.launcher.Const;
import com.hmdm.launcher.R;
import com.hmdm.launcher.db.LocationTable;
import com.hmdm.launcher.helper.SettingsHelper;
import com.hmdm.launcher.json.ServerConfig;
import com.hmdm.launcher.util.RemoteLogger;
import com.hmdm.launcher.worker.LocationUploadWorker;
import com.hmdm.launcher.worker.PhotoUploadWorker;

import java.util.Calendar;

/**
 * These functions are available in Pro-version only
 * In a free version, the class contains stubs
 */
public class ProUtils {

    public static boolean isPro() {
        return false;
    }

    public static boolean kioskModeRequired(Context context) {
        ServerConfig config = SettingsHelper.getInstance(context).getConfig();
        return config != null && config.isKioskMode();
    }

    public static void initCrashlytics(Context context) {
        // Stub
    }

    public static void sendExceptionToCrashlytics(Throwable e) {
        // Stub
    }

    // Start the service checking if the foreground app is allowed to the user (by usage statistics)
    public static boolean checkAccessibilityService(Context context) {
        // Stub
        return true;
    }

    // Pro-version
    public static boolean checkUsageStatistics(Context context) {
        // Stub
        return true;
    }

    // Add a transparent view on top of the status bar which prevents user interaction with the status bar
    public static View preventStatusBarExpansion(Activity activity) {
        // Stub
        return null;
    }

    // Add a transparent view on top of a swipeable area at the right (opens app list on Samsung tablets)
    public static View preventApplicationsList(Activity activity) {
        // Stub
        return null;
    }

    public static View createKioskUnlockButton(Activity activity) {
        // Stub
        return null;
    }

    public static boolean isKioskAppInstalled(Context context) {
        ServerConfig config = SettingsHelper.getInstance(context).getConfig();
        if (config == null || config.getMainApp() == null || config.getMainApp().isEmpty()) {
            return false;
        }
        String packageName = config.getMainApp();
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isKioskModeRunning(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return false;
        }
        return am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_LOCKED;
    }

    public static Intent getKioskAppIntent(String kioskApp, Activity activity) {
        String packageName = kioskApp;
        if (packageName == null || packageName.isEmpty()) {
            ServerConfig config = SettingsHelper.getInstance(activity).getConfig();
            if (config != null && config.getMainApp() != null) {
                packageName = config.getMainApp();
            }
        }

        if (packageName == null || packageName.isEmpty()) {
            return null;
        }

        try {
            PackageManager pm = activity.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
            return launchIntent;
        } catch (Exception e) {
            RemoteLogger.log(activity, Const.LOG_ERROR, "Failed to get kiosk app intent: " + e.getMessage());
            return null;
        }
    }

    // Start COSU kiosk mode
    public static boolean startCosuKioskMode(String kioskApp, Activity activity, boolean enableSettings) {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = new ComponentName(activity, com.hmdm.launcher.AdminReceiver.class);

            if (!dpm.isAdminActive(adminComponent)) {
                RemoteLogger.log(activity, Const.LOG_ERROR, "Device admin not active, cannot start kiosk");
                return false;
            }

            // Get main app package name
            String packageName = kioskApp;
            if (packageName == null || packageName.isEmpty()) {
                ServerConfig config = SettingsHelper.getInstance(activity).getConfig();
                if (config != null && config.getMainApp() != null) {
                    packageName = config.getMainApp();
                }
            }

            if (packageName == null || packageName.isEmpty()) {
                RemoteLogger.log(activity, Const.LOG_ERROR, "No kiosk app specified");
                return false;
            }

            // Verify app is installed
            try {
                activity.getPackageManager().getPackageInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                RemoteLogger.log(activity, Const.LOG_ERROR, "Kiosk app not installed: " + packageName);
                return false;
            }

            // Set lock task packages
            dpm.setLockTaskPackages(adminComponent, new String[]{packageName});

            // Start lock task mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.startLockTask();
            }

            RemoteLogger.log(activity, Const.LOG_INFO, "Kiosk mode started for: " + packageName);
            return true;
        } catch (Exception e) {
            RemoteLogger.log(activity, Const.LOG_ERROR, "Failed to start kiosk: " + e.getMessage());
            return false;
        }
    }

    // Set/update kiosk mode options (lock task features)
    public static void updateKioskOptions(Activity activity) {
        try {
            ServerConfig config = SettingsHelper.getInstance(activity).getConfig();
            if (config == null) {
                return;
            }

            DevicePolicyManager dpm = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = new ComponentName(activity, com.hmdm.launcher.AdminReceiver.class);

            // Update status bar and other kiosk options
            // Note: Some options require Android 9+

        } catch (Exception e) {
            RemoteLogger.log(activity, Const.LOG_ERROR, "Failed to update kiosk options: " + e.getMessage());
        }
    }

    // Update app list in the kiosk mode
    public static void updateKioskAllowedApps(String kioskApp, Activity activity, boolean enableSettings) {
        try {
            // If kiosk mode is already running, update allowed apps
            if (isKioskModeRunning(activity)) {
                startCosuKioskMode(kioskApp, activity, enableSettings);
            }
        } catch (Exception e) {
            RemoteLogger.log(activity, Const.LOG_ERROR, "Failed to update kiosk allowed apps: " + e.getMessage());
        }
    }

    public static void unlockKiosk(Activity activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.stopLockTask();
            }
            RemoteLogger.log(activity, Const.LOG_INFO, "Kiosk mode unlocked");
        } catch (Exception e) {
            RemoteLogger.log(activity, Const.LOG_ERROR, "Failed to unlock kiosk: " + e.getMessage());
        }
    }

    public static void processConfig(Context context, ServerConfig config) {
        // Stub
    }

    /**
     * Capture and upload photo.
     * This method triggers photo capture and upload to server.
     */
    public static boolean capturePhoto(Context context, String fileName) {
        try {
            // Trigger photo upload worker
            PhotoUploadWorker.scheduleUpload(context);
            RemoteLogger.log(context, Const.LOG_INFO, "Photo capture initiated");
            return true;
        } catch (Exception e) {
            RemoteLogger.log(context, Const.LOG_ERROR, "Failed to capture photo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Save photo to local storage for later upload.
     */
    public static void savePhotoForUpload(Context context, byte[] photoData, String fileName) {
        PhotoUploadWorker.savePhoto(context, photoData, fileName);
        // Trigger upload
        PhotoUploadWorker.scheduleUpload(context);
    }

    public static void processLocation(Context context, Location location, String provider) {
        // Check if location tracking is enabled
        if (!isLocationTrackingEnabled(context)) {
            return;
        }

        // Create location data object
        LocationTable.Location locData = new LocationTable.Location();
        locData.setTs(System.currentTimeMillis());
        locData.setLat(location.getLatitude());
        locData.setLon(location.getLongitude());
        locData.setSpeed(location.getSpeed());
        locData.setAltitude((float) location.getAltitude());
        locData.setAccuracy(location.getAccuracy());
        locData.setBearing(location.getBearing());
        locData.setProvider(provider);

        // Store to database
        try {
            SQLiteDatabase db = context.openOrCreateDatabase("hmdm.db", Context.MODE_PRIVATE, null);
            LocationTable.insert(db, locData);
            db.close();
        } catch (Exception e) {
            RemoteLogger.log(context, Const.LOG_ERROR, "Failed to save location: " + e.getMessage());
        }

        // Trigger batch upload
        LocationUploadWorker.scheduleUpload(context);
    }

    private static boolean isLocationTrackingEnabled(Context context) {
        // Read from config, default to enabled if not set
        ServerConfig config = SettingsHelper.getInstance(context).getConfig();
        if (config == null) {
            return false;
        }
        Boolean enabled = config.getLocationTrackingEnabled();
        return enabled == null || enabled;
    }

    public static String getAppName(Context context) {
        return context.getString(R.string.app_name);
    }

    public static String getCopyright(Context context) {
        return "(c) " + Calendar.getInstance().get(Calendar.YEAR) + " " + context.getString(R.string.vendor);
    }
}
