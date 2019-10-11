package org.eyeseetea.malariacare.data.database.datasources;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.IAllServersDataSource;
import org.eyeseetea.malariacare.data.IServerDataSource;
import org.eyeseetea.malariacare.data.database.model.ServerDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.Server;

import java.util.ArrayList;
import java.util.List;

public class ServerLocalDataSource implements IServerDataSource, IAllServersDataSource {

    private Context context;

    public ServerLocalDataSource(Context context){
        this.context = context;
    }

    @Override
    public Server get() {
        Server server = null;

        ServerDB serverDB = getServerFromDB();

        byte[] logo = null;

        if (serverDB != null) {
            if (serverDB.getLogo() != null) {
                logo = serverDB.getLogo().getBlob();
            }

            server = new Server(serverDB.getUrl(), serverDB.getName(),logo);
        }

        return server;
    }

    @Override
    public void save(Server server) {
        ServerDB serverDB = getServerFromDB();

        if (serverDB == null){
            serverDB = new ServerDB();
        }

        serverDB.setUrl(server.getUrl());
        serverDB.setName(server.getName());

        if (server.getLogo() != null) {
            serverDB.setLogo(new Blob(server.getLogo()));
        }

        serverDB.save();
    }

    @Override
    public List<Server> getAll() {
        String[] serverUrls = context.getResources().getStringArray(R.array.server_list);
        List<Server> servers = new ArrayList<>();

        for (String url:serverUrls) {
            Server server = new Server(url);
            servers.add(server);
        }

        return servers;
    }

    private ServerDB getServerFromDB() {
        return new Select().from(ServerDB.class).querySingle();
    }
}
