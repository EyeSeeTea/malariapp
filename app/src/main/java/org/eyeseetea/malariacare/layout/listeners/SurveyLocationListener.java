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

package org.eyeseetea.malariacare.layout.listeners;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import org.eyeseetea.malariacare.data.database.utils.LocationMemory;
import org.eyeseetea.malariacare.data.database.utils.Session;

/**
 * Basic location listener that persist coordinates associated to every survey in preferences (probably to review)
 * Created by arrizabalaga on 23/09/15.
 */
public class SurveyLocationListener implements LocationListener {

    private static String TAG=".SurveyLocation";
    private long idSurvey;

    public SurveyLocationListener(long idSurvey){
        this.idSurvey=idSurvey;
    }

    public void saveLocation(Location location){
        LocationMemory.put(idSurvey, location);
    }

    /**
     * Listens for 1 locationChange event
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, String.format("onLocationChanged idSurvey:%d location: %s", idSurvey, location.toString()));
        //Save location
        saveLocation(location);

        //No more updates
        LocationManager locationManager=(LocationManager)LocationMemory.getContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
