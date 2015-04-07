package com.android.settings.slim.util;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.StringBuilder;
import java.util.Scanner;

import com.android.settings.R;

interface IChangelog {
    public void onResponseReceived(String result);
}

public abstract class Changelog extends AsyncTask<Context, Integer, String> implements IChangelog {

    private static final String CHANGELOG_FILE = "/system/etc/changelog.txt";

    public abstract void onResponseReceived(String result);

    @Override
    protected String doInBackground(Context... arg) {
        Context context = arg[0];
        File file = new File(CHANGELOG_FILE);
        if (!file.exists()) {
            return context.getString(R.string.no_changelog_summary);
        }

        StringBuilder sb = new StringBuilder();
        Scanner scanner = null;
        String changelog;
        try {
            scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                sb.append(line);
                sb.append("\n");
            }
            changelog = sb.toString();
        } catch (FileNotFoundException ex) {
            changelog = ex.getMessage();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return changelog;
    }

    @Override
    protected void onPostExecute(String result) {
        onResponseReceived(result);
    }
}
