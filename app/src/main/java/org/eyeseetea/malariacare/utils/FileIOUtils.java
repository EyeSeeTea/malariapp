/*
 * Copyright (c) 2017.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

public class FileIOUtils {


    /**
     * Databases folder
     */
    private final static String DATABASE_FOLDER = "databases/";

    /**
     * This method copy a file in other file
     */
    public static void copyFile(File current, File backup) throws IOException {
        if (current.exists()) {
            FileChannel src = new FileInputStream(current)
                    .getChannel();
            FileChannel dst = new FileOutputStream(backup)
                    .getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
        }
    }

    public static void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);
        try {
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }


    /**
     * This method returns the databases app folder
     */
    public static File getDatabasesFolder() {
        String databasesPath = getAppPath() + DATABASE_FOLDER;
        File file = new File(databasesPath);
        return file;
    }

    /**
     * This method returns the app path
     */
    public static String getAppPath() {
        return "/data/data/" + PreferencesState.getInstance().getContext().getPackageName() + "/";

    }

    public static File getAppDatabaseFile() {
        return new File(getDatabasesFolder(), AppDatabase.NAME + ".db");
    }

    public static String getRawPath(String filename) {
        return String.format("android.resource://%s/raw/%s",
                removeExtension(PreferencesState.getInstance().getContext().getPackageName()), filename);
    }

    public static Uri getRawUri(String filename) {
        Uri url = Uri.parse(String.format("android.resource://%s/raw/%s",
                PreferencesState.getInstance().getContext().getPackageName(), removeExtension(filename)));
        return url;
    }

    public static AssetFileDescriptor getAssetFileDescriptorFromRaw(String filename) {
        Context context = PreferencesState.getInstance().getContext();
        AssetFileDescriptor afd = context.getResources().openRawResourceFd(
                getRawIdentifier(filename, context));
        return afd;
    }

    public static int getRawIdentifier(String filename, Context context) {
        if(filename.contains("."))
            filename=FileIOUtils.removeExtension(filename);
        return context.getResources().getIdentifier(filename, "raw",
                context.getPackageName());
    }

    public static String removeExtension(String filename) {
        return filename.substring(0, filename.lastIndexOf("."));
    }

}
