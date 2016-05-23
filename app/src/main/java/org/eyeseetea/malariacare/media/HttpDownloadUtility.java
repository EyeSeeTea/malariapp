package org.eyeseetea.malariacare.media;

/**
 * Created by arrizabalaga on 17/05/16.
 */
import android.util.Log;

import org.eyeseetea.malariacare.DashboardActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 * A utility that downloads a file from a URL.
 * @author www.codejava.net
 *
 */
public class HttpDownloadUtility {
    private static final int BUFFER_SIZE = 4096;
    public static final String TAG = "HttpDownloadUtility";
    private static final String GOOGLE_DRIVE_URL="https://docs.google.com/uc?id=%s&export=download";
    public static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";

    /**
     * Downloads a file from a URL
     * @param fileID The google drive file id
     * @throws IOException
     */
    public static String downloadFile(String fileID)
            throws IOException {

        String fileName=null;
        String fileURL=String.format(GOOGLE_DRIVE_URL,fileID);
        String saveDir= DashboardActivity.dashboardActivity.getFilesDir().getAbsolutePath();

        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            Log.i(TAG,"No file to download. Server replied HTTP code: " + responseCode);
            httpConn.disconnect();
            return null;
        }



        // always check HTTP response code first

        String disposition = httpConn.getHeaderField(CONTENT_DISPOSITION_HEADER);

        if(disposition==null || disposition.isEmpty()){
            throw new IOException("No disposition header, check if file shows \"can't scan for viruses\"");
        }

        fileName = getFileName(disposition);

        String contentType = httpConn.getContentType();
        int contentLength = httpConn.getContentLength();



        Log.i(TAG,"Content-Type = " + contentType);
        Log.i(TAG,"Content-Disposition = " + disposition);
        Log.i(TAG,"Content-Length = " + contentLength);
        Log.i(TAG,"FileName = " + fileName);

        // opens input stream from the HTTP connection
        InputStream inputStream = httpConn.getInputStream();
        String saveFilePath = saveDir + File.separator + fileName;

        // opens an output stream to save into file
        FileOutputStream outputStream = new FileOutputStream(saveFilePath);

        int bytesRead = -1;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();

        Log.i(TAG,"File downloaded");

        httpConn.disconnect();
        return fileName;
    }

    private static String getFileName(String disposition) throws IOException{

        String[] elements=disposition.split(";");
        for(int i=0;i<elements.length;i++){
            String element=elements[i];
            if(element.startsWith("filename=")){
                String filename=element.replace("filename=","");
                filename=filename.replace("\"","");
                return filename;
            }
        }

        throw  new IOException(String.format("Filename not found in %s",disposition));
    }
}