package org.eyeseetea.malariacare.strategies;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.views.DoublePieChart;
import org.eyeseetea.malariacare.domain.entity.Settings;

public class ActionBarStrategy extends LayoutUtils {
    public ActionBarStrategy(Settings settings){
        super(settings);
    }
    public void setActionBarForSurveyFeedback(DashboardActivity dashboardActivity, SurveyDB survey) {
        getActionBarPie(DashboardActivity.dashboardActivity).findViewById(R.id.action_bar_chart).setVisibility(View.GONE);
        setToolBarTitleForSurveyFeedback(dashboardActivity,survey);
    }

    public void setActionBarTitleForSurveyAndChart(DashboardActivity dashboardActivity,
            SurveyDB survey, String moduleName, SurveyAnsweredRatio surveyAnsweredRatio) {
    }

    public void setActionBarDashboard(AppCompatActivity activity, String title) {
        getActionBarPie(DashboardActivity.dashboardActivity).findViewById(R.id.action_bar_chart).setVisibility(View.GONE);
        setToolbarBarDashboard(activity, title);
    }

    public DoublePieChart getActionBarPie(AppCompatActivity appCompatActivity) {
        DoublePieChart view = (DoublePieChart)appCompatActivity.findViewById(R.id.toolbar).findViewById(
                R.id.action_bar_chart);
        view.setVisibility(View.VISIBLE);
        return view;
    }
}
