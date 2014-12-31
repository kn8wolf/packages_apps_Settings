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
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import android.widget.Toast;

import org.cyanogenmod.hardware.KeyDisabler;

public class ButtonsSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "ButtonsSettings";
    private static final String DISABLE_HARDWARE_BUTTONS = "disable_hardware_button";
    private static final String KEY_BUTTON_BACKLIGHT = "button_backlight";
    private static final String ENABLE_NAVIGATION_BAR = "enable_nav_bar";
    private static final String KEYS_OVERFLOW_BUTTON = "keys_overflow_button";

    private SwitchPreference mDisableHardwareButtons;
    private ButtonBacklightBrightness mBacklight;
    private SwitchPreference mEnableNavigationBar;
    private ListPreference mOverflowButtonMode;

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

        mBacklight = (ButtonBacklightBrightness) prefs.findPreference(KEY_BUTTON_BACKLIGHT);
        if (!mBacklight.isButtonSupported() && !mBacklight.isKeyboardSupported()) {
            prefs.removePreference(mBacklight);
        }

        mOverflowButtonMode = (ListPreference) prefs.findPreference(KEYS_OVERFLOW_BUTTON);
        mOverflowButtonMode.setOnPreferenceChangeListener(this);

        updateSettings();
    }

    private void updateSettings() {
        if (isKeyDisablerSupported()) {
            boolean isHWKeysDisabled = KeyDisabler.isActive();
            mDisableHardwareButtons.setChecked(isHWKeysDisabled);
            mEnableNavigationBar.setEnabled(isHWKeysDisabled ? false : true);
            mOverflowButtonMode.setEnabled(isHWKeysDisabled ? false : true);
            mBacklight.setEnabled(isHWKeysDisabled ? false : true);
        }
     
        int navbarIsDefault = getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar) ? 1 : 0;
        boolean enableNavigationBar = Settings.System.getInt(getContentResolver(),
            Settings.System.NAVIGATION_BAR_SHOW, navbarIsDefault) == 1;
        mEnableNavigationBar.setChecked(enableNavigationBar);

        String overflowButtonMode = Integer.toString(Settings.System.getInt(getContentResolver(),
                Settings.System.UI_OVERFLOW_BUTTON, 2));
        mOverflowButtonMode.setValue(overflowButtonMode);
        mOverflowButtonMode.setSummary(mOverflowButtonMode.getEntry());

        mBacklight.updateSummary();
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

            // Enable overflow button
            Settings.System.putInt(getContentResolver(), Settings.System.UI_OVERFLOW_BUTTON, 2);
            if (mOverflowButtonMode != null) {
                mOverflowButtonMode.setSummary(mOverflowButtonMode.getEntries()[2]);
            }

            // Enable NavBar
            Settings.System.putInt(getContentResolver(), Settings.System.NAVIGATION_BAR_SHOW, 1);

            // Update preferences
            updateSettings();

            return true;
        } else if (preference == mEnableNavigationBar) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_SHOW,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        } else if (preference == mOverflowButtonMode) {
            int val = Integer.parseInt((String) newValue);
            int index = mOverflowButtonMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(), Settings.System.UI_OVERFLOW_BUTTON, val);
            mOverflowButtonMode.setSummary(mOverflowButtonMode.getEntries()[index]);
            Toast.makeText(getActivity(), R.string.keys_overflow_toast, Toast.LENGTH_LONG).show();
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
