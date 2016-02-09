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

package org.eyeseetea.malariacare.test.utils;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.IdlingResource;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.OrgUnit$Table;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Program$Table;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Survey$Table;
import org.eyeseetea.malariacare.utils.Constants;
import org.hamcrest.Matchers;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Created by arrizabalaga on 8/02/16.
 */
public class SDKTestUtils {

    public static final String HNQIS_DEV_STAGING = "https://hnqis-dev-staging.psi-mis.org";
    public static final String TEST_USERNAME_NO_PERMISSION = "test";
    public static final String TEST_PASSWORD_NO_PERMISSION = "testN0permission";

    public static final String TEST_USERNAME_WITH_PERMISSION = "idelcano";
    public static final String TEST_PASSWORD_WITH_PERMISSION = "Ignacio2015xx";

    public static final String MARK_AS_COMPLETED = "Mark as completed";


    public static void login(String server, String user, String password) {
        IdlingPolicies.setIdlingResourceTimeout(60, TimeUnit.SECONDS);

        //when: login
        onView(withId(org.hisp.dhis.android.sdk.R.id.server_url)).perform(replaceText(server));
        onView(withId(org.hisp.dhis.android.sdk.R.id.username)).perform(replaceText(user));
        onView(withId(org.hisp.dhis.android.sdk.R.id.password)).perform(replaceText(password));
        onView(withId(org.hisp.dhis.android.sdk.R.id.login_button)).perform(click());
    }

    public static void forceDisconnection() {
        //then: wait for progressactivity + dialog + ok (to move to dashboard)
        WifiManager wifiManager = (WifiManager)getActivityInstance().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
    }
    public static void waitForPull(int secs) {
        //then: wait for progressactivity + dialog + ok (to move to dashboard)
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(secs * 1000);
        Espresso.registerIdlingResources(idlingResource);

        try{
            Thread.sleep(secs*2*1000);
        }catch(Exception ex){
        }
        onView(withText(android.R.string.ok)).perform(click());

        Espresso.unregisterIdlingResources(idlingResource);
    }
    public static void cancellPull(int secs) {
        //then: wait for progressactivity + dialog + ok (to move to dashboard)
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(secs * 1000);
        Espresso.registerIdlingResources(idlingResource);

        try{
            Thread.sleep(secs*1000);
        }catch(Exception ex){
        }

        onView(withText(android.R.string.cancel)).perform(click());

        Espresso.unregisterIdlingResources(idlingResource);
    }
    public static Survey waitForPush(int secs, Long idSurvey){
        //then: wait for pushservice
        try{
            Thread.sleep(secs*1000);
        }catch(Exception ex){
        }

        return Survey.findById(idSurvey);
    }

    public static void startSurvey(int idxOrgUnit, int idxProgram) {
        //when: click on assess tab + plus button
        onView(withTagValue(Matchers.is((Object) "tab_assess"))).perform(click());
        onView(withId(R.id.plusButton)).perform(click());

        //then: start survey 'test facility 1'+ 'family planning'+start
        onView(withId(R.id.org_unit)).perform(click());
        onData(is(instanceOf(OrgUnit.class))).atPosition(idxOrgUnit).perform(click());

        onView(withId(R.id.program)).perform(click());
        onData(is(instanceOf(Program.class))).atPosition(idxProgram).perform(click());

        onView(withId(R.id.create_form_button)).perform(click());

    }

    public static void fillSurvey(int numQuestions, String optionValue) {
        //when: answer NO to every question
        for (int i = 0; i < numQuestions; i++) {
            onData(is(instanceOf(Question.class)))
                    .inAdapterView(withId(R.id.listView))
                    .atPosition(i)
                    .onChildView(withId(R.id.answer))
                    .onChildView(withText(optionValue))
                    .perform(click());
        }

        //then: back + confirm
        Espresso.pressBack();
        onView(withText(android.R.string.ok)).perform(click());
    }

    public static Long markInProgressAsCompleted() {
        Long idSurvey = getSurveyId();

        //when: Mark as completed
        onView(withId(R.id.score)).perform(click());
        onView(withText(MARK_AS_COMPLETED)).perform(click());
        return idSurvey;
    }

    private static Long getSurveyId(){
        return getSurveyInProgress().getId_survey();
    }

    private static Survey getSurveyInProgress(){
        return new Select()
                .from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS)
                        .eq(Constants.SURVEY_IN_PROGRESS))
                .querySingle();
    }
    public static  OrgUnit getOrgUnit(String name){
        return new Select()
                .from(OrgUnit.class)
                .where(Condition.column(OrgUnit$Table.NAME)
                        .eq(name))
                .querySingle();
    }
    public static Program getProgram(String name){
        return new Select()
                .from(Program.class)
                .where(Condition.column(Program$Table.NAME)
                        .eq(name))
                .querySingle();
    }


    public static List<org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit> getAllSDKOrganisationUnits() {
        return new Select().all().from(org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit.class).queryList();
    }

    public static List<org.hisp.dhis.android.sdk.persistence.models.Program> getAllSDKPrograms() {
        return new Select().all().from(org.hisp.dhis.android.sdk.persistence.models.Program.class).queryList();
    }

    public static Activity getActivityInstance() {
        final Activity[] activity = new Activity[1];
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
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

    public static void setWifiEnabled(boolean state){
        WifiManager wifiManager = (WifiManager)getActivityInstance().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(state);
        //Force wait to wifi conection
        try{
            Thread.sleep(2*1000);
        }catch(Exception ex){
        }
    }
    public static boolean networkState(){
        ConnectivityManager cm =
                (ConnectivityManager) SDKTestUtils.getActivityInstance().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected;
        if(activeNetwork==null || (activeNetwork!=null && !activeNetwork.isConnectedOrConnecting()))
            isConnected=false;
        else
            isConnected = true;
        return isConnected;
    }
}
