/*
 * Copyright (c) 2017.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.domain.usecase;

import android.util.Log;

import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.domain.boundary.IOrgUnitRepository;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UpdateQuarantineSurveyStatusUseCase implements UseCase{
    private final static String TAG = ".UpdateQuarantine";
    private IAsyncExecutor mAsyncExecutor;
    private IOrgUnitRepository orgUnitRepository;
    private ISurveyDataSource remoteSurveyDataSource;
    private ISurveyDataSource localSurveyDataSource;
    //This flag is necessary to avoid duplicates related with problems when multiple updateQuarantineUseCase instances and push controller running in background at same time.
    //first two updateQuarantineUseCase have same surveys and set a survey as completed, but between that happens the push controller might push the survey set as completed by the first updateQuarantineUseCase
    private static boolean isActive;

    public interface Callback {
        void onComplete();

        void onError();
    }


    public UpdateQuarantineSurveyStatusUseCase(
            IAsyncExecutor asyncExecutor,
            IOrgUnitRepository orgUnitRepository,
            ISurveyDataSource localSurveyDataSource,
            ISurveyDataSource remoteSurveyDataSource) {
        this.mAsyncExecutor = asyncExecutor;
        this.orgUnitRepository = orgUnitRepository;
        this.localSurveyDataSource = localSurveyDataSource;
        this.remoteSurveyDataSource = remoteSurveyDataSource;
    }

    public void execute() {
        mAsyncExecutor.run(this);
    }


    @Override
    public void run() {
        if(isActive)
            return;
        isActive = true;
        Log.d(TAG, "quarantine surveys update start");
        List<LocalSurveyFilter> filters = getGroupsOfSurveyFilters();
        for(LocalSurveyFilter filter: filters) {
            try {
                List<Survey> surveys = localSurveyDataSource.getSurveys(filter);
                if(surveys!=null && surveys.size()>0) {
                    List<Survey> quarantineSurveysInServer = remoteSurveyDataSource.getSurveys(filter);
                    updateSurveyStatus(surveys, quarantineSurveysInServer);
                    localSurveyDataSource.save(surveys);
                }
            } catch (Exception e) {
                notifyError(e);
                return;
            }
        }
        notifyCompleted();
    }


    private List<LocalSurveyFilter> getGroupsOfSurveyFilters() {
        List<LocalSurveyFilter> filters = new ArrayList<>();
        List<OrgUnit> orgUnitList = orgUnitRepository.getAll();
        for (OrgUnit orgUnit:orgUnitList) {
            for (String programUId : orgUnit.getRelatedPrograms()) {

                LocalSurveyFilter getLocalQuarantineSurveysFilter = LocalSurveyFilter.createGetQuarantineSurveys(programUId, orgUnit.getUid());
                List<Survey> quarantineSurveys = null;
                try {
                    quarantineSurveys = localSurveyDataSource.getSurveys(getLocalQuarantineSurveysFilter);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

                if (quarantineSurveys.size() == 0) {
                    continue;
                }

                Date highestCompletionDate = findHighestCompletionDate(quarantineSurveys);
                Date lowestCompletionDate = findLowestCompletionDate(quarantineSurveys);

                filters.add(LocalSurveyFilter.createCheckQuarantineOnServerFilter(lowestCompletionDate, highestCompletionDate, programUId, orgUnit.getUid()));
            }
        }
        return filters;
    }

    private void updateSurveyStatus(List<Survey> surveys, List<Survey> quarantineSurveysInServer) {
        for(Survey localSurvey : surveys) {
            Log.d(TAG, "searching quarantine survey on server, uid: "+localSurvey.getUId());
            localSurvey.changeStatus(SurveyStatus.COMPLETED);
            for (Survey quarantineSurvey : quarantineSurveysInServer) {
                if(localSurvey.getCreationDate().equals(quarantineSurvey.getCreationDate())){
                    Log.d(TAG, "quarantine survey match, uid: "+localSurvey.getUId());
                    localSurvey.changeStatus(SurveyStatus.SENT);
                }
            }
        }
    }

    private Date findHighestCompletionDate(List<Survey> quarantineSurveys) {
        Date date = new Date();
        date.setTime(0);
        for(Survey survey: quarantineSurveys){
            if(survey.getCompletionDate()!=null && survey.getCompletionDate().after(date)) {
                date = survey.getCompletionDate();
            }
        }
        return date;
    }

    private Date findLowestCompletionDate(List<Survey> quarantineSurveys) {
        Date date = new Date();
        for(Survey survey: quarantineSurveys){
            if(survey.getCompletionDate()!=null && survey.getCompletionDate().before(date)) {
                date = survey.getCompletionDate();
            }
        }
        return date;
    }

    private void notifyCompleted() {
        Log.d(TAG, "quarantine surveys updated");
        isActive = false;
    }

    private void notifyError(final Throwable throwable) {
        Log.d(TAG, "quarantine surveys update error");
        throwable.printStackTrace();
        isActive = false;
    }
}
