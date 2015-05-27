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
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositiveScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.database.utils.Session;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.List;

/**
 * Created by arrizabalaga on 25/05/15.
 */
public class MalariaEspressoTest {

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
    }

    public static void cleanSession(){
        Session.setUser(null);
        Session.setSurvey(null);
        Session.setAdapter(null);
    }

    public static void cleanDB(){
        if(!databaseExists()){
            return;
        }
        Question.deleteAll(Question.class);
        CompositiveScore.deleteAll(CompositiveScore.class);
        Option.deleteAll(Option.class);
        Answer.deleteAll(Answer.class);
        Header.deleteAll(Header.class);
        Tab.deleteAll(Tab.class);
        Program.deleteAll(Program.class);
        OrgUnit.deleteAll(OrgUnit.class);
        User.deleteAll(User.class);
        Value.deleteAll(Value.class);
        Survey.deleteAll(Survey.class);
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
            PopulateDB.populateDummyData();
            PopulateDB.populateDB(assetManager);
        }catch(Exception ex){
            Log.e(".MalariaEspressoTest", ex.getMessage());
        }
    }

    public static void mockSurveys(int num){
        List<OrgUnit> orgUnitList=OrgUnit.find(OrgUnit.class, null, null);
        List<Program> programList=Program.find(Program.class,null,null);
        Program program=programList.get(0);
        User user =getSafeUser();

        for(int i=0;i<num;i++){
            Survey survey=new Survey(orgUnitList.get(i%num),program,user);
            survey.save();
        }

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

}
