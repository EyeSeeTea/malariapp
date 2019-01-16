package org.eyeseetea.malariacare.data.database.datasources;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.IServerInfoDataSource;
import org.eyeseetea.malariacare.domain.entity.ServerInfo;

public class ServerInfoLocalDataSource implements IServerInfoDataSource {

    private Context context;

    public ServerInfoLocalDataSource(Context context){
        this.context = context;
    }

    @Override
    public ServerInfo get() {
        ServerInfo serverInfo = new ServerInfo(getServerVersion());
        if(!getIsServerVersionSupported()) {
            serverInfo.markAsUnsupported();
        }
        return serverInfo;
    }

    @Override
    public void save(ServerInfo serverInfo) {
        saveServerVersion(serverInfo);
    }

    private boolean getIsServerVersionSupported() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getResources().getString(R.string.is_server_version_supported), true);
    }

    private int getServerVersion() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(context.getResources().getString(R.string.server_version_preference), -1);
    }

    private void saveServerVersion(ServerInfo serverInfo) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(context.getString(R.string.server_version_preference), serverInfo.getVersion());
        editor.putBoolean(context.getString(R.string.is_server_version_supported), serverInfo.isServerSupported());
        editor.commit();
    }
}
