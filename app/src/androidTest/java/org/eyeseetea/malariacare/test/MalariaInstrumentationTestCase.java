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
import android.app.Fragment;
import android.app.FragmentManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;

import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositiveScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.database.utils.Session;

/**
 * Created by arrizabalaga on 21/05/15.
 */

public abstract class MalariaInstrumentationTestCase <T extends Activity> extends ActivityInstrumentationTestCase2<T> {
//public abstract class  MalariaInstrumentationTestCase<T extends Activity> extends Activity{

    public static final String DATABASE_FULL_PATH = "/data/data/org.eyeseetea.malariacare/databases/malariacare.db";

    public MalariaInstrumentationTestCase(Class<T> activityClass) {
        super(activityClass);
    }

    public void cleanDB(){
        if(!databaseExists()){
            return;
        }

        Log.i(".LoginActivityTest", "Cleaning DB");
        Question.deleteAll(Question.class);
        CompositiveScore.deleteAll(CompositiveScore.class);
        Option.deleteAll(Option.class);
        Answer.deleteAll(Answer.class);
        Header.deleteAll(Header.class);
        Tab.deleteAll(Tab.class);
        Program.deleteAll(Program.class);
        OrgUnit.deleteAll(OrgUnit.class);
        User.deleteAll(User.class);

        Session.setUser(null);
        Session.setSurvey(null);
        Session.setAdapter(null);
    }

    public Fragment waitForFragment(int id, int timeout) {
        long endTime = SystemClock.uptimeMillis() + timeout;
        FragmentManager fragmentManager=getActivity().getFragmentManager();
        while (SystemClock.uptimeMillis() <= endTime) {
            Fragment fragment = fragmentManager.findFragmentById(id);
            if (fragment != null) {
                return fragment;
            }
        }
        return null;
    }

    public void setText(final View v,String text){
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                v.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();
        getInstrumentation().sendStringSync(text);
        getInstrumentation().waitForIdleSync();
    }

    public void populateData(Activity activity){
        //prerequisite
        cleanDB();

        try {
            PopulateDB.populateDummyData();
            PopulateDB.populateDB(activity.getAssets());
        }catch(Exception ex){
            Log.e(".DashboardActivityTest",ex.getMessage());
        }
    }

    private boolean databaseExists() {
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
}
