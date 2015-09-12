/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.layout.utils;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jose on 22/02/2015.
 */
public class LayoutUtils {

    public static final int [] rowBackgrounds = {R.drawable.background_even, R.drawable.background_odd};
    public static final int [] rowBackgroundsNoBorder = {R.drawable.background_even_wo_border, R.drawable.background_odd_wo_border};

    // Given a index, this method return a background color
    public static int calculateBackgrounds(int index) {
        return LayoutUtils.rowBackgrounds[index % LayoutUtils.rowBackgrounds.length];
    }

    // Given an index, this method returns a background color but without a cell border
    public static int calculateBackgroundsNoBorder(int index) {
        return LayoutUtils.rowBackgroundsNoBorder[index % LayoutUtils.rowBackgroundsNoBorder.length];
    }

    // Depending on a score sets the first view color (0<x<50:poor ; 50<x<80:fare ; 80<x<100:good)
    // If a second view is given, it also writes the text good, fare or given there
    public static void trafficLight(View view, float score, View textCard){
        //Suppose it is 'Good' && Green
        int color=view.getContext().getResources().getColor(R.color.green);
        String tag=view.getContext().getResources().getString(R.string.good);

        if (score < 80.0F){
            color= view.getContext().getResources().getColor(R.color.amber);
            tag=view.getContext().getResources().getString(R.string.fair);
        }
        if (score < 50.0F){
            color= view.getContext().getResources().getColor(R.color.red);
            tag=view.getContext().getResources().getString(R.string.poor);
        }
        //Change color for number
        ((CustomTextView)view).setTextColor(color);
        //Change color& text for qualitative score
        if(textCard != null) {
            ((CustomTextView)textCard).setTextColor(color); // red
            ((CustomTextView)textCard).setText(tag);
        }
    }

    // Used to setup the usual actionbar with the logo and the app name
    public static void setActionBarLogo(ActionBar actionBar){
        actionBar.setLogo(R.drawable.qualityapp_logo);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.qualityapp_logo);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
    }

    // Used to put the org unit name and the kind of survey instead of the app name
    public static void setActionBarText(ActionBar actionBar, String title, String subtitle){
        actionBar.setDisplayUseLogoEnabled(false);
        // Uncomment in case of we want the logo out
        // actionBar.setLogo(null);
        // actionBar.setIcon(null);
        actionBar.setTitle(title);
        actionBar.setSubtitle(subtitle);
    }
}
