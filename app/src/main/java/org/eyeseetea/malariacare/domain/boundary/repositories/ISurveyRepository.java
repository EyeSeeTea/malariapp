package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.Survey;

import java.util.ArrayList;

public interface ISurveyRepository {

    ArrayList<Survey> getQuarantineSurveysByProgramAndOrgUnit(String uidProgram,
            String uidOrgUnit);

}
