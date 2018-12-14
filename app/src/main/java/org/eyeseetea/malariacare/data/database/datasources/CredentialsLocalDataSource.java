package org.eyeseetea.malariacare.data.database.datasources;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.boundaries.ICredentialsDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.ICredentialsRepository;
import org.eyeseetea.malariacare.domain.entity.Credentials;

public class CredentialsLocalDataSource implements ICredentialsDataSource {
    private Context mContext;

    public CredentialsLocalDataSource(Context context) {
        mContext = context;
    }

    @Override
    public Credentials getCredentials() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                mContext);

        String serverURL = sharedPreferences.getString(mContext.getString(R.string.dhis_url), "");
        String username = sharedPreferences.getString(mContext.getString(R.string.dhis_user), "");
        String password = sharedPreferences.getString(mContext.getString(R.string.dhis_password),
                "");

       return new Credentials(serverURL, username, password);
    }

    @Override
    public void saveCredentials(Credentials credentials) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(mContext.getString(R.string.dhis_url),credentials.getServerURL());
        editor.putString(mContext.getString(R.string.dhis_user),credentials.getUsername());
        editor.putString(mContext.getString(R.string.dhis_password),credentials.getPassword());
    }
}
