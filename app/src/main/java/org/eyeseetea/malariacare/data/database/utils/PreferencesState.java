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

import static org.eyeseetea.malariacare.utils.Constants.SYSTEM_DEFINED_LANGUAGE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.Server;
import org.eyeseetea.malariacare.layout.dashboard.builder.AppSettingsBuilder;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardListFilter;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardOrientation;
import org.eyeseetea.malariacare.layout.dashboard.config.DatabaseOriginType;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PreferencesState {

    static Context context;
    private static String TAG = ".PreferencesState";
    /**
     * Singleton reference
     */
    private static PreferencesState instance;
    /**
     * Selected scale, one between [xsmall,small,medium,large,xlarge,system]
     */
    private String scale;
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
     * Flag that force if get all surveys
     */
    private Boolean forceAllSentSurveys = false;
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
    /**
     * Sets the monitoring target of events
     */
    private int monitoringTarget;
    /**
     * Active language code;
     */
    private static String languageCode;

    /**
     * Flag that determines if the user did accept the announcement
     */
    private boolean userAccept;
    private Server server;
    private Credentials creedentials;

    private PreferencesState() {
    }

    public static PreferencesState getInstance() {
        if (instance == null) {
            instance = new PreferencesState();
        }
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void init(Context context) {
        this.context = context;
        scaleDimensionsMap = initScaleDimensionsMap();
        reloadPreferences();
    }

    public Context getContext() {
        return context;
    }

    public void reloadPreferences() {
        scale = initScale();
        locationRequired = initLocationRequired();
        hidePlanningTab = initHidePlanningTab();
        maxEvents = initMaxEvents();
        languageCode = initLanguageCode();
        userAccept = initUserAccept();
        Log.d(TAG, String.format(
                "reloadPreferences: scale: %s | locationRequired: %b | "
                        + "maxEvents: %d | largeTextOption: %b",
                scale, locationRequired, maxEvents, showLargeText));

        creedentials = initCredentials();
    }

    private Credentials initCredentials() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context);
        String url = sharedPreferences.getString(
                context.getResources().getString(R.string.dhis_url), "");
        String name =
                sharedPreferences.getString(context.getString(R.string.dhis_user), "");
        String password =
                sharedPreferences.getString(context.getString(R.string.dhis_password), "");
        
        if (!url.isEmpty() && !name.isEmpty() && !password.isEmpty()) {
            creedentials = new Credentials(url, name, password);
        }

        return creedentials;
    }

    /**
     * Returns 'language code' from sharedPreferences
     */
    private String initLanguageCode() {
        String languagePreferenceKey = instance.getContext().getString(R.string.language_code);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                instance.getContext());

        String languageCode = sharedPreferences.getString(languagePreferenceKey, "");

        if (languageCode.isEmpty()) {
            languageCode = SYSTEM_DEFINED_LANGUAGE;

            putStringOnPreferences(sharedPreferences, languagePreferenceKey, languageCode);
        }

        return languageCode;
    }

    /**
     * Inits user accept flag according to preferences
     */
    private boolean initUserAccept() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                instance.getContext());
        return sharedPreferences.getBoolean(
                instance.getContext().getString(R.string.user_accept_key), false);
    }

    /**
     * Inits scale according to preferences
     */
    private String initScale() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                instance.getContext());
        if (sharedPreferences.getBoolean(instance.getContext().getString(R.string.customize_fonts),
                false)) {
            return sharedPreferences.getString(instance.getContext().getString(R.string.font_sizes),
                    instance.getContext().getString(R.string.font_size_system));
        }

        return context.getString(R.string.font_size_system);
    }

    /**
     * Inits location flag according to preferences
     */
    private boolean initLocationRequired() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                instance.getContext());
        return sharedPreferences.getBoolean(
                instance.getContext().getString(R.string.location_required), false);
    }

    /**
     * Inits hidePlanningTab flag according to preferences
     */
    private boolean initHidePlanningTab() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                instance.getContext());
        return sharedPreferences.getBoolean(
                instance.getContext().getString(R.string.hide_planning_tab_key), false);
    }

    /**
     * Inits hidePlanningTab flag according to preferences
     */
    public boolean isDevelopOptionActive() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                instance.getContext());
        return sharedPreferences.getBoolean(
                instance.getContext().getString(R.string.developer_option), false);
    }

    /**
     * Inits maxEvents settings
     */
    private int initMaxEvents() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                instance.getContext());
        String maxValue = sharedPreferences.getString(
                instance.getContext().getString(R.string.dhis_max_items),
                instance.getContext().getString(R.string.dhis_default_max_items));
        return Integer.valueOf(maxValue);
    }

    /**
     * Inits maps of dimensions
     */
    private Map<String, Map<String, Float>> initScaleDimensionsMap() {
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

    public String getScale() {
        return scale;
    }

    public void setScale(String value) {
        this.scale = value;
    }

    public boolean isLocationRequired() {
        return locationRequired;
    }

    public void setLocationRequired(boolean value) {
        this.locationRequired = value;
    }

    public boolean isHidePlanningTab() {
        return this.hidePlanningTab;
    }

    public int getMaxEvents() {
        return this.maxEvents;
    }

    public void setMaxEvents(int maxEvents) {
        this.maxEvents = maxEvents;
    }

    public int getMonitoringTarget() {
        // TODO: The monitoring target preference is removed because is used by old monitoring
        //  without filters that It's not visible any more. But since the old monitoring code
        //  is still present reading from this getter, we leave the getter returning
        //  always default value. When the old monitoring code is removed, delete this getter.
        return Integer.valueOf(instance.getContext().getString(R.string.default_monitoring_target));
    }

    public Float getFontSize(String scale, String dimension) {
        if (scaleDimensionsMap.get(scale) == null) {
            return context.getResources().getDimension(
                    R.dimen.small_large_text_size);
        }
        return scaleDimensionsMap.get(scale).get(dimension);
    }


    /**
     * Tells if metaData is pulled from server or locally populated
     */
    public Boolean getPullFromServer() {
        return DatabaseOriginType.DHIS.equals(AppSettingsBuilder.getDatabaseOriginType());
    }

    /**
     * Tells if the application is Vertical or horizontall
     */
    public Boolean isVerticalDashboard() {
        return DashboardOrientation.VERTICAL.equals(AppSettingsBuilder.getDashboardOrientation());
    }

    /**
     * Tells if the application is filter for last org unit
     */
    public Boolean isLastForOrgUnit() {
        return (!forceAllSentSurveys && DashboardListFilter.LAST_FOR_ORG.equals(
                AppSettingsBuilder.getDashboardListFilter()));
    }

    public void setForceAllSentSurveys(boolean value) {
        forceAllSentSurveys = value;
    }

    /**
     * Tells if the application is none filter
     */
    public Boolean isNoneFilter() {
        return DashboardListFilter.NONE.equals(AppSettingsBuilder.getDashboardListFilter());
    }

    public Class getMainActivity() {
        if (getPullFromServer()) {
            return ProgressActivity.class;
        }

        return DashboardActivity.class;
    }


    public void clearOrgUnitPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getResources().getString(R.string.default_orgUnits), "");
        editor.putString(context.getResources().getString(R.string.default_orgUnit), "");
        editor.commit();
    }

    /**
     * it determines if large text is shown in preferences
     * The screen size should be more bigger than the width and height constants to show the large
     * text option.
     */
    public boolean isLargeTextShown() {
        if (showLargeText == null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            Log.d(TAG, metrics.widthPixels + " x " + metrics.heightPixels);
            if (metrics.widthPixels > Constants.MINIMAL_WIDTH_PIXEL_RESOLUTION_TO_SHOW_LARGE_TEXT
                    && metrics.heightPixels
                    >= Constants.MINIMAL_HEIGHT_PIXEL_RESOLUTION_TO_SHOW_LARGE_TEXT) {
                showLargeText = true;
            } else {
                showLargeText = false;
            }
        }
        return showLargeText;
    }

    public boolean isPushInProgress() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                instance.getContext());
        return sharedPreferences.getBoolean(
                instance.getContext().getString(R.string.push_in_progress), false);
    }

    public void setPushInProgress(boolean inProgress) {
        Log.d(TAG, "change set push in progress to " + inProgress);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getResources().getString(R.string.push_in_progress), inProgress);
        editor.commit();
    }

    /**
     * Tells if user accepted the announcement message
     */
    public Boolean isUserAccept() {
        return userAccept;
    }

    /**
     * Set userAccept in the preferences and local memory
     */
    public Boolean setUserAccept(boolean isAccepted) {
        this.userAccept = isAccepted;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getResources().getString(R.string.user_accept_key), isAccepted);
        editor.commit();
        return userAccept;
    }

    public void initalizateActivityDependencies() {
        loadsLanguageInActivity();
    }

    public void loadsLanguageInActivity() {
        setLocale(getCurrentLocale());
    }

    public String getCurrentLocale() {
        String temLanguageCode = languageCode;
        if (languageCode.equals(SYSTEM_DEFINED_LANGUAGE)) {
            temLanguageCode = getPhoneLanguage();
        }
        return temLanguageCode;
    }

    private void setLocale(String languageCode) {
        Resources res = context.getResources();
        // Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(new Locale(languageCode));
        } else {
            conf.locale = new Locale(languageCode);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            res.updateConfiguration(conf, dm);
        } else {
            context.createConfigurationContext(conf);
        }
    }

    public Server getServer() {
        if (server == null) {
            server = loadServer();
        }
        return server;

    }

    private Server loadServer() {
        server = null;

        String serverUrl=getServerUrl();

        if (serverUrl != null && !serverUrl.isEmpty() ){
            server = new Server(serverUrl);
        }

        return server;
    }

    private String formatUrl(String serverUrl) {
        if (serverUrl == null || serverUrl.isEmpty()) {
            return "";
        }
        if (!(serverUrl.substring(serverUrl.length() - 1)).equals("/")) {
            serverUrl = serverUrl + "/";
        }
        return serverUrl;
    }

    private String getServerUrl() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context);
        String serverUrl = sharedPreferences.getString(
                PreferencesState.getInstance().getContext().getResources().getString(
                        R.string.dhis_url), "");
        return serverUrl;
    }

    public void reloadServerUrl() {
        loadServer();
    }

    private String getPhoneLanguage() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = Resources.getSystem().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            locale = Resources.getSystem().getConfiguration().locale;
        }
        //Taking only the 2 first elements of the string, for e.g en_US => en, fr_CA => fr..etc.
        return locale.getLanguage().substring(0, 2);
    }

    public Credentials getCreedentials() {
        return creedentials;
    }

    public String getProgramUidFilter() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                instance.getContext());

        return sharedPreferences.getString(
                instance.getContext().getString(R.string.user_preference_program_filter), "");
    }

    public void setProgramUidFilter(String programUid) {
        Log.d(TAG, "change user_preference_program_filter to " + programUid);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getResources().getString(R.string.user_preference_program_filter),
                programUid);
        editor.commit();
    }

    public String getOrgUnitUidFilter() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                instance.getContext());

        return sharedPreferences.getString(
                instance.getContext().getString(R.string.user_preference_org_unit_filter), "");
    }

    public void setOrgUnitUidFilter(String orgUnitUid) {
        Log.d(TAG, "change user_preference_org_unit_filter to " + orgUnitUid);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getResources().getString(R.string.user_preference_org_unit_filter),
                orgUnitUid);
        editor.commit();
    }

    private void putStringOnPreferences(SharedPreferences sharedPreferences,
            String languagePreferenceKey,
            String languageCode) {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(languagePreferenceKey, languageCode);
        editor.apply();
    }
}
