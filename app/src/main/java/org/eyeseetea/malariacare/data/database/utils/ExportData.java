package org.eyeseetea.malariacare.data.database.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;

import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import android.util.Log;

import org.eyeseetea.malariacare.BuildConfig;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.sdk.common.DatabaseUtils;
import org.eyeseetea.sdk.common.FileUtils;
import org.hisp.dhis.client.sdk.android.api.persistence.DbDhis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExportData {

    private final static String TAG = ".ExportData";

    /**
     * Temporal folder that contains all the files to send
     */
    private final static String EXPORT_DATA_FOLDER = "exportdata/";
    /**
     * Temporal file to be attached
     */
    public final static String EXPORT_DATA_FILE = "compressedData.zip";
    /**
     * Temporal file that contains phonemetadata and app version info
     */
    private final static String EXTRA_INFO = "extrainfo.txt";
    /**
     * Databases folder
     */
    private final static String DATABASE_FOLDER = "databases/";
    /**
     * Shared preferences folder
     */
    private final static String SHAREDPREFERENCES_FOLDER = "shared_prefs/";

    /**
     * This method create the dump and returns the intent
     */
    public static Intent dumpAndSendToAIntent(Activity activity) {
        File compressedFile = dumpAndCompress(activity);
        if (compressedFile == null) return null;

        return createEmailIntent(activity, compressedFile);
    }

    private static File dumpAndCompress(Activity activity) {
        ExportData.removeDumpIfExist(activity);
        File tempFolder = new File(getCacheDir() + "/" + EXPORT_DATA_FOLDER);
        tempFolder.mkdir();
        //copy databases
        dumpDatabase(AppDatabase.NAME + ".db", tempFolder);
        dumpDatabase(DbDhis.NAME + ".db", tempFolder);
        //Copy the sharedPreferences
        dumpSharedPreferences(tempFolder);

        //copy phonemetadata and gradle version
        File customInformation = new File(tempFolder + "/" + EXTRA_INFO);
        dumpMetadata(customInformation, activity);

        //compress and send
        File compressedFile = compressFolder(tempFolder);
        if (compressedFile == null) {
            return null;
        }
        return compressedFile;
    }

    public static boolean dumpAndExportToLocalStorage(Activity activity) {
        boolean result = false;
        File compressedFile = dumpAndCompress(activity);

        if (compressedFile != null) {
            result = exportToDownloadsFolder(compressedFile);
        }

        return result;
    }

    private static boolean exportToDownloadsFolder(File compressedFile) {
        boolean result = false;

        File download_folder = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);

        File exportDataFile = new File(download_folder, EXPORT_DATA_FILE);

        try {
            FileUtils.copyFile(compressedFile, exportDataFile);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * This method create the dump the metadata in a temporally file
     */
    private static void dumpMetadata(File customInformation, Activity activity) {
        try {
            customInformation.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            FileWriter fw = new FileWriter(customInformation.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("Flavour: " + BuildConfig.FLAVOR + "\n");
            bw.write(Session.getPhoneMetaData().getPhone_metaData() + "\n");
            bw.write("Version code: " + BuildConfig.VERSION_CODE + "\n");
            bw.write("Version name: " + BuildConfig.VERSION_NAME + "\n");
            bw.write("Aplication Id: " + BuildConfig.APPLICATION_ID + "\n");
            bw.write("Build type: " + BuildConfig.BUILD_TYPE + "\n");
            bw.write("Hash: " + AUtils.getCommitHash(activity));

            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method checks if the tempfolder contains files and zip it.
     */
    private static File compressFolder(File tempFolder) {
        if (tempFolder.listFiles() == null) {
            Log.d(TAG, "Error, nothing to convert");
            return null;
        }
        zipFolder(tempFolder.getAbsolutePath(), getCacheDir() + "/" + EXPORT_DATA_FILE);
        File file = new File(getCacheDir() + "/" + EXPORT_DATA_FILE);
        return file;
    }


    /**
     * This method compress all the files in the temporal folder to be sent
     */
    private static void zipFolder(String inputFolderPath, String outputFilePath) {
        try {
            FileOutputStream fos = new FileOutputStream(outputFilePath);
            ZipOutputStream zos = new ZipOutputStream(fos);
            File srcFile = new File(inputFolderPath);
            File[] files = srcFile.listFiles();
            Log.d("", "Zip directory: " + srcFile.getName());
            for (int i = 0; i < files.length; i++) {
                Log.d("", "Adding file: " + files[i].getName());
                byte[] buffer = new byte[1024];
                FileInputStream fis = new FileInputStream(files[i]);
                zos.putNextEntry(new ZipEntry(files[i].getName()));
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();
        } catch (IOException ioe) {
            Log.e("", ioe.getMessage());
        }
    }


    /**
     * This method dump a database
     */
    private static void dumpDatabase(String dbName, File tempFolder) {
        File backupDB = null;
        if (tempFolder.canWrite()) {
            File currentDB = new File(getDatabasesFolder(), dbName);
            backupDB = new File(tempFolder, dbName);
            try {
                FileUtils.copyFile(currentDB, backupDB);
            } catch (IOException e) {
                Log.d(TAG, "Error exporting file " + currentDB + " to " + backupDB);
            }
        }
    }

    /**
     * This method dump the sharedPreferences
     */
    private static void dumpSharedPreferences(File tempFolder) {
        File files[] = getSharedPreferencesFolder().listFiles();
        Log.d("Files", "Size: " + files.length);
        for (int i = 0; i < files.length; i++) {
            Log.d("Files", "FileName:" + files[i].getName());
            File backupFile = new File(tempFolder, files[i].getName());
            try {
                FileUtils.copyFile(files[i], backupFile);
            } catch (IOException e) {
                Log.d(TAG, "Error exporting file " + files[i] + " to " + backupFile);
            }
        }
    }

    /**
     * This method returns the app cache dir
     */
    private static File getCacheDir() {
        return PreferencesState.getInstance().getContext().getCacheDir();

    }

    /**
     * This method returns the app path
     */
    private static String getAppPath() {
        return "/data/data/" + PreferencesState.getInstance().getContext().getPackageName() + "/";

    }

    /**
     * This method returns the sharedPreferences app folder
     */
    private static File getSharedPreferencesFolder() {
        String sharedPreferencesPath = DatabaseUtils.getAppPath(
                PreferencesState.getInstance().getContext().getPackageName())
                + SHAREDPREFERENCES_FOLDER;
        File file = new File(sharedPreferencesPath);
        return file;
    }

    /**
     * This method returns the databases app folder
     */
    private static File getDatabasesFolder() {
        String databasesPath = getAppPath() + DATABASE_FOLDER;
        File file = new File(databasesPath);
        return file;
    }

    /**
     * This method create the email intent
     */
    private static Intent createEmailIntent(Activity activity, File data) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("application/zip");

        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                new String[]{""});

        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                "Local " + PreferencesState.getInstance().getContext().getString(
                        R.string.app_name)
                        + " db " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                        Calendar.getInstance().getTime()));
        //sets file as readable for external apps
        data.setReadable(true, false);
        Log.d(TAG, data.toURI() + "");
        emailIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(activity,
                BuildConfig.APPLICATION_ID + ".data.database.utils.ExportData", data));
        Intent chooser = Intent.createChooser(
                emailIntent,
                activity.getResources().getString(R.string.export_data_option_title));
        return chooser;

    }

    /**
     * This method remove the dump.
     */
    public static void removeDumpIfExist(Activity activity) {
        File file = new File(activity.getCacheDir() + "/" + EXPORT_DATA_FILE);
        file.delete();

        File tempFolder = new File(activity.getCacheDir() + "/" + EXPORT_DATA_FOLDER);
        File[] files = tempFolder.listFiles();
        if (files == null) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
        tempFolder.delete();
    }

    public static void shareFileIntent(Activity activity, String data, String title,
            File attached) {
        ShareCompat.IntentBuilder.from(activity)
                .setType("text/plain")
                .addEmailTo("")
                .setSubject(title)
                .setStream(FileProvider.getUriForFile(activity,
                        BuildConfig.APPLICATION_ID + ".data.database.utils.ExportData", attached))
                .setText(data)
                .startChooser();
    }


}
