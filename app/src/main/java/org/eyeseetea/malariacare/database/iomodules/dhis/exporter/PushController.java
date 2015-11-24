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
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.squareup.otto.Subscribe;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.ConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.SyncProgressStatus;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.DataElementExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OptionSetExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OrganisationUnitExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.UserAccountExtended;
import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Match;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.OrgUnitLevel;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.QuestionOption;
import org.eyeseetea.malariacare.database.model.QuestionRelation;
import org.eyeseetea.malariacare.database.model.Score;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.model.Value;
import org.hisp.dhis.android.sdk.controllers.DhisService;
import org.hisp.dhis.android.sdk.controllers.LoadingController;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController;
import org.hisp.dhis.android.sdk.job.NetworkJob;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.OptionSet;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.preferences.ResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * A static controller that orchestrate the push process
 * Created by arrizabalaga on 4/11/15.
 */
public class PushController {
    private final String TAG=".PushController";

    private static PushController instance;

    /**
     * Context required to i18n error messages while pulling
     */
    private Context context;

    /**
     * The stateful converter used to turn a survey into its corresponding event + datavalues;
     */
    ConvertToSDKVisitor converter;

    /**
     * Constructs and register this pull controller to the event bus
     */
    PushController(){
    }

    private void register(){
        Dhis2Application.bus.register(this);
    }

    /**
     * Unregister pull controller from bus events
     */
    private void unregister(){
        Dhis2Application.bus.unregister(this);
    }

    /**
     * Singleton constructor
     * @return
     */
    public static PushController getInstance(){
        if(instance==null){
            instance=new PushController();
        }
        return instance;
    }

    /**
     * Launches the pull process:
     *  - Loads metadata from dhis2 server
     *  - Wipes app database
     *  - Turns SDK into APP data
     * @param ctx
     */
    public void push(Context ctx,Survey survey){
        Log.d(TAG, "Starting PUSH process...");
        context=ctx;

        //No survey no push
        if(survey==null){
            postException(new Exception(context.getString(R.string.progress_push_no_survey)));
            return;
        }

        try {
            //Register for event bus
            register();

            //Converts app data into sdk events
            postProgress(context.getString(R.string.progress_push_preparing_survey));
            Log.d(TAG, "Preparing survey for pushing...");
            convertToSDK(survey);

            //Asks sdk to push localdata
            postProgress(context.getString(R.string.progress_push_posting_survey));
            Log.d(TAG, "Pushing survey data to server...");
            DhisService.sendData();

        }catch (Exception ex){
            Log.e(TAG,"push: "+ex.getLocalizedMessage());
            unregister();
            postException(ex);
        }
    }

    @Subscribe
    public void onSendDataFinished(final NetworkJob.NetworkJobResult<ResourceType> result) {
        new Thread(){
            @Override
            public void run(){
                try {
                    if (result == null) {
                        Log.e(TAG, "onSendDataFinished with null");
                        return;
                    }

                    //Error while pulling
                    if (result.getResponseHolder() != null && result.getResponseHolder().getApiException() != null) {
                        Log.e(TAG, result.getResponseHolder().getApiException().getMessage());
                        postException(new Exception(context.getString(R.string.dialog_pull_error)));
                        return;
                    }
                    //Ok: Updates
                    postProgress(context.getString(R.string.progress_push_updating_survey));
                    Log.d(TAG, "Updating pushed survey data...");
                    converter.saveSurveyStatus();

                    postFinish();
                    Log.d(TAG, "PUSH process...OK");
                }catch (Exception ex){
                    Log.e(TAG,"onSendDataFinished: "+ex.getLocalizedMessage());
                    postException(ex);
                }finally {
                    unregister();
                }
            }
        }.start();
    }

    /**
     * Launches visitor that turns an APP survey into a SDK event
     */
    private void convertToSDK(Survey survey)throws  Exception{
        Log.d(TAG,"Converting APP survey into a SDK event");
        converter =new ConvertToSDKVisitor(context);
        survey.accept(converter);
    }

    /**
     * Notifies a progress into the bus (the caller activity will be listening)
     * @param msg
     */
    private void postProgress(String msg){
        Dhis2Application.getEventBus().post(new SyncProgressStatus(msg));
    }

    /**
     * Notifies an exception while pulling
     * @param ex
     */
    private void postException(Exception ex){
        Dhis2Application.getEventBus().post(new SyncProgressStatus(ex));
    }

    /**
     * Notifies that the pull is over
     */
    private void postFinish(){
        Dhis2Application.getEventBus().post(new SyncProgressStatus());
    }


}
