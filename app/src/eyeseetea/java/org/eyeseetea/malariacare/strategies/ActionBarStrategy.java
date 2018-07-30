package org.eyeseetea.malariacare.strategies;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.views.DoublePieChart;

public class ActionBarStrategy extends LayoutUtils {

    public static void setActionBarForSurveyFeedback(DashboardActivity dashboardActivity, SurveyDB survey) {
        ActionBarStrategy.getActionBarPie(DashboardActivity.dashboardActivity).findViewById(R.id.action_bar_chart).setVisibility(View.GONE);
        LayoutUtils.setToolBarTitleForSurveyFeedback(dashboardActivity,survey);
    }

    public static void setActionBarBackButton(DashboardActivity dashboardActivity) {
        LayoutUtils.setToolBarBackButton(dashboardActivity);
    }

    public static void setActionBarTitleForSurveyAndChart(DashboardActivity dashboardActivity,
            SurveyDB survey, String moduleName, SurveyAnsweredRatio surveyAnsweredRatio) {
    }

    public static void setActionBarDashboard(AppCompatActivity activity, String title) {
        ActionBarStrategy.getActionBarPie(DashboardActivity.dashboardActivity).findViewById(R.id.action_bar_chart).setVisibility(View.GONE);
        LayoutUtils.setToolbarBarDashboard(activity, title);
    }

    public static DoublePieChart getActionBarPie(AppCompatActivity appCompatActivity) {
        DoublePieChart view = (DoublePieChart)appCompatActivity.findViewById(R.id.toolbar).findViewById(
                R.id.action_bar_chart);
        view.setVisibility(View.VISIBLE);
        return view;
    }
}
