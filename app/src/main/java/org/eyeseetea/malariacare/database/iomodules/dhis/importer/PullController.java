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

package org.eyeseetea.malariacare.database.iomodules.dhis.importer;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.squareup.otto.Subscribe;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OptionSetVisitableFromSDK;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramVisitableFromSDK;
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
import org.hisp.dhis.android.sdk.job.NetworkJob;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.OptionSet;
import org.hisp.dhis.android.sdk.persistence.preferences.ResourceType;
import java.util.List;

/**
 * A static controller that orchestrate the pull process
 * Created by arrizabalaga on 4/11/15.
 */
public class PullController {
    private final String TAG=".PullController";

    private static PullController instance;

    /**
     * Context required to i18n error messages while pulling
     */
    private Context context;

    /**
     * Constructs and register this pull controller to the event bus
     */
    PullController(){
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
    public static PullController getInstance(){
        if(instance==null){
            instance=new PullController();
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
    public void pull(Context ctx){
        Log.d(TAG,"Starting PULL process...");
        context=ctx;
        //Register for event bus
        register();
        //Enabling resources to pull
        enableMetaDataFlags();
        //Delete previous metadata
        MetaDataController.wipe();
        //Pull new metadata
        DhisService.loadData(context);
    }

    /**
     * Enables loading all metadata
     */
    private void enableMetaDataFlags(){
        LoadingController.enableLoading(context, ResourceType.ASSIGNEDPROGRAMS);
        LoadingController.enableLoading(context, ResourceType.PROGRAMS);
        LoadingController.enableLoading(context, ResourceType.OPTIONSETS);
    }

    @Subscribe
    public void onLoadMetadataFinished(NetworkJob.NetworkJobResult<ResourceType> result) {
        if(result==null){
            Log.e(TAG,"onLoadMetadataFinished with null");
            return;
        }

        //Error while pulling
        if(result.getResponseHolder()!=null && result.getResponseHolder().getApiException()!=null){
            Log.e(TAG,result.getResponseHolder().getApiException().getMessage());
            showStatus(context.getString(R.string.dialog_pull_error));
            return;
        }

        //Ok
        wipeDatabase();
        convertFromSDK();
        showStatus(context.getString(R.string.dialog_pull_success));
        unregister();
        Log.d(TAG, "PULL process...OK");
    }

    /**
     * Erase data from app database
     */
    private void wipeDatabase(){
        Log.d(TAG,"Deleting app database...");
        Delete.tables(
                Value.class,
                Score.class,
                Survey.class,
                OrgUnit.class,
                OrgUnitLevel.class,
                User.class,
                QuestionOption.class,
                Match.class,
                QuestionRelation.class,
                Question.class,
                CompositeScore.class,
                Option.class,
                Answer.class,
                Header.class,
                Tab.class,
                TabGroup.class,
                Program.class
        );
    }

    /**
     * Launches visitor that turns SDK data into APP data
     */
    private void convertFromSDK(){
        Log.d(TAG,"Converting SDK into APP data");

        //Convert Programs, Tabgroups, Tabs
        List<String> assignedProgramsIDs=MetaDataController.getAssignedPrograms();
        for(String assignedProgramID:assignedProgramsIDs){
            ConvertFromSDKVisitor converter = new ConvertFromSDKVisitor();
            ProgramVisitableFromSDK programVisitableFromSDK=new ProgramVisitableFromSDK(MetaDataController.getProgram(assignedProgramID));
            programVisitableFromSDK.accept(converter);
        }

        //Convert Answers, Options
        List<OptionSet> optionSets=MetaDataController.getOptionSets();
        for(OptionSet optionSet:optionSets){
            ConvertFromSDKVisitor converter = new ConvertFromSDKVisitor();
            OptionSetVisitableFromSDK optionSetVisitableFromSDK=new OptionSetVisitableFromSDK(optionSet);
            optionSetVisitableFromSDK.accept(converter);
        }
    }

    /**
     * Shows a dialog with the given message
     * @param msg
     */
    private void showStatus(String msg){
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.dialog_title_pull_response))
                .setMessage(msg)
                .setNeutralButton(android.R.string.yes,null).create().show();
    }




}
