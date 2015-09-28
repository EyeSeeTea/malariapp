/*
 * Copyright (c) 2015.
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

package org.eyeseetea.malariacare.database.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;

/**
 * Created by arrizabalaga on 23/09/15.
 */
public class LocationMemory {

    private static final String PREFIX_LATITUDE="LAT";
    private static final String PREFIX_LONGITUDE="LNG";

    /**
     * Singleton reference
     */
    private static LocationMemory instance;

    /**
     * App context required to recover sharedpreferences
     */
    static Context context;

    private LocationMemory(){ }

    /**
     * Init method required to hold a reference to the app context
     * @param context
     */
    public void init(Context context){
        this.context=context;
    }

    /**
     * Singleton method
     * @return
     */
    public static LocationMemory getInstance(){
        if(instance==null){
            instance=new LocationMemory();
        }
        return instance;
    }

    /**
     * Saves the coordinates for the given survey into internal shared preferences
     * @param idSurvey
     * @param location
     */
    public synchronized static void put(long idSurvey, Location location){
        if(location==null){
            return;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(instance.context);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putFloat(PREFIX_LONGITUDE + idSurvey, (float) location.getLongitude());
        editor.putFloat(PREFIX_LATITUDE + idSurvey, (float) location.getLatitude());
        editor.commit();
    }

    /**
     * Gets the coordinates for a given survey id from the shared preferences
     * @param idSurvey
     * @return A Location if it is stored in preferences, null otherwise
     */
    public static Location get(long idSurvey){
        Location location=new Location(LocationManager.GPS_PROVIDER);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(instance.context);

        float longitude=sharedPreferences.getFloat(PREFIX_LONGITUDE+idSurvey, 0f);
        float latitude=sharedPreferences.getFloat(PREFIX_LATITUDE+idSurvey, 0f);

        //No coordinates were stored for the given survey
        if(longitude==0 && latitude==0){
            return null;
        }

        //Found
        location.setLongitude(longitude);
        location.setLatitude(latitude);
        return location;
    }

    /**
     * Gets the app context
     * @return
     */
    public static Context getContext(){
        return instance.context;
    }

}
