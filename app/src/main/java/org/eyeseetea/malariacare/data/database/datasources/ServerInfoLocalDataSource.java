package org.eyeseetea.malariacare.data.database.datasources;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.IServerInfoDataSource;
import org.eyeseetea.malariacare.data.remote.api.BasicAuthenticator;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.ServerInfo;

import java.util.regex.Pattern;

import okhttp3.Response;

import static org.eyeseetea.malariacare.data.remote.api.OkHttpClientDataSource.executeCall;
import static org.eyeseetea.malariacare.data.remote.api.OkHttpClientDataSource.parseResponse;

public class ServerInfoLocalDataSource implements IServerInfoDataSource {

    private Context context;

    public ServerInfoLocalDataSource(Context context){
        this.context = context;
    }

    @Override
    public ServerInfo get() {
        return new ServerInfo(getServerVersion());
    }

    @Override
    public void save(ServerInfo serverInfo) {
        saveServerVersion(serverInfo);
    }

    @Override
    public void clear() {
        clearServerVersion();
    }

    private int getServerVersion() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(context.getResources().getString(R.string.server_version_preference), -1);
    }

    private void saveServerVersion(ServerInfo serverInfo) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(context.getString(R.string.server_version_preference), serverInfo.getVersion());
        editor.commit();
    }

    private void clearServerVersion() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(context.getString(R.string.server_version_preference), -1);
        editor.commit();
    }
}
