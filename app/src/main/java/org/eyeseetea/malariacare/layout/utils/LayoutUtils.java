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
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.entity.ScoreType;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.strategies.ActionBarStrategy;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.views.DoublePieChart;
import org.eyeseetea.sdk.presentation.views.DoubleRectChart;

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

        ScoreType scoreType = new ScoreType(score);

        if(scoreType.getClassification() == ScoreType.Classification.MEDIUM){

            color = view.getContext().getResources().getColor(R.color.amber);
            tag = view.getContext().getResources().getString(R.string.fair);

        }else if(scoreType.getClassification() == ScoreType.Classification.LOW){

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
        ScoreType scoreType = new ScoreType(score);
        if (scoreType.getClassification() == ScoreType.Classification.LOW) {
            return R.color.darkRed;
        } else if (scoreType.getClassification() == ScoreType.Classification.MEDIUM) {
            return R.color.amber;
        }else {
            return R.color.tab_green_monitor;
        }
    }

    //// TODO: 30/07/2018  remove if is not used in hnqis
    public static void setActionBarBackButton(AppCompatActivity activity) {
        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public static void setToolBarTitleForSurveyFeedback(DashboardActivity dashboardActivity,
                                                  SurveyDB survey) {
        String title = "";
        String subtitle = "";
        int appNameColor = dashboardActivity.getResources().getColor(R.color.appNameColor);
        String appNameColorString = String.format("%X", appNameColor).substring(2);
        ProgramDB program = survey.getProgram();
        if (survey.getOrgUnit().getName() != null) {
            title = survey.getOrgUnit().getName();
        }
        if (program.getName() != null) {
            subtitle = program.getName();
        }
        if (PreferencesState.getInstance().isVerticalDashboard()) {
            setActionbarVerticalSurvey(dashboardActivity, title, subtitle);
        } else {
            Spanned spannedTitle = Html.fromHtml(
                    String.format("<font color=\"#%s\"><b>", appNameColorString) + title
                            + "</b></font>");
            setToolbarTitle(dashboardActivity, spannedTitle, subtitle);
        }
    }

    public static void setActionBarForSurveyFeedback(AppCompatActivity dashboardActivity,
            SurveyDB survey) {
        String title = "";
        String subtitle = "";
        int appNameColor = dashboardActivity.getResources().getColor(R.color.appNameColor);
        String appNameColorString = String.format("%X", appNameColor).substring(2);
        ProgramDB program = survey.getProgram();
        if (survey.getOrgUnit().getName() != null) {
            title = survey.getOrgUnit().getName();
        }
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

    public static void setActionBarTitleForSurveyAndChart(DashboardActivity dashboardActivity,
            SurveyDB survey, String moduleName, SurveyAnsweredRatio surveyAnsweredRatio) {
        String title = "";
        if (survey.getProgram().getName() != null) {
            title = survey.getProgram().getName();
        }
        //Get Tab + User
        title = getCapitalizeName(title);
        String subtitle = getCurrentUsername();
        String appNameColorString = getAppNameColorString();
        String appName = getAppName();
        Spanned spannedTitle = Html.fromHtml(
                String.format("<font color=\"#%s\"><b>%s</b></font> - %s", appNameColorString,
                        appName +" - " + moduleName, title));
        setSurveyActionbarTitle(dashboardActivity, spannedTitle, subtitle, survey.getId_survey(), surveyAnsweredRatio);
    }

    private static void setActionbarVerticalSurvey(AppCompatActivity dashboardActivity, String title,
            String subtitle) {
        android.support.v7.app.ActionBar actionBar = dashboardActivity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setCustomView(R.layout.abc_action_bar_title_item);
        actionBar.setSubtitle(subtitle);
        actionBar.setTitle(title);
    }

    private static void setToolbarTitle(AppCompatActivity activity, Spanned title,
                                          String subtitle) {
        Toolbar toolbar = getToolbar(activity);
        if(PreferencesState.getInstance().isDevelopOptionActive()) {
            String server = PreferencesState.getInstance().getServerUrl();
            ((CustomTextView) toolbar.findViewById(R.id.action_bar_multititle_dev_subtitle)).setText(server);
            (toolbar.findViewById(R.id.action_bar_multititle_dev_subtitle)).setVisibility(View.VISIBLE);
        }
        ((CustomTextView) activity.findViewById(R.id.action_bar_multititle_title)).setText(title);
        ((CustomTextView) activity.findViewById(R.id.action_bar_multititle_subtitle)).setText(subtitle);
    }

    private static void setActionbarTitle(AppCompatActivity activity, Spanned title,
            String subtitle) {
        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
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

    @NonNull
    private static Toolbar getToolbar(AppCompatActivity activity) {
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        if(toolbar==null){
            toolbar = new Toolbar(activity);
            View view = activity.getLayoutInflater().inflate(R.layout.dev_custom_tool_bar, toolbar);
            toolbar.addView(view);
        }
        activity.setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.qualityapp_logo);
        toolbar.setBackgroundColor(Color.WHITE);
        return toolbar;
    }

    private static void setSurveyActionbarTitle(AppCompatActivity activity, Spanned title,
            String subtitle, long surveyId, SurveyAnsweredRatio surveyAnsweredRatio) {
        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        if(PreferencesState.getInstance().isDevelopOptionActive()) {
            actionBar.setCustomView(R.layout.dev_custom_action_bar);
            String server = PreferencesState.getInstance().getServerUrl();
            ((CustomTextView) actionBar.getCustomView().findViewById(R.id.action_bar_multititle_dev_subtitle)).setText(server);
        }else {
            actionBar.setCustomView(R.layout.custom_action_bar_with_chart);
        }
        updateSurveyActionBarChart(actionBar, surveyAnsweredRatio);
        String server = PreferencesState.getInstance().getServerUrl();
        ((CustomTextView) activity.findViewById(R.id.action_bar_multititle_title)).setText(title);
        ((CustomTextView) activity.findViewById(R.id.action_bar_multititle_subtitle)).setText(subtitle);
    }



    private static void setActionbarAppName(AppCompatActivity activity) {
        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setCustomView(R.layout.abc_action_bar_title_item);
        actionBar.setSubtitle(null);
        actionBar.setTitle(activity.getResources().getString(R.string.app_name));
    }

    public static void setActionBarDashboard(AppCompatActivity activity, String title) {

        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setLogo(R.drawable.qualityapp_logo);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        if (PreferencesState.getInstance().isVerticalDashboard()) {
            LayoutUtils.setActionbarAppName(activity);
        } else {
            //Get Tab + User
            title = getCapitalizeName(title);
            String user = getCurrentUsername();
            String appNameColorString = getAppNameColorString();
            String appName = getAppName();
            Spanned spannedTitle = Html.fromHtml(
                    String.format("<font color=\"#%s\"><b>%s</b></font> - %s", appNameColorString,
                            appName, title));
            LayoutUtils.setActionbarTitle(activity, spannedTitle, user);
        }
    }


    protected static String getAppName() {
        return PreferencesState.getInstance().getContext().getResources().getString(
                R.string.app_name);
    }

    protected static String getCapitalizeName(String title) {
        StringBuilder tabtemp = new StringBuilder(title);
        if(tabtemp.length()==0){
            return "";
        }
        tabtemp.setCharAt(0, Character.toUpperCase(tabtemp.charAt(0)));
        return tabtemp.toString();
    }

    protected static String getCurrentUsername() {
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


    protected static String getAppNameColorString() {
        int appNameColor = PreferencesState.getInstance().getContext().getResources().getColor(
                R.color.appNameColor);
        return String.format("%X", appNameColor).substring(2);
    }

    public static void updateChart(SurveyAnsweredRatio surveyAnsweredRatio,
            DoublePieChart doublePieChart) {
        doublePieChart.createDoublePie(surveyAnsweredRatio.getMandatoryStatus(),
                surveyAnsweredRatio.getTotalStatus());
    }

    private static void updateSurveyActionBarChart(ActionBar actionBar, SurveyAnsweredRatio surveyAnsweredRatio){
        final DoublePieChart doublePieChart = ActionBarStrategy.getActionBarPie(DashboardActivity.dashboardActivity);
        doublePieChart.setVisibility(View.VISIBLE);
        updateChart(surveyAnsweredRatio, doublePieChart);
    }

    public static void drawScore(Float score, DoubleRectChart doubleRectChart) {
        int color = LayoutUtils.trafficColor(score);
        String scoreText;
        if(score==null){
            scoreText = "NaN";
        }else {
            scoreText = Math.round(score) + ".0";
        }
        if(scoreText.equals("NaN")){
            doubleRectChart.createNaNDoubleRectChart(scoreText,
                    ContextCompat.getColor(PreferencesState.getInstance().getContext(),
                            R.color.nan_color),
                    ContextCompat.getColor(PreferencesState.getInstance().getContext(),
                            R.color.white));
        }else {
            doubleRectChart.createDoubleRectChart(scoreText, score.intValue(),
                    ContextCompat.getColor(PreferencesState.getInstance().getContext(), color),
                    ContextCompat.getColor(PreferencesState.getInstance().getContext(),
                            R.color.white),
                    ContextCompat.getColor(PreferencesState.getInstance().getContext(),
                            R.color.black),
                    ContextCompat.getColor(PreferencesState.getInstance().getContext(),
                            R.color.white));
        }
    }

    public static void setToolbarBarDashboard(AppCompatActivity activity, String title) {

        //Get Tab + User
        title = getCapitalizeName(title);
        String user = getCurrentUsername();
        String moduleColorString = getModuleColorString();
        String appName = getAppName();
        Spanned spannedSubTitle = Html.fromHtml(
                String.format("<font color=\"#%s\"><b"
                        + ">%s</b></font>", moduleColorString, title));

        Toolbar toolbar = LayoutUtils.getToolbar(activity);
        toolbar.setBackgroundResource(R.drawable.actionbar_gradient);
        ((TextView) toolbar.findViewById(R.id.action_bar_multititle_title)).setText(appName);
        ((TextView) toolbar.findViewById(R.id.action_bar_multititle_subtitle)).setText(spannedSubTitle);
        ((TextView) toolbar.findViewById(R.id.action_bar_user)).setText(user);
    }

    private static String getModuleColorString() {
        int appNameColor = PreferencesState.getInstance().getContext().getResources().getColor(
                R.color.grey_dark);
        return String.format("%X", appNameColor).substring(2);
    }

    public static DoublePieChart getActionBarPie(AppCompatActivity activity) {
        return (DoublePieChart) activity.getSupportActionBar
                ().getCustomView().findViewById(
                R.id.action_bar_chart);
    }
}
