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

package com.hmdm.launcher.ui;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hmdm.launcher.Const;
import com.hmdm.launcher.R;
import com.hmdm.launcher.util.RemoteLogger;

/**
 * Activity for confirming dangerous operations like remote lock and factory reset.
 * Displays operation details and requires user confirmation before executing.
 */
public class ConfirmDialogActivity extends AppCompatActivity {

    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_COUNTDOWN = "countdown";
    public static final String EXTRA_WIPE_EXTERNAL = "wipeExternal";

    public static final String ACTION_LOCK = "lock";
    public static final String ACTION_FACTORY_RESET = "factoryReset";

    private String action;
    private String adminMessage;
    private int countdown;
    private boolean wipeExternal;
    private boolean countdownRunning = true;
    private CountDownTimer countDownTimer;

    private TextView countdownText;
    private Button confirmButton;
    private Button cancelButton;
    private EditText confirmInput;
    private View inputContainer;

    private static final int DEFAULT_COUNTDOWN_LOCK = 30;
    private static final int DEFAULT_COUNTDOWN_RESET = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on and show over lock screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_confirm_dialog);

        // Get parameters from intent
        action = getIntent().getStringExtra(EXTRA_ACTION);
        adminMessage = getIntent().getStringExtra(EXTRA_MESSAGE);
        countdown = getIntent().getIntExtra(EXTRA_COUNTDOWN, 0);
        wipeExternal = getIntent().getBooleanExtra(EXTRA_WIPE_EXTERNAL, true);

        // Set default countdown if not provided
        if (countdown <= 0) {
            countdown = ACTION_FACTORY_RESET.equals(action) ? DEFAULT_COUNTDOWN_RESET : DEFAULT_COUNTDOWN_LOCK;
        }

        initViews();
        setupUI();
        startCountdown();
    }

    private void initViews() {
        countdownText = findViewById(R.id.countdown_text);
        confirmButton = findViewById(R.id.confirm_button);
        cancelButton = findViewById(R.id.cancel_button);
        confirmInput = findViewById(R.id.confirm_input);
        inputContainer = findViewById(R.id.input_container);
    }

    private void setupUI() {
        TextView titleText = findViewById(R.id.dialog_title);
        TextView messageText = findViewById(R.id.dialog_message);
        View lockInfoContainer = findViewById(R.id.lock_info_container);
        View factoryResetInfoContainer = findViewById(R.id.factory_reset_info_container);

        if (ACTION_LOCK.equals(action)) {
            titleText.setText(R.string.remote_lock_title);
            messageText.setText(R.string.remote_lock_message);
            lockInfoContainer.setVisibility(View.VISIBLE);
            factoryResetInfoContainer.setVisibility(View.GONE);
            inputContainer.setVisibility(View.GONE);
            confirmButton.setText(R.string.confirm_lock);
        } else if (ACTION_FACTORY_RESET.equals(action)) {
            titleText.setText(R.string.factory_reset_title);
            messageText.setText(R.string.factory_reset_message);
            lockInfoContainer.setVisibility(View.GONE);
            factoryResetInfoContainer.setVisibility(View.VISIBLE);
            inputContainer.setVisibility(View.VISIBLE);
            confirmButton.setText(R.string.confirm_factory_reset);
        }

        // Display admin message if provided
        TextView adminMessageText = findViewById(R.id.admin_message);
        if (adminMessage != null && !adminMessage.isEmpty()) {
            adminMessageText.setText(adminMessage);
            adminMessageText.setVisibility(View.VISIBLE);
        } else {
            adminMessageText.setVisibility(View.GONE);
        }

        // Set up button listeners
        confirmButton.setOnClickListener(v -> onConfirmClick());
        cancelButton.setOnClickListener(v -> onCancelClick());

        // Initially disable confirm button
        confirmButton.setEnabled(false);
    }

    private void startCountdown() {
        countdownRunning = true;

        countDownTimer = new CountDownTimer(countdown * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                countdownText.setText(String.format("%d", secondsRemaining));
                cancelButton.setText(getString(R.string.cancel_with_countdown, secondsRemaining));
            }

            @Override
            public void onFinish() {
                countdownRunning = false;
                countdownText.setText("0");
                cancelButton.setText(R.string.cancel);
                confirmButton.setEnabled(true);
                confirmButton.setText(R.string.execute_now);
            }
        };

        countDownTimer.start();
    }

    private void onConfirmClick() {
        if (ACTION_FACTORY_RESET.equals(action)) {
            // Factory reset requires typing "确认"
            String input = confirmInput.getText().toString();
            if (!"确认".equals(input)) {
                Toast.makeText(this, R.string.confirm_input_hint, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!countdownRunning) {
            executeAction();
        }
    }

    private void onCancelClick() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        finish();
    }

    private void executeAction() {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = new ComponentName(this, com.hmdm.launcher.AdminReceiver.class);

            // Check if admin is enabled
            if (!dpm.isAdminActive(adminComponent)) {
                RemoteLogger.log(this, Const.LOG_ERROR, "Device admin not active, cannot execute remote command");
                Toast.makeText(this, R.string.admin_not_active, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            if (ACTION_LOCK.equals(action)) {
                dpm.lockNow();
                RemoteLogger.log(this, Const.LOG_INFO, "Device locked by user confirmation");
                Toast.makeText(this, R.string.device_locked, Toast.LENGTH_SHORT).show();
            } else if (ACTION_FACTORY_RESET.equals(action)) {
                int flags = wipeExternal ? DevicePolicyManager.WIPE_EXTERNAL_STORAGE : 0;
                dpm.wipeData(flags);
                RemoteLogger.log(this, Const.LOG_INFO, "Factory reset initiated by user confirmation");
            }
        } catch (Exception e) {
            RemoteLogger.log(this, Const.LOG_ERROR, "Failed to execute action: " + e.getMessage());
            Toast.makeText(this, getString(R.string.operation_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        } finally {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}