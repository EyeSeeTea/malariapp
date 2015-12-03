/*
 * Copyright (c) 2015.
 *
 * This file is part of Health Network QIS App.
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

package org.eyeseetea.malariacare;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.telephony.TelephonyManager;

import com.crashlytics.android.Crashlytics;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.utils.LocationMemory;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.phonemetadata.PhoneMetaData;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;

import io.fabric.sdk.android.Fabric;

/**
 * Created by nacho on 04/08/15.
 */
public class EyeSeeTeaApplication extends Dhis2Application  {

    public Class<? extends Activity> getMainActivity() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (User.getLoggedUser() != null && sharedPreferences.getBoolean(getApplicationContext().getResources().getString(R.string.pull_metadata),false)){
            return new DashboardActivity().getClass();
        }else {
         //   return new DashboardActivity().getClass();
            return new ProgressActivity().getClass();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        PreferencesState.getInstance().init(getApplicationContext());
        LocationMemory.getInstance().init(getApplicationContext());
        FlowManager.init(this, "_EyeSeeTeaDB");

        //Set the Phone metadata
        PhoneMetaData phoneMetaData=this.getPhoneMetadata();
        Session.setPhoneMetaData(phoneMetaData);
    }

    PhoneMetaData getPhoneMetadata(){
        PhoneMetaData phoneMetaData=new PhoneMetaData();
        TelephonyManager phoneManagerMetaData=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String imei = phoneManagerMetaData.getDeviceId();
        String phone = phoneManagerMetaData.getLine1Number();
        String serial = phoneManagerMetaData.getSimSerialNumber();
        phoneMetaData.setImei(imei);
        phoneMetaData.setPhone_number(phone);
        phoneMetaData.setPhone_serial(serial);
        return phoneMetaData;
    }
    @Override
    public void onTerminate() {
        super.onTerminate();
        FlowManager.destroy();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
