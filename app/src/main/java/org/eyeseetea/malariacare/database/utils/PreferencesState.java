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

package org.eyeseetea.malariacare.database.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton that holds info related to preferences
 * Created by arrizabalaga on 26/06/15.
 */
public class PreferencesState {

    private static String TAG=".PreferencesState";

    /**
     * Singleton reference
     */
    private static PreferencesState instance;

    /**
     * Selected scale, one between [xsmall,small,medium,large,xlarge,system]
     */
    private String scale;

    /**
     * Flag that determines if numerator/denominator are shown in scores.
     */
    private boolean showNumDen;

    private Boolean pullFromServer;
    static Boolean isPicture;

    /**
     * Map that holds the relationship between a scale and a set of dimensions
     */
    private Map<String, Map<String, Float>> scaleDimensionsMap;

    /**
     * Flag that determines if the location is required for push
     */
    private boolean locationRequired;

    static Context context;

    private PreferencesState(){ }

    public void init(Context context){
        this.context=context;
        scaleDimensionsMap=initScaleDimensionsMap();
        reloadPreferences();
    }

    public Context getContext() {
        return context;
    }

    public void reloadPreferences(){
        scale= initScale();
        showNumDen=initShowNumDen();
        locationRequired=initLocationRequired();
        Log.d(TAG,"reloadPreferences: scale:"+scale+" | showNumDen:"+showNumDen+" | locationRequired:"+locationRequired);
    }

    /**
     * Inits scale according to preferences
     * @return
     */
    private String initScale(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(instance.getContext());
        if (sharedPreferences.getBoolean(instance.getContext().getString(R.string.customize_fonts), false)) {
            return sharedPreferences.getString(instance.getContext().getString(R.string.font_sizes), instance.getContext().getString(R.string.font_size_system));
        }

        return context.getString(R.string.font_size_system);
    }

    /**
     * Inits flag according to preferences
     * @return
     */
    private boolean initShowNumDen(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(instance.getContext());
        return sharedPreferences.getBoolean(instance.getContext().getString(R.string.show_num_dems), false);
    }

    /**
     * Inits location flag according to preferences
     * @return
     */
    private boolean initLocationRequired(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(instance.getContext());
        return sharedPreferences.getBoolean(instance.getContext().getString(R.string.location_required), false);
    }

    /**
     * Inits maps of dimensions
     * @return
     */
    private Map<String, Map<String, Float>> initScaleDimensionsMap(){
        Map<String, Float> xsmall = new HashMap<>();
        String xsmallKey = instance.getContext().getString(R.string.font_size_level0),
                smallKey = context.getString(R.string.font_size_level1),
                mediumKey = context.getString(R.string.font_size_level2),
                largeKey = context.getString(R.string.font_size_level3),
                xlargeKey = context.getString(R.string.font_size_level4);

        xsmall.put(xsmallKey, context.getResources().getDimension(R.dimen.xsmall_xsmall_text_size));
        xsmall.put(smallKey, context.getResources().getDimension(R.dimen.xsmall_small_text_size));
        xsmall.put(mediumKey, context.getResources().getDimension(R.dimen.xsmall_medium_text_size));
        xsmall.put(largeKey, context.getResources().getDimension(R.dimen.xsmall_large_text_size));
        xsmall.put(xlargeKey, context.getResources().getDimension(R.dimen.xsmall_xlarge_text_size));
        Map<String, Float> small = new HashMap<>();
        small.put(xsmallKey, context.getResources().getDimension(R.dimen.small_xsmall_text_size));
        small.put(smallKey, context.getResources().getDimension(R.dimen.small_small_text_size));
        small.put(mediumKey, context.getResources().getDimension(R.dimen.small_medium_text_size));
        small.put(largeKey, context.getResources().getDimension(R.dimen.small_large_text_size));
        small.put(xlargeKey, context.getResources().getDimension(R.dimen.small_xlarge_text_size));
        Map<String, Float> medium = new HashMap<>();
        medium.put(xsmallKey, context.getResources().getDimension(R.dimen.medium_xsmall_text_size));
        medium.put(smallKey, context.getResources().getDimension(R.dimen.medium_small_text_size));
        medium.put(mediumKey, context.getResources().getDimension(R.dimen.medium_medium_text_size));
        medium.put(largeKey, context.getResources().getDimension(R.dimen.medium_large_text_size));
        medium.put(xlargeKey, context.getResources().getDimension(R.dimen.medium_xlarge_text_size));
        Map<String, Float> large = new HashMap<>();
        large.put(xsmallKey, context.getResources().getDimension(R.dimen.large_xsmall_text_size));
        large.put(smallKey, context.getResources().getDimension(R.dimen.large_small_text_size));
        large.put(mediumKey, context.getResources().getDimension(R.dimen.large_medium_text_size));
        large.put(largeKey, context.getResources().getDimension(R.dimen.large_large_text_size));
        large.put(xlargeKey, context.getResources().getDimension(R.dimen.large_xlarge_text_size));
        Map<String, Float> xlarge = new HashMap<>();
        xlarge.put(xsmallKey, context.getResources().getDimension(R.dimen.extra_xsmall_text_size));
        xlarge.put(smallKey, context.getResources().getDimension(R.dimen.extra_small_text_size));
        xlarge.put(mediumKey, context.getResources().getDimension(R.dimen.extra_medium_text_size));
        xlarge.put(largeKey, context.getResources().getDimension(R.dimen.extra_large_text_size));
        xlarge.put(xlargeKey, context.getResources().getDimension(R.dimen.extra_xlarge_text_size));

        Map scaleDimensionsMap = new HashMap<>();
        scaleDimensionsMap.put(xsmallKey, xsmall);
        scaleDimensionsMap.put(smallKey, small);
        scaleDimensionsMap.put(mediumKey, medium);
        scaleDimensionsMap.put(largeKey, large);
        scaleDimensionsMap.put(xlargeKey, xlarge);
        return scaleDimensionsMap;
    }

    public static PreferencesState getInstance(){
        if(instance==null){
            instance=new PreferencesState();
        }
        return instance;
    }

    public String getScale() {
        return scale;
    }

    public void setScale(String value){
        this.scale=value;
    }

    public boolean isShowNumDen() {
        return showNumDen;
    }

    public void setShowNumDen(boolean value){
        this.showNumDen=value;
    }

    public boolean isLocationRequired(){return locationRequired;}

    public void setLocationRequired(boolean value){
        this.locationRequired=value;
    }

    public Float getFontSize(String scale,String dimension){
        return scaleDimensionsMap.get(scale).get(dimension);
    }


    /**
     * Tells if metaData is pulled from server or locally populated
     * @return
     */
    public Boolean getPullFromServer() {
        if(pullFromServer==null){
            pullFromServer = context.getResources().getBoolean(R.bool.pullFromServer);
        }
        return pullFromServer;
    }

    public Class getMainActivity(){
        if(getPullFromServer()){
            return ProgressActivity.class;
        }

        return DashboardActivity.class;
    }

    public static boolean isPictureQuestion(){
        if(isPicture==null){
            isPicture = context.getResources().getBoolean(R.bool.picture);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Log.d(TAG,"isPicture:"+isPicture+"");
            if(isPicture){
                editor.putBoolean(context.getResources().getString(R.string.pull_metadata), false);
            }
            else{
                editor.putBoolean(context.getResources().getString(R.string.pull_csv), false);
            }
            editor.commit();
        }
        return isPicture;
    }
}
