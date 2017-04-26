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

import android.content.res.AssetManager;
import android.util.Log;

import com.opencsv.CSVReader;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.model.Answer;
import org.eyeseetea.malariacare.data.database.model.CompositeScore;
import org.eyeseetea.malariacare.data.database.model.Header;
import org.eyeseetea.malariacare.data.database.model.Option;
import org.eyeseetea.malariacare.data.database.model.Program;
import org.eyeseetea.malariacare.data.database.model.Question;
import org.eyeseetea.malariacare.data.database.model.Tab;
import org.eyeseetea.malariacare.data.database.utils.PopulateDB;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by idelcano on 20/04/2016.
 */
public class PopulateDbTestUtils {


    public static final String TAG = ".PopulateDB";

    public static final String PROGRAMS_CSV = "ProgramsTest.csv";
    public static final String TABS_CSV = "TabsTest.csv";
    public static final String HEADERS_CSV = "HeadersTest.csv";
    public static final String ANSWERS_CSV = "AnswersTest.csv";
    public static final String OPTIONS_CSV = "OptionsTest.csv";
    public static final String COMPOSITE_SCORES_CSV = "CompositeScoresTest.csv";
    public static final String QUESTIONS_CSV = "QuestionsTest.csv";
    public static final String COMPOSITE_SCORES2_CSV = "CompositeScores2Test.csv";
    public static final String QUESTIONS2_CSV = "Questions2Test.csv";

    public static Map<Integer, Program> programs;
    public static Map<Integer, Tab> tabs;
    public static Map<Integer, Header> headers;
    public static Map<Integer, Question> questions;
    public static Map<Integer, Option> options;
    public static Map<Integer, Answer> answers;
    public static Map<Integer, CompositeScore> compositeScores;


    protected static void saveItem(Map items, BaseModel model, Integer pk) {
        items.put(pk, model);
        model.save();
    }


    protected static void initMaps() {
        programs = new LinkedHashMap<>();
        tabs = new LinkedHashMap<>();
        headers = new LinkedHashMap<>();
        questions = new LinkedHashMap<>();
        options = new LinkedHashMap<>();
        answers = new LinkedHashMap<>();
        compositeScores = new LinkedHashMap<>();
    }

    public static void populateDBListTestFolder(AssetManager assetManager) throws IOException {

        Log.d(TAG, "Populating metaData from local csv files");

        //Clear maps from previous populations (this might be called after logout)
        initMaps();
        List<String> tables2populate = Arrays.asList(PROGRAMS_CSV, TABS_CSV, HEADERS_CSV,
                ANSWERS_CSV, OPTIONS_CSV, COMPOSITE_SCORES_CSV, QUESTIONS_CSV);
        populateTestDb(assetManager, tables2populate);

    }

    public static void populateOtherCSV(AssetManager assetManager) throws Exception {

        Log.d(TAG, "Populating metaData from local csv files");

        //Clear maps from previous populations (this might be called after logout)
        initMaps();
        List<String> tables2populate = Arrays.asList(PROGRAMS_CSV, TABS_CSV, HEADERS_CSV,
                ANSWERS_CSV, OPTIONS_CSV, COMPOSITE_SCORES2_CSV, QUESTIONS2_CSV);
        populateTestDb(assetManager, tables2populate);


    }

    private static void populateTestDb(AssetManager assetManager, List<String> tables2populate)
            throws IOException {
        //Clear database
        PopulateDB.wipeDatabase();
        CSVReader reader;
        for (String table : tables2populate) {
            reader = new CSVReader(new InputStreamReader(assetManager.open(table)), ';', '\'');
            String[] line;
            while ((line = reader.readNext()) != null) {
                switch (table) {
                    case PROGRAMS_CSV:
                        Program program = new Program();
                        program.setUid(line[1]);
                        program.setName(line[2]);
                        saveItem(programs, program, Integer.valueOf(line[0]));
                        break;
                    case TABS_CSV:
                        Tab tab = new Tab();
                        tab.setName(line[1]);
                        tab.setOrder_pos(Integer.valueOf(line[2]));
                        tab.setProgram(programs.get(Integer.valueOf(line[3])));
                        tab.setType(Integer.valueOf(line[4]));
                        saveItem(tabs, tab, Integer.valueOf(line[0]));
                        break;
                    case HEADERS_CSV:
                        Header header = new Header();
                        header.setShort_name(line[1]);
                        header.setName(line[2]);
                        header.setOrder_pos(Integer.valueOf(line[3]));
                        header.setTab(tabs.get(Integer.valueOf(line[4])));
                        saveItem(headers, header, Integer.valueOf(line[0]));
                        break;
                    case ANSWERS_CSV:
                        Answer answer = new Answer();
                        answer.setName(line[1]);
                        saveItem(answers, answer, Integer.valueOf(line[0]));
                        break;
                    case OPTIONS_CSV:
                        Option option = new Option();
                        option.setCode(line[1]);
                        option.setName(line[2]);
                        option.setFactor(Float.valueOf(line[3]));
                        option.setAnswer(answers.get(Integer.valueOf(line[4])));
                        saveItem(options, option, Integer.valueOf(line[0]));
                        break;
                    case COMPOSITE_SCORES_CSV:
                    case COMPOSITE_SCORES2_CSV:
                        CompositeScore compositeScore = new CompositeScore();
                        compositeScore.setHierarchical_code(line[1]);
                        compositeScore.setLabel(line[2]);
                        if (line.length > 3 && !line[3].equals("")) {
                            compositeScore.setCompositeScore(
                                    compositeScores.get(Integer.valueOf(line[3])));
                        }
                        compositeScore.setUid(line[4]);
                        if (line.length > 4 && !line[5].equals("")) {
                            compositeScore.setOrder_pos(Integer.valueOf(line[5]));
                        }
                        saveItem(compositeScores, compositeScore, Integer.valueOf(line[0]));
                        break;
                    case QUESTIONS_CSV:
                    case QUESTIONS2_CSV:
                        Question question = new Question();
                        question.setCode(line[1]);
                        question.setDe_name(line[2]);
                        question.setShort_name(line[3]);
                        question.setForm_name(line[4]);
                        if (!line[5].equals("")) {
                            question.setFeedback(line[5]);
                        }
                        question.setUid(line[6]);
                        question.setOrder_pos(Integer.valueOf(line[7]));
                        question.setNumerator_w(Float.valueOf(line[8]));
                        question.setDenominator_w(Float.valueOf(line[9]));
                        question.setHeader(headers.get(Integer.valueOf(line[10])));
                        Log.d(TAG, "header" + Integer.valueOf(line[10]));
                        Log.d(TAG, headers.get(Integer.valueOf(line[10])).toString());
                        if (!line[11].equals("")) {
                            question.setAnswer(answers.get(Integer.valueOf(line[11])));
                        }
                        //set the output suvreillance
                        question.setCompulsory(false);
                        if (!line[13].equals("")) {
                            question.setOutput(Integer.valueOf(line[13]));
                        }
                        if (line.length >= 14 && !line[14].equals("")) {
                            question.setCompositeScore(
                                    compositeScores.get(Integer.valueOf(line[14])));
                        }
                        saveItem(questions, question, Integer.valueOf(line[0]));
                        Log.d(TAG, question.toString());
                        break;
                }
            }
            reader.close();
        }
    }
}