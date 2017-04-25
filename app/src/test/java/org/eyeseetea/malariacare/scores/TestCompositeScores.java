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

package org.eyeseetea.malariacare.scores;

import android.support.annotation.NonNull;
import android.test.mock.MockContext;

import com.google.api.client.googleapis.testing.TestUtils;

import org.eyeseetea.malariacare.data.database.model.CompositeScore;
import org.eyeseetea.malariacare.data.database.model.Option;
import org.eyeseetea.malariacare.data.database.model.Question;
import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.data.database.model.Value;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by idelcano on 19/04/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCompositeScores {

    public static final String TEST_MODULE="TEST_MODULE";
    public static Survey survey;
    @Before
    public void createMockDB() {
        try {
            String filePath = new File("").getAbsolutePath();
            TestUtils.populateDBTest(filePath + "\\src\\test\\java\\org\\eyeseetea\\malariacare\\scores\\assets\\");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCompositeScoresCalc() {
        int count = 1;

        //The 1.1.1 questions are Yes and the 1 Score 50% CS is 25%
        System.out.println("Test number " + count++);
        testSurveyOneOneOneQuestion();
        //All the questions are Yes and the Scores are 100%.
        System.out.println("Test number " + count++);
        testSurveyAllYes();
        //All the questions are Yes and the Scores are 0%.
        System.out.println("Test number " + count++);
        testSurveyAllNo();
        //The 1.2.1 questions are Yes and the 1.1 Score 25% CS is 6.25%
        System.out.println("Test number " + count++);
        testSurveyOneTwoOneQuestion();
        //The 1.2.3 questions are Yes and the 1.1 Score 50% CS is  12.5%
        System.out.println("Test number " + count++);
        testSurveyOneTwoThreeQuestion();
        //The 1.2.3 questions are Yes  and 1.1.1 question too. the 1 Score is 50% CS is  12.5%
        System.out.println("Test number " + count++);
        testSurveyOneQuestions();
    }

    @Test
    public void testCompositeScoreWithQuestionsInParent() {
        int count = 1;
        //Test other CompositeScore tree with questions in the parents
        generateSecondCompositeScores();
        testSurveySecondCSTree();

        System.out.println("Test number " + count++);
        testSurveySecondCSTree2();

        System.out.println("Test number " + count++);
        testSurveySecondCSTree3();

        System.out.println("Test number " + count++);
        testSurveySecondCSTree4();

        System.out.println("Test number " + count++);
        testSurveySecondCSTree5();
    }

    private void generateSecondCompositeScores() {
        String filePath = new File("").getAbsolutePath();
        List<String> tables2populate = Arrays.asList(TestUtils.COMPOSITE_SCORES2_CSV, TestUtils.QUESTIONS2_CSV);
        TestUtils.populateOtherCSV(filePath + "\\src\\test\\java\\org\\eyeseetea\\malariacare\\scores\\assets\\", tables2populate);
    }

    public void readFirst() {
        for (int i = 1; i <= TestUtils.compositeScores.size(); i++) {
            Float result = ScoreRegister.getCompositeScore(TestUtils.compositeScores.get(i), survey.getId_survey(), TEST_MODULE);
            System.out.println("CompositeScore " + TestUtils.compositeScores.get(i).getId_composite_score() + " Hierarchicalcode" + TestUtils.compositeScores.get(i).getHierarchical_code() + " result:" + result + "CS0");
        }
    }

    private void then(List<Float> expected) {
        int count=0;
        for (int i = 1; i <= TestUtils.compositeScores.size(); i++) {
            Float result = ScoreRegister.getCompositeScore(TestUtils.compositeScores.get(i), survey.getId_survey(), TEST_MODULE);
            System.out.println("CompositeScore " + TestUtils.compositeScores.get(i).getId_composite_score() + " Hierarchicalcode" + TestUtils.compositeScores.get(i).getHierarchical_code() + " result:" + result + "Expected: "+expected.get(count));
            assertThat("Assert Composite score " + TestUtils.compositeScores.get(i).getHierarchical_code(), result.equals(expected.get(count++)));
        }
    }

    private void loadScores(Survey survey, List<Question> questions, String module) {
        ScoreRegister.clear(survey, module);
        Session.setSurveyByModule(survey, module);
        List<CompositeScore> compositeScoresLoaded = ScoreRegister.loadCompositeScoresFromMemory(survey, questions, module);
        System.out.println(ScoreRegister.calculateMainScore(compositeScoresLoaded, survey.getId_survey(), module));
    }

    private void testSurveyAllYes() {
        ScoreRegister.clear(survey, TEST_MODULE);
        List<Question> questions = new ArrayList<>();
        questions = resetQuestions();
        int count = 0;
        survey = new Survey();
        survey.setProgram(TestUtils.programs.get(1));
        //CS 1.1.1
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(1));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(2));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(3));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(4));
        //CS 1.2.1
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(5));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(6));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(7));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(8));
        //CS 1.2.2
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(9));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(10));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(11));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(12));
        //CS 1.2.3
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(13));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(14));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(15));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(16));
        //CS 2.1.1
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(17));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(18));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(19));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(20));
        //CS 2.2.1
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(21));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(22));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(23));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(24));
        //CS 2.3.1
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(25));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(26));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(27));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(28));
        //CS 2.3.2
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(29));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(30));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(31));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(32));
        //CS 2.3.3
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(33));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(34));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(35));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(36));

        loadScores(survey, questions, TEST_MODULE);
        List<Float> expected = new ArrayList<>();
        expected.add(100f);//cs0
        expected.add(100f);//cs1
        expected.add(100f);//cs1.1
        expected.add(100f);//cs1.1.1
        expected.add(100f);//CS 1.2
        expected.add(100f);//CS 1.2.1
        expected.add(100f);//CS 1.2.2
        expected.add(100f);//CS 1.2.3
        expected.add(100f);//CS 2
        expected.add(100f);//CS 2.1
        expected.add(100f);//CS 2.1.1
        expected.add(100f);//CS 2.2
        expected.add(100f);//CS 2.2.1
        expected.add(100f);//CS 2.3
        expected.add(100f);//CS 2.3.1
        expected.add(100f);//CS 2.3.2
        expected.add(100f);//CS 2.3.3
        then(expected);
    }
    private void testSurveyAllNo() {
        ScoreRegister.clear(survey, TEST_MODULE);
        List<Question> questions = new ArrayList<>();
        questions = resetQuestions();
        int count = 0;
        survey = new Survey();
        survey.setProgram(TestUtils.programs.get(1));
        //CS 1.1.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(1));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(2));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(3));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(4));
        //CS 1.2.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(5));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(6));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(7));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(8));
        //CS 1.2.2
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(9));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(10));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(11));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(12));
        //CS 1.2.3
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(13));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(14));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(15));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(16));
        //CS 2.1.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(17));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(18));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(19));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(20));
        //CS 2.2.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(21));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(22));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(23));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(24));
        //CS 2.3.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(25));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(26));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(27));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(28));
        //CS 2.3.2
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(29));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(30));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(31));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(32));
        //CS 2.3.3
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(33));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(34));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(35));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(36));
        loadScores(survey, questions, TEST_MODULE);
        List<Float> expected = new ArrayList<>();
        expected.add(0f);//cs0
        expected.add(0f);//cs1
        expected.add(0f);//cs1.1
        expected.add(0f);//cs1.1.1
        expected.add(0f);//CS 1.2
        expected.add(0f);//CS 1.2.1
        expected.add(0f);//CS 1.2.2
        expected.add(0f);//CS 1.2.3
        expected.add(0f);//CS 2
        expected.add(0f);//CS 2.1
        expected.add(0f);//CS 2.1.1
        expected.add(0f);//CS 2.2
        expected.add(0f);//CS 2.2.1
        expected.add(0f);//CS 2.3
        expected.add(0f);//CS 2.3.1
        expected.add(0f);//CS 2.3.2
        expected.add(0f);//CS 2.3.3
        then(expected);
    }

    private void testSurveyOneQuestions() {
        ScoreRegister.clear(survey, TEST_MODULE);
        List<Question> questions = new ArrayList<>();
        questions = resetQuestions();
        int count = 0;
        survey = new Survey();
        survey.setProgram(TestUtils.programs.get(1));
        //CS 1.1.1
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(1));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(2));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(3));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(4));
        //CS 1.2.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(5));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(6));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(7));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(8));
        //CS 1.2.2
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(9));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(10));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(11));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(12));
        //CS 1.2.3
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(13));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(14));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(15));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(16));
        //CS 2.1.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(17));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(18));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(19));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(20));
        //CS 2.2.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(21));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(22));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(23));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(24));
        //CS 2.3.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(25));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(26));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(27));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(28));
        //CS 2.3.2
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(29));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(30));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(31));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(32));
        //CS 2.3.3
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(33));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(34));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(35));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(36));
        loadScores(survey, questions, TEST_MODULE);
        List<Float> expected = new ArrayList<>();
        expected.add(25.0f);//cs0
        expected.add(50.0f);//cs1
        expected.add(100f);//cs1.1
        expected.add(100f);//cs1.1.1
        expected.add(0f);//CS 1.2
        expected.add(0f);//CS 1.2.1
        expected.add(0f);//CS 1.2.2
        expected.add(0f);//CS 1.2.3
        expected.add(0f);//CS 2
        expected.add(0f);//CS 2.1
        expected.add(0f);//CS 2.1.1
        expected.add(0f);//CS 2.2
        expected.add(0f);//CS 2.2.1
        expected.add(0f);//CS 2.3
        expected.add(0f);//CS 2.3.1
        expected.add(0f);//CS 2.3.2
        expected.add(0f);//CS 2.3.3
        then(expected);
    }

    private void testSurveySecondCSTree() {
        ScoreRegister.clear(survey, TEST_MODULE);
        List<Question> questions = new ArrayList<>();
        for (int i = 1; i <= TestUtils.questions.size(); i++) {
            if (TestUtils.questions.get(i) != null)
                questions.add(TestUtils.questions.get(i));
        }
        survey = new Survey();
        survey.setProgram(TestUtils.programs.get(1));
        //CS 1.1.1
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(1));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(2));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(3));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(4));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(5));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(6));
        loadScores(survey, questions, TEST_MODULE);
        List<Float> expected = new ArrayList<>();
        expected.add(100f);//cs0
        expected.add(100f);//cs1
        expected.add(100f);//cs1.1
        expected.add(100f);//cs1.1.1
        expected.add(100f);//cs1.1.2
        then(expected);
    }

    private void testSurveySecondCSTree2() {
        ScoreRegister.clear(survey, TEST_MODULE);
        List<Question> questions = resetQuestions();
        survey = new Survey();
        survey.setProgram(TestUtils.programs.get(1));
        //CS 1.1.1
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(1));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(3));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(6));
        //CS 1.1.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(2));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(4));
        //CS 1.2.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(5));
        loadScores(survey, questions, TEST_MODULE);
        List<Float> expected = new ArrayList<>();
        expected.add(50f);//cs0
        expected.add(50f);//cs1
        expected.add(50f);//cs1.1
        expected.add(50f);//cs1.1.1
        expected.add(50f);//cs1.1.2
        then(expected);
    }

    private void testSurveySecondCSTree4() {
        ScoreRegister.clear(survey, TEST_MODULE);
        List<Question> questions = resetQuestions();
        survey = new Survey();
        survey.setProgram(TestUtils.programs.get(1));
        //CS 1.1.2
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(3));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(4));
        //CS 1.1.2
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(5));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(6));
        //CS 1.1.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(1));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(2));
        loadScores(survey, questions, TEST_MODULE);
        readFirst();
        List<Float> expected = new ArrayList<>();
        expected.add(33.333336f);//cs0
        expected.add(33.333336f);//cs1
        expected.add(33.333336f);//cs1.1
        expected.add(100f);//cs1.1.1
        expected.add(100f);//cs1.1.2
        then(expected);
    }

    private void testSurveySecondCSTree3() {
        ScoreRegister.clear(survey, TEST_MODULE);
        List<Question> questions = resetQuestions();
        survey = new Survey();
        survey.setProgram(TestUtils.programs.get(1));
        //CS 1.1
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(1));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(2));
        //CS 1.1.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(3));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(4));
        //CS 1.2.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(5));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(6));
        loadScores(survey, questions, TEST_MODULE);
        List<Float> expected = new ArrayList<>();
        expected.add(66.66667f);//cs0
        expected.add(66.66667f);//cs1
        expected.add(66.66667f);//cs1.1
        expected.add(0f);//cs1.1.1
        expected.add(0f);//cs1.1.2
        then(expected);
    }


    private void testSurveySecondCSTree5() {
        ScoreRegister.clear(survey, TEST_MODULE);
        List<Question> questions = resetQuestions();
        survey = new Survey();
        survey.setProgram(TestUtils.programs.get(1));
        //CS 1.1
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(3));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(4));
        //CS 1.1.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(1));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(2));
        //CS 1.2.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(5));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(6));
        loadScores(survey, questions, TEST_MODULE);
        List<Float> expected = new ArrayList<>();
        expected.add(33.333336f);//cs0
        expected.add(33.333336f);//cs1
        expected.add(33.333336f);//cs1.1
        expected.add(100f);//cs1.1.1
        expected.add(0f);//cs1.1.2
        then(expected);
    }

    private void testSurveyOneOneOneQuestion() {
        ScoreRegister.clear(survey, TEST_MODULE);
        List<Question> questions = new ArrayList<>();
        questions = resetQuestions();
        int count = 0;
        survey = new Survey();
        survey.setProgram(TestUtils.programs.get(1));
        //CS 1.1.1 50% 1.1 50%
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(1));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(2));
        //CS 1.1.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(3));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(4));
        //CS 1.2.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(5));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(6));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(7));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(8));

        //CS 1.2.2
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(9));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(10));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(11));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(12));

        //CS 1.2.3 100% 1.2 50%
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(13));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(14));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(15));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(16));
        //CS 2.1.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(17));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(18));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(19));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(20));
        //CS 2.2.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(21));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(22));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(23));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(24));
        //CS 2.3.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(25));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(26));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(27));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(28));
        //CS 2.3.2
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(29));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(30));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(31));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(32));
        //CS 2.3.3
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(33));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(34));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(35));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(36));
        loadScores(survey, questions, TEST_MODULE);

        List<Float> expected = new ArrayList<>();
        expected.add(25.0f);//cs0
        expected.add(50.0f);//cs1
        expected.add(50f);//cs1.1
        expected.add(50f);//cs1.1.1
        expected.add(50f);//CS 1.2
        expected.add(0f);//CS 1.2.1
        expected.add(0f);//CS 1.2.2
        expected.add(100f);//CS 1.2.3
        expected.add(0f);//CS 2
        expected.add(0f);//CS 2.1
        expected.add(0f);//CS 2.1.1
        expected.add(0f);//CS 2.2
        expected.add(0f);//CS 2.2.1
        expected.add(0f);//CS 2.3
        expected.add(0f);//CS 2.3.1
        expected.add(0f);//CS 2.3.2
        expected.add(0f);//CS 2.3.3
        then(expected);
    }

    private void testSurveyOneTwoOneQuestion() {
        ScoreRegister.clear(survey, TEST_MODULE);
        List<Question> questions = new ArrayList<>();
        questions = resetQuestions();
        int count = 0;
        survey = new Survey();
        survey.setProgram(TestUtils.programs.get(1));
        //CS 1.2.1
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(5));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(6));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(7));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(8));
        //CS 1.1.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(1));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(2));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(3));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(4));
        //CS 1.2.2
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(9));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(10));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(11));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(12));
        //CS 1.2.3
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(13));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(14));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(15));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(16));
        //CS 2.1.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(17));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(18));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(19));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(20));
        //CS 2.2.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(21));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(22));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(23));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(24));
        //CS 2.3.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(25));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(26));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(27));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(28));
        //CS 2.3.2
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(29));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(30));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(31));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(32));
        //CS 2.3.3
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(33));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(34));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(35));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(36));
        loadScores(survey, questions, TEST_MODULE);
        List<Float> expected = new ArrayList<>();
        expected.add(6.25f);//cs0
        expected.add(12.5f);//cs1
        expected.add(0f);//cs1.1
        expected.add(0f);//cs1.1.1
        expected.add(25.0f);//CS 1.2
        expected.add(100f);//CS 1.2.1
        expected.add(0f);//CS 1.2.2
        expected.add(0f);//CS 1.2.3
        expected.add(0f);//CS 2
        expected.add(0f);//CS 2.1
        expected.add(0f);//CS 2.1.1
        expected.add(0f);//CS 2.2
        expected.add(0f);//CS 2.2.1
        expected.add(0f);//CS 2.3
        expected.add(0f);//CS 2.3.1
        expected.add(0f);//CS 2.3.2
        expected.add(0f);//CS 2.3.3
        then(expected);
    }

    private void testSurveyOneTwoThreeQuestion() {
        ScoreRegister.clear(survey, TEST_MODULE);
        List<Question> questions = new ArrayList<>();
        questions = resetQuestions();
        int count = 0;
        survey = new Survey();
        survey.setProgram(TestUtils.programs.get(1));
        //CS 1.2.1
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(13));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(14));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(15));
        survey = setValue(survey, TestUtils.options.get(1), TestUtils.questions.get(16));
        //CS 1.1.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(1));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(2));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(3));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(4));
        //CS 1.2.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(5));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(6));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(7));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(8));
        //CS 1.2.2
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(9));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(10));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(11));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(12));
        //CS 2.1.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(17));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(18));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(19));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(20));
        //CS 2.2.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(21));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(22));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(23));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(24));
        //CS 2.3.1
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(25));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(26));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(27));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(28));
        //CS 2.3.2
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(29));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(30));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(31));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(32));
        //CS 2.3.3
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(33));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(34));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(35));
        survey = setValue(survey, TestUtils.options.get(2), TestUtils.questions.get(36));
        loadScores(survey, questions, TEST_MODULE);
        List<Float> expected = new ArrayList<>();
        expected.add(12.5f);//cs0
        expected.add(25f);//cs1
        expected.add(0f);//cs1.1
        expected.add(0f);//cs1.1.1
        expected.add(50.0f);//CS 1.2
        expected.add(0f);//CS 1.2.1
        expected.add(0f);//CS 1.2.2
        expected.add(100f);//CS 1.2.3
        expected.add(0f);//CS 2
        expected.add(0f);//CS 2.1
        expected.add(0f);//CS 2.1.1
        expected.add(0f);//CS 2.2
        expected.add(0f);//CS 2.2.1
        expected.add(0f);//CS 2.3
        expected.add(0f);//CS 2.3.1
        expected.add(0f);//CS 2.3.2
        expected.add(0f);//CS 2.3.3
        then(expected);
    }

    @NonNull
    private List<Question> resetQuestions() {
        List<Question> questions;
        questions = new ArrayList<>();
        for (int i = 1; i <= TestUtils.questions.size(); i++) {
            TestUtils.questions.get(i).resetValues();
            questions.add(TestUtils.questions.get(i));
        }
        return questions;
    }

    private Survey setValue(Survey survey, Option optionMapped, Question questionMapped) {
        Option option = new Option();
        option.setAnswer(optionMapped.getAnswer());
        option.setCode(optionMapped.getCode());
        option.setFactor(optionMapped.getFactor());
        option.setName(optionMapped.getName());
        Question question = new Question();
        question.setId_question(questionMapped.getId_question());
        question.setCode(questionMapped.getCode());
        question.setAnswer(questionMapped.getAnswer());
        question.setCompositeScore(questionMapped.getCompositeScore());
        question.setNumerator_w(questionMapped.getNumerator_w());
        question.setDenominator_w(questionMapped.getDenominator_w());
        Value value = new Value();
        value.setId_value(question.getId_question());
        value.setValue(option.getCode());
        value.setSurvey(survey);
        value.setOption(option);
        value.setQuestion(question);
        survey.addValue(value);
        return survey;
    }

}