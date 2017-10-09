package org.eyeseetea.malariacare.fragments;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.usecase.ShowListOfSurveyUseCase;

import java.util.ArrayList;

public class WebViewInterceptor {

    DashboardActivity mDashboardActivity;
    IMainExecutor mMainExecutor;

    public WebViewInterceptor(DashboardActivity dashboardActivity, IMainExecutor mainExecutor) {
        mDashboardActivity = dashboardActivity;
        mMainExecutor = mainExecutor;
    }

    @android.webkit.JavascriptInterface
    public void clickLog() {
        System.out.println("Event on javascript detected");
    }

    @android.webkit.JavascriptInterface
    public void passUidList(String uidList) {
        ArrayList<SurveyDB> surveys = new ArrayList<>();
        if (uidList.length() > 0) {
            String uids[] = uidList.split(";");
            for (String uid : uids) {
                surveys.add(SurveyDB.findById(Long.parseLong(uid)));
            }
        }
        ShowListOfSurveyUseCase showListOfSurveyUseCase = new ShowListOfSurveyUseCase(
                mDashboardActivity, mMainExecutor);
        showListOfSurveyUseCase.execute(surveys);
    }
}
