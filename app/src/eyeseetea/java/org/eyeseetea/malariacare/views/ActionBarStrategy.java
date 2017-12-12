package org.eyeseetea.malariacare.views;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ActionBarContainer;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;

public class ActionBarStrategy extends LayoutUtils {

    public static void setActionBarForSurvey(DashboardActivity dashboardActivity, SurveyDB survey) {
        LayoutUtils.setActionBarTitleForSurvey(dashboardActivity,survey);
    }

    public static void setActionBarBackButton(DashboardActivity dashboardActivity) {
        LayoutUtils.setActionBarBackButton(dashboardActivity);
    }

    public static void setActionBarLogo(ActionBar actionBar) {
        LayoutUtils.setActionBarLogo(actionBar);
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
        setSurveyActionbarTitle(dashboardActivity, spannedTitle, subtitle, survey.getId_survey());
    }


    private static void setSurveyActionbarTitle(ActionBarActivity activity, Spanned title,
            String subtitle, long surveyId) {
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
        String server = PreferencesState.getInstance().getServerUrl();
        ((CustomTextView) activity.findViewById(R.id.action_bar_multititle_title)).setText(title);
        ((CustomTextView) activity.findViewById(R.id.action_bar_multititle_subtitle)).setText(subtitle);
    }

    public static void setActionBarDashboard(ActionBarActivity activity, String title, String module) {
            //Get Tab + User
            title = getCapitalizeName(title);
            String user = getCurrentUsername();
            String appNameColorString = getAppNameColorString();
        String moduleColorString = getModuleColorString();
            String appName = getAppName();
        Spanned spannedTitle = Html.fromHtml(
                String.format("<font color=\"#%s\"><b>%s</b></font> <font color=\"#%s\"><b"
                                + ">%s</b></font>", appNameColorString,
                        appName, moduleColorString, title));
        Spanned spannedSubTitle = Html.fromHtml(
                String.format("<font color=\"#%s\"><b"
                                + ">%s</b></font>", moduleColorString, title));

            setActionbarTitle(activity, spannedTitle, spannedSubTitle, user);
        }

    protected static String getModuleColorString() {
        int appNameColor = PreferencesState.getInstance().getContext().getResources().getColor(
                R.color.grey_dark);
        return String.format("%X", appNameColor).substring(2);
    }

    protected static void setActionbarTitle(ActionBarActivity activity, Spanned title, Spanned subtitle,
            String user) {
        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setCustomView(R.layout.custom_action_bar);
        ((Toolbar)actionBar.getCustomView().getParent()).setBackgroundResource(R.drawable.actionbar_gradient);
        ((ActionBarContainer)actionBar.getCustomView().getParent().getParent()).setBackgroundResource(R.drawable.actionbar_gradient);
        ((CustomTextView) activity.findViewById(R.id.action_bar_multititle_title)).setText(title);
        ((CustomTextView) activity.findViewById(R.id.action_bar_multititle_subtitle)).setText(subtitle);
        ((CustomTextView) activity.findViewById(R.id.action_bar_user)).setText(user);

    }
}
