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
        int[] month = NextScheduleMonths.getMonthArray(serverUrl);
        return new Server(getServerUrl(), new NextScheduleMonths(month));
    }

    private String getServerUrl() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context);
        String serverUrl = sharedPreferences.getString(
                PreferencesState.getInstance().getContext().getResources().getString(
                        R.string.dhis_url), "");
        return serverUrl;
    }
}
