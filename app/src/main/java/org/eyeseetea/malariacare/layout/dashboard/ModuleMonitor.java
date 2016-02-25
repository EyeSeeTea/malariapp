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

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.drawable.Drawable;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.fragments.MonitorFragment;

/**
 * Created by idelcano on 25/02/2016.
 */
public class ModuleMonitor  extends AModule {
    MonitorFragment monitorFragment;

    public ModuleMonitor(int layout, boolean visible) {
        this.layout=layout;
        this.icon= PreferencesState.getInstance().getContext().getResources().getDrawable(R.drawable.tab_monitor);
        this.name= PreferencesState.getInstance().getContext().getResources().getString(R.string.tab_tag_monitor);
        this.animatorInLeft= R.animator.anim_slide_in_left;
        this.animatorOutLeft= R.animator.anim_slide_out_left;
        this.animatorInRight=R.animator.anim_slide_in_right;
        this.animatorOutRight=R.animator.anim_slide_out_right;
        this.fragmentTransactionLeft=fragmentTransactionLeft;
        this.fragmentTransactionRight=fragmentTransactionRight;
        this.visible=visible;
        monitorFragment = new MonitorFragment();
    }

    @Override
    public Fragment getFragment() {
        return monitorFragment;
    }

    @Override
    public void reloadData() {
        monitorFragment.reloadMonitor();
    }
}
