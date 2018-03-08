package org.eyeseetea.malariacare.strategies;

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

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;

public class ActionBarStrategy extends LayoutUtils {
    public static void setActionBarForSurvey(DashboardActivity dashboardActivity, SurveyDB survey) {
        LayoutUtils.setActionBarTitleForSurvey(dashboardActivity,survey);
    }
}
