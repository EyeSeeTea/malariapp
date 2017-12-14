package org.eyeseetea.malariacare.views;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;

public class ActionBarStrategy extends LayoutUtils {
    public static void setActionBarForSurvey(DashboardActivity dashboardActivity, SurveyDB survey) {
        LayoutUtils.setActionBarTitleForSurvey(dashboardActivity,survey);
    }
}
