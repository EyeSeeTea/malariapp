/*
 * Copyright (c) 2016.
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

package org.eyeseetea.malariacare.layout.dashboard.builder;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.layout.dashboard.config.AppSettings;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardListFilter;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardOrientation;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardSettings;
import org.eyeseetea.malariacare.layout.dashboard.config.DatabaseOriginType;
import org.eyeseetea.malariacare.layout.dashboard.config.ModuleSettings;
import org.eyeseetea.malariacare.layout.dashboard.controllers.DashboardController;
import org.eyeseetea.malariacare.layout.dashboard.controllers.ModuleController;

import java.io.InputStream;

/**
 * Loads settings.json into a AppSettings pojo and DashboardController
 * Created by arrizabalaga on 17/03/16.
 */
public class AppSettingsBuilder {

    private static final String TAG = ".AppSettingsBuilder";

    /**
     * Reference to loaded config from json
     */
    AppSettings settings;

    /**
     * Reference to built dashboard controller
     */
    DashboardController dashboardController;

    private static AppSettingsBuilder instance;

    public static AppSettingsBuilder getInstance(){
        if(instance==null){
            instance = new AppSettingsBuilder();
        }
        return instance;
    }

    public static DatabaseOriginType getDatabaseOriginType(){
        return getInstance().getSettings().getDatabaseSettings().getOriginType();
    }

    public static DashboardOrientation getDashboardOrientation(){
        return getInstance().getSettings().getDashboardSettings().getOrientation();
    }

    public static DashboardListFilter getDashboardListFilter(){
        return getInstance().getSettings().getDashboardSettings().getListFilter();
    }

    public static boolean isFullHierarchy(){
        return getInstance().getSettings().getDatabaseSettings().isFullHierarchy();
    }

    public static boolean isDownloadOnlyLastEvents(){
        return getInstance().getSettings().getDatabaseSettings().isDownloadOnlyLastEvents();
    }

    public static boolean isDeveloperOptionsActive(){
        return getInstance().getSettings().getDashboardSettings().isDeveloperOptions();
    }

    public static boolean isTabTitleVisible(){
        return getInstance().getSettings().getDashboardSettings().isTabTitleVisible();
    }

    public void init(Context context){
        settings = parse(R.raw.settings,context);
        dashboardController = build(settings);
    }

    public AppSettings getSettings(){
        return settings;
    }

    public DashboardController getDashboardController(){
        if(dashboardController==null){
            dashboardController = build(settings);
        }
        return dashboardController;
    }

    private AppSettings parse(int jsonReference, Context context){
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = context.getResources().openRawResource(jsonReference);
            return mapper.readValue(inputStream, AppSettings.class);
        }catch (Exception ex){
            ex.printStackTrace();
            Log.e(TAG, "Error loading 'settings.json'");
            return null;
        }
    }

    private DashboardController build(AppSettings appSettings){
        if(appSettings==null){
            return null;
        }
        //Build dashboard controller
        DashboardSettings dashboardSettings = appSettings.getDashboardSettings();
        if(dashboardSettings==null){
            return null;
        }

        //Add its module controllers
        DashboardController dashboardController = new DashboardController(dashboardSettings);
        for(ModuleSettings moduleSettings:dashboardSettings.getModules()){
            dashboardController.addModule(build(moduleSettings));
        }

        return dashboardController;
    }

    private ModuleController build(ModuleSettings moduleSettings){
        Class moduleControllerClass = moduleSettings.getClassController();

        try {
            return (ModuleController)moduleControllerClass.getDeclaredConstructor(ModuleSettings.class).newInstance(moduleSettings);
        }catch(Exception ex){
            Log.e(TAG,String.format("Error build module controller with class %s",moduleControllerClass.getName()));
            return null;
        }
    }
}
