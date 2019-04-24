package org.eyeseetea.malariacare.data.repositories;

import android.content.Context;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository;
import org.eyeseetea.malariacare.domain.entity.Server;

import java.util.ArrayList;
import java.util.List;

public class ServerRepository implements IServerRepository {
    private Context context;

    public ServerRepository (Context context){
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
}
