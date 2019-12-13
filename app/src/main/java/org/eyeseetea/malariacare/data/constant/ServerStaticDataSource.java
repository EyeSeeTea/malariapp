package org.eyeseetea.malariacare.data.constant;

import android.content.Context;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.ReadableServerDataSource;
import org.eyeseetea.malariacare.domain.entity.Server;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ServerStaticDataSource implements ReadableServerDataSource {

    private Context context;

    public ServerStaticDataSource(Context context){
        this.context = context;
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

    @NotNull
    @Override
    public Server get() {
        return getAll().get(0);
    }
}
