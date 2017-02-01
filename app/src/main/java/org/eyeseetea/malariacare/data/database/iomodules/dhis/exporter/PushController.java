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

package org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter;

import android.content.Context;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Delete;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.data.database.utils.PopulateDB;
import org.eyeseetea.malariacare.data.remote.PullDhisSDKDataSource;
import org.eyeseetea.malariacare.receivers.AlarmPushReceiver;
import org.eyeseetea.malariacare.data.remote.SdkController;
import org.eyeseetea.malariacare.data.remote.SdkPushController;
import org.eyeseetea.malariacare.utils.Constants;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.StateFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.TrackedEntityDataValueFlow;

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
    public static ConvertToSDKVisitor converter;

    /**
     * Constructs and register this pull controller to the event bus
     */
    PushController(){
    }

    private void register(){
        SdkController.register(context);
    }

    /**
     * Unregister pull controller from bus events
     */
    private void unregister(){
        SdkController.unregister(context);
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
     * Flag that locks the push of events.
     *
     */

    public static boolean isPushing;

    public boolean isPushing() {
        return isPushing;
    }
    /**
     * Launches the pull process:
     *  - Loads metadata from dhis2 server
     *  - Wipes app database
     *  - Turns SDK into APP data
     * @param ctx
     */
    public boolean push(Context ctx,List<Survey> surveys){
        Log.d(TAG, "Starting PUSH process...");
        if(isPushing)
            return false;
        isPushing =true;

        context=ctx;

        //No survey no push
        if(surveys==null || surveys.size()==0){
            postException(new Exception(context.getString(R.string.progress_push_no_survey)));
            isPushing =false;
            return false;
        }

        try {
            //Converts app data into sdk events
            postProgress(context.getString(R.string.progress_push_preparing_survey));
            Log.d(TAG, "Preparing survey for pushing...");
            
            //// FIXME: 01/02/2017 refactor with clean arquitecture
            Delete.tables(
                    EventFlow.class,
                    TrackedEntityDataValueFlow.class, 
                    StateFlow.class
            );
            convertToSDK(surveys);
            isPushing =EventExtended.getAllEvents().size()>0;
            if(!isPushing)
                return false;
            //Asks sdk to push localdata
            postProgress(context.getString(R.string.progress_push_posting_survey));
            Log.d(TAG, "Pushing survey data to server...");
            SdkPushController.sendEventChanges();
        }catch (Exception ex) {
            Log.e(TAG, "push: " + ex.getLocalizedMessage());
            unregister();
            postException(ex);
            isPushing =false;
            return false;
        }
        return true;
    }

    /**
     * Launches visitor that turns an APP survey into a SDK event
     */
    private void convertToSDK(List<Survey> surveys)throws  Exception{
        Log.d(TAG,"Converting APP survey into a SDK event");
        converter =new ConvertToSDKVisitor(context);
        for(Survey survey:surveys){
            survey.accept(converter);
        }
    }

    /**
     * Notifies a progress into the bus (the caller activity will be listening)
     * @param msg
     */
    private void postProgress(String msg){
        SdkController.postProgress(msg);
    }

    /**
     * Notifies an exception while pulling
     * @param ex
     */
    private void postException(Exception ex){
        AlarmPushReceiver.isDoneFail();
        ex.printStackTrace();
        SdkController.postException(ex);
    }

    /**
     * Notifies that the push is over
     */
    public static void postFinish(boolean success){
        try {
            if(success){
                AlarmPushReceiver.isDoneSuccess();
            }else{
                AlarmPushReceiver.isDoneFail();
            }
            SdkController.postFinish();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    public static void setSurveysAsQuarantine() {
        for(Survey survey:converter.surveys){
            survey.setStatus(Constants.SURVEY_QUARANTINE);
        }
    }
}
