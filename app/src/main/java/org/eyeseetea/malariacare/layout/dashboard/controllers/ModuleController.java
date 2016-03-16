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

/**
 * Created by idelcano on 25/02/2016.
 */
public abstract class ModuleController {

    DashboardActivity dashboardActivity;
    int layout;
    int tabLayout;
    Drawable icon;
    int backgroundColor;
    String name;
    Fragment fragment;
    boolean visible;

    public int getLayout() {
        return layout;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public int getTabLayout() {
        return tabLayout;
    }


    public void setTabLayout(int tabLayout) {
        this.tabLayout = tabLayout;
    }


    public Drawable getIcon() {
        return icon;
    }


    public void setIcon(Drawable icon) {
        this.icon = icon;
    }


    public void setBackgroundColor(int color) {
        this.backgroundColor =color;
    }


    public int getBackgroundColor() {
        return backgroundColor;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void init(DashboardActivity activity){
        this.dashboardActivity = activity;
    }

    public void reloadData(){
        if(fragment==null){
            return;
        }

        ((IModuleFragment)fragment).reloadData();
    }
}
