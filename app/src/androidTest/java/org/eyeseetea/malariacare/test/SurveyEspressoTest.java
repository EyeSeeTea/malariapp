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
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.test.suitebuilder.annotation.LargeTest;

import org.eyeseetea.malariacare.DashboardDetailsActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.SurveyActivity;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.eyeseetea.malariacare.test.utils.EditCardScaleMatcher.hasEditCardScale;
import static org.eyeseetea.malariacare.test.utils.TextCardScaleMatcher.hasTextCardScale;
import static org.eyeseetea.malariacare.test.utils.UncheckeableRadioButtonScaleMatcher.hasRadioButtonScale;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;


/**
 *
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SurveyEspressoTest extends MalariaEspressoTest{

    @Rule
    public IntentsTestRule<SurveyActivity> mActivityRule = new IntentsTestRule<>(
            SurveyActivity.class);

    @BeforeClass
    public static void init(){
        populateData(InstrumentationRegistry.getTargetContext().getAssets());
        mockSessionSurvey(2, 0);
    }

    @Before
    public void setup(){
        super.setup();
    }

    @Test
    public void form_views() {
        //GIVEN
        Activity activity = getActivityInstance();
        cleanAll();
        populateData(InstrumentationRegistry.getTargetContext().getAssets());
        mockSessionSurvey(1, 0, 0);

        //THEN
        onView(withId(R.id.tabSpinner)).check(matches(isDisplayed()));
        onView(withText("General Info")).check(matches(isDisplayed()));
    }

    @Test
    public void back_shows_dialog(){
        //GIVEN
        pressBack();

        //THEN
        onView(withText(android.R.string.no)).check(matches(isDisplayed()));
        onView(withText(android.R.string.yes)).check(matches(isDisplayed()));
    }

    @Test
    public void back_yes_intent(){
        //GIVEN
        pressBack();

        //WHEN
        onView(withText(android.R.string.yes)).perform(click());

        //THEN
        assertEquals(DashboardDetailsActivity.class, getActivityInstance().getClass());
    }

    @Test
    public void change_to_scored_tab(){
        //WHEN: Select 'Profile' tab
        whenTabSelected(1);

        //THEN
        onView(withText("HR - Nurses")).check(matches(isDisplayed()));
        onView(withId(R.id.subtotalScoreText)).check(matches(isDisplayed()));
    }

    @Test
    public void change_to_score(){
        //WHEN: Select 'Score' tab
        whenTabSelected(10);

        //THEN
        onView(withText(R.string.score_info_case1)).check(matches(isDisplayed()));
        onView(withText(R.string.score_info_case2)).check(matches(isDisplayed()));
        onView(withText(R.string.score_info_case3)).check(matches(isDisplayed()));
    }

    @Test
    public void change_to_composite_score(){
        //WHEN: Select 'Composite Score' tab
        whenTabSelected(11);

        //THEN
        onView(withText("Services, materials and reporting")).check(matches(isDisplayed()));
    }

    @Test
    public void in_c1_rdt_score_some_points() {
        //GIVEN
        Activity activity = getActivityInstance();
        cleanAll();
        populateData(InstrumentationRegistry.getTargetContext().getAssets());
        mockSessionSurvey(1, 0, 0);

        //WHEN: Select 'C1-RDT' tab
        whenTabSelected(3);

        //WHEN: Some answers 'Yes'
        for(int i=6;i<=16;i++){
            whenDropDownAnswered(i,true);
        }

        //THEN
        onView(withId(R.id.score)).check(matches(withText("66")));
        onView(withId(R.id.qualitativeScore)).check(matches(withText(getActivityInstance().getString(R.string.fair))));
    }

    @Test
    public void global_scores_are_calculated(){
        //WHEN: Select 'C1-RDT' tab | Some answers 'Yes'
        whenTabSelected(3);

        for(int i=6;i<=16;i++){
            whenDropDownAnswered(i,true);
        }

        //WHEN: Select 'Score' tab
        whenTabSelected(10);

        //THEN
        onView(withId(R.id.totalScore)).check(matches(withText("4")));
        onView(withId(R.id.rdtAvg)).check(matches(withText("22")));
    }

    @Test
    public void radiobutton_textsize_changes(){
        //GIVEN
        Activity activity = getActivityInstance();
        cleanAll();
        populateData(InstrumentationRegistry.getTargetContext().getAssets());
        mockSessionSurvey(1, 1, 0);

        //WHEN: Select Large fonts on Settings
        whenFontSizeChange(activity, 3);

        //WHEN: Access a ICM survey (contains rabiobuttons)
        WhenAssessmentSelected("Health Facility 0", "ICM");

        //THEN: Check font size has properly changed
        onData(is(instanceOf(Question.class))).inAdapterView(withId(R.id.listView)).atPosition(0)
                .onChildView(withId(R.id.answer))
                .onChildView(withText(activity.getString(R.string.yes)))
                .check(matches(hasRadioButtonScale(activity.getString(R.string.font_size_level3))));
    }

    @Test
    public void textcard_textsize_changes(){
        //GIVEN
        Activity activity = getActivityInstance();
        cleanAll();
        populateData(InstrumentationRegistry.getTargetContext().getAssets());
        mockSessionSurvey(1, 1, 0);

        //WHEN: Select Large fonts on Settings
        whenFontSizeChange(activity, 3);

        //WHEN: Access a ICM survey (contains rabiobuttons)
        WhenAssessmentSelected("Health Facility 0", "ICM");

        //THEN: Check font size has properly changed
        onData(is(instanceOf(Question.class))).inAdapterView(withId(R.id.listView)).atPosition(1)
                .onChildView(withId(R.id.statement))
                .check(matches(hasTextCardScale(activity.getString(R.string.font_size_level3))));
    }

    @Test
    public void editcard_textsize_changes(){
        //GIVEN
        Activity activity = getActivityInstance();
        cleanAll();
        populateData(InstrumentationRegistry.getTargetContext().getAssets());
        mockSessionSurvey(1, 0, 0);

        //WHEN: Select Large fonts on Settings
        whenFontSizeChange(activity, 3);

        //WHEN: Access a Clinical Case Management survey (contains edit fields)
        WhenAssessmentSelected("Health Facility 0", "Clinical Case Management");

        //THEN: Check font size has properly changed
        onData(is(instanceOf(Question.class))).inAdapterView(withId(R.id.listView)).atPosition(1)
                .onChildView(withId(R.id.answer))
                .check(matches(hasEditCardScale(activity.getString(R.string.font_size_level3))));
    }

    @Test
    public void num_dem_show_hide(){
        //GIVEN
        Activity activity = getActivityInstance();
        cleanAll();
        populateData(InstrumentationRegistry.getTargetContext().getAssets());
        mockSessionSurvey(1, 0, 0);

        //WHEN: We are in Dashboard
        pressBack();
        onView(withText(activity.getString(android.R.string.ok))).perform(click()); // confirm exit

        //WHEN: Access a Clinical Case Management survey
        WhenAssessmentSelected("Health Facility 0", "Clinical Case Management");
        whenTabSelected(1);

        //THEN: Check that num/dems are not yet being shown
        onView(withId(R.id.totalNum)).check(matches(not(isDisplayed())));

        //WHEN: Select show num/dems (by default they're not shown)
        whenToggleShowHideNumDem(activity);

        //WHEN: Access a Clinical Case Management survey
        WhenAssessmentSelected("Health Facility 0", "Clinical Case Management");
        whenTabSelected(1);

        //THEN: Check that num/dems are now being shown
        onView(withId(R.id.totalNum)).check(matches(isDisplayed()));
    }

    @Test
    public void delete_survey_by_swipping(){
        //GIVEN
        Activity activity = getActivityInstance();
        cleanAll();
        populateData(InstrumentationRegistry.getTargetContext().getAssets());
        mockSessionSurvey(1, 1, 0);

        //WHEN: Access a ICM survey (contains rabiobuttons)
        pressBack();
        onView(withText(activity.getString(android.R.string.ok))).perform(click()); // confirm exit
        WhenAssessmentSwipeAndOk("Health Facility 0", "ICM");

        //THEN: Check font size has properly changed
        checkAssessmentDoesntExist("Health Facility 0", "ICM");
    }

    private void checkAssessmentDoesntExist(String orgUnit, String program) {
        onView(allOf(withId(R.id.assessment_row),
                withChild(allOf(
                        withChild(allOf(withId(R.id.facility), withText(orgUnit))),
                        withChild(allOf(withId(R.id.survey_type), withText("- " + program))))))).check(doesNotExist());
    }

    /**
     * From Dashboard access survey
     * @param orgUnit orgUnit of the survey we want to access
     * @param program program of the survey we want to access
     */
    private void WhenAssessmentSelected(String orgUnit, String program) {
        onView(allOf(withId(R.id.assessment_row),
                withChild(allOf(
                        withChild(allOf(withId(R.id.facility), withText(orgUnit))),
                        withChild(allOf(withId(R.id.survey_type), withText("- " + program)))))))
                .perform(click());
    }

    /**
     * From Dashboard delete survey
     * @param orgUnit orgUnit of the survey we want to delete
     * @param program program of the survey we want to delete
     */
    private void WhenAssessmentSwipeAndOk(String orgUnit, String program) {
        onView(allOf(withId(R.id.assessment_row),
                withChild(allOf(
                        withChild(allOf(withId(R.id.facility), withText(orgUnit))),
                        withChild(allOf(withId(R.id.survey_type), withText("- " + program)))))))
                .perform(swipeRight());
        // FIXME: It looks like sometimes this ok button is not being found, maybe it appears with a little delay and the check occurs before?
        onView(withText(getActivityInstance().getString(android.R.string.ok))).perform(click()); // confirm delete
    }

    /**
     * Change font size
     * @param activity activity to get the preferences
     * @param num font size in a discrete int scale [0: xsmall - 1: small - 2: medium - 3: large - 4: xlarge]
     */
    private void whenFontSizeChange(Activity activity, int num) {
        openActionBarOverflowOrOptionsMenu(getActivityInstance());
        onView(withText(activity.getString(R.string.settings_menu_configuration))).perform(click());
        onView(withText(activity.getString(R.string.settings_checkbox_customize_fonts))).perform(click());
        onView(withText(activity.getString(R.string.settings_list_font_sizes))).perform(click());
        onView(withText((activity.getResources().getStringArray(R.array.settings_array_titles_font_sizes))[num])).perform(click());
        pressBack(); // Exit settings
        pressBack(); // Exit survey
        onView(withText(activity.getString(android.R.string.ok))).perform(click()); // confirm exit
    }

    /**
     * Change show/hide num/dem preference
     * @param activity
     */
    private void whenToggleShowHideNumDem(Activity activity) {
        openActionBarOverflowOrOptionsMenu(getActivityInstance());
        onView(withText(activity.getString(R.string.settings_menu_configuration))).perform(click());
        onView(withText(activity.getString(R.string.settings_checkbox_show_num_dems))).perform(click());
        pressBack(); // Exit settings
        //pressBack(); // Exit survey
        //onView(withText(activity.getString(android.R.string.ok))).perform(click()); // confirm exit
    }

    /**
     * Select the tab number 'x'
     * @param num Index of the tab to select
     */
    private void whenTabSelected(int num){
        onView(withId(R.id.tabSpinner)).perform(click());
        onData(is(instanceOf(Tab.class))).atPosition(num).perform(click());
    }

    /**
     * Answers the question at position 'x'.
     * @param position Index of the question to answer
     * @param answer True (Yes), False (No)
     */
    private void whenDropDownAnswered(int position,boolean answer){
        onData(is(instanceOf(Question.class))).
                inAdapterView(withId(R.id.listView)).
                atPosition(position).
                onChildView(withId(R.id.answer))
                .perform(click());
        int indexAnswer=answer?1:2;
        onData(is(instanceOf(Option.class))).atPosition(indexAnswer).perform(click());
    }

    private Activity getActivityInstance(){
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
}