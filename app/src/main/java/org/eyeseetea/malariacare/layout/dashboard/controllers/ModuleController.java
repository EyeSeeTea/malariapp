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

package org.eyeseetea.malariacare.layout.dashboard.controllers;

import android.app.Fragment;
import android.graphics.drawable.Drawable;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.fragments.IModuleFragment;
import org.eyeseetea.malariacare.layout.dashboard.config.ModuleSettings;

/**
 * Created by idelcano on 25/02/2016.
 */
public abstract class ModuleController {

    /**
     * Reference that points to the dashboard activity to resolve context stuff
     */
    DashboardActivity dashboardActivity;

    /**
     * Reference to the module properties
     */
    ModuleSettings moduleSettings;


    String name;
    Drawable icon;
    int backgroundColor;
    int layout;
    int tabLayout;

    Fragment fragment;
    boolean visible;

    protected ModuleController(){
    }

    public ModuleController(ModuleSettings moduleSettings){
        this.visible = true;
        this.moduleSettings = moduleSettings;
    }

    public void init(DashboardActivity activity){
        this.dashboardActivity = activity;
    }

    public String getName() {
        return dashboardActivity.getResources().getString(moduleSettings.getResName());
    }

    public Drawable getIcon() {
        return dashboardActivity.getResources().getDrawable(moduleSettings.getResIcon());
    }

    public int getBackgroundColor() {
        return dashboardActivity.getResources().getColor(moduleSettings.getResBackgroundColor());
    }


    public int getLayout() {
        return moduleSettings.getResLayout();
    }

    public int getTabLayout() {
        return tabLayout;
    }


    public Fragment getFragment() {
        return fragment;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void reloadData(){
        if(fragment==null){
            return;
        }

        ((IModuleFragment)fragment).reloadData();
    }
}
