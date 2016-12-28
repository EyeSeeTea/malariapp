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
 *  along with Foobar.  If not, see <http:www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.test.scoreRegister;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

import com.raizlabs.android.dbflow.config.FlowManager;

import junit.framework.Assert;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.test.utils.PopulateDbTestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;


/**
 * Espresso tests for the survey that contains scores, compositeScores
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class ScoreRegisterAlternativeCsvTest extends ScoreRegisterBase {

    public static final String TEST_MODULE = "TEST_MODULE";
    private static String TAG = "ScoreRegisterTest";
    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule;

    public ScoreRegisterAlternativeCsvTest() {
        mActivityRule = new ActivityTestRule<>(
                LoginActivity.class);
    }

    @BeforeClass
    public static void init() {
        FlowManager.init(InstrumentationRegistry.getTargetContext());
        wipeDB();
    }

    @Before
    public void populateDB() {
        try {
            PopulateDbTestUtils.populateOtherCSV(
                    InstrumentationRegistry.getTargetContext().getAssets());
        } catch (Exception e) {
            e.printStackTrace();
        }
        initOptions();
    }

    @Test
    @MediumTest
    public void isDatabasePopulated() {
        Program.list();
        Assert.assertEquals(true, Program.getAllPrograms().size() >= 1);
        List<Question> questions = Question.getQuestionsByProgram(
                Program.getAllPrograms().get(0).getId_program());
        Assert.assertEquals(true, questions.size() == 6);
    }

    @Test
    @MediumTest
    public void TestCompositeScores() {
        Program.list();
        Assert.assertEquals(true, Program.getAllPrograms().size() == 1);
        int count = 1;
        //Test other CompositeScore tree with questions in the parents
//        testSurveySecondCSTree();

        System.out.println("Test number " + count++);
        testSurveySecondCSTree2();

        System.out.println("Test number " + count++);
        testSurveySecondCSTree3();

        System.out.println("Test number " + count++);
        testSurveySecondCSTree4();

        System.out.println("Test number " + count++);
        testSurveySecondCSTree5();
    }

    private void testSurveySecondCSTree5() {
        Survey survey = initSurvey();

        List<Question> questions = Question.getQuestionsByProgram(
                survey.getProgram().getId_program());
        //CS 1.1.1 1.1
        survey = setValue(survey, no, questions.get(0));
        survey = setValue(survey, no, questions.get(1));
        survey = setValue(survey, yes, questions.get(2));
        survey = setValue(survey, yes, questions.get(3));
        survey = setValue(survey, no, questions.get(4));
        survey = setValue(survey, no, questions.get(5));
        survey.save();
        List<CompositeScore> compositeScores = loadScores(survey, TEST_MODULE);

        HashMap<String, Float> expected = new HashMap<>();
        expected.put("0", 25.0f);
        expected.put("1", 25.0f);//cs1
        expected.put("1.1", 25.0f);//cs1.1
        expected.put("1.1.1", 100f);//cs1.1.1
        expected.put("1.1.2", 0f);//CS 1.1.2
        then(survey, compositeScores, expected);

    }

    private void testSurveySecondCSTree4() {

        Survey survey = initSurvey();

        List<Question> questions = Question.getQuestionsByProgram(
                survey.getProgram().getId_program());
        //CS 1.1.1 1.1
        Log.d(TAG, yes.toString());
        Log.d(TAG, no.toString());
        survey = setValue(survey, no, questions.get(0));
        survey = setValue(survey, no, questions.get(1));
        survey = setValue(survey, yes, questions.get(2));
        survey = setValue(survey, yes, questions.get(3));
        survey = setValue(survey, yes, questions.get(4));
        survey = setValue(survey, yes, questions.get(5));
        survey.save();
        List<CompositeScore> compositeScores = loadScores(survey, TEST_MODULE);

        HashMap<String, Float> expected = new HashMap<>();
        expected.put("0", 50f);
        expected.put("1", 50f);//cs1
        expected.put("1.1", 50f);//cs1.1
        expected.put("1.1.1", 100f);//cs1.1.1
        expected.put("1.1.2", 100f);//CS 1.1.2
        then(survey, compositeScores, expected);
    }

    private void testSurveySecondCSTree3() {
        Survey survey = initSurvey();

        List<Question> questions = Question.getQuestionsByProgram(
                survey.getProgram().getId_program());
        //CS 1.1.1 1.1
        Log.d(TAG, yes.toString());
        Log.d(TAG, no.toString());
        survey = setValue(survey, yes, questions.get(0));
        survey = setValue(survey, yes, questions.get(1));
        survey = setValue(survey, no, questions.get(2));
        survey = setValue(survey, no, questions.get(3));
        survey = setValue(survey, no, questions.get(4));
        survey = setValue(survey, no, questions.get(5));
        survey.save();
        List<CompositeScore> compositeScores = loadScores(survey, TEST_MODULE);

        HashMap<String, Float> expected = new HashMap<>();
        expected.put("0", 50f);
        expected.put("1", 50f);//cs1
        expected.put("1.1", 50f);//cs1.1
        expected.put("1.1.1", 0f);//cs1.1.1
        expected.put("1.1.2", 0f);//CS 1.1.2
        then(survey, compositeScores, expected);

    }

    private void testSurveySecondCSTree2() {
        Survey survey = initSurvey();

        List<Question> questions = Question.getQuestionsByProgram(
                survey.getProgram().getId_program());
        //CS 1.1.1 1.1
        Log.d(TAG, yes.toString());
        Log.d(TAG, no.toString());
        survey = setValue(survey, yes, questions.get(0));
        survey = setValue(survey, no, questions.get(1));
        survey = setValue(survey, yes, questions.get(2));
        survey = setValue(survey, no, questions.get(3));
        survey = setValue(survey, no, questions.get(4));
        survey = setValue(survey, yes, questions.get(5));
        survey.save();
        List<CompositeScore> compositeScores = loadScores(survey, TEST_MODULE);

        HashMap<String, Float> expected = new HashMap<>();
        expected.put("0", 50f);
        expected.put("1", 50f);//cs1
        expected.put("1.1", 50f);//cs1.1
        expected.put("1.1.1", 50f);//cs1.1.1
        expected.put("1.1.2", 50f);//CS 1.1.2
        then(survey, compositeScores, expected);

    }

    private void testSurveySecondCSTree() {

        Survey survey = initSurvey();

        List<Question> questions = Question.getQuestionsByProgram(
                survey.getProgram().getId_program());
        //CS 1.1.1 1.1
        survey = setValue(survey, yes, questions.get(0));
        survey = setValue(survey, yes, questions.get(1));
        survey = setValue(survey, yes, questions.get(2));
        survey = setValue(survey, yes, questions.get(3));
        survey = setValue(survey, yes, questions.get(4));
        survey = setValue(survey, yes, questions.get(5));
        survey.save();
        List<CompositeScore> compositeScores = loadScores(survey, TEST_MODULE);

        HashMap<String, Float> expected = new HashMap<>();
        expected.put("0", 100f);
        expected.put("1", 100f);//cs1
        expected.put("1.1", 100f);//cs1.1
        expected.put("1.1.1", 100f);//cs1.1.1
        expected.put("1.1.2", 100f);//CS 1.1.2
        then(survey, compositeScores, expected);
    }
}