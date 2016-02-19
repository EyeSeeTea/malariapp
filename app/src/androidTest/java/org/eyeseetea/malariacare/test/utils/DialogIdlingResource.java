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

package org.eyeseetea.malariacare.test.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.support.test.espresso.IdlingResource;
import android.util.Log;

import org.eyeseetea.malariacare.ProgressActivity;

/**
 * An IdlingResource that waits for a dialog to be shown
 * Created by arrizabalaga on 24/06/15.
 */
public class DialogIdlingResource implements IdlingResource {

    private static final String TAG="DialogIdlingResource";

    private final Context context;
    private ResourceCallback resourceCallback;
    private ProgressActivity progressActivity;


    public DialogIdlingResource(Context context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return DialogIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        boolean idle = !isDialogShowing();
        Log.d(TAG,"isIdleNow->"+idle);
        if (idle) {
            if (resourceCallback != null) {
                resourceCallback.onTransitionToIdle();
            }
        }
        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }

    /**
     * Wait for a dialog to the shown
     * @return
     */
    private boolean isDialogShowing() {
        return this.progressActivity!=null && this.progressActivity.isDialogShowing();
    }

    /**
     * Annotates progress activity to check for dialog
     * @param activity
     */
    public void setProgressActivity(ProgressActivity activity){
        Log.d(TAG,"setProgressActivity: "+activity);
        this.progressActivity = activity;
    }
}
