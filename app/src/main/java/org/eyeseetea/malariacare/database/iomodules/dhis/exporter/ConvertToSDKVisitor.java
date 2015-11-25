/*
 * Copyright (c) 2015.
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

package org.eyeseetea.malariacare.database.iomodules.dhis.exporter;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.LocationMemory;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;
import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.Event$Table;

import java.util.Date;
import java.util.List;

/**
 * Turns a given survey into its corresponding events+datavalues.
 */
public class ConvertToSDKVisitor implements IConvertToSDKVisitor {

    private final static String TAG=".ConvertToSDKVisitor";

    /**
     * Context required to recover magic UID for mainScore dataElements
     */
    Context context;

    String mainScoreUID;
    String mainScoreAUID;
    String mainScoreBUID;
    String mainScoreCUID;

    /**
     * The last survey that it is being translated
     */
    Survey survey;

    /**
     * The generated event
     */
    Event event;

    /**
     * Timestamp that captures the moment when the survey is converted right before being sent
     */
    Date completionDate;

    ConvertToSDKVisitor(Context context){
        this.context=context;
        mainScoreUID=context.getString(R.string.main_score);
        mainScoreAUID=context.getString(R.string.main_score_a);
        mainScoreBUID=context.getString(R.string.main_score_b);
        mainScoreCUID=context.getString(R.string.main_score_c);
    }

    @Override
    public void visit(Survey survey) throws Exception{
        //Turn survey into an event
        this.survey=survey;
        Log.d(TAG,"Creating event...");
        this.event=buildEvent();

        //Calculates scores and update survey
        Log.d(TAG,"Registering scores...");
        List<CompositeScore> compositeScores = ScoreRegister.loadCompositeScores(survey);
        updateSurvey(compositeScores);

        //Turn score values into dataValues
        Log.d(TAG,"Creating datavalues from scores...");
        for(CompositeScore compositeScore:compositeScores){
            compositeScore.accept(this);
        }

        //Turn question values into dataValues
        Log.d(TAG,"Creating datavalues from questions...");
        for(Value value:survey.getValues()){
            value.accept(this);
        }

        Log.d(TAG,"Creating datavalues from other stuff...");
        buildMainScores(survey);
    }

    @Override
    public void visit(CompositeScore compositeScore) {
        DataValue dataValue=new DataValue();
        dataValue.setDataElement(compositeScore.getUid());
        dataValue.setLocalEventId(event.getLocalId());
        dataValue.setProvidedElsewhere(false);
        dataValue.setStoredBy(Session.getUser().getName());
        dataValue.setValue(Utils.round(ScoreRegister.getCompositeScore(compositeScore)));
        dataValue.save();
    }

    @Override
    public void visit(Value value) {
        DataValue dataValue=new DataValue();
        dataValue.setDataElement(value.getQuestion().getUid());
        dataValue.setLocalEventId(event.getLocalId());
        dataValue.setProvidedElsewhere(false);
        dataValue.setStoredBy(Session.getUser().getName());
        if(value.getOption()!=null){
            dataValue.setValue(value.getOption().getCode());
        }else{
            dataValue.setValue(value.getValue());
        }
        dataValue.save();
    }

    /**
     * Builds an event from a survey
     * @return
     */
    private Event buildEvent()throws Exception{
        event=new Event();

        event.setStatus(Event.STATUS_COMPLETED);
        event.setFromServer(false);
        event.setOrganisationUnitId(survey.getOrgUnit().getUid());
        event.setProgramId(survey.getTabGroup().getProgram().getUid());
        event.setProgramStageId(survey.getTabGroup().getUid());
        updateEventLocation();
        updateEventDates();
        Log.d(TAG, "Saving event "+event.toString());
        event.save();
        return event;
    }

    /**
     * Fulfills the dates of the event
     */
    private void updateEventDates() {
        completionDate=new Date();
        String completionDateStr=EventExtended.format(completionDate);
        event.setEventDate(completionDateStr);

        //FIXME This should probably be changed in the future
        event.setLastUpdated(completionDateStr);
    }

    /**
     * Builds several datavalues from the mainScore of the survey
     * @param survey
     */
    private void buildMainScores(Survey survey) {

        //MainScoreUID
        DataValue dataValue=new DataValue();
        dataValue.setDataElement(mainScoreUID);
        dataValue.setLocalEventId(event.getLocalId());
        dataValue.setProvidedElsewhere(false);
        dataValue.setStoredBy(Session.getUser().getName());
        dataValue.setValue(survey.getType());
        dataValue.save();

        //MainScore A
        DataValue dataValueA=new DataValue();
        dataValueA.setDataElement(mainScoreAUID);
        dataValueA.setLocalEventId(event.getLocalId());
        dataValueA.setProvidedElsewhere(false);
        dataValueA.setStoredBy(Session.getUser().getName());
        dataValueA.setValue(survey.isTypeA() ? "true" : "false");
        dataValueA.save();

        //MainScore B
        DataValue dataValueB=new DataValue();
        dataValueB.setDataElement(mainScoreBUID);
        dataValueB.setLocalEventId(event.getLocalId());
        dataValueB.setProvidedElsewhere(false);
        dataValueB.setStoredBy(Session.getUser().getName());
        dataValueB.setValue(survey.isTypeB() ? "true" : "false");
        dataValueB.save();

        //MainScoreC
        DataValue dataValueC=new DataValue();
        dataValueC.setDataElement(mainScoreCUID);
        dataValueC.setLocalEventId(event.getLocalId());
        dataValueC.setProvidedElsewhere(false);
        dataValueC.setStoredBy(Session.getUser().getName());
        dataValueC.setValue(survey.isTypeC() ? "true" : "false");
        dataValueC.save();

    }

    /**
     * Several properties must be updated when a survey is about to be sent.
     * This changes will be saved just when process finish successfully.
     * @param compositeScores
     */
    private void updateSurvey(List<CompositeScore> compositeScores){
        survey.setMainScore(ScoreRegister.calculateMainScore(compositeScores));
        survey.setStatus(Constants.SURVEY_SENT);
        survey.setCompletionDate(completionDate);
    }

    /**
     * Updates the location of the current event that it is being processed
     * @throws Exception
     */
    private void updateEventLocation() throws Exception{
        Location lastLocation = LocationMemory.get(survey.getId_survey());
        //If location is required but there is no location -> exception
        if(PreferencesState.getInstance().isLocationRequired() && lastLocation==null){
            throw new Exception(context.getString(R.string.dialog_error_push_no_location_and_required));
        }

        //No location + not required -> done
        if(lastLocation==null){
            return;
        }

        //location -> set lat/lng
        event.setLatitude(lastLocation.getLatitude());
        event.setLongitude(lastLocation.getLongitude());
    }

    /**
     * Saves changes in the survey (supposedly after a successfull push)
     */
    public void saveSurveyStatus(){
        survey.saveMainScore();
        survey.save();
        //To avoid several pushes
        event.setFromServer(true);
        event.save();
    }
}
