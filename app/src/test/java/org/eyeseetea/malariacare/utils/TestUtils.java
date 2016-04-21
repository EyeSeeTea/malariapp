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

package org.eyeseetea.malariacare.utils;

import com.opencsv.CSVReader;

import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Match;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.OrgUnitLevel;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.QuestionOption;
import org.eyeseetea.malariacare.database.model.QuestionRelation;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.utils.Constants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by idelcano on 20/04/2016.
 */
public class TestUtils {


    public static final String TAG = ".PopulateDB";

    public static final String PROGRAMS_CSV = "Programs.csv";
    public static final String TAB_GROUPS_CSV = "TabGroups.csv";
    public static final String TABS_CSV = "Tabs.csv";
    public static final String HEADERS_CSV = "Headers.csv";
    public static final String ANSWERS_CSV = "Answers.csv";
    public static final String OPTIONS_CSV = "Options.csv";
    public static final String COMPOSITE_SCORES_CSV = "CompositeScores.csv";
    public static final String QUESTIONS_CSV = "Questions.csv";
    public static final String SURVEY_CSV = "Survey.csv";
    public static final String VALUES_CSV = "Values.csv";

    public static final String COMPOSITE_SCORES2_CSV = "CompositeScores2.csv";
    public static final String QUESTIONS2_CSV = "Questions2.csv";

    public static Map<Integer, Program> programs;
    public static Map<Integer, TabGroup> tabGroups;
    public static Map<Integer, Tab> tabs;
    public static Map<Integer, Header> headers;
    public static Map<Integer, Question> questions;
    public static Map<Integer, Option> options;
    public static Map<Integer, Answer> answers;
    public static Map<Integer, CompositeScore> compositeScores;

    public static void populateDBTest(String path) throws IOException {
        initMaps();
        List<String> tables2populate = Arrays.asList(PROGRAMS_CSV, TAB_GROUPS_CSV, TABS_CSV, HEADERS_CSV, ANSWERS_CSV, OPTIONS_CSV, COMPOSITE_SCORES_CSV, QUESTIONS_CSV);

        CSVReader reader = null;
        for (String table : tables2populate) {
            try {
                InputStream inputStream = new FileInputStream(path + table);
                reader = new CSVReader(new InputStreamReader(inputStream), ';', '\'');
                String[] line;
                while ((line = reader.readNext()) != null) {
                    switch (table) {
                        case PROGRAMS_CSV:
                            Program program = new Program();
                            program.setId_program(Long.parseLong(line[0]));
                            program.setUid(line[1]);
                            program.setName(line[2]);
                            programs.put(Integer.valueOf(line[0]),program);
                            break;
                        case TAB_GROUPS_CSV:
                            TabGroup tabGroup = new TabGroup();
                            tabGroup.setId_tab_group(Long.parseLong(line[0]));
                            tabGroup.setName(line[1]);
                            tabGroup.setProgram(programs.get(Integer.valueOf(line[2])));
                            tabGroups.put(Integer.valueOf(line[0]),tabGroup);
                            break;
                        case TABS_CSV:
                            Tab tab = new Tab();
                            tab.setId_tab(Long.parseLong(line[0]));
                            tab.setName(line[1]);
                            tab.setOrder_pos(Integer.valueOf(line[2]));
                            tab.setType(Integer.valueOf(line[4]));
                            TabGroup tabgroup=tabGroups.get(Integer.valueOf(line[3]));
                            tabgroup.addTab(tab);
                            tabGroups.put(Integer.valueOf(line[3]),tabgroup);
                            tab.setTabGroup(tabGroups.get(Integer.valueOf(line[3])));
                            tabs.put(Integer.valueOf(line[0]),tab);
                            break;
                        case HEADERS_CSV:
                            Header header = new Header();
                            header.setId_header(Long.parseLong(line[0]));
                            header.setShort_name(line[1]);
                            header.setName(line[2]);
                            header.setOrder_pos(Integer.valueOf(line[3]));
                            header.setTab(tabs.get(Integer.valueOf(line[4])));
                            headers.put(Integer.valueOf(line[0]),header);
                            break;
                        case ANSWERS_CSV:
                            Answer answer = new Answer();
                            answer.setId_answer(Long.parseLong(line[0]));
                            answer.setName(line[1]);
//                        answer.setOutput(Integer.valueOf(line[2]));
                            answers.put(Integer.valueOf(line[0]),answer);
                            break;
                        case OPTIONS_CSV:
                            Option option = new Option();
                            option.setId_option(Long.parseLong(line[0]));
                            option.setCode(line[1]);
                            option.setName(line[2]);
                            option.setFactor(Float.valueOf(line[3]));
                            option.setAnswer(answers.get(Integer.valueOf(line[4])));
                            options.put(Integer.valueOf(line[0]),option);
                            break;
                        case COMPOSITE_SCORES_CSV:
                            CompositeScore compositeScore = new CompositeScore();
                            compositeScore.setId_composite_score((Long.parseLong(line[0])));
                            compositeScore.setHierarchical_code(line[1]);
                            compositeScore.setLabel(line[2]);
                            if (!line[3].equals(""))
                                compositeScore.setCompositeScore(compositeScores.get(Integer.valueOf(line[3])));
                            compositeScore.setUid(line[4]);
                            if (!line[5].equals(""))
                                compositeScore.setOrder_pos(Integer.valueOf(line[5]));
                            compositeScores.put(Integer.valueOf(line[0]),compositeScore);
                            break;
                        case QUESTIONS_CSV:
                            Question question = new Question();
                            question.setId_question(Long.parseLong(line[0]));
                            question.setCode(line[1]);
                            question.setDe_name(line[2]);
                            question.setShort_name(line[3]);
                            question.setForm_name(line[4]);
                            if (!line[5].equals(""))
                                question.setFeedback(line[5]);
                            question.setUid(line[6]);
                            question.setOrder_pos(Integer.valueOf(line[7]));
                            question.setNumerator_w(Float.valueOf(line[8]));
                            question.setDenominator_w(Float.valueOf(line[9]));
                            question.setHeader(headers.get(Integer.valueOf(line[10])));
                            if (!line[11].equals(""))
                                question.setAnswer(answers.get(Integer.valueOf(line[11])));
                            if (!line[12].equals(""))
                                question.setQuestion(questions.get(Integer.valueOf(line[12])));
                            else
                                question.setParent(false);
                            if (line.length == 14 && !line[13].equals("")) {
                                question.setCompositeScore(compositeScores.get(Integer.valueOf(line[13])));
                            }
                            question.setOutput(Constants.RADIO_GROUP_HORIZONTAL);
                            questions.put(Integer.valueOf(line[0]),question);
                            break;
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                reader.close();
            }
        }
        for(int i=1;i<compositeScores.size();i++){
            for(int d=1;d<compositeScores.size();d++){
                if(compositeScores.get(i).getComposite_score()!=null){
                    if(compositeScores.get(i).getComposite_score().equals(compositeScores.get(d))) {
                        if(compositeScores.get(d).getComposite_score()==null || (compositeScores.get(d).getComposite_score()!=null && (compositeScores.get(d).compositeScoreChildren==null || !compositeScores.get(d).getCompositeScoreChildren().contains(compositeScores.get(i)))))
                            compositeScores.get(d).addCompositeScoreChild(compositeScores.get(i));
                    }
                }
            }
        }

    }


    protected static void initMaps(){
        programs = new LinkedHashMap<>();
        tabGroups = new LinkedHashMap<>();
        tabs = new LinkedHashMap<>();
        headers = new LinkedHashMap<>();
        questions = new LinkedHashMap<>();
        options = new LinkedHashMap<>();
        answers = new LinkedHashMap<>();
        compositeScores = new LinkedHashMap<>();
    }

    public static void populateOtherCSV(String path, List<String> tables2populate) {

        for (String table : tables2populate) {
            switch (table){
                case COMPOSITE_SCORES2_CSV:
                    compositeScores = new LinkedHashMap<>();
                    break;
                case QUESTIONS2_CSV:
                    questions = new LinkedHashMap<>();
                    break;
            }
        }
        CSVReader reader = null;
        for (String table : tables2populate) {
            try {
                InputStream inputStream = new FileInputStream(path + table);
                reader = new CSVReader(new InputStreamReader(inputStream), ';', '\'');
                String[] line;
                while ((line = reader.readNext()) != null) {
                    switch (table) {
                        case COMPOSITE_SCORES2_CSV:
                            CompositeScore compositeScore = new CompositeScore();
                            compositeScore.setId_composite_score((Long.parseLong(line[0])));
                            compositeScore.setHierarchical_code(line[1]);
                            compositeScore.setLabel(line[2]);
                            if (!line[3].equals(""))
                                compositeScore.setCompositeScore(compositeScores.get(Integer.valueOf(line[3])));
                            compositeScore.setUid(line[4]);
                            if (!line[5].equals(""))
                                compositeScore.setOrder_pos(Integer.valueOf(line[5]));
                            compositeScores.put(Integer.valueOf(line[0]), compositeScore);
                            break;
                        case QUESTIONS2_CSV:
                            Question question = new Question();
                            question.setId_question(Long.parseLong(line[0]));
                            question.setCode(line[1]);
                            question.setDe_name(line[2]);
                            question.setShort_name(line[3]);
                            question.setForm_name(line[4]);
                            if (!line[5].equals(""))
                                question.setFeedback(line[5]);
                            question.setUid(line[6]);
                            question.setOrder_pos(Integer.valueOf(line[7]));
                            question.setNumerator_w(Float.valueOf(line[8]));
                            question.setDenominator_w(Float.valueOf(line[9]));
                            question.setHeader(headers.get(Integer.valueOf(line[10])));
                            if (!line[11].equals(""))
                                question.setAnswer(answers.get(Integer.valueOf(line[11])));
                            if (!line[12].equals(""))
                                question.setQuestion(questions.get(Integer.valueOf(line[12])));
                            else
                                question.setParent(false);
                            if (line.length == 14 && !line[13].equals("")) {
                                question.setCompositeScore(compositeScores.get(Integer.valueOf(line[13])));
                            }
                            question.setOutput(Constants.RADIO_GROUP_HORIZONTAL);
                            questions.put(Integer.valueOf(line[0]), question);
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            for (int i = 1; i < compositeScores.size(); i++) {
                for (int d = 1; d < compositeScores.size(); d++) {
                    if (compositeScores.get(i).getComposite_score() != null) {
                        if (compositeScores.get(i).getComposite_score().equals(compositeScores.get(d))) {
                            if (compositeScores.get(d).getComposite_score() == null || (compositeScores.get(d).getComposite_score() != null && (compositeScores.get(d).compositeScoreChildren == null || !compositeScores.get(d).getCompositeScoreChildren().contains(compositeScores.get(i)))))
                                compositeScores.get(d).addCompositeScoreChild(compositeScores.get(i));
                        }
                    }
                }
            }
        }
    }
}
