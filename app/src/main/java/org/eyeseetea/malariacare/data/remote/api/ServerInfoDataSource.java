package org.eyeseetea.malariacare.data.remote.api;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.okhttp.Response;

import org.eyeseetea.malariacare.data.IServerInfoDataSource;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.ServerInfo;

import java.util.regex.Pattern;

import static org.eyeseetea.malariacare.data.remote.api.OkHttpClientDataSource.executeCall;
import static org.eyeseetea.malariacare.data.remote.api.OkHttpClientDataSource.parseResponse;

public class ServerInfoDataSource implements IServerInfoDataSource {

    private static final String SERVER_VERSION_CALL = "api/system/info/";
    private static final String VERSION = "version";

    private static final String TAG = ".PullDhisApiDataSource";
    private Credentials credentials;

    public ServerInfoDataSource(Credentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public ServerInfo get() {
        return new ServerInfo(getServerVersion(credentials));
    }

    public static Integer getServerVersion(Credentials credentials) {
        Integer version = null;
        try {
            Response response = executeCall(new BasicAuthenticator(credentials), credentials.getServerURL(), SERVER_VERSION_CALL);
            JsonNode jsonNode = parseResponse(response.body().string());
            JsonNode jsonVersionNode = jsonNode.get(VERSION);
            String[] completedVersionParts = jsonVersionNode.asText().split(Pattern.quote("."));
            version = Integer.parseInt(completedVersionParts[1]);
        } catch (Exception ex) {
            Log.e(TAG, "Cannot read server version from server with");
            ex.printStackTrace();
        }
        return version;
    }
}
