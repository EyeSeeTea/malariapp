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

package org.eyeseetea.malariacare.sdk.activities;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import org.eyeseetea.malariacare.BaseActivity;
import org.hisp.dhis.client.sdk.ui.activities.AbsHomeActivity;

/**
 * Created by idelcano on 09/11/2016.
 */

public class Dhis2Application extends AbsHomeActivity {
    @NonNull
    @Override
    protected Fragment getProfileFragment() {
        return null;
    }

    @NonNull
    @Override
    protected Fragment getSettingsFragment() {
        return null;
    }

    @Override
    protected boolean onItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
