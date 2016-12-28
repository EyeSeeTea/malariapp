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
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Select;

import junit.framework.Assert;

import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.test.utils.PopulateDbTestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


/**
 * Espresso tests for the survey that contains scores, compositeScores
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class ScoreRegisterTest extends ScoreRegisterBase {
    @BeforeClass
    public static void startBeforeClass() {
        FlowManager.init(InstrumentationRegistry.getTargetContext());
        wipeDB();
    }


    @Before
    public void populateDB() {
        try {
            PopulateDbTestUtils.populateDBListTestFolder(
                    InstrumentationRegistry.getTargetContext().getAssets());
        } catch (IOException e) {
            e.printStackTrace();
        }
        initOptions();
    }


    protected static void initOptions() {
        Answer answer = new Select().from(Answer.class).querySingle();
        yes = new Option("Yes", "Yes", 1f, answer);
        yes.save();
        no = new Option("No", "No", 0f, answer);
        no.save();
    }

    @Test
    @MediumTest
    public void isDatabasePopulated() {
        Program.list();
        Assert.assertEquals(true, Program.getAllPrograms().size() >= 1);
        List<Question> questions = Question.getQuestionsByProgram(
                Program.getAllPrograms().get(0).getId_program());
        Assert.assertEquals(true, questions.size() == 36);
    }

    @Test
    @MediumTest
    public void TestCompositeScores() {
        Program.list();
        Assert.assertEquals(true, Program.getAllPrograms().size() == 1);
        int count = 1;

        //All the questions are Yes and the Scores are 0%.
        Log.d(TAG, "Test number " + count++);
        Log.d(TAG, "testSurveyAllNo");
        testSurveyAllNo();
        //All the questions are Yes and the Scores are 100%.
        Log.d(TAG, "Test number " + count++);
        Log.d(TAG, "testSurveyAllYes");
        testSurveyAllYes();
        //The 1.1.1 questions are Yes and the 1 Score 50% CS is 25%
        Log.d(TAG, "Test number " + count++);
        Log.d(TAG, "testSurveyOneOneOneQuestion");
        testSurveyOneOneOneQuestion();
        //The 1.2.1 questions are Yes and the 1.1 Score 25% CS is 6.25%
        Log.d(TAG, "Test number " + count++);
        Log.d(TAG, "testSurveyOneTwoOneQuestion");
        testSurveyOneTwoOneQuestion();
        //The 1.2.3 questions are Yes and the 1.1 Score 50% CS is  12.5%
        Log.d(TAG, "Test number " + count++);
        Log.d(TAG, "testSurveyOneTwoThreeQuestion");
        testSurveyOneTwoThreeQuestion();
        //The 1.2.3 questions are Yes  and 1.1.1 question too. the 1 Score is 50% CS is  12.5%
        Log.d(TAG, "Test number " + count++);
        Log.d(TAG, "testSurveyOneQuestions");
        testSurveyOneQuestions();
    }

    private void testSurveyOneQuestions() {

        Survey survey = initSurvey();

        List<Question> questions = Question.getQuestionsByProgram(
                survey.getProgram().getId_program());
        //CS 1.1.1 1.1
        Log.d(TAG, yes.toString());
        Log.d(TAG, no.toString());
        //CS 1.1.1
        survey = setValue(survey, yes, questions.get(0));
        survey = setValue(survey, yes, questions.get(1));
        survey = setValue(survey, yes, questions.get(2));
        survey = setValue(survey, yes, questions.get(3));
        //CS 1.2.1
        survey = setValue(survey, no, questions.get(4));
        survey = setValue(survey, no, questions.get(5));
        survey = setValue(survey, no, questions.get(6));
        survey = setValue(survey, no, questions.get(7));

        //CS 1.2.2
        survey = setValue(survey, no, questions.get(8));
        survey = setValue(survey, no, questions.get(9));
        survey = setValue(survey, no, questions.get(10));
        survey = setValue(survey, no, questions.get(11));

        //CS 1.2.3 1.2
        survey = setValue(survey, no, questions.get(12));
        survey = setValue(survey, no, questions.get(13));
        survey = setValue(survey, no, questions.get(14));
        survey = setValue(survey, no, questions.get(15));
        //CS 2.1.1
        survey = setValue(survey, no, questions.get(16));
        survey = setValue(survey, no, questions.get(17));
        survey = setValue(survey, no, questions.get(18));
        survey = setValue(survey, no, questions.get(19));
        //CS 2.2.1
        survey = setValue(survey, no, questions.get(20));
        survey = setValue(survey, no, questions.get(21));
        survey = setValue(survey, no, questions.get(22));
        survey = setValue(survey, no, questions.get(23));
        //CS 2.3.1
        survey = setValue(survey, no, questions.get(24));
        survey = setValue(survey, no, questions.get(25));
        survey = setValue(survey, no, questions.get(26));
        survey = setValue(survey, no, questions.get(27));
        //CS 2.3.2
        survey = setValue(survey, no, questions.get(28));
        survey = setValue(survey, no, questions.get(29));
        survey = setValue(survey, no, questions.get(30));
        survey = setValue(survey, no, questions.get(31));
        survey.save();
        List<CompositeScore> compositeScores = loadScores(survey, TEST_MODULE);

        HashMap<String, Float> expected = new HashMap<>();
        expected.put("0", 25f);
        expected.put("1", 50f);//cs1
        expected.put("1.1", 100f);//cs1.1
        expected.put("1.1.1", 100.0f);//cs1.1.1
        expected.put("1.2", 0f);//CS 1.2
        expected.put("1.2.1", 0f);//CS 1.2.1
        expected.put("1.2.2", 0f);//CS 1.2.2
        expected.put("1.2.3", 0f);//CS 1.2.3
        expected.put("2", 0f);//CS 2
        expected.put("2.1", 0f);//CS 2.1
        expected.put("2.1.1", 0f);//CS 2.1.1
        expected.put("2.2", 0f);//CS 2.2
        expected.put("2.2.1", 0f);//CS 2.2.1
        expected.put("2.3", 0f);//CS 2.3
        expected.put("2.3.1", 0f);//CS 2.3.1
        expected.put("2.3.2", 0f);//CS 2.3.2
        expected.put("2.3.3", 0f);//CS 2.3.3
        then(survey, compositeScores, expected);
    }

    private void testSurveyOneTwoThreeQuestion() {

        Survey survey = initSurvey();

        List<Question> questions = Question.getQuestionsByProgram(
                survey.getProgram().getId_program());
        //CS 1.1.1 1.1
        Log.d(TAG, yes.toString());
        Log.d(TAG, no.toString());
        survey = setValue(survey, no, questions.get(0));
        survey = setValue(survey, no, questions.get(1));
        //CS 1.1.1
        survey = setValue(survey, no, questions.get(2));
        survey = setValue(survey, no, questions.get(3));
        //CS 1.2.1
        survey = setValue(survey, no, questions.get(4));
        survey = setValue(survey, no, questions.get(5));
        survey = setValue(survey, no, questions.get(6));
        survey = setValue(survey, no, questions.get(7));

        //CS 1.2.2
        survey = setValue(survey, no, questions.get(8));
        survey = setValue(survey, no, questions.get(9));
        survey = setValue(survey, no, questions.get(10));
        survey = setValue(survey, no, questions.get(11));

        //CS 1.2.3 1.2
        survey = setValue(survey, yes, questions.get(12));
        survey = setValue(survey, yes, questions.get(13));
        survey = setValue(survey, yes, questions.get(14));
        survey = setValue(survey, yes, questions.get(15));
        //CS 2.1.1
        survey = setValue(survey, no, questions.get(16));
        survey = setValue(survey, no, questions.get(17));
        survey = setValue(survey, no, questions.get(18));
        survey = setValue(survey, no, questions.get(19));
        //CS 2.2.1
        survey = setValue(survey, no, questions.get(20));
        survey = setValue(survey, no, questions.get(21));
        survey = setValue(survey, no, questions.get(22));
        survey = setValue(survey, no, questions.get(23));
        //CS 2.3.1
        survey = setValue(survey, no, questions.get(24));
        survey = setValue(survey, no, questions.get(25));
        survey = setValue(survey, no, questions.get(26));
        survey = setValue(survey, no, questions.get(27));
        //CS 2.3.2
        survey = setValue(survey, no, questions.get(28));
        survey = setValue(survey, no, questions.get(29));
        survey = setValue(survey, no, questions.get(30));
        survey = setValue(survey, no, questions.get(31));
        survey.save();
        List<CompositeScore> compositeScores = loadScores(survey, TEST_MODULE);

        HashMap<String, Float> expected = new HashMap<>();
        expected.put("0", 12.5f);
        expected.put("1", 25f);//cs1
        expected.put("1.1", 0f);//cs1.1
        expected.put("1.1.1", 0f);//cs1.1.1
        expected.put("1.2", 50.0f);//CS 1.2
        expected.put("1.2.1", 0f);//CS 1.2.1
        expected.put("1.2.2", 0f);//CS 1.2.2
        expected.put("1.2.3", 100f);//CS 1.2.3
        expected.put("2", 0f);//CS 2
        expected.put("2.1", 0f);//CS 2.1
        expected.put("2.1.1", 0f);//CS 2.1.1
        expected.put("2.2", 0f);//CS 2.2
        expected.put("2.2.1", 0f);//CS 2.2.1
        expected.put("2.3", 0f);//CS 2.3
        expected.put("2.3.1", 0f);//CS 2.3.1
        expected.put("2.3.2", 0f);//CS 2.3.2
        expected.put("2.3.3", 0f);//CS 2.3.3
        then(survey, compositeScores, expected);

    }

    private void testSurveyOneTwoOneQuestion() {


        Survey survey = initSurvey();

        List<Question> questions = Question.getQuestionsByProgram(
                survey.getProgram().getId_program());
        //CS 1.1.1 1.1
        Log.d(TAG, yes.toString());
        Log.d(TAG, no.toString());
        survey = setValue(survey, no, questions.get(0));
        survey = setValue(survey, no, questions.get(1));
        //CS 1.1.1
        survey = setValue(survey, no, questions.get(2));
        survey = setValue(survey, no, questions.get(3));
        //CS 1.2.1
        survey = setValue(survey, yes, questions.get(4));
        survey = setValue(survey, yes, questions.get(5));
        survey = setValue(survey, yes, questions.get(6));
        survey = setValue(survey, yes, questions.get(7));

        //CS 1.2.2
        survey = setValue(survey, no, questions.get(8));
        survey = setValue(survey, no, questions.get(9));
        survey = setValue(survey, no, questions.get(10));
        survey = setValue(survey, no, questions.get(11));

        //CS 1.2.3 1.2
        survey = setValue(survey, no, questions.get(12));
        survey = setValue(survey, no, questions.get(13));
        survey = setValue(survey, no, questions.get(14));
        survey = setValue(survey, no, questions.get(15));
        //CS 2.1.1
        survey = setValue(survey, no, questions.get(16));
        survey = setValue(survey, no, questions.get(17));
        survey = setValue(survey, no, questions.get(18));
        survey = setValue(survey, no, questions.get(19));
        //CS 2.2.1
        survey = setValue(survey, no, questions.get(20));
        survey = setValue(survey, no, questions.get(21));
        survey = setValue(survey, no, questions.get(22));
        survey = setValue(survey, no, questions.get(23));
        //CS 2.3.1
        survey = setValue(survey, no, questions.get(24));
        survey = setValue(survey, no, questions.get(25));
        survey = setValue(survey, no, questions.get(26));
        survey = setValue(survey, no, questions.get(27));
        //CS 2.3.2
        survey = setValue(survey, no, questions.get(28));
        survey = setValue(survey, no, questions.get(29));
        survey = setValue(survey, no, questions.get(30));
        survey = setValue(survey, no, questions.get(31));
        survey.save();
        List<CompositeScore> compositeScores = loadScores(survey, TEST_MODULE);

        HashMap<String, Float> expected = new HashMap<>();
        expected.put("0", 6.25f);
        expected.put("1", 12.5f);//cs1
        expected.put("1.1", 0f);//cs1.1
        expected.put("1.1.1", 0f);//cs1.1.1
        expected.put("1.2", 25.0f);//CS 1.2
        expected.put("1.2.1", 100f);//CS 1.2.1
        expected.put("1.2.2", 0f);//CS 1.2.2
        expected.put("1.2.3", 0f);//CS 1.2.3
        expected.put("2", 0f);//CS 2
        expected.put("2.1", 0f);//CS 2.1
        expected.put("2.1.1", 0f);//CS 2.1.1
        expected.put("2.2", 0f);//CS 2.2
        expected.put("2.2.1", 0f);//CS 2.2.1
        expected.put("2.3", 0f);//CS 2.3
        expected.put("2.3.1", 0f);//CS 2.3.1
        expected.put("2.3.2", 0f);//CS 2.3.2
        expected.put("2.3.3", 0f);//CS 2.3.3
        then(survey, compositeScores, expected);

    }

    private void testSurveyAllYes() {

        Survey survey = initSurvey();

        List<Question> questions = Question.getQuestionsByProgram(
                survey.getProgram().getId_program());
        //CS 1.1.1 1.1
        Log.d(TAG, yes.toString());
        Log.d(TAG, no.toString());
        survey = setValue(survey, yes, questions.get(0));
        survey = setValue(survey, yes, questions.get(1));
        //CS 1.1.1
        survey = setValue(survey, yes, questions.get(2));
        survey = setValue(survey, yes, questions.get(3));
        //CS 1.2.1
        survey = setValue(survey, yes, questions.get(4));
        survey = setValue(survey, yes, questions.get(5));
        survey = setValue(survey, yes, questions.get(6));
        survey = setValue(survey, yes, questions.get(7));

        //CS 1.2.2
        survey = setValue(survey, yes, questions.get(8));
        survey = setValue(survey, yes, questions.get(9));
        survey = setValue(survey, yes, questions.get(10));
        survey = setValue(survey, yes, questions.get(11));

        //CS 1.2.3 1.2
        survey = setValue(survey, yes, questions.get(12));
        survey = setValue(survey, yes, questions.get(13));
        survey = setValue(survey, yes, questions.get(14));
        survey = setValue(survey, yes, questions.get(15));
        //CS 2.1.1
        survey = setValue(survey, yes, questions.get(16));
        survey = setValue(survey, yes, questions.get(17));
        survey = setValue(survey, yes, questions.get(18));
        survey = setValue(survey, yes, questions.get(19));
        //CS 2.2.1
        survey = setValue(survey, yes, questions.get(20));
        survey = setValue(survey, yes, questions.get(21));
        survey = setValue(survey, yes, questions.get(22));
        survey = setValue(survey, yes, questions.get(23));
        //CS 2.3.1
        survey = setValue(survey, yes, questions.get(24));
        survey = setValue(survey, yes, questions.get(25));
        survey = setValue(survey, yes, questions.get(26));
        survey = setValue(survey, yes, questions.get(27));
        //CS 2.3.2
        survey = setValue(survey, yes, questions.get(28));
        survey = setValue(survey, yes, questions.get(29));
        survey = setValue(survey, yes, questions.get(30));
        survey = setValue(survey, yes, questions.get(31));
        //CS 2.3.3
        survey = setValue(survey, yes, questions.get(32));
        survey = setValue(survey, yes, questions.get(33));
        survey = setValue(survey, yes, questions.get(34));
        survey = setValue(survey, yes, questions.get(35));
        survey.save();
        List<CompositeScore> compositeScores = loadScores(survey, TEST_MODULE);

        HashMap<String, Float> expected = new HashMap<>();
        expected.put("0", 100.0f);
        expected.put("1", 100.0f);//cs1
        expected.put("1.1", 100.0f);//cs1.1
        expected.put("1.1.1", 100.0f);//cs1.1.1
        expected.put("1.2", 100.0f);//CS 1.2
        expected.put("1.2.1", 100.0f);//CS 1.2.1
        expected.put("1.2.2", 100f);//CS 1.2.2
        expected.put("1.2.3", 100f);//CS 1.2.3
        expected.put("2", 100f);//CS 2
        expected.put("2.1", 100f);//CS 2.1
        expected.put("2.1.1", 100f);//CS 2.1.1
        expected.put("2.2", 100f);//CS 2.2
        expected.put("2.2.1", 100f);//CS 2.2.1
        expected.put("2.3", 100f);//CS 2.3
        expected.put("2.3.1", 100f);//CS 2.3.1
        expected.put("2.3.2", 100f);//CS 2.3.2
        expected.put("2.3.3", 100f);//CS 2.3.3
        then(survey, compositeScores, expected);
    }

    private void testSurveyAllNo() {
        Survey survey = initSurvey();

        List<Question> questions = Question.getQuestionsByProgram(
                survey.getProgram().getId_program());
        //CS 1.1.1 50% 1.1 50% 
        Log.d(TAG, yes.toString());
        Log.d(TAG, no.toString());
        survey = setValue(survey, no, questions.get(0));
        survey = setValue(survey, no, questions.get(1));
        //CS 1.1.1
        survey = setValue(survey, no, questions.get(2));
        survey = setValue(survey, no, questions.get(3));
        //CS 1.2.1
        survey = setValue(survey, no, questions.get(4));
        survey = setValue(survey, no, questions.get(5));
        survey = setValue(survey, no, questions.get(6));
        survey = setValue(survey, no, questions.get(7));

        //CS 1.2.2
        survey = setValue(survey, no, questions.get(8));
        survey = setValue(survey, no, questions.get(9));
        survey = setValue(survey, no, questions.get(10));
        survey = setValue(survey, no, questions.get(11));

        //CS 1.2.3  1.2
        survey = setValue(survey, no, questions.get(12));
        survey = setValue(survey, no, questions.get(13));
        survey = setValue(survey, no, questions.get(14));
        survey = setValue(survey, no, questions.get(15));
        //CS 2.1.1
        survey = setValue(survey, no, questions.get(16));
        survey = setValue(survey, no, questions.get(17));
        survey = setValue(survey, no, questions.get(18));
        survey = setValue(survey, no, questions.get(19));
        //CS 2.2.1
        survey = setValue(survey, no, questions.get(20));
        survey = setValue(survey, no, questions.get(21));
        survey = setValue(survey, no, questions.get(22));
        survey = setValue(survey, no, questions.get(23));
        //CS 2.3.1
        survey = setValue(survey, no, questions.get(24));
        survey = setValue(survey, no, questions.get(25));
        survey = setValue(survey, no, questions.get(26));
        survey = setValue(survey, no, questions.get(27));
        //CS 2.3.2
        survey = setValue(survey, no, questions.get(28));
        survey = setValue(survey, no, questions.get(29));
        survey = setValue(survey, no, questions.get(30));
        survey = setValue(survey, no, questions.get(31));
        //CS 2.3.3
        survey = setValue(survey, no, questions.get(32));
        survey = setValue(survey, no, questions.get(33));
        survey = setValue(survey, no, questions.get(34));
        survey = setValue(survey, no, questions.get(35));
        survey.save();
        List<CompositeScore> compositeScores = loadScores(survey, TEST_MODULE);

        HashMap<String, Float> expected = new HashMap<>();
        expected.put("0", 0f);
        expected.put("1", 0f);//cs1
        expected.put("1.1", 0f);//cs1.1
        expected.put("1.1.1", 0f);//cs1.1.1
        expected.put("1.2", 0f);//CS 1.2
        expected.put("1.2.1", 0f);//CS 1.2.1
        expected.put("1.2.2", 0f);//CS 1.2.2
        expected.put("1.2.3", 0f);//CS 1.2.3
        expected.put("2", 0f);//CS 2
        expected.put("2.1", 0f);//CS 2.1
        expected.put("2.1.1", 0f);//CS 2.1.1
        expected.put("2.2", 0f);//CS 2.2
        expected.put("2.2.1", 0f);//CS 2.2.1
        expected.put("2.3", 0f);//CS 2.3
        expected.put("2.3.1", 0f);//CS 2.3.1
        expected.put("2.3.2", 0f);//CS 2.3.2
        expected.put("2.3.3", 0f);//CS 2.3.3
        then(survey, compositeScores, expected);
    }

    private void testSurveyOneOneOneQuestion() {

        Log.d(TAG, "testSurveyOneOneOneQuestion");
        Survey survey = initSurvey();

        List<Question> questions = Question.getQuestionsByProgram(
                survey.getProgram().getId_program());
        //CS 1.1.1 50% 1.1 50%
        Log.d(TAG, yes.toString());
        Log.d(TAG, no.toString());

        survey = setValue(survey, yes, questions.get(0));
        survey = setValue(survey, yes, questions.get(1));
        //CS 1.1.1
        survey = setValue(survey, no, questions.get(2));
        survey = setValue(survey, no, questions.get(3));
        //CS 1.2.1
        survey = setValue(survey, no, questions.get(4));
        survey = setValue(survey, no, questions.get(5));
        survey = setValue(survey, no, questions.get(6));
        survey = setValue(survey, no, questions.get(7));

        //CS 1.2.2
        survey = setValue(survey, no, questions.get(8));
        survey = setValue(survey, no, questions.get(9));
        survey = setValue(survey, no, questions.get(10));
        survey = setValue(survey, no, questions.get(11));

        //CS 1.2.3 100% 1.2 50%
        survey = setValue(survey, yes, questions.get(12));
        survey = setValue(survey, yes, questions.get(13));
        survey = setValue(survey, yes, questions.get(14));
        survey = setValue(survey, yes, questions.get(15));
        //CS 2.1.1
        survey = setValue(survey, no, questions.get(16));
        survey = setValue(survey, no, questions.get(17));
        survey = setValue(survey, no, questions.get(18));
        survey = setValue(survey, no, questions.get(19));
        //CS 2.2.1
        survey = setValue(survey, no, questions.get(20));
        survey = setValue(survey, no, questions.get(21));
        survey = setValue(survey, no, questions.get(22));
        survey = setValue(survey, no, questions.get(23));
        //CS 2.3.1
        survey = setValue(survey, no, questions.get(24));
        survey = setValue(survey, no, questions.get(25));
        survey = setValue(survey, no, questions.get(26));
        survey = setValue(survey, no, questions.get(27));
        //CS 2.3.2
        survey = setValue(survey, no, questions.get(28));
        survey = setValue(survey, no, questions.get(29));
        survey = setValue(survey, no, questions.get(30));
        survey = setValue(survey, no, questions.get(31));
        survey.save();
        List<CompositeScore> compositeScores = loadScores(survey, TEST_MODULE);

        HashMap<String, Float> expected = new HashMap<>();
        expected.put("0", 25.0f);
        expected.put("1", 50.0f);//cs1
        expected.put("1.1", 50f);//cs1.1
        expected.put("1.1.1", 50f);//cs1.1.1
        expected.put("1.2", 50f);//CS 1.2
        expected.put("1.2.1", 0f);//CS 1.2.1
        expected.put("1.2.2", 0f);//CS 1.2.2
        expected.put("1.2.3", 100f);//CS 1.2.3
        expected.put("2", 0f);//CS 2
        expected.put("2.1", 0f);//CS 2.1
        expected.put("2.1.1", 0f);//CS 2.1.1
        expected.put("2.2", 0f);//CS 2.2
        expected.put("2.2.1", 0f);//CS 2.2.1
        expected.put("2.3", 0f);//CS 2.3
        expected.put("2.3.1", 0f);//CS 2.3.1
        expected.put("2.3.2", 0f);//CS 2.3.2
        expected.put("2.3.3", 0f);//CS 2.3.3
        then(survey, compositeScores, expected);
    }
}