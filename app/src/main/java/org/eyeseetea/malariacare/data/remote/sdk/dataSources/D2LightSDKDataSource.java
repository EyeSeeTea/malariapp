package org.eyeseetea.malariacare.data.remote.sdk.dataSources;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.eyeseetea.dhis2.lightsdk.D2Api;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.domain.entity.Credentials;

public class D2LightSDKDataSource {

    protected D2Api d2Api;
    private Context context;

    public D2LightSDKDataSource(Context context) {
        this.context = context;
    }

    public D2Api getD2Api()
    {
        Credentials credentials = getCredentials();

        d2Api = new D2Api.Builder()
                .url(credentials.getServerURL())
                .credentials(credentials.getUsername(), credentials.getPassword())
                .build();

        return d2Api;
    }


    private Credentials getCredentials() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context);

        String serverURL = sharedPreferences.getString(context.getString(R.string.dhis_url), "");
        String username = sharedPreferences.getString(context.getString(R.string.dhis_user), "");
        String password = sharedPreferences.getString(context.getString(R.string.dhis_password),
                "");

        return new Credentials(serverURL, username, password);
    }
}
