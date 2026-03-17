/*
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC
 *
 * Licensed under the Apache License, Version 2.0
 */

package com.hmdm.launcher.worker;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.hmdm.launcher.Const;
import com.hmdm.launcher.helper.SettingsHelper;
import com.hmdm.launcher.server.ServerService;
import com.hmdm.launcher.server.ServerServiceKeeper;
import com.hmdm.launcher.util.RemoteLogger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Worker for syncing contacts between device and server.
 */
public class ContactSyncWorker extends Worker {

    public static final String WORK_NAME = "contact_sync";
    private static final long SYNC_INTERVAL = 60; // minutes

    public ContactSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
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

            // Download contacts from server
            ServerService serverService = ServerServiceKeeper.getServerServiceInstance(context);
            Response<ResponseBody> response = serverService.getContacts(project, deviceNumber).execute();

            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                JSONArray contacts = new JSONArray(json);
                int count = saveContactsToDevice(context, contacts);
                RemoteLogger.log(context, Const.LOG_INFO, "Synced " + count + " contacts from server");
            }

            return Result.success();
        } catch (Exception e) {
            RemoteLogger.log(context, Const.LOG_ERROR, "Contact sync error: " + e.getMessage());
            return Result.retry();
        }
    }

    private int saveContactsToDevice(Context context, JSONArray contacts) throws Exception {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        for (int i = 0; i < contacts.length(); i++) {
            JSONObject contact = contacts.getJSONObject(i);
            String name = contact.optString("name", "");
            String phone = contact.optString("phone", "");
            String email = contact.optString("email", "");

            if (name.isEmpty() && phone.isEmpty()) {
                continue;
            }

            // Get the index for the raw contact that will be created
            // Each contact adds: 1 raw contact + (name?1:0) + (phone?1:0) + (email?1:0) operations
            int rawContactIndex = operations.size();

            operations.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            // Add name
            if (!name.isEmpty()) {
                operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                        .build());
            }

            // Add phone
            if (!phone.isEmpty()) {
                operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build());
            }

            // Add email
            if (!email.isEmpty()) {
                operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                        .build());
            }
        }

        if (!operations.isEmpty()) {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
        }

        return contacts.length();
    }

    /**
     * Schedule periodic contact sync.
     */
    public static void scheduleSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                ContactSyncWorker.class,
                SYNC_INTERVAL, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
        );
    }

    /**
     * Trigger immediate sync.
     */
    public static void syncNow(Context context) {
        scheduleSync(context);
    }

    /**
     * Cancel scheduled sync.
     */
    public static void cancelSync(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
}