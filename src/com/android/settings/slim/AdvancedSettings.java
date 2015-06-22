/*
 * Copyright (C) 2015 Chandra Poerwanto
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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.slim.util.BugReport;

public class AdvancedSettings extends SettingsPreferenceFragment {

    private static final String TAG = "AdvancedSettings";

    private static final String CATEGORY_ROOT = "root_category";
    private static final String CATEGORY_TWEAK = "tweak_category";

    private static final String KEY_KERNEL_ADIUTOR = "key_kernel_adiutor";
    private static final String KEY_VIPER4ANDROID = "key_viper4android";
    private static final String KEY_MAXXAUDIOFX = "key_maxxaudiofx";
    private static final String KEY_SUPERSU = "key_supersu";
    private static final String KEY_BUGREPORT = "key_bug_report";
    private static final String KEY_LAYERS_MANAGER = "key_layers_manager";
    private static final String KEY_LAYERS_BACKUP = "key_layers_backup";

    private BugReport mBugReportTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.slim_advanced_settings);

        // Tweak category
        PreferenceCategory tweakCat = (PreferenceCategory) findPreference(CATEGORY_TWEAK);
        PreferenceScreen kernelAdiutor = (PreferenceScreen) findPreference(KEY_KERNEL_ADIUTOR);
        try {
            getActivity().getPackageManager().getPackageInfo("com.grarak.kerneladiutor", 0);
        } catch (PackageManager.NameNotFoundException e) {
            tweakCat.removePreference(kernelAdiutor);
        }

        PreferenceScreen viper4android = (PreferenceScreen) findPreference(KEY_VIPER4ANDROID);
        boolean supported = false;
        try {
            supported = (getActivity().getPackageManager().getPackageInfo("com.vipercn.viper4android_v2", 0).versionCode >= 18);
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (!supported) {
            tweakCat.removePreference(viper4android);
        }

        PreferenceScreen maxxaudiofx = (PreferenceScreen) findPreference(KEY_MAXXAUDIOFX);
        try {
            getActivity().getPackageManager().getPackageInfo("com.cyngn.maxxaudio", 0);
        } catch (PackageManager.NameNotFoundException e) {
            tweakCat.removePreference(maxxaudiofx);
        }

        if (tweakCat.getPreferenceCount() == 0) {
            getPreferenceScreen().removePreference(tweakCat);
        }

        // Root category
        PreferenceCategory rootCat = (PreferenceCategory) findPreference(CATEGORY_ROOT);
        if (!isSuperSUSupported()) {
            getPreferenceScreen().removePreference(rootCat);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(KEY_BUGREPORT)) {
            if (mBugReportTask == null || mBugReportTask.getStatus() != AsyncTask.Status.RUNNING) {
                mBugReportTask = new BugReport();
                mBugReportTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getActivity());
            }
            return true;
        } else if (preference.getKey().equals(KEY_LAYERS_MANAGER)) {
            final String appPackageName = "com.lovejoy777.rroandlayersmanager";
            try {
                getActivity().getPackageManager().getPackageInfo(appPackageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                try {
                    getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (ActivityNotFoundException ex) {
                    getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                return true;
            }
            return false;
        } else if (preference.getKey().equals(KEY_LAYERS_BACKUP)) {
            final String appPackageName = "com.kantjer.xda.layers";
            try {
                getActivity().getPackageManager().getPackageInfo(appPackageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                String url = "https://www.androidfilehost.com/?w=files&flid=33949";
                getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }
            return false;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private boolean isSuperSUSupported() {
        // Embedding into Settings is supported from SuperSU v1.85 and up
        boolean supported = false;
        try {
           supported = (getActivity().getPackageManager().getPackageInfo("eu.chainfire.supersu", 0).versionCode >= 185);
        } catch (PackageManager.NameNotFoundException e) {
        }
        return supported;
    }
}
