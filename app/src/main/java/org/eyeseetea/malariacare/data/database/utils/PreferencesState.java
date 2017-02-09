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

package org.eyeseetea.malariacare.data.database.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.layout.dashboard.builder.AppSettingsBuilder;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardAdapter;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardListFilter;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardOrientation;
import org.eyeseetea.malariacare.layout.dashboard.config.DatabaseOriginType;

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
     * Flag that determines if data must be pulled from server
     */
    private Boolean pullFromServer;

    /**
     * Flag that determines if large text is show in preferences
     */
    private Boolean showLargeText;

    /**
     * Flag that determines if the planning tab must be hide or not
     */
    private Boolean hidePlanningTab;

    /**
     * Map that holds the relationship between a scale and a set of dimensions
     */
    private Map<String, Map<String, Float>> scaleDimensionsMap;

    /**
     * Flag that determines if the location is required for push
     */
    private boolean locationRequired;

    /**
     * Sets the max number of events to download from dhis server
     */
    private int maxEvents;

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
        hidePlanningTab = initHidePlanningTab();
        maxEvents=initMaxEvents();
        Log.d(TAG,String.format("reloadPreferences: scale: %s | showNumDen: %b | locationRequired: %b | maxEvents: %d | largeTextOption: %b ",scale,showNumDen,locationRequired,maxEvents,showLargeText));
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
     * Inits hidePlanningTab flag according to preferences
     * @return
     */
    private boolean initHidePlanningTab(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(instance.getContext());
        return sharedPreferences.getBoolean(instance.getContext().getString(R.string.hide_planning_tab_key), false);
    }
    /**
     * Inits hidePlanningTab flag according to preferences
     * @return
     */
    public boolean isDevelopOptionActive(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(instance.getContext());
        return sharedPreferences.getBoolean(instance.getContext().getString(R.string.developer_option), false);
    }
    /**
     * Inits maxEvents settings
     * @return
     */
    private int initMaxEvents(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(instance.getContext());
        String maxValue=sharedPreferences.getString(instance.getContext().getString(R.string.dhis_max_items), instance.getContext().getString(R.string.dhis_default_max_items));
        return Integer.valueOf(maxValue);
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

    public boolean isHidePlanningTab(){
        return this.hidePlanningTab;
    }

    public int getMaxEvents(){
        return this.maxEvents;
    }

    public void setMaxEvents(int maxEvents){
        this.maxEvents=maxEvents;
    }

    public Float getFontSize(String scale,String dimension){
        if (scaleDimensionsMap.get(scale)==null) return context.getResources().getDimension(R.dimen.small_large_text_size);
        return scaleDimensionsMap.get(scale).get(dimension);
    }


    /**
     * Tells if metaData is pulled from server or locally populated
     * @return
     */
    public Boolean getPullFromServer() {
        return DatabaseOriginType.DHIS.equals(AppSettingsBuilder.getDatabaseOriginType());
    }

    /**
     * Tells if the application is Vertical or horizontall
     * @return
     */
    public Boolean isVerticalDashboard() {
        return DashboardOrientation.VERTICAL.equals(AppSettingsBuilder.getDashboardOrientation());
    }

    /**
     * Tells if the application is filter for last org unit
     * @return
     */
    public Boolean isLastForOrgUnit() {
        return DashboardListFilter.LAST_FOR_ORG.equals(AppSettingsBuilder.getDashboardListFilter());
    }

    /**
     * Tells if the application is none filter
     * @return
     */
    public Boolean isNoneFilter() {
        return DashboardListFilter.NONE.equals(AppSettingsBuilder.getDashboardListFilter());
    }

    /**
     * Tells if the application use the Automatic  adapter
     * @return
     */
    public Boolean isAutomaticAdapter() {
        return DashboardAdapter.AUTOMATIC.equals(AppSettingsBuilder.getDashboardAdapter());
    }
    /**
     * Tells if the application use the Dynamic adapter
     * @return
     */
    public Boolean isDynamicAdapter() {
        return DashboardAdapter.DYNAMIC.equals(AppSettingsBuilder.getDashboardAdapter());
    }
    public Class getMainActivity(){
        if(getPullFromServer()){
            return ProgressActivity.class;
        }

        return DashboardActivity.class;
    }


    public void clearOrgUnitPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor =sharedPreferences.edit();
        editor.putString(context.getResources().getString(R.string.default_orgUnits), "");
        editor.putString(context.getResources().getString(R.string.default_orgUnit), "");
        editor.commit();
    }

    /**
     * it determines if large text is shown in preferences
     * The screen size should be more bigger than the width and height constants to show the large text option.
     */
    public boolean isLargeTextShown(){
        if(showLargeText==null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            Log.d(TAG,metrics.widthPixels +" x "+ metrics.heightPixels);
            if (metrics.widthPixels > Constants.MINIMAL_WIDTH_PIXEL_RESOLUTION_TO_SHOW_LARGE_TEXT && metrics.heightPixels >= Constants.MINIMAL_HEIGHT_PIXEL_RESOLUTION_TO_SHOW_LARGE_TEXT) {
                showLargeText= true;
            } else {
                showLargeText = false;
            }
        }
        return  showLargeText;
    }

    public boolean isPushInProgress() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(instance.getContext());
        return sharedPreferences.getBoolean(instance.getContext().getString(R.string.push_in_progress), false);
    }

    public void setPushInProgress(boolean inProgress){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor =sharedPreferences.edit();
        editor.putBoolean(context.getResources().getString(R.string.push_in_progress), inProgress);
        editor.commit();
    }
}
