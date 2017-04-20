/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.database.model.AnswerDB;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.HeaderDB;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.Survey$Table;
import org.eyeseetea.malariacare.data.database.model.TabDB;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.data.database.utils.PopulateDB;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.test.utils.IntentServiceIdlingResource;

import java.util.Collection;
import java.util.List;

/**
 * Created by arrizabalaga on 25/05/15.
 */
public class MalariaEspressoTest {

    protected IntentServiceIdlingResource idlingResource;
    protected Resources res;
    public static final String DATABASE_NAME="malariacare.db";
    public static final String DATABASE_FULL_PATH = "/data/data/org.eyeseetea.malariacare/databases/"+DATABASE_NAME;

    public static void init(){
        cleanAll();
    }

    public void setup(){
        res = InstrumentationRegistry.getTargetContext().getResources();
    }

    public static void cleanAll(){
        cleanDB();
        cleanSession();
        cleanSettings();
    }

    public static void cleanSession(){
        Session.setUser(null);
        Session.setSurveyByModule(null,"");
    }

    public static void cleanDB(){
        if(!databaseExists()){
            return;
        }
        // Clean DB
        Delete.tables(QuestionDB.class, CompositeScoreDB.class, OptionDB.class, AnswerDB.class, HeaderDB.class, TabDB.class, ProgramDB.class, OrgUnitDB.class, UserDB.class, ValueDB.class, SurveyDB.class);
        /*new Delete().from(Question.class).where(Condition.column(Question$Table.ID).isNotNull()).query();
        new Delete().from(CompositeScoreDB.class).where(Condition.column(CompositeScoreDB$Table.ID).isNotNull()).query();;
        new Delete().from(Option.class).where(Condition.column(Option$Table.ID).isNotNull()).query();;
        new Delete().from(Answer.class).where(Condition.column(Answer$Table.ID).isNotNull()).query();;
        new Delete().from(Header.class).where(Condition.column(Header$Table.ID).isNotNull()).query();;
        new Delete().from(Tab.class).where(Condition.column(Tab$Table.ID).isNotNull()).query();;
        new Delete().from(Program.class).where(Condition.column(Program$Table.ID).isNotNull()).query();;
        new Delete().from(OrgUnit.class).where(Condition.column(OrgUnit$Table.ID).isNotNull()).query();;
        new Delete().from(User.class).where(Condition.column(User$Table.ID).isNotNull()).query();;
        new Delete().from(Value.class).where(Condition.column(Value$Table.ID).isNotNull()).query();;
        new Delete().from(Survey.class).where(Condition.column(Survey$Table.ID).isNotNull()).query();;*/
    }

    public static void cleanSettings(){
        Context activity = InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }


    public static boolean databaseExists() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(DATABASE_FULL_PATH, null, SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
            checkDB.releaseReference();
        } catch (Exception e) {
            return false;
        }
        return checkDB != null;
    }

    public static void populateData(AssetManager assetManager){
        try {
            cleanAll();
            PopulateDB.populateDB(assetManager);
        }catch(Exception ex){
            Log.e(".MalariaEspressoTest", ex.getMessage());
        }
    }

    public static SurveyDB mockSessionSurvey(int numSurvey, int numProgram, int select){
        List<SurveyDB> surveys=mockSurveys(numSurvey, numProgram);
        SurveyDB survey=surveys.get(select);
        Session.setSurveyByModule(survey,"");
        return survey;
    }

    public static void mockSessionSurvey(int num, int select){
        mockSessionSurvey(num, 0, select);
    }

    public static List<SurveyDB> mockSurveys(int numOrgs, int numPrograms){
        List<OrgUnitDB> orgUnitList = new Select().all().from(OrgUnitDB.class).queryList();
        List<ProgramDB> programList = ProgramDB.list();
        ProgramDB program=programList.get(numPrograms);
        UserDB user =getSafeUser();

        for(int i=0;i<numOrgs;i++){
            SurveyDB survey=new SurveyDB(orgUnitList.get(i%numOrgs),program,user);
            survey.save();
        }
        List<SurveyDB> surveys = new Select().from(SurveyDB.class).where(Condition.column(Survey$Table.ID_USER).eq(user.getId_user())).queryList();
        return surveys;
    }

    public static List<SurveyDB> mockSurveys(int num){
        return mockSurveys(num, 0);
    }

    private static UserDB getSafeUser(){
        UserDB user=Session.getUser();
        if(user!=null){
            return user;
        }
        user = new UserDB("user", "user");
        user.save();
        Session.setUser(user);
        return user;
    }

    protected Activity getActivityInstance(){
        final Activity[] activity = new Activity[1];
        Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
        instrumentation.waitForIdleSync();
        instrumentation.runOnMainSync(new Runnable() {
            public void run() {
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                if (resumedActivities.iterator().hasNext()) {
                    activity[0] = (Activity) resumedActivities.iterator().next();
                }
            }
        });

        return activity[0];
    }

    protected static void clearSharedPreferences(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getTargetContext());
        sharedPreferences.edit().clear().commit();
    }

}

