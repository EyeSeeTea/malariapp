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
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.IdlingResource;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.util.Log;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.SettingsActivity;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.OrgUnit$Table;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Survey$Table;
import org.eyeseetea.malariacare.utils.Constants;
import org.hamcrest.Matchers;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit$Table;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitDataSet;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitDataSet$Table;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitGroup;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitGroup$Table;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Created by arrizabalaga on 8/02/16.
 */
public class SDKTestUtils {

    private static final String TAG="TestingUtils";
    public static final int DEFAULT_WAIT_FOR_PULL=29;
    public static final int DEFAULT_TEST_TIME_LIMIT=180;

    public static final String HNQIS_DEV_STAGING = "https://hnqis-dev-staging.psi-mis.org";
    public static final String HNQIS_DEV_CI = "https://hnqis-dev-ci.psi-mis.org";
    public static final String TEST_USERNAME_NO_PERMISSION = "testFAIL";
    public static final String TEST_PASSWORD_NO_PERMISSION = "testN0P3rmission";

    public static final String TEST_USERNAME_WITH_PERMISSION = "testOK";
    public static final String TEST_PASSWORD_WITH_PERMISSION = "testP3rmission";

    public static final int TEST_FACILITY_1_IDX=1;
    public static final int TEST_FACILITY_2_IDX=2;

    public static final int TEST_IMCI=1;
    public static final int TEST_CERVICAL_CANCER=2;
    public static final int TEST_FAMILY_PLANNING_IDX=3;

    public static final String MARK_AS_COMPLETED = "Mark as completed";

    public static final String UNABLE_TO_LOGIN = "Unable to log in due to an invalid username or password.";

    public static void setTestTimeoutSeconds(int seconds){
        IdlingPolicies.setMasterPolicyTimeout(
                seconds, TimeUnit.SECONDS);
        IdlingPolicies.setIdlingResourceTimeout(
                seconds, TimeUnit.SECONDS);
    }

    public static void login(String server, String user, String password) {
        //when: login
        onView(withId(org.hisp.dhis.android.sdk.R.id.server_url)).perform(replaceText(server));
        onView(withId(org.hisp.dhis.android.sdk.R.id.username)).perform(replaceText(user));
        onView(withId(org.hisp.dhis.android.sdk.R.id.password)).perform(replaceText(password));
        onView(withId(org.hisp.dhis.android.sdk.R.id.login_button)).perform(click());
    }

    public static void waitForPull(int seconds) {
        //then: wait for progressactivity + dialog + ok (to move to dashboard)
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(seconds * 1000);
        Espresso.registerIdlingResources(idlingResource);

        onView(withText(android.R.string.ok)).perform(click());

        Espresso.unregisterIdlingResources(idlingResource);
    }

    public static Survey waitForPush(int seconds, Long idSurvey){
        //then: wait for pushservice
        //Wait for push: For any reason Thread.sleep is necessary.
        try{
            Thread.sleep(seconds*1000);
        }catch(Exception ex){
        }

        IdlingResource idlingResource = new ElapsedTimeIdlingResource(seconds * 1000);
        Espresso.registerIdlingResources(idlingResource);

        Survey survey=Survey.findById(idSurvey);

        Espresso.unregisterIdlingResources(idlingResource);
        return survey;
    }

    public static void goToSentSurveys(){
        onView(withTagValue(Matchers.is((Object) getActivityInstance().getApplicationContext().getString(R.string.tab_tag_improve)))).perform(click());
    }

    public static void startSurvey(int idxOrgUnit, int idxProgram) {
        //when: click on assess tab + plus button
        onView(withTagValue(Matchers.is((Object) getActivityInstance().getApplicationContext().getString(R.string.tab_tag_assess)))).perform(click());
        onView(withId(R.id.plusButton)).perform(click());

        //then: start survey 'test facility 1'+ 'family planning'+start


        //Wait for SurveyService loads the Orgunit and programs
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(5 * 1000);
        Espresso.registerIdlingResources(idlingResource);

        onView(withId(R.id.org_unit)).perform(click());

        Espresso.unregisterIdlingResources(idlingResource);

        onData(is(instanceOf(OrgUnit.class))).atPosition(idxOrgUnit).perform(click());

        onView(withId(R.id.program)).perform(click());
        onData(is(instanceOf(Program.class))).atPosition(idxProgram).perform(click());

        onView(withId(R.id.create_form_button)).perform(click());

    }

    public static void fillSurvey(int numQuestions, String optionValue) {
        //when: answer NO to every question
        //Wait for fragment load data from SurveyService
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(5 * 1000);
        Espresso.registerIdlingResources(idlingResource);

        onView(withTagValue(Matchers.is((Object) getActivityInstance().getApplicationContext().getString(R.string.tab_tag_assess)))).perform(click());
        for (int i = 0; i < numQuestions; i++) {
            try {
                onData(is(instanceOf(Question.class)))
                        .inAdapterView(withId(R.id.listView))
                        .atPosition(i)
                        .onChildView(withId(R.id.answer))
                        .onChildView(withText(optionValue))//.onChildView(withTagValue(allOf(Matchers.hasProperty("name", containsString(optionValue)))))
                        .perform(click());
            } catch (Exception e){}
        }

        Espresso.unregisterIdlingResources(idlingResource);

        //then: back + confirm
        exitSurvey();
    }

    public static void exitSurvey(){
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

    public static Long getSurveyId(){
        return getSurveyInProgress().getId_survey();
    }

    public static Survey getSurveyInProgress(){
        return new Select()
                .from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS)
                        .eq(Constants.SURVEY_IN_PROGRESS))
                .querySingle();
    }
    public static  OrgUnit getOrgUnit(String id){
        return new Select()
                .from(OrgUnit.class)
                .where(Condition.column(OrgUnit$Table.UID)
                        .eq(id))
                .querySingle();
    }
    public static OrganisationUnit getOrganisationUnit(String id){
        return new Select()
                .from(OrganisationUnit.class)
                .where(Condition.column(OrganisationUnit$Table.ID)
                        .eq(id))
                .querySingle();
    }

    public static List<OrganisationUnitGroup> getOrganisationUnitGroups(String id){
        return new Select()
                .from(OrganisationUnitGroup.class)
                .where(Condition.column(OrganisationUnitGroup$Table.ORGANISATIONUNITID)
                        .eq(id))
                .queryList();
    }

    public static List<OrganisationUnitDataSet> getOrganisationUnitDataSets(String id){
        return new Select()
                .from(OrganisationUnitDataSet.class)
                .where(Condition.column(OrganisationUnitDataSet$Table.ORGANISATIONUNITID)
                        .eq(id))
                .queryList();
    }

    public static org.hisp.dhis.android.sdk.persistence.models.Program getSDKProgram(String id){
        return new Select()
                .from(org.hisp.dhis.android.sdk.persistence.models.Program.class)
                .where(Condition.column(org.hisp.dhis.android.sdk.persistence.models.Program$Table.ID)
                        .eq(id))
                .querySingle();
    }

    public static List<org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit> getAllSDKOrganisationUnits() {
        return new Select().all().from(org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit.class).queryList();
    }

    public static List<org.hisp.dhis.android.sdk.persistence.models.Program> getAllSDKPrograms() {
        return new Select().all().from(org.hisp.dhis.android.sdk.persistence.models.Program.class).queryList();
    }

    public static List<org.hisp.dhis.android.sdk.persistence.models.Event> getAllSDKEvents() {
        return new Select().all().from(org.hisp.dhis.android.sdk.persistence.models.Event.class).queryList();
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

    public static void exitApp(){
        Class actualClass=null;
        try {
            actualClass = SDKTestUtils.getActivityInstance().getClass();
        }catch(Exception e){
        }
        if(actualClass!=null  && !actualClass.equals(LoginActivity.class)) {
            goToLogin();
            Espresso.pressBack();
        }
        else{
            Espresso.pressBack();
        }
    }

    public static void goToLogin(){
        Class actualClass=null;
        try {
            actualClass = SDKTestUtils.getActivityInstance().getClass();
        }catch(Exception e){
        }
        if(actualClass!=null  && !actualClass.equals(LoginActivity.class)) {
            Log.d(TAG, actualClass+"");
            try {
                if (ProgressActivity.class.equals(actualClass)) {
                        try {
                            onView(withText(android.R.string.cancel)).perform(click());
                        } catch (Exception e) {}
                } else if(DashboardActivity.class.equals(actualClass)){
                        try {
                            clickLogout();
                            onView(withText(android.R.string.ok)).perform(click());
                        } catch (Exception e) {}
                    }
                else if(SettingsActivity.class.equals(actualClass)){
                    Espresso.pressBack();
                }
            } catch (Exception e) {}
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            goToLogin();
        }
    }

    public static void clickLogout() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(R.string.settings_menu_logout)).perform(click());
    }

    public static DataInteraction selectRow(int i){
        return onData(is(instanceOf(Survey.class)))
                .inAdapterView(withChild(withId(R.id.assessment_row)))
                .atPosition(i);
    }
}
