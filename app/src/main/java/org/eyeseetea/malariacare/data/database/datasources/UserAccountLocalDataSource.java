/*
 * Copyright (c) 2017.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.data.database.datasources;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.IDataSourceCallback;
import org.eyeseetea.malariacare.data.IUserAccountDataSource;
import org.eyeseetea.malariacare.data.database.model.ServerDB;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.PopulateDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.UserAccount;

public class UserAccountLocalDataSource implements IUserAccountDataSource {

    Context mContext;

    public UserAccountLocalDataSource(Context context){
        mContext = context;
    }

    @Override
    public void logout(IDataSourceCallback<Void> callback) {
        clearPreferences();

        Session.logout();

        PreferencesState.getInstance().clearOrgUnitPreference();

        PopulateDB.wipeDatabase();

        callback.onSuccess(null);
    }

    @Override
    public void login(Credentials credentials, IDataSourceCallback<UserAccount> callback) {

        saveUser(credentials);

        saveCredentials(credentials);

        callback.onSuccess(null);
    }

    private void saveUser(Credentials credentials) {
        UserDB user = new UserDB(credentials.getUserUid(), credentials.getUsername());
        user.setUsername(credentials.getUsername());
        user.save();

        Session.setUser(user);
    }

    private void saveCredentials(Credentials credentials) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(mContext.getString(R.string.dhis_url), credentials.getServerURL());
        editor.putString(mContext.getString(R.string.dhis_user), credentials.getUsername());
        editor.putString(mContext.getString(R.string.dhis_password), credentials.getPassword());
        editor.commit();

        PreferencesState.getInstance().reloadPreferences();
        Session.setCredentials(credentials);
    }

    private void clearPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(mContext.getString(R.string.dhis_url), "");
        editor.putString(mContext.getString(R.string.dhis_user), "");
        editor.putString(mContext.getString(R.string.dhis_password), "");
        editor.putInt(mContext.getString(R.string.server_version_preference), -1);
        editor.putBoolean(mContext.getString(R.string.is_server_version_supported), true);
        editor.commit();
        PreferencesState.getInstance().reloadServerUrl();
    }
}
