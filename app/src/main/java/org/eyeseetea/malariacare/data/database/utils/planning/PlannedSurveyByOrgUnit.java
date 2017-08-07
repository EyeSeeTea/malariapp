package org.eyeseetea.malariacare.data.database.utils.planning;

/**
 * Created by idelcano on 07/09/2016.
 */

import org.eyeseetea.malariacare.domain.entity.Survey;

/**
 * Created by ina on 16/08/2016.
 */
public class PlannedSurveyByOrgUnit extends PlannedSurvey {

    private boolean isChecked;

    public PlannedSurveyByOrgUnit(Survey survey, PlannedHeader header) {
        super(survey, header);
    }

    public void setChecked(boolean value){
        this.isChecked=value;
    }

    public boolean getChecked(){
        return this.isChecked;
    }


}