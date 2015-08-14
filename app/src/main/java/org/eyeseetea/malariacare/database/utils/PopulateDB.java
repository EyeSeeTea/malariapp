package org.eyeseetea.malariacare.database.utils;

import android.content.res.AssetManager;

import com.opencsv.CSVReader;
import com.raizlabs.android.dbflow.runtime.TransactionManager;

import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Match;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.QuestionOption;
import org.eyeseetea.malariacare.database.model.QuestionRelation;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.TabGroup;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PopulateDB {

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
    public static final String ORG_UNITS_CSV = "OrgUnits.csv";
    static Map<Integer, Program> programs = new LinkedHashMap<Integer, Program>();
    static Map<Integer, TabGroup> tabGroups = new LinkedHashMap<Integer, TabGroup>();
    static Map<Integer, Tab> tabs = new LinkedHashMap<Integer, Tab>();
    static Map<Integer, Header> headers = new LinkedHashMap<Integer, Header>();
    static Map<Integer, Question> questions = new LinkedHashMap<Integer, Question>();
    static Map<Integer, Option> options = new LinkedHashMap<Integer, Option>();
    static Map<Integer, Answer> answers = new LinkedHashMap<Integer, Answer>();
    static Map<Integer, CompositeScore> compositeScores = new LinkedHashMap<Integer, CompositeScore>();
    static Map<Integer, QuestionRelation> questionRelations = new LinkedHashMap<Integer, QuestionRelation>();
    static Map<Integer, Match> matches = new LinkedHashMap<Integer, Match>();
    static Map<Integer, QuestionOption> questionOptions = new LinkedHashMap<Integer, QuestionOption>();
    static Map<Integer, OrgUnit> orgUnits = new LinkedHashMap<>();

    public static void populateDB(AssetManager assetManager) throws IOException {

        List<String> tables2populate = Arrays.asList(PROGRAMS_CSV, TAB_GROUPS_CSV, TABS_CSV, HEADERS_CSV, ANSWERS_CSV, OPTIONS_CSV, COMPOSITE_SCORES_CSV, QUESTIONS_CSV, QUESTION_RELATIONS_CSV, MATCHES_CSV, QUESTION_OPTIONS_CSV, ORG_UNITS_CSV);

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
                        programs.put(Integer.valueOf(line[0]), program);
                        break;
                    case TAB_GROUPS_CSV:
                        TabGroup tabGroup = new TabGroup();
                        tabGroup.setName(line[1]);
                        tabGroup.setProgram(programs.get(Integer.valueOf(line[2])));
                        tabGroups.put(Integer.valueOf(line[0]), tabGroup);
                        break;
                    case TABS_CSV:
                        Tab tab = new Tab();
                        tab.setName(line[1]);
                        tab.setOrder_pos(Integer.valueOf(line[2]));
                        tab.setTabGroup(tabGroups.get(Integer.valueOf(line[3])));
                        tab.setType(Integer.valueOf(line[4]));
                        tabs.put(Integer.valueOf(line[0]), tab);
                        break;
                    case HEADERS_CSV:
                        Header header = new Header();
                        header.setShort_name(line[1]);
                        header.setName(line[2]);
                        header.setOrder_pos(Integer.valueOf(line[3]));
                        header.setTab(tabs.get(Integer.valueOf(line[4])));
                        headers.put(Integer.valueOf(line[0]), header);
                        break;
                    case ANSWERS_CSV:
                        Answer answer = new Answer();
                        answer.setName(line[1]);
                        answer.setOutput(Integer.valueOf(line[2]));
                        answers.put(Integer.valueOf(line[0]), answer);
                        break;
                    case OPTIONS_CSV:
                        Option option = new Option();
                        option.setName(line[1]);
                        option.setFactor(Float.valueOf(line[2]));
                        option.setAnswer(answers.get(Integer.valueOf(line[3])));
                        options.put(Integer.valueOf(line[0]), option);
                        break;
                    case COMPOSITE_SCORES_CSV:
                        CompositeScore compositeScore = new CompositeScore();
                        compositeScore.setCode(line[1]);
                        compositeScore.setLabel(line[2]);
                        if (!line[3].equals("")) compositeScore.setCompositeScore(compositeScores.get(Integer.valueOf(line[3])));
                        compositeScore.setUid(line[4]);
                        if (!line[5].equals(""))
                            compositeScore.setOrder_pos(Integer.valueOf(line[5]));
                        compositeScores.put(Integer.valueOf(line[0]), compositeScore);
                        break;
                    case QUESTIONS_CSV:
                        Question question = new Question();
                        question.setCode(line[1]);
                        question.setDe_name(line[2]);
                        question.setShort_name(line[3]);
                        question.setForm_name(line[4]);
                        question.setUid(line[5]);
                        question.setOrder_pos(Integer.valueOf(line[6]));
                        question.setNumerator_w(Float.valueOf(line[7]));
                        question.setDenominator_w(Float.valueOf(line[8]));
                        question.setHeader(headers.get(Integer.valueOf(line[9])));
                        if (!line[10].equals(""))
                            question.setAnswer(answers.get(Integer.valueOf(line[10])));
                        if (!line[11].equals(""))
                            question.setQuestion(questions.get(Integer.valueOf(line[11])));
                        if (line.length == 13 && !line[12].equals("")) question.setCompositeScore(compositeScores.get(Integer.valueOf(line[12])));
                        questions.put(Integer.valueOf(line[0]), question);
                        break;
                    case QUESTION_RELATIONS_CSV:
                        QuestionRelation questionRelation = new QuestionRelation();
                        questionRelation.setOperation(Integer.valueOf(line[1]));
                        questionRelation.setQuestion(questions.get(Integer.valueOf(line[2])));
                        questionRelations.put(Integer.valueOf(line[0]), questionRelation);
                        break;
                    case MATCHES_CSV:
                        Match match = new Match();
                        match.setQuestionRelation(questionRelations.get(Integer.valueOf(line[1])));
                        matches.put(Integer.valueOf(line[0]), match);
                        break;
                    case QUESTION_OPTIONS_CSV:
                        QuestionOption questionOption = new QuestionOption();
                        questionOption.setOption(options.get(Integer.valueOf(line[1])));
                        questionOption.setQuestion(questions.get(Integer.valueOf(line[2])));
                        questionOption.setMatch(matches.get(Integer.valueOf(line[3])));
                        questionOptions.put(Integer.valueOf(line[0]), questionOption);
                        break;
                    case ORG_UNITS_CSV:
                        OrgUnit orgUnit = new OrgUnit();
                        orgUnit.setUid(line[1]);
                        orgUnit.setName(line[2]);
                        if (!line[3].equals(""))
                            orgUnit.setOrgUnit(orgUnits.get(Integer.valueOf(line[3])));
                        orgUnits.put(Integer.valueOf(line[0]), orgUnit);
                        break;
                }
            }
            reader.close();
        }

        TransactionManager.getInstance().saveOnSaveQueue(programs.values());
        TransactionManager.getInstance().saveOnSaveQueue(tabGroups.values());
        TransactionManager.getInstance().saveOnSaveQueue(tabs.values());
        TransactionManager.getInstance().saveOnSaveQueue(headers.values());
        TransactionManager.getInstance().saveOnSaveQueue(answers.values());
        TransactionManager.getInstance().saveOnSaveQueue(options.values());
        TransactionManager.getInstance().saveOnSaveQueue(compositeScores.values());
        TransactionManager.getInstance().saveOnSaveQueue(questions.values());
        TransactionManager.getInstance().saveOnSaveQueue(questionRelations.values());
        TransactionManager.getInstance().saveOnSaveQueue(matches.values());
        TransactionManager.getInstance().saveOnSaveQueue(questionOptions.values());
        TransactionManager.getInstance().saveOnSaveQueue(orgUnits.values());

    }
}
