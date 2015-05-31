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

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.widget.ListView;
import android.widget.Spinner;

import org.eyeseetea.malariacare.CreateSurveyActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.SurveyActivity;
import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.survey.AutoTabAdapter;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
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
        mockSessionSurvey();
    }

    @Before
    public void setup(){
        super.setup();
    }

    @Test
    public void form_views() {
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

        //THEN
        onView(withText(android.R.string.yes)).perform(click());
        intended(anyIntent());
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
    public void change_to_compositive_score(){
        //WHEN: Select 'Compositive Score' tab
        whenTabSelected(11);

        //THEN
        onView(withText("Services, materials and reporting")).check(matches(isDisplayed()));
    }

    @Test
    public void in_c1_rdt_score_some_points(){
        //WHEN: Select 'C1-RDT' tab
        whenTabSelected(3);

        //WHEN: Some answers 'Yes'
        for(int i=6;i<=16;i++){
            whenDropDownAnswered(i,true);
        }

        //THEN
        onView(withId(R.id.score)).check(matches(withText("66")));
        onView(withId(R.id.cualitativeScore)).check(matches(withText("Fare")));
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
                onChildView(withId(R.id.answer)).
                perform(click());
        int indexAnswer=answer?1:2;
        onData(is(instanceOf(Option.class))).atPosition(indexAnswer).perform(click());
    }
}