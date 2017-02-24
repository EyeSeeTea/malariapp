/*
 * Copyright (c) 2015.
 *
 * This file is part of Health Network QIS App.
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

package org.eyeseetea.malariacare;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.raizlabs.android.dbflow.config.EyeSeeTeaGeneratedDatabaseHolder;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.eyeseetea.malariacare.data.database.model.User;
import org.eyeseetea.malariacare.data.database.utils.LocationMemory;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.layout.dashboard.builder.AppSettingsBuilder;
import org.eyeseetea.malariacare.layout.utils.AutoTabLayoutUtils;
import org.eyeseetea.malariacare.utils.Permissions;
import org.eyeseetea.malariacare.views.TypefaceCache;
import org.hisp.dhis.client.sdk.android.api.D2;

import io.fabric.sdk.android.Fabric;

public class EyeSeeTeaApplication extends Application {

    public static Permissions permissions;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        AppSettingsBuilder.getInstance().init(getApplicationContext());
        PreferencesState.getInstance().init(getApplicationContext());
        LocationMemory.getInstance().init(getApplicationContext());
        TypefaceCache.getInstance().init(getApplicationContext());
        AutoTabLayoutUtils.init();

        D2.init(this);
        FlowConfig flowConfig = new FlowConfig
                .Builder(this)
                .addDatabaseHolder(EyeSeeTeaGeneratedDatabaseHolder.class)
                .build();
        FlowManager.init(flowConfig);

        // Create indexes to accelerate the DB selects and avoid SQlite errors
        createDBIndexes();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        FlowManager.destroy();
    }


    public Class<? extends Activity> getMainActivity() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (User.getLoggedUser() != null && sharedPreferences.getBoolean(
                getApplicationContext().getResources().getString(R.string.pull_metadata), false)) {
            return new DashboardActivity().getClass();
        } else if (!ProgressActivity.PULL_CANCEL) {
            return PreferencesState.getInstance().getMainActivity();
        } else {
            return LoginActivity.class;
        }

    }

    private void createDBIndexes() {
        // NOTE: This is to speed up some DB requests, and avoid some anoying messages from the
        // DB on execution time
        /*
        new Index<ProgramStageDataElement>("ProgramStageDataElement_DataElement").on
        (ProgramStageDataElement.class, ProgramStageDataElement$Table.DATAELEMENT).enable();
        new Index<ProgramStageDataElement>("ProgramStageDataElement_ProgramStage").on
        (ProgramStageDataElement.class, ProgramStageDataElement$Table.PROGRAMSTAGE).enable();
        new Index<ProgramStageDataElement>("ProgramStageDataElement_ProgramStageSection").on
        (ProgramStageDataElement.class, ProgramStageDataElement$Table.PROGRAMSTAGESECTION).enable();
        new Index<ProgramStage>("ProgramStage_Program").on(ProgramStage.class, ProgramStage$Table
        .PROGRAM).enable();
        new Index<ProgramStage>("ProgramStage_Id").on(ProgramStage.class, ProgramStage$Table.ID)
        .enable();
        new Index<QuestionOption>("QuestionOption_id_question").on(QuestionOption.class,
        QuestionOption$Table.ID_QUESTION).enable();
        new Index<QuestionRelation>("QuestionRelation_operation").on(QuestionRelation.class,
        QuestionRelation$Table.OPERATION).enable();
        new Index<QuestionRelation>("QuestionRelation_id_question").on(QuestionRelation.class,
        QuestionRelation$Table.ID_QUESTION).enable();
        new Index<Match>("Match_id_question_relation").on(Match.class, Match$Table
        .ID_QUESTION_RELATION).enable();
        new Index<Value>("Value_id_survey").on(Value.class, Value$Table.ID_SURVEY).enable();
        new Index<DataElement>("DataElement_id").on(DataElement.class, DataElement$Table.ID)
        .enable();
        */
    }

    /**
     * Function used to make DBFlow compatible with multidex
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
