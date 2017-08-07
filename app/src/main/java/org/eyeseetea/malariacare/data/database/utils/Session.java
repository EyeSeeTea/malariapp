/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.data.database.utils;

import android.location.Location;
import android.util.Log;
import android.widget.ListView;

import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.metadata.PhoneMetaData;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.usecase.LoadUserAndCredentialsUseCase;
import org.eyeseetea.malariacare.layout.adapters.dashboard.IDashboardAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An application scoped object that stores transversal information:
 *  -User
 *  -Survey
 *  -..
 */
public class Session {

    private final static String TAG=".Session";

    /**
     * The current selected survey by module
     */
    private static Map<String, SurveyDB> surveyMappedByModule = new HashMap<>();
    /**
     *  The current selected surveyFeedback
    */
    private static SurveyDB surveyFeedback;
    /**
    /**
     * The current user
     */
    private static UserDB user;

    /**
     * The current location
     */
    private static Location location;

    /**
     * Map that holds non serializable results from services
     */
    private static Map<String,Object> serviceValues = new HashMap<>();

    /**
     * Adapters that hold dashboard sent and unset surveys adapters
     */
    private static IDashboardAdapter adapterUnsent, adapterSent, adapterOrgUnit;

    public static ListView listViewUnsent, listViewSent;

    /**
     * Cache containing the list of ordered items that compounds each tab
     */
    private static Map<Long, List<? extends BaseModel>> tabsCache = new HashMap<>();

    /**
     * The current phone metadata
     */
    private static PhoneMetaData phoneMetaData;

    private static Credentials credentials;


    public static SurveyDB getSurveyByModule(String module) {
        return surveyMappedByModule.get(module);
    }

    public static void setSurveyByModule(SurveyDB survey, String module) {
            surveyMappedByModule.put(module,survey);
    }

    public static UserDB getUser() {
        if(user==null)
            user= UserDB.getLoggedUser();
        return user;
    }

    public static void setUser(UserDB user) {
        Log.d(TAG,"setUser: "+user);
        Session.user = user;
    }

    public static Credentials getCredentials() {
        if (credentials == null) {
            LoadUserAndCredentialsUseCase loadUserAndCredentialsUseCase =
                    new LoadUserAndCredentialsUseCase(
                            PreferencesState.getInstance().getContext());
            loadUserAndCredentialsUseCase.execute();
            if (credentials == null) {
                return  null;
            }
        }
        return credentials;
    }

    public static void setCredentials(Credentials credentials) {
        Session.credentials = credentials;
    }

    public static IDashboardAdapter getAdapterOrgUnit() {
        return adapterOrgUnit;
    }

    public static void setAdapterOrgUnit(IDashboardAdapter adapterOrgUnit) {
        Session.adapterOrgUnit = adapterOrgUnit;
    }

    public static IDashboardAdapter getAdapterUnsent() {
        return adapterUnsent;
    }

    public static void setAdapterUnsent(IDashboardAdapter adapterUnsent) {
        Session.adapterUnsent = adapterUnsent;
    }

    public static IDashboardAdapter getAdapterSent() {
        return adapterSent;
    }

    public static void setAdapterSent(IDashboardAdapter adapterSent) {
        Session.adapterSent = adapterSent;
    }

    public static Map<Long, List<? extends BaseModel>> getTabsCache() {
        return tabsCache;
    }

    /**
     * Closes the current session when the user logs out
     */
    public static void logout(){
        List<SurveyDB> surveys = SurveyDB.getAllUnsentUnplannedSurveys();
        for (SurveyDB survey : surveys) {
            survey.delete();
        }
        if(user!=null){
            user.delete();
            user=null;
        }
        surveyMappedByModule=new HashMap<>();
        adapterUnsent=null;
        if(serviceValues!=null){
            serviceValues.clear();
        }

        Session.setUser(null);

        Session.setCredentials(null);
    }

    /**
     * Puts a pair key/value into a shared map.
     * Used to share values that are not serializable and thus cannot be put into an intent (domains and so).
     * @param key
     * @param value
     */
    public static void putServiceValue(String key, Object value){
        Log.i(TAG,"putServiceValue("+key+", "+value.toString()+")");
        serviceValues.put(key,value);
    }

    /**
     * Pops the value of the given key out of the map.
     * @param key
     * @return
     */
    public static Object popServiceValue(String key){
        return serviceValues.get(key);
//        return serviceValues.remove(key);
    }

    /**
     * Clears the service values in memory.
     * Used for clean testing.
     */
    public static void clearServiceValues(){
        serviceValues.clear();
    }

    public static Location getLocation() {
        return location;
    }

    public static void setLocation(Location location) {
        Session.location = location;
    }

    public static PhoneMetaData getPhoneMetaData(){return phoneMetaData;}

    public static void setPhoneMetaData(PhoneMetaData phoneMetaData) {
        Session.phoneMetaData = phoneMetaData;
    }

}
