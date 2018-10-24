package org.eyeseetea.malariacare.data.repositories;

import android.content.Context;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository;
import org.eyeseetea.malariacare.domain.entity.NextScheduleMonths;
import org.eyeseetea.malariacare.domain.entity.Server;

import java.util.ArrayList;
import java.util.List;


public class ServerRepository implements IServerRepository {

    Context context;

    public ServerRepository(Context context) {
        this.context = context;
    }

    @Override
    public List<Server> getServers() {
        List<Server> servers = new ArrayList<>();
        for(String serverUrl:getServerList()){
            int [] month = NextScheduleMonths.getMonthArray(serverUrl);
            servers.add(new Server(serverUrl, new NextScheduleMonths(month)));
        }
        return servers;
    }

    private String[] getServerList() {
        return context.getResources().getStringArray(R.array.server_list);
    }
}