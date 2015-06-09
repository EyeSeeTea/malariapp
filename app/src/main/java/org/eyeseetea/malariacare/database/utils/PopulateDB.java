package org.eyeseetea.malariacare.database.utils;

import android.content.res.AssetManager;

import com.opencsv.CSVReader;

import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.QuestionRelation;
import org.eyeseetea.malariacare.database.model.Tab;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PopulateDB {

    public static final String PROGRAMS_CSV = "Programs.csv";
    public static final String TABS_CSV = "Tabs.csv";
    public static final String HEADERS_CSV = "Headers.csv";
    public static final String ANSWERS_CSV = "Answers.csv";
    public static final String OPTIONS_CSV = "Options.csv";
    public static final String COMPOSITE_SCORES_CSV = "CompositeScores.csv";
    public static final String QUESTIONS_CSV = "Questions.csv";
    public static final String QUESTION_RELATIONS_CSV = "QuestionRelations.csv";
    static Map<Integer, Program> programList = new LinkedHashMap<Integer, Program>();
    static Map<Integer, Tab> tabList = new LinkedHashMap<Integer, Tab>();
    static Map<Integer, Header> headerList = new LinkedHashMap<Integer, Header>();
    static Map<Integer, Question> questionList = new LinkedHashMap<Integer, Question>();
    static Map<Integer, Option> optionList = new LinkedHashMap<Integer, Option>();
    static Map<Integer, Answer> answerList = new LinkedHashMap<Integer, Answer>();
    static Map<Integer, CompositeScore> compositeScoreMap = new LinkedHashMap<Integer, CompositeScore>();
    static Map<Integer, QuestionRelation> relationList = new LinkedHashMap<Integer, QuestionRelation>();

    public static void populateDB(AssetManager assetManager) throws IOException {

        List<String> tables2populate = Arrays.asList(PROGRAMS_CSV, TABS_CSV, HEADERS_CSV, ANSWERS_CSV, OPTIONS_CSV, COMPOSITE_SCORES_CSV, QUESTIONS_CSV, QUESTION_RELATIONS_CSV);

        CSVReader reader = null;
        for (String table : tables2populate) {
            reader = new CSVReader(new InputStreamReader(assetManager.open(table)), ';', '\'');

            String[] line;
            while ((line = reader.readNext()) != null) {
                switch (table) {
                    case PROGRAMS_CSV:
                        Program program = new Program();
                        program.setUid(line[1]);
                        program.setName(line[2]);
                        programList.put(Integer.valueOf(line[0]), program);
                        break;
                    case TABS_CSV:
                        Tab tab = new Tab();
                        tab.setName(line[1]);
                        tab.setOrder_pos(Integer.valueOf(line[2]));
                        tab.setProgram(programList.get(Integer.valueOf(line[3])));
                        tab.setType(Integer.valueOf(line[4]));
                        tabList.put(Integer.valueOf(line[0]), tab);
                        break;
                    case HEADERS_CSV:
                        Header header = new Header();
                        header.setShort_name(line[1]);
                        header.setName(line[2]);
                        header.setOrder_pos(Integer.valueOf(line[3]));
                        header.setTab(tabList.get(Integer.valueOf(line[4])));
                        headerList.put(Integer.valueOf(line[0]), header);
                        break;
                    case ANSWERS_CSV:
                        Answer answer = new Answer();
                        answer.setName(line[1]);
                        answer.setOutput(Integer.valueOf(line[2]));
                        answerList.put(Integer.valueOf(line[0]), answer);
                        break;
                    case OPTIONS_CSV:
                        Option option = new Option();
                        option.setName(line[1]);
                        option.setFactor(Float.valueOf(line[2]));
                        option.setAnswer(answerList.get(Integer.valueOf(line[3])));
                        optionList.put(Integer.valueOf(line[0]), option);
                        break;
                    case COMPOSITE_SCORES_CSV:
                        CompositeScore compositeScore = new CompositeScore();
                        compositeScore.setCode(line[1]);
                        compositeScore.setLabel(line[2]);
                        if (!line[3].equals("")) compositeScore.setCompositeScore(compositeScoreMap.get(Integer.valueOf(line[3])));
                        compositeScoreMap.put(Integer.valueOf(line[0]), compositeScore);
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
                        question.setHeader(headerList.get(Integer.valueOf(line[9])));
                        question.setAnswer(answerList.get(Integer.valueOf(line[10])));
                        if (!line[11].equals(""))
                            question.setQuestion(questionList.get(Integer.valueOf(line[11])));
                        if (line.length == 13 && !line[12].equals("")) question.setCompositeScore(compositeScoreMap.get(Integer.valueOf(line[12])));
                        questionList.put(Integer.valueOf(line[0]), question);
                        break;
                    case QUESTION_RELATIONS_CSV:
                        QuestionRelation relation = new QuestionRelation();
                        relation.setMaster(questionList.get(Integer.valueOf(line[1])));
                        relation.setRelative(questionList.get(Integer.valueOf(line[2])));
                        relation.setOperation(Integer.valueOf(line[3]));
                        relationList.put(Integer.valueOf(line[0]),relation);
                        break;
                }
            }
            reader.close();
        }

        Program.saveInTx(programList.values());
        Tab.saveInTx(tabList.values());
        Header.saveInTx(headerList.values());
        Answer.saveInTx(answerList.values());
        Option.saveInTx(optionList.values());
        CompositeScore.saveInTx(compositeScoreMap.values());
        Question.saveInTx(questionList.values());
        QuestionRelation.saveInTx(relationList.values());
    }

    public static void populateDummyData(){
        for (int i=0; i<10; i++) {
            OrgUnit orgUnit = new OrgUnit("123" + i, "Health Facility " + i);
            orgUnit.save();
        }
    }

}
