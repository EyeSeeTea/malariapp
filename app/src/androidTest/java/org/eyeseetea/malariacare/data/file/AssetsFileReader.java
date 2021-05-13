package org.eyeseetea.malariacare.data.file;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class AssetsFileReader implements IFileReader {

    public String getStringFromFile(Class clazz, String filename) throws IOException {
        return getContentFromFile(clazz, filename);
    }

    @NonNull
    private String getContentFromFile(Class clazz, String filename) throws IOException {
        FileInputStream inputStream = new FileInputStream(getFile(clazz, filename));
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private File getFile(Class clazz, String filename) {
        ClassLoader classLoader = clazz.getClassLoader();
        URL resource = classLoader.getResource(filename);
        return new File(resource.getPath());
    }

    @Override
    public String getStringFromFile(String filename) throws IOException {
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        InputStream inputStream = testContext.getAssets().open(filename);

        InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}