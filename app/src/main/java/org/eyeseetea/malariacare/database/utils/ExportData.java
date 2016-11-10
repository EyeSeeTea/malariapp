package org.eyeseetea.malariacare.database.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.FileProvider;
import android.util.Log;

import org.eyeseetea.malariacare.BuildConfig;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.sdk.SdkController;
import org.eyeseetea.malariacare.utils.AUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by idelcano on 26/08/2016.
 */
public class ExportData {

    private final static String TAG=".ExportData";

    /**
    * Temporal folder that contains all the files to send
    */
    private final static String EXPORT_DATA_FOLDER="exportdata/";
    /**
     * Temporal file to be attached
     */
    private final static String EXPORT_DATA_FILE="compressedData.zip";
    /**
     * Temporal file that contains phonemetadata and app version info
     */
    private final static String EXTRA_INFO="extrainfo.txt";
    /**
     * Databases folder
     */
    private final static String DATABASE_FOLDER="databases/";
    /**
     * Shared preferences folder
     */
    private final static String SHAREDPREFERENCES_FOLDER="shared_prefs/";

    /**
     * This method create the dump and returns the intent
     */
    public static Intent dumpAndSendToAIntent(Activity activity) {
        ExportData.removeDumpIfExist(activity);
        File tempFolder = new File(getCacheDir()+"/"+EXPORT_DATA_FOLDER);
        tempFolder.mkdir();
        //copy databases
        dumpDatabase(AppDatabase.NAME + ".db", tempFolder);
        dumpDatabase(SdkController.getDhisDatabaseName() + ".db", tempFolder);
        //Copy the sharedPreferences
        dumpSharedPreferences(tempFolder);

        //copy phonemetadata and gradle version
        File customInformation= new File(tempFolder+"/"+EXTRA_INFO);
        dumpMetadata(customInformation, activity);

        //compress and send
        File compressedFile=compressFolder(tempFolder);
        if(compressedFile==null) {
            return null;
        }
        return createEmailIntent(activity, compressedFile);
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
            FileWriter fw = new FileWriter(customInformation.getAbsoluteFile(),true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("Flavour: "+ BuildConfig.FLAVOR+"\n");
            bw.write(Session.getPhoneMetaData().getPhone_metaData()+"\n");
            bw.write("Version code: "+ BuildConfig.VERSION_CODE+"\n");
            bw.write("Version name: "+ BuildConfig.VERSION_NAME+"\n");
            bw.write("Aplication Id: "+ BuildConfig.APPLICATION_ID+"\n");
            bw.write("Build type: "+ BuildConfig.BUILD_TYPE+"\n");
            bw.write("Hash: "+ AUtils.getCommitHash(activity));

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
        if(tempFolder.listFiles()==null) {
            Log.d(TAG, "Error, nothing to convert");
            return null;
        }
        zipFolder(tempFolder.getAbsolutePath(),getCacheDir()+"/"+EXPORT_DATA_FILE);
        File file =new File(getCacheDir()+"/"+EXPORT_DATA_FILE);
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
            copyFile(currentDB, backupDB);
        }
    }

    /**
     * This method dump the sharedPreferences
     */
    private static void dumpSharedPreferences(File tempFolder) {
        File files[] = getSharedPreferencesFolder().listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (int i=0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
            copyFile(files[i], new File(tempFolder, files[i].getName()));
        }
    }

    /**
     * This method copy a file in other file
     */
    private static void copyFile(File current, File backup) {
        if (current.exists()) {

            try {
                FileChannel src = new FileInputStream(current)
                        .getChannel();
                FileChannel dst = new FileOutputStream(backup)
                        .getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }catch (IOException e)
            {
                e.printStackTrace();
                Log.d(TAG,"Error exporting file "+current+ " to "+backup);
            }
        }
    }

    /**
     * This method returns the app cache dir
     */
    private static File getCacheDir(){
        return PreferencesState.getInstance().getContext().getCacheDir();

    }

    /**
     * This method returns the app path
     */
    private static String getAppPath(){
        return "/data/data/" + PreferencesState.getInstance().getContext().getPackageName()+"/";

    }

    /**
     * This method returns the sharedPreferences app folder
     */
    private static File getSharedPreferencesFolder(){
        String sharedPreferencesPath = getAppPath() + SHAREDPREFERENCES_FOLDER;
        File file = new File(sharedPreferencesPath);
        return file;
    }

    /**
     * This method returns the databases app folder
     */
    private static File getDatabasesFolder(){
        String databasesPath = getAppPath() + DATABASE_FOLDER;
        File file = new File(databasesPath);
        return file;
    }

    /**
     * This method create the email intent
     */
    private static Intent createEmailIntent(Activity activity, File data) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("*/*");

        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                new String[] { "" });

        Random r = new Random();

        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                "Local db " + r.nextInt());
        //sets file as readable for external apps
        data.setReadable(true,false);
        Log.d(TAG,data.toURI()+"");
        emailIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(activity, "org.eyeseetea.malariacare.database.utils.ExportData", data));
        Intent chooser = Intent.createChooser(
                emailIntent,
                activity.getResources().getString(R.string.export_data_option_title));
        return chooser;

    }

    /**
     * This method remove the dump.
     */
    public static void removeDumpIfExist(Activity activity) {
        File file= new File(activity.getCacheDir()+"/"+EXPORT_DATA_FILE);
        file.delete();

        File tempFolder = new File(activity.getCacheDir()+"/"+EXPORT_DATA_FOLDER);
        File[] files=tempFolder.listFiles();
        if(files==null)
            return;
        for (int i=0; i < files.length; i++)
        {
            files[i].delete();
        }
        tempFolder.delete();
    }
}
