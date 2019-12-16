package org.eyeseetea.malariacare.data.database.datasources;


import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.ReadableServerDataSource;
import org.eyeseetea.malariacare.data.WritableServerDataSource;
import org.eyeseetea.malariacare.data.database.model.ServerDB;
import org.eyeseetea.malariacare.data.database.model.ServerDB_Table;
import org.eyeseetea.malariacare.domain.entity.Server;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ServerLocalDataSource implements ReadableServerDataSource, WritableServerDataSource {
    @Override
    public Server get() {
        ServerDB serverDB = getConnectedServerFromDB();

        return mapServer(serverDB);
    }

    @Override
    public void saveAll(@NotNull List<? extends Server> servers)  {
        Delete.table(ServerDB.class);

        for (Server server:servers) {
            save(server);
        }
    }

    @Override
    public void save(Server server) {
        ServerDB serverDB = getServerFromDByUrl(server.getUrl());

        if (serverDB == null){
            serverDB = new ServerDB();
        }

        serverDB.setUrl(server.getUrl());
        serverDB.setName(server.getName());
        serverDB.setConnected(server.isConnected());

        if (server.getLogo() != null) {
            serverDB.setLogo(new Blob(server.getLogo()));
        }

        serverDB.save();
    }

    @Override
    public List<Server> getAll() {
        List<Server> servers = new ArrayList<>();

        List<ServerDB> serversDB = getAllServersFromDB();

        for (ServerDB serverDB:serversDB) {
            servers.add(mapServer(serverDB));
        }

        return servers;
    }

    private ServerDB getConnectedServerFromDB() {
        return new Select().from(ServerDB.class)
                .where(ServerDB_Table.connected.is(true)).querySingle();
    }

    private List<ServerDB> getAllServersFromDB() {
        return new Select().from(ServerDB.class).queryList();
    }

    private ServerDB getServerFromDByUrl(String url) {
        return new Select().from(ServerDB.class)
                .where(ServerDB_Table.url.eq(url)).querySingle();
    }

    private Server mapServer(ServerDB serverDB) {
        Server server = null;

        byte[] logo = null;

        if (serverDB != null) {
            if (serverDB.getLogo() != null) {
                logo = serverDB.getLogo().getBlob();
            }

            server = new Server(serverDB.getUrl(), serverDB.getName(),logo,serverDB.isConnected());
        }
        return server;
    }

}
