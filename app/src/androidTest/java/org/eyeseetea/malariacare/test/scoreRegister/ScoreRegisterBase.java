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

package org.eyeseetea.malariacare.test.scoreRegister;

import static junit.framework.Assert.assertEquals;

import static org.hamcrest.MatcherAssert.assertThat;

import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.junit.After;
import org.junit.Rule;

import java.util.HashMap;
import java.util.List;

public class ScoreRegisterBase {
    protected static String TAG = "ScoreRegisterTest";
    public static Option yes;
    public static Option no;
    public static final String TEST_MODULE = "TEST_MODULE";
    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule;

    public ScoreRegisterBase() {
        mActivityRule = new ActivityTestRule<>(
                LoginActivity.class);
    }

    protected Survey initSurvey() {
        Survey survey = new Survey();
        survey.setProgram(Program.getAllPrograms().get(0));
        survey.save();//gen the survey id
        return survey;
    }

    protected static void initOptions() {
        Answer answer = new Select().from(Answer.class).querySingle();
        yes = new Option("Yes", "Yes", 1f, answer);
        yes.save();
        no = new Option("No", "No", 0f, answer);
        no.save();
    }

    @After
    public void finish() {
        wipeDB();
    }

    protected static void wipeDB() {
        Delete.tables(Question.class, CompositeScore.class, Option.class, Answer.class,
                Header.class, Tab.class, Program.class, OrgUnit.class, User.class, Value.class,
                Survey.class);
    }


    protected void then(Survey survey, List<CompositeScore> compositeScores, HashMap<String, Float> expected) {
        Log.d(TAG, "Num CompositeScore " + compositeScores.size());


        for (int i = 0; i < compositeScores.size(); i++) {
            Float result = ScoreRegister.getCompositeScore(compositeScores.get(i),
                    survey.getId_survey(), TEST_MODULE);
            System.out.println("CompositeScore " + compositeScores.get(i).getId_composite_score()
                    + " Hierarchicalcode" + compositeScores.get(i).getHierarchical_code()
                    + " result:" + result + "Expected: " + expected.get(
                    compositeScores.get(i).getHierarchical_code()));

            Log.d(TAG, "CompositeScore " + compositeScores.get(i).getId_composite_score()
                    + " Hierarchicalcode" + compositeScores.get(i).getHierarchical_code()
                    + " result:" + result + "Expected: " + expected.get(
                    compositeScores.get(i).getHierarchical_code()));

            Float compositeScoreExpected = expected.get(
                    compositeScores.get(i).getHierarchical_code());
            if (!result.equals(compositeScoreExpected)) {
                Log.d(TAG, "Failed CompositeScore " + compositeScores.get(i).getId_composite_score()
                        + " Hierarchicalcode" + compositeScores.get(i).getHierarchical_code()
                        + " result:" + result + "Expected: " + expected.get(
                        compositeScores.get(i).getHierarchical_code()));

            }
            assertThat("Assert Composite score " + compositeScores.get(i).getHierarchical_code(),
                    result.equals(compositeScoreExpected));
        }
    }

    protected Survey setValue(Survey survey, Option option, Question question) {
        Value value = new Value();
        value.setId_value(question.getId_question());
        value.setValue(option.getCode());
        value.setSurvey(survey);
        value.setOption(option);
        value.setQuestion(question);
        value.save();
        survey.addValue(value);
        return survey;
    }

    protected List<CompositeScore> loadScores(Survey survey, String testModule) {
        //Calculates scores and update survey
        Log.d(TAG, "Registering scores...");
        ScoreRegister.clear(survey.getId_survey(), testModule);
        Session.setSurveyByModule(survey, TEST_MODULE);
        List<CompositeScore> compositeScores = ScoreRegister.loadCompositeScores(survey, testModule);
        return compositeScores;
    }


}