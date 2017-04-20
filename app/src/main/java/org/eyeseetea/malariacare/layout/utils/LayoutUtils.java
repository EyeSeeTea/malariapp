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
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.Spanned;
import android.view.View;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.entity.Program;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomTextView;

public class LayoutUtils {

    public static final int[] rowBackgrounds =
            {R.drawable.background_even, R.drawable.background_odd};
    public static final int[] rowBackgroundsNoBorder =
            {R.drawable.background_even_wo_border, R.drawable.background_odd_wo_border};
    public static final int[] rowBackgroundsImprove =
            {R.drawable.background_even_improve, R.drawable.background_odd};
    public static final int[] rowBackgroundsNoBorderImprove =
            {R.drawable.background_even_wo_border_improve, R.drawable.background_odd_wo_border};

    // Given a index, this method return a background color
    public static int calculateBackgrounds(int index) {
        return LayoutUtils.rowBackgrounds[index % LayoutUtils.rowBackgrounds.length];
    }

    // Given a index, this method return a background color
    public static int calculateBackgroundsImprove(int index) {
        return LayoutUtils.rowBackgroundsImprove[index % LayoutUtils.rowBackgroundsImprove.length];
    }
    // Depending on a score sets the first view color (0<x<50:poor ; 50<x<80:fare ; 80<x<100:good)
    // If a second view is given, it also writes the text good, fare or given there
    public static void trafficLight(View view, float score, View textCard) {
        //Suppose it is 'Good' && Green
        int color = view.getContext().getResources().getColor(R.color.green);
        String tag = view.getContext().getResources().getString(R.string.good);

        if (score < Constants.MAX_AMBER) {
            color = view.getContext().getResources().getColor(R.color.amber);
            tag = view.getContext().getResources().getString(R.string.fair);
        }
        if (score < Constants.MAX_RED) {
            color = view.getContext().getResources().getColor(R.color.red);
            tag = view.getContext().getResources().getString(R.string.poor);
        }
        //Change color for number
        ((CustomTextView) view).setTextColor(color);
        //Change color& text for qualitative score
        if (textCard != null) {
            ((CustomTextView) textCard).setTextColor(color); // red
            ((CustomTextView) textCard).setText(tag);
        }
    }

    /**
     * Calculates de proper background according to an score
     */
    public static int trafficColor(float score) {
        if (score < Constants.MAX_RED) {
            return R.color.darkRed;
        }

        if (score < Constants.MAX_AMBER) {
            return R.color.amber;
            //return R.color.assess_yellow;
        }

        return R.color.tab_green_monitor;
        //return R.color.lightGreen;
    }


    // Used to setup the usual actionbar with the logo and the app name
    public static void setActionBarLogo(ActionBar actionBar) {
        actionBar.setLogo(R.drawable.qualityapp_logo);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.qualityapp_logo);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
    }

    public static void setActionBarBackButton(ActionBarActivity activity) {
        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public static void setActionBarTitleForSurvey(DashboardActivity dashboardActivity,
            Survey survey) {
        String title = "";
        String subtitle = "";
        int appNameColor = dashboardActivity.getResources().getColor(R.color.appNameColor);
        String appNameColorString = String.format("%X", appNameColor).substring(2);
        if (survey.getOrgUnit().getName() != null) {
            title = survey.getOrgUnit().getName();
        }
        Program program = survey.getProgram();
        if (program.getName() != null) {
            subtitle = program.getName();
        }
        if (PreferencesState.getInstance().isVerticalDashboard()) {
            setActionbarVerticalSurvey(dashboardActivity, title, subtitle);
        } else {
            Spanned spannedTitle = Html.fromHtml(
                    String.format("<font color=\"#%s\"><b>", appNameColorString) + title
                            + "</b></font>");
            setActionbarTitle(dashboardActivity, spannedTitle, subtitle);
        }
    }

    public static void setActionbarVerticalSurvey(DashboardActivity dashboardActivity, String title,
            String subtitle) {
        android.support.v7.app.ActionBar actionBar = dashboardActivity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setCustomView(R.layout.abc_action_bar_title_item);
        actionBar.setSubtitle(subtitle);
        actionBar.setTitle(title);
    }

    public static void setActionbarTitle(ActionBarActivity activity, Spanned title,
            String subtitle) {
        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        if(PreferencesState.getInstance().isDevelopOptionActive()) {
            actionBar.setCustomView(R.layout.dev_custom_action_bar);
            String server = PreferencesState.getInstance().getServerUrl();
            ((CustomTextView) actionBar.getCustomView().findViewById(R.id.action_bar_multititle_dev_subtitle)).setText(server);
        }else {
            actionBar.setCustomView(R.layout.custom_action_bar);
        }
        ((CustomTextView) activity.findViewById(R.id.action_bar_multititle_title)).setText(title);
        ((CustomTextView) activity.findViewById(R.id.action_bar_multititle_subtitle)).setText(subtitle);
    }


    public static void setActionbarAppName(ActionBarActivity activity) {
        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setCustomView(R.layout.abc_action_bar_title_item);
        actionBar.setSubtitle(null);
        actionBar.setTitle(activity.getResources().getString(R.string.app_name));
    }

    public static void setActionBarDashboard(ActionBarActivity activity, String title) {

        if (PreferencesState.getInstance().isVerticalDashboard()) {
            LayoutUtils.setActionbarAppName(activity);
        } else {
            //Get Tab + User
            title = getCapitalizeName(title);
            String user = getCurrentUsername();
            String appNameColorString = getAppNameColorString();
            String appName = getAppName();
            Spanned spannedTitle = Html.fromHtml(
                    String.format("<font color=\"#%s\"><b>%s</b></font> | %s", appNameColorString,
                            appName, title));
            LayoutUtils.setActionbarTitle(activity, spannedTitle, user);
        }
    }


    public static String getAppName() {
        return PreferencesState.getInstance().getContext().getResources().getString(
                R.string.app_name);
    }

    public static String getCapitalizeName(String title) {
        StringBuilder tabtemp = new StringBuilder(title);
        tabtemp.setCharAt(0, Character.toUpperCase(tabtemp.charAt(0)));
        return tabtemp.toString();
    }

    public static String getCurrentUsername() {
        UserDB user = Session.getUser();
        if (user == null) {
            return "";
        }
        String userName = user.getName();
        if (userName == null) {
            return "";
        }
        return userName;
    }


    public static String getAppNameColorString() {
        int appNameColor = PreferencesState.getInstance().getContext().getResources().getColor(
                R.color.appNameColor);
        return String.format("%X", appNameColor).substring(2);
    }
}
