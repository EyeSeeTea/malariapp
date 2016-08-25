/*
 * Copyright (c) 2016.
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

package org.eyeseetea.malariacare.database.iomodules.dhis.importer;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Score;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.Program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Since 1 Event might correspond to N surveys (1 per tabgroup) it is needed to track all the info to share
 * Created by arrizabalaga on 28/04/16.
 */
//TODO Remove: This is no longer required as long as there wont be tabgroups (1event -> 1 survey)
public class EventToSurveyBuilder {
    Survey defaultSurvey;
    Map<String,Survey> mapProgramStageSurvey;
    Score mainScore;
    Date createdOn;
    Date uploadedOn;
    User uploadedBy;
    Date defaultUploadedOn;

    public EventToSurveyBuilder(Survey survey){
        this.mapProgramStageSurvey =new HashMap<>();
        this.defaultSurvey=survey;
        this.defaultUploadedOn = new Date();
    }

    public Survey getDefaultSurvey(){
        return this.defaultSurvey;
    }

    public String getEventUid(){
        if(defaultSurvey==null){
            return null;
        }

        return defaultSurvey.getEventUid();
    }

    public void setMainScore(CompositeScore compositeScore, DataValue dataValue){
        //Only root scores are important
        if(!CompositeScoreBuilder.isRootScore(compositeScore)) {
            return;
        }

        //Create a prototype of the score since it cannot be saved yet (will need 1 per final survey)
        mainScore= new Score();
        mainScore.setScore(AUtils.safeParseFloat(dataValue.getValue()));
        mainScore.setUid(dataValue.getDataElement());
    }

    public void setCreatedOn(DataValue dataValue){
        this.createdOn= EventExtended.parseShortDate(dataValue.getValue());
    }

    /**
     * Returns the uploaded on date from the control data element.
     * If it has NOT been loaded when a value comes in it will return a default 'now'.
     * @return
     */
    public Date getSafeUploadedOn(){
        if(this.uploadedOn!=null){
            return this.uploadedOn;
        }
        return this.defaultUploadedOn;
    }

    public void setUploadedOn(DataValue dataValue){
        this.uploadedOn= EventExtended.parseShortDate(dataValue.getValue());
    }

    public void setUploadedBy(DataValue dataValue){
        uploadedBy =User.getUser(dataValue.getValue());
        if(uploadedBy ==null) {
            uploadedBy = new User(dataValue.getValue(), dataValue.getValue());
            uploadedBy.save();
        }
    }

    /**
     * Once all the values have been parsed you can safely
     */
    public void saveCommonData(){
        Collection<Survey> surveys= mapProgramStageSurvey.values();
        //No tabgroups, just update defaultsurvey;
        if(surveys.size()==0){
            surveys=new ArrayList<>();
            surveys.add(defaultSurvey);
        }
        //Spread the common data across the N surveys whenever possible
        for(Survey survey:surveys){
            updateDataFromControlDataElements(survey);
            saveCommonScore(survey);
        }
    }

    private void updateDataFromControlDataElements(Survey survey){
        if(survey==null){
            return ;
        }

        if(createdOn!=null) {
            survey.setCreationDate(createdOn);
        }

        if(uploadedOn!=null){
            survey.setUploadDate(uploadedOn);
        }

        if(uploadedBy!=null){
            survey.setUser(uploadedBy);
        }

        survey.save();
    }

    /**
     * Finds of adds a new survey for the given tabgroup
     * @param program
     * @return
     */
    public Survey addSurveyForTabGroup(Program program){
        if(program==null){
            return null;
        }
        //Already there nothing to add
        Survey surveyForTabGroup=mapProgramStageSurvey.get(program.getName());
        if(surveyForTabGroup!=null){
            return surveyForTabGroup;
        }

        //Real new survey is required
        surveyForTabGroup=copyDefaultSurvey(program);

        //Annotate tabgroup (for rest of values)
        mapProgramStageSurvey.put(program.getName(),surveyForTabGroup);
        return surveyForTabGroup;
    }

    /**
     * Tries to get the question from the dataValue.dataElement.
     * Returns null if its not possible.
     * @param appMapObjects
     * @param dataValue
     * @return
     */
    public Question getQuestionFromDataValue(Map<String,Object> appMapObjects, DataValue dataValue){
        if(appMapObjects==null || dataValue==null){
            return null;
        }
        Object potentialQuestion=appMapObjects.get(dataValue.getDataElement());
        if (potentialQuestion instanceof Question){
            return (Question)potentialQuestion;
        }

        return null;
    }

    private Survey copyDefaultSurvey(Program program){
        if(defaultSurvey==null){
            return null;
        }

        Survey copySurvey=new Survey();
        copySurvey.setStatus(Constants.SURVEY_SENT);
        copySurvey.setCompletionDate(defaultSurvey.getCompletionDate());
        copySurvey.setCreationDate(defaultSurvey.getCreationDate());
        copySurvey.setUploadDate(defaultSurvey.getUploadDate());
        copySurvey.setScheduleDate(defaultSurvey.getScheduleDate());
        copySurvey.setOrgUnit(defaultSurvey.getOrgUnit());
        copySurvey.setEventUid(defaultSurvey.getEventUid());
        copySurvey.setProgram(program);
        copySurvey.save();
        return copySurvey;
    }

    /**
     * Saves a copy of the inner main score associated to the given survey
     * @param survey
     */
    private void saveCommonScore(Survey survey){
        if(mainScore==null || survey==null){
            return;
        }

        Score mainScoreForSurvey=new Score(survey,"",mainScore.getScore());
        mainScoreForSurvey.save();
    }

}
