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

import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Survey$Table;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AssessmentUnsentAdapter;
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
        Session.setSurvey(null);
        Session.setAdapterUnsent(null);
    }

    public static void cleanDB(){
        if(!databaseExists()){
            return;
        }
        // Clean DB
        Delete.tables(Question.class, CompositeScore.class, Option.class, Answer.class, Header.class, Tab.class, Program.class, OrgUnit.class, User.class, Value.class, Survey.class);
        /*new Delete().from(Question.class).where(Condition.column(Question$Table.ID).isNotNull()).query();
        new Delete().from(CompositeScore.class).where(Condition.column(CompositeScore$Table.ID).isNotNull()).query();;
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

    public static Survey mockSessionSurvey(int numSurvey, int numProgram, int select){
        List<Survey> surveys=mockSurveys(numSurvey, numProgram);
        Survey survey=surveys.get(select);
        Session.setSurvey(survey);
        return survey;
    }

    public static void mockSessionSurvey(int num, int select){
        mockSessionSurvey(num, 0, select);
    }

    public static List<Survey> mockSurveys(int numOrgs, int numPrograms){
        List<OrgUnit> orgUnitList = new Select().all().from(OrgUnit.class).queryList();
        List<Program> programList = Program.list();
        Program program=programList.get(numPrograms);
        User user =getSafeUser();

        for(int i=0;i<numOrgs;i++){
            Survey survey=new Survey(orgUnitList.get(i%numOrgs),program.getTabGroups().get(0),user);
            survey.save();
        }
        List<Survey> surveys = new Select().from(Survey.class).where(Condition.column(Survey$Table.ID_USER).eq(user.getId_user())).queryList();
        Session.setAdapterUnsent(new AssessmentUnsentAdapter(surveys, InstrumentationRegistry.getTargetContext()));
        return surveys;
    }

    public static List<Survey> mockSurveys(int num){
        return mockSurveys(num, 0);
    }

    private static User getSafeUser(){
        User user=Session.getUser();
        if(user!=null){
            return user;
        }
        user = new User("user", "user");
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

