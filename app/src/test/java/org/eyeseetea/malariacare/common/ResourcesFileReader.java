package org.eyeseetea.malariacare.common;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;

public final class ResourcesFileReader  {

    public String getStringFromFile(String filename) throws IOException {
        FileInputStream inputStream = new FileInputStream(getFile(getClass(), filename));

        InputStreamReader isr = new InputStreamReader(inputStream, Charset.defaultCharset());
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private static File getFile(Class clazz, String filename) {
        ClassLoader classLoader = clazz.getClassLoader();
        URL resource = classLoader.getResource(filename);
        return new File(resource.getPath());
    }

    @NonNull
    public Gson createGson() {
        GsonBuilder builder = new GsonBuilder();

        builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        // Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter());
        builder.registerTypeAdapter(Date.class, new DateTypeAdapter());

        return builder.create();
    }
}