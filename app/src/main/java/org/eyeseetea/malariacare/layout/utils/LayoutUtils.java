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

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomTextView;

/**
 * Created by Jose on 22/02/2015.
 */
public class LayoutUtils {

    public static final int [] rowBackgrounds = {R.drawable.background_even, R.drawable.background_odd};
    public static final int [] rowBackgroundsNoBorder = {R.drawable.background_even_wo_border, R.drawable.background_odd_wo_border};
    public static final int [] rowBackgroundsImprove = {R.drawable.background_even_improve, R.drawable.background_odd};
    public static final int [] rowBackgroundsNoBorderImprove = {R.drawable.background_even_wo_border_improve, R.drawable.background_odd_wo_border};

    // Given a index, this method return a background color
    public static int calculateBackgrounds(int index) {
        return LayoutUtils.rowBackgrounds[index % LayoutUtils.rowBackgrounds.length];
    }

    // Given a index, this method return a background color
    public static int calculateBackgroundsImprove(int index) {
        return LayoutUtils.rowBackgroundsImprove[index % LayoutUtils.rowBackgroundsImprove.length];
    }

    // Given an index, this method returns a background color but without a cell border
    public static int calculateBackgroundsNoBorder(int index) {
        return LayoutUtils.rowBackgroundsNoBorder[index % LayoutUtils.rowBackgroundsNoBorder.length];
    }

    // Given an index, this method returns a background color but without a cell border
    public static int calculateBackgroundsNoBorderImprove(int index) {
        return LayoutUtils.rowBackgroundsNoBorderImprove[index % LayoutUtils.rowBackgroundsNoBorderImprove.length];
    }
    // Depending on a score sets the first view color (0<x<50:poor ; 50<x<80:fare ; 80<x<100:good)
    // If a second view is given, it also writes the text good, fare or given there
    public static void trafficLight(View view, float score, View textCard){
        //Suppose it is 'Good' && Green
        int color=view.getContext().getResources().getColor(R.color.green);
        String tag=view.getContext().getResources().getString(R.string.good);

        if (score < Constants.MAX_AMBER){
            color= view.getContext().getResources().getColor(R.color.amber);
            tag=view.getContext().getResources().getString(R.string.fair);
        }
        if (score < Constants.MAX_RED){
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

    /**
     * Calculates de proper background according to an score
     * @param score
     * @return
     */
    public static int trafficColor(float score){
        if(score< Constants.MAX_RED){
            return R.color.darkRed;
        }

        if(score< Constants.MAX_AMBER){
            return R.color.amber;
            //return R.color.assess_yellow;
        }

        return R.color.tab_green_monitor;
        //return R.color.lightGreen;
    }



    // Used to setup the usual actionbar with the logo and the app name
    public static void setActionBarLogo(ActionBar actionBar){
        actionBar.setLogo(R.drawable.qualityapp_logo);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.qualityapp_logo);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
    }

    public static void setActionBarBackButton(DashboardActivity dashboardActivity){
        android.support.v7.app.ActionBar actionBar = dashboardActivity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public static void setActionBarTitleForSurvey(DashboardActivity dashboardActivity,Survey survey){
        String title="";
        String subtitle="";
        int appNameColor = dashboardActivity.getResources().getColor(R.color.appNameColor);
        String appNameColorString = String.format("%X", appNameColor).substring(2);
        Program program = survey.getProgram();
        if(survey.getOrgUnit().getName()!=null) {
            title = survey.getOrgUnit().getName();
        }
        if(program.getName()!=null) {
            subtitle = program.getName();
        }
        if(PreferencesState.getInstance().isVerticalDashboard()) {
            setActionbarVerticalSurvey(dashboardActivity,title, subtitle);
        }
        else{
            Spanned spannedTitle = Html.fromHtml(String.format("<font color=\"#%s\"><b>", appNameColorString) + title + "</b></font>");
            setActionbarTitle(dashboardActivity,spannedTitle, subtitle);
        }
    }

    public static void setActionbarVerticalSurvey(DashboardActivity dashboardActivity,String title, String subtitle) {
        android.support.v7.app.ActionBar actionBar = dashboardActivity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setCustomView(R.layout.abc_action_bar_title_item);
        actionBar.setSubtitle(subtitle);
        actionBar.setTitle(title);
    }

    public static void setActionbarTitle(DashboardActivity dashboardActivity,Spanned title, String subtitle) {
        android.support.v7.app.ActionBar actionBar = dashboardActivity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setCustomView(R.layout.custom_action_bar);
        ((TextView) dashboardActivity.findViewById(R.id.action_bar_multititle_title)).setText(title);
        ((TextView) dashboardActivity.findViewById(R.id.action_bar_multititle_subtitle)).setText(subtitle);
    }


    public static void setActionbarAppName(DashboardActivity dashboardActivity) {
        android.support.v7.app.ActionBar actionBar = dashboardActivity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setCustomView(R.layout.abc_action_bar_title_item);
        actionBar.setSubtitle(null);
        actionBar.setTitle(dashboardActivity.getResources().getString(R.string.app_name));
    }

}
