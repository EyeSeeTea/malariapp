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

package org.eyeseetea.malariacare.views;

/**
 * Created by idelcano on 18/02/2016.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;

/**
 * Progressbar that is used in UI Tests
 * Prevents the progressbar from ever showing and animating
 * Thus allowing Espresso to continue with tests and Espresso won't be blocked
 */

public class CustomProgressBar extends android.widget.ProgressBar {

    public CustomProgressBar(Context context) {
        super(context);
        setUpView();
    }

    public CustomProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUpView();
    }

    public CustomProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUpView();
    }

    private void setUpView() {
        this.setVisibility(GONE);
    }

    @Override
    public void setVisibility(int v) {
        // Progressbar should never show
        v = GONE;
        super.setVisibility(v);
    }

    @Override
    public void startAnimation(Animation animation) {
        // Do nothing in test cases, to not block ui thread
    }
}