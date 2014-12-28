/*
 * Copyright (C) 2012 Slimroms
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

package com.android.settings.slim;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import org.cyanogenmod.hardware.KeyDisabler;

public class ButtonsSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "ButtonsSettings";
    private static final String DISABLE_HARDWARE_BUTTONS = "disable_hardware_button";
    private static final String ENABLE_NAVIGATION_BAR = "enable_nav_bar";

    private SwitchPreference mDisableHardwareButtons;
    private SwitchPreference mEnableNavigationBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.buttons_settings);

        PreferenceScreen prefs = getPreferenceScreen();

        mEnableNavigationBar = (SwitchPreference) prefs.findPreference(ENABLE_NAVIGATION_BAR);
        mEnableNavigationBar.setOnPreferenceChangeListener(this);

        mDisableHardwareButtons = (SwitchPreference) prefs.findPreference(DISABLE_HARDWARE_BUTTONS);
        if (isKeyDisablerSupported()) {
            mDisableHardwareButtons.setOnPreferenceChangeListener(this);
        } else {
            prefs.removePreference(mDisableHardwareButtons);
        }

        updateSettings();
    }

    private void updateSettings() {
        if (isKeyDisablerSupported()) {
            boolean isHWKeysDisabled = KeyDisabler.isActive();
            mDisableHardwareButtons.setChecked(isHWKeysDisabled);
            mEnableNavigationBar.setEnabled(isHWKeysDisabled ? false : true);
        }
     
        boolean enableNavigationBar = Settings.System.getInt(getContentResolver(),
            Settings.System.NAVIGATION_BAR_SHOW, -1) == 1;
        mEnableNavigationBar.setChecked(enableNavigationBar);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDisableHardwareButtons) {
            boolean value = (Boolean) newValue;

            // Disable hw keys on kernel level
            KeyDisabler.setActive(value);

            // Disable backlight
            int defaultBrightness = getResources().getInteger(
                    com.android.internal.R.integer.config_buttonBrightnessSettingDefault);
            int brightness = value ? 0 : defaultBrightness;
            Settings.System.putInt(getContentResolver(), Settings.System.BUTTON_BRIGHTNESS, brightness);

            // Enable NavBar
            Settings.System.putInt(getActivity().getContentResolver(),
                                    Settings.System.NAVIGATION_BAR_SHOW, 1);

            // Update preferences
            updateSettings();

            return true;
        } else if (preference == mEnableNavigationBar) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_SHOW,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSettings();
    }

    private static boolean isKeyDisablerSupported() {
        try {
            return KeyDisabler.isSupported();
        } catch (NoClassDefFoundError e) {
            // Hardware abstraction framework not installed
            return false;
        }
    }

    public static void restore(Context context) {
        if (isKeyDisablerSupported()) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            final boolean enabled = prefs.getBoolean(DISABLE_HARDWARE_BUTTONS, false);
            KeyDisabler.setActive(enabled);
        }
    }

}
