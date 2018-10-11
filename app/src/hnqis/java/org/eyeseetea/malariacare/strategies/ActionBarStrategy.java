package org.eyeseetea.malariacare.strategies;

import android.support.v7.app.AppCompatActivity;

import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;

public class ActionBarStrategy extends LayoutUtils {
    public static void setActionBarForSurveyFeedback(AppCompatActivity dashboardActivity, SurveyDB survey) {
        LayoutUtils.setActionBarForSurveyFeedback(dashboardActivity,survey);
    }
}
