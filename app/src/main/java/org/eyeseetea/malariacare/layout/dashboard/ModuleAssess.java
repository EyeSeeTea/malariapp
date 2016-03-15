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

package org.eyeseetea.malariacare.layout.dashboard;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.graphics.drawable.Drawable;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.fragments.DashboardUnsentFragment;

/**
 * Created by idelcano on 25/02/2016.
 */
public class ModuleAssess extends AModule {

    public ModuleAssess( boolean visible) {
        this.layout=R.id.dashboard_details_container;
        this.tabLayout=R.id.tab_assess_layout;
        this.visible=visible;
        createModule();
    }

    public ModuleAssess(int layout, boolean visible) {
        this.layout=layout;
        this.visible=visible;
        createModule();
    }

    public ModuleAssess(int layout, int tabLayout, boolean visible) {
        this.layout=layout;
        this.tabLayout=tabLayout;
        this.visible=visible;
        createModule();
    }

    private void createModule() {
        this.icon= PreferencesState.getInstance().getContext().getResources().getDrawable(R.drawable.tab_assess);
        this.name= PreferencesState.getInstance().getContext().getResources().getString(R.string.tab_tag_assess);
        this.color= PreferencesState.getInstance().getContext().getResources().getColor(R.color.tab_yellow_assess);
    }

    @Override
    public void init(DashboardActivity activity) {
        super.init(activity);
        fragment = new DashboardUnsentFragment();
    }

}