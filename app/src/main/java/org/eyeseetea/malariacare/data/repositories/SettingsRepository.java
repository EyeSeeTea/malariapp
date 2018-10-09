package org.eyeseetea.malariacare.data.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.ServerMetadataDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISettingsRepository;
import org.eyeseetea.malariacare.domain.entity.NextScheduleMonths;
import org.eyeseetea.malariacare.domain.entity.Server;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.domain.entity.ServerMetadataItem;
import org.eyeseetea.malariacare.domain.entity.Settings;

import java.util.HashMap;


public class SettingsRepository implements ISettingsRepository {
    public static final String DEFAULT_SCHEDULE_MONTHS_VALUE ="https://data.psi-mis.org";
    public static final String ALTERNATIVE_SCHEDULE_MONTHS_VALUE ="https://zw.hnqis.org/";
    public static final HashMap<String, int[]> nextScheduleMonths = new HashMap<>();

    static {
        nextScheduleMonths.put(DEFAULT_SCHEDULE_MONTHS_VALUE, new int[]{2, 4, 6});
        nextScheduleMonths.put(ALTERNATIVE_SCHEDULE_MONTHS_VALUE, new int[]{1, 1, 6});
    }

    Context context;

    public SettingsRepository(Context context) {
        this.context = context;
    }

    @Override
    public Settings getSettings() {
        Server server = getServer();
        return new Settings(server);
    }

    private Server getServer() {
        String serverUrl=getServerUrl();
        return new Server(getServerUrl(), new NextScheduleMonths(getMonthArray(serverUrl)));
    }

    private String getServerUrl() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context);
        String serverUrl = sharedPreferences.getString(
                PreferencesState.getInstance().getContext().getResources().getString(
                        R.string.dhis_url), "");
        return serverUrl;
    }

    private int[] getMonthArray(String serverUrl) {
        if(nextScheduleMonths.containsKey(serverUrl))
        {
            return nextScheduleMonths.get(serverUrl);
        } else {
            return nextScheduleMonths.get(DEFAULT_SCHEDULE_MONTHS_VALUE);
        }
    }
}
