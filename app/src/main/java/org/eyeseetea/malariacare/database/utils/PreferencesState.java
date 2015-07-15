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

import com.orm.SugarApp;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.utils.Constants;

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

    /**
     * Map that holds the relationship between a scale and a set of dimensions
     */
    private Map<String, Map<String, Float>> scaleDimensionsMap;

    private PreferencesState(){
        scaleDimensionsMap=initScaleDimensionsMap();
        reloadPreferences();
    }

    public void reloadPreferences(){
        scale= initScale();
        showNumDen=initShowNumDen();
        Log.d(TAG,"reloadPreferences: scale:"+scale+" | showNumDen:"+showNumDen);
    }

    /**
     * Inits scale according to preferences
     * @return
     */
    private String initScale(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SugarApp.getSugarContext());
        if (sharedPreferences.getBoolean(SugarApp.getSugarContext().getString(R.string.show_num_dems), false)) {
            return sharedPreferences.getString(SugarApp.getSugarContext().getString(R.string.font_sizes), Constants.FONTS_SYSTEM);
        }

        return Constants.FONTS_SYSTEM;
    }

    /**
     * Inits flag according to preferences
     * @return
     */
    private boolean initShowNumDen(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SugarApp.getSugarContext());
        return sharedPreferences.getBoolean(SugarApp.getSugarContext().getString(R.string.show_num_dems), false);
    }

    /**
     * Inits maps of dimensions
     * @return
     */
    private Map<String, Map<String, Float>> initScaleDimensionsMap(){
        Map<String, Float> xsmall = new HashMap<>();
        Context ctx= SugarApp.getSugarContext();
        xsmall.put(Constants.FONTS_XSMALL, ctx.getResources().getDimension(R.dimen.xsmall_xsmall_text_size));
        xsmall.put(Constants.FONTS_SMALL, ctx.getResources().getDimension(R.dimen.xsmall_small_text_size));
        xsmall.put(Constants.FONTS_MEDIUM, ctx.getResources().getDimension(R.dimen.xsmall_medium_text_size));
        xsmall.put(Constants.FONTS_LARGE, ctx.getResources().getDimension(R.dimen.xsmall_large_text_size));
        xsmall.put(Constants.FONTS_XLARGE, ctx.getResources().getDimension(R.dimen.xsmall_xlarge_text_size));
        Map<String, Float> small = new HashMap<>();
        small.put(Constants.FONTS_XSMALL, ctx.getResources().getDimension(R.dimen.small_xsmall_text_size));
        small.put(Constants.FONTS_SMALL, ctx.getResources().getDimension(R.dimen.small_small_text_size));
        small.put(Constants.FONTS_MEDIUM, ctx.getResources().getDimension(R.dimen.small_medium_text_size));
        small.put(Constants.FONTS_LARGE, ctx.getResources().getDimension(R.dimen.small_large_text_size));
        small.put(Constants.FONTS_XLARGE, ctx.getResources().getDimension(R.dimen.small_xlarge_text_size));
        Map<String, Float> medium = new HashMap<>();
        medium.put(Constants.FONTS_XSMALL, ctx.getResources().getDimension(R.dimen.medium_xsmall_text_size));
        medium.put(Constants.FONTS_SMALL, ctx.getResources().getDimension(R.dimen.medium_small_text_size));
        medium.put(Constants.FONTS_MEDIUM, ctx.getResources().getDimension(R.dimen.medium_medium_text_size));
        medium.put(Constants.FONTS_LARGE, ctx.getResources().getDimension(R.dimen.medium_large_text_size));
        medium.put(Constants.FONTS_XLARGE, ctx.getResources().getDimension(R.dimen.medium_xlarge_text_size));
        Map<String, Float> large = new HashMap<>();
        large.put(Constants.FONTS_XSMALL, ctx.getResources().getDimension(R.dimen.large_xsmall_text_size));
        large.put(Constants.FONTS_SMALL, ctx.getResources().getDimension(R.dimen.large_small_text_size));
        large.put(Constants.FONTS_MEDIUM, ctx.getResources().getDimension(R.dimen.large_medium_text_size));
        large.put(Constants.FONTS_LARGE, ctx.getResources().getDimension(R.dimen.large_large_text_size));
        large.put(Constants.FONTS_XLARGE, ctx.getResources().getDimension(R.dimen.large_xlarge_text_size));
        Map<String, Float> xlarge = new HashMap<>();
        xlarge.put(Constants.FONTS_XSMALL, ctx.getResources().getDimension(R.dimen.extra_xsmall_text_size));
        xlarge.put(Constants.FONTS_SMALL, ctx.getResources().getDimension(R.dimen.extra_small_text_size));
        xlarge.put(Constants.FONTS_MEDIUM, ctx.getResources().getDimension(R.dimen.extra_medium_text_size));
        xlarge.put(Constants.FONTS_LARGE, ctx.getResources().getDimension(R.dimen.extra_large_text_size));
        xlarge.put(Constants.FONTS_XLARGE, ctx.getResources().getDimension(R.dimen.extra_xlarge_text_size));

        Map scaleDimensionsMap = new HashMap<>();
        scaleDimensionsMap.put(Constants.FONTS_XSMALL, xsmall);
        scaleDimensionsMap.put(Constants.FONTS_SMALL, small);
        scaleDimensionsMap.put(Constants.FONTS_MEDIUM, medium);
        scaleDimensionsMap.put(Constants.FONTS_LARGE, large);
        scaleDimensionsMap.put(Constants.FONTS_XLARGE, xlarge);
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

    public Float getFontSize(String scale,String dimension){
        return scaleDimensionsMap.get(scale).get(dimension);
    }
}
