package org.eyeseetea.malariacare.database.utils;

import android.content.res.AssetManager;
import android.util.Log;

import com.opencsv.CSVReader;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.ControlDataElement;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Match;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.OrgUnitLevel;
import org.eyeseetea.malariacare.database.model.OrgUnitProgramRelation;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.QuestionOption;
import org.eyeseetea.malariacare.database.model.QuestionRelation;
import org.eyeseetea.malariacare.database.model.Score;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.SurveySchedule;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.model.Value;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PopulateDB {

    public static final String TAG=".PopulateDB";

    public static final String PROGRAMS_CSV = "Programs.csv";
    public static final String TAB_GROUPS_CSV = "TabGroups.csv";
    public static final String TABS_CSV = "Tabs.csv";
    public static final String HEADERS_CSV = "Headers.csv";
    public static final String ANSWERS_CSV = "Answers.csv";
    public static final String OPTIONS_CSV = "Options.csv";
    public static final String COMPOSITE_SCORES_CSV = "CompositeScores.csv";
    public static final String QUESTIONS_CSV = "Questions.csv";
    public static final String QUESTION_RELATIONS_CSV = "QuestionRelations.csv";
    public static final String MATCHES_CSV = "Matches.csv";
    public static final String QUESTION_OPTIONS_CSV = "QuestionOptions.csv";
    public static final String ORG_UNIT_LEVELS_CSV = "OrgUnitLevels.csv";
    public static final String ORG_UNITS_CSV = "OrgUnits.csv";

    static Map<Integer, Program> programs;
    static Map<Integer, TabGroup> tabGroups;
    static Map<Integer, Tab> tabs;
    static Map<Integer, Header> headers;
    static Map<Integer, Question> questions;
    static Map<Integer, Option> options;
    static Map<Integer, Answer> answers;
    static Map<Integer, CompositeScore> compositeScores;
    static Map<Integer, QuestionRelation> questionRelations;
    static Map<Integer, Match> matches;
    static Map<Integer, QuestionOption> questionOptions;
    static Map<Integer, OrgUnitLevel> orgUnitLevels;
    static Map<Integer, OrgUnit> orgUnits;

    public static void populateDB(AssetManager assetManager) throws IOException {

        Log.d(TAG,"Populating metaData from local csv files");

        //Clear maps from previous populations (this might be called after logout)
        initMaps();

        //Clear database
        wipeDatabase();

        List<String> tables2populate = Arrays.asList(PROGRAMS_CSV, TAB_GROUPS_CSV, TABS_CSV, HEADERS_CSV, ANSWERS_CSV, OPTIONS_CSV, COMPOSITE_SCORES_CSV, QUESTIONS_CSV, QUESTION_RELATIONS_CSV, MATCHES_CSV, QUESTION_OPTIONS_CSV, ORG_UNIT_LEVELS_CSV, ORG_UNITS_CSV);

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
                    case TAB_GROUPS_CSV:
                        TabGroup tabGroup = new TabGroup();
                        tabGroup.setName(line[1]);
                        tabGroup.setProgram(programs.get(Integer.valueOf(line[2])));
                        saveItem(tabGroups, tabGroup, Integer.valueOf(line[0]));
                        break;
                    case TABS_CSV:
                        Tab tab = new Tab();
                        tab.setName(line[1]);
                        tab.setOrder_pos(Integer.valueOf(line[2]));
                        tab.setTabGroup(tabGroups.get(Integer.valueOf(line[3])));
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
//                        answer.setOutput(Integer.valueOf(line[2]));
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
                        CompositeScore compositeScore = new CompositeScore();
                        compositeScore.setHierarchical_code(line[1]);
                        compositeScore.setLabel(line[2]);
                        if (!line[3].equals("")) compositeScore.setCompositeScore(compositeScores.get(Integer.valueOf(line[3])));
                        compositeScore.setUid(line[4]);
                        if (!line[5].equals(""))
                            compositeScore.setOrder_pos(Integer.valueOf(line[5]));
                        saveItem(compositeScores, compositeScore, Integer.valueOf(line[0]));
                        break;
                    case QUESTIONS_CSV:
                        Question question = new Question();
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
                        if (line.length == 14 && !line[13].equals("")) question.setCompositeScore(compositeScores.get(Integer.valueOf(line[13])));
                        saveItem(questions, question, Integer.valueOf(line[0]));
                        break;
                    case QUESTION_RELATIONS_CSV:
                        QuestionRelation questionRelation = new QuestionRelation();
                        questionRelation.setOperation(Integer.valueOf(line[1]));
                        questionRelation.setQuestion(questions.get(Integer.valueOf(line[2])));
                        saveItem(questionRelations, questionRelation, Integer.valueOf(line[0]));
                        break;
                    case MATCHES_CSV:
                        Match match = new Match();
                        match.setQuestionRelation(questionRelations.get(Integer.valueOf(line[1])));
                        saveItem(matches, match, Integer.valueOf(line[0]));
                        break;
                    case QUESTION_OPTIONS_CSV:
                        QuestionOption questionOption = new QuestionOption();
                        questionOption.setOption(options.get(Integer.valueOf(line[1])));
                        questionOption.setQuestion(questions.get(Integer.valueOf(line[2])));
                        questionOption.setMatch(matches.get(Integer.valueOf(line[3])));
                        saveItem(questionOptions, questionOption, Integer.valueOf(line[0]));
                        break;
                    case ORG_UNIT_LEVELS_CSV:
                        OrgUnitLevel orgUnitLevel = new OrgUnitLevel();
                        orgUnitLevel.setName(line[1]);
                        saveItem(orgUnitLevels, orgUnitLevel, Integer.valueOf(line[0]));
                        break;
                    case ORG_UNITS_CSV:
                        OrgUnit orgUnit = new OrgUnit();
                        orgUnit.setUid(line[1]);
                        orgUnit.setName(line[2]);
                        if (!line[3].equals(""))
                            orgUnit.setOrgUnit(orgUnits.get(Integer.valueOf(line[3])));
                        if (!line[4].equals(""))
                            orgUnit.setOrgUnitLevel(orgUnitLevels.get(Integer.valueOf(line[4])));
                        saveItem(orgUnits, orgUnit, Integer.valueOf(line[0]));
                        break;
                }
            }
            reader.close();
        }

    }

    /**
     * Deletes all data from the app database
     */
    public static void wipeDatabase() {
        Delete.tables(
                Value.class,
                Score.class,
                Survey.class,
                SurveySchedule.class,
                OrgUnit.class,
                OrgUnitLevel.class,
                OrgUnitProgramRelation.class,
                User.class,
                QuestionOption.class,
                Match.class,
                QuestionRelation.class,
                Question.class,
                CompositeScore.class,
                Option.class,
                Answer.class,
                Header.class,
                Tab.class,
                TabGroup.class,
                Program.class,
                ControlDataElement.class
        );
    }

    protected static void saveItem(Map items, BaseModel model, Integer pk){
        items.put(pk,model);
        model.save();
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
        questionRelations = new LinkedHashMap<>();
        matches = new LinkedHashMap<>();
        questionOptions = new LinkedHashMap<>();
        orgUnitLevels = new LinkedHashMap<>();
        orgUnits = new LinkedHashMap<>();
    }
}
