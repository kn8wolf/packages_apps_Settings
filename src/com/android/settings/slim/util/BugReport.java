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

package com.android.settings.slim.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.android.settings.R;

public class BugReport extends AsyncTask<Context, Void, Context> {

    private File mLogcat;
    private File mLastKmsg;
    private File mDmesg;
    private File mZip;

    public BugReport() {
        File extdir = Environment.getExternalStorageDirectory();
        File path = new File(extdir.getAbsolutePath(), "Bugreport");
        if (!path.exists()) {
            path.mkdirs();
        }
        mLogcat = new File(path, "logcat.log");
        mLastKmsg = new File(path, "last_kmsg.log");
        mDmesg = new File(path, "dmesg.log");
        mZip = new File(path, "bugreport.zip");
        if (mLogcat.exists()) {
            mLogcat.delete();
        }
        if (mLastKmsg.exists()) {
            mLastKmsg.delete();
        }
        if (mDmesg.exists()) {
            mDmesg.delete();
        }
        if (mZip.exists()) {
            mZip.delete();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        RootUtils.rootAccess();
    }

    @Override
    protected Context doInBackground(Context... arg) {
        Context context = arg[0];
        RootUtils.runCommand("logcat -d -f " + mLogcat.toString() + " *:V\n");
        RootUtils.runCommand("cat /proc/last_kmsg > " + mLastKmsg.toString());
        RootUtils.runCommand("dmesg > " + mDmesg.toString());
        return context;
    }

    @Override
    protected void onPostExecute(Context context) {
        zipLogs(context);
        RootUtils.closeSU();
    }

    private void toast(String text, Context context) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void zipLogs(Context context) {
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(mZip);
            zos = new ZipOutputStream(fos);
            addToZipFile(mLogcat, zos);
            addToZipFile(mLastKmsg, zos);
            addToZipFile(mDmesg, zos);
            toast(context.getResources().getString(R.string.bug_report_success), context);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            toast(context.getResources().getString(R.string.bug_report_failed), context);
        } catch (IOException e) {
            e.printStackTrace();
            toast(context.getResources().getString(R.string.bug_report_failed), context);
        } finally {
            if (zos != null) try { zos.close(); } catch (IOException e) { e.printStackTrace(); }
            if (fos != null) try { fos.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void addToZipFile(File file, ZipOutputStream zos) throws FileNotFoundException, IOException {
        if (zos == null || file == null || !file.exists()) {
            return;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }
        } finally {
            zos.closeEntry();
            if (fis != null) fis.close();
        }
   }
}
