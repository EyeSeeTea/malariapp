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

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by idelcano on 20/09/2016.
 */

public class CustomSpinner extends androidx.appcompat.widget.AppCompatSpinner {
    public CustomSpinner(Context context) {
        super(context);
    }

    public CustomSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // set the ceaseFireOnItemClickEvent argument to true to avoid firing an event
    public void setSelection(int position, boolean animate, boolean ceaseFireOnItemClickEvent) {
        OnItemSelectedListener l = getOnItemSelectedListener();
        if (ceaseFireOnItemClickEvent) {
            setOnItemSelectedListener(null);
        }

        super.setSelection(position, animate);

        if (ceaseFireOnItemClickEvent) {
            setOnItemSelectedListener(l);
        }
    }
}