package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.Survey;

import java.util.ArrayList;
import java.util.List;

public interface ISurveyRepository {

    ArrayList<Survey> getQuarantineSurveysByProgramAndOrgUnit(String uidProgram,
            String uidOrgUnit);

    List<Survey> getQuarantineSurveys();

    void saveOldSurvey(Survey survey);

    Survey getMinQuarantineCompletionDateByProgramAndOrgUnit(String programUID,
            String orgUnitUID);

    Survey getMaxQuarantineUpdatedDateByProgramAndOrgUnit(String programUID,
            String orgUnitUID);
}
