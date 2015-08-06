package org.eyeseetea.malariacare.database.utils;

import android.content.res.AssetManager;

import com.opencsv.CSVReader;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;

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
    static Map<Integer, Program> programs = new LinkedHashMap<Integer, Program>();
    static Map<Integer, Tab> tabs = new LinkedHashMap<Integer, Tab>();
    static Map<Integer, Header> headers = new LinkedHashMap<Integer, Header>();
    static Map<Integer, Question> questions = new LinkedHashMap<Integer, Question>();
    static Map<Integer, Option> options = new LinkedHashMap<Integer, Option>();
    static Map<Integer, Answer> answers = new LinkedHashMap<Integer, Answer>();
    static Map<Integer, CompositeScore> compositeScores = new LinkedHashMap<Integer, CompositeScore>();
    static Map<Integer, QuestionRelation> relations = new LinkedHashMap<Integer, QuestionRelation>();

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
                        programs.put(Integer.valueOf(line[0]), program);
                        break;
                    case TABS_CSV:
                        Tab tab = new Tab();
                        tab.setName(line[1]);
                        tab.setOrder_pos(Integer.valueOf(line[2]));
                        tab.setProgram(programs.get(Integer.valueOf(line[3])));
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
                        QuestionRelation relation = new QuestionRelation();
                        relation.setMaster(questions.get(Integer.valueOf(line[1])));
                        relation.setRelative(questions.get(Integer.valueOf(line[2])));
                        relation.setOperation(Integer.valueOf(line[3]));
                        relations.put(Integer.valueOf(line[0]), relation);
                        break;
                }
            }
            reader.close();
        }

        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(programs.values())));
        //Program.saveInTx(programs.values());
        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(tabs.values())));
        //Tab.saveInTx(tabs.values());
        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(headers.values())));
        //Header.saveInTx(headers.values());
        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(answers.values())));
        //Answer.saveInTx(answers.values());
        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(options.values())));
        //Option.saveInTx(options.values());
        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(compositeScores.values())));
        //CompositeScore.saveInTx(compositeScores.values());
        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(questions.values())));
        //Question.saveInTx(questions.values());
        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(relations.values())));
        //QuestionRelation.saveInTx(relations.values());
    }

    public static void populateDummyData(){
        /*for (int i=0; i<10; i++) {
            OrgUnit orgUnit = new OrgUnit("123" + i, "Health Facility " + i);
            orgUnit.save();
        }*/

        OrgUnit orgUnit = new OrgUnit("gN7EhLCgKAS", "Outlet 1-1");
        orgUnit.save();

        orgUnit = new OrgUnit("avIJ8BAiEzA", "Outlet 1-2");
        orgUnit.save();

        orgUnit = new OrgUnit("lP4wCykG8Tm", "Outlet 1-3");
        orgUnit.save();

        orgUnit = new OrgUnit("DMBrIWRzFPo", "Outlet 1-4");
        orgUnit.save();

        orgUnit = new OrgUnit("HEpmESXlcLn", "Outlet 1-5");
        orgUnit.save();

        orgUnit = new OrgUnit("XD0wteyXBbf", "Outlet 2-1");
        orgUnit.save();

        orgUnit = new OrgUnit("JsWspiAFl9X", "Outlet 2-2");
        orgUnit.save();

        orgUnit = new OrgUnit("wqbYPZZd7Fp", "Outlet 2-3");
        orgUnit.save();

        orgUnit = new OrgUnit("EjDVgc1MI4O", "Outlet 2-4");
        orgUnit.save();

        orgUnit = new OrgUnit("g91OgEIKIm", "Outlet 2-5");
        orgUnit.save();



    }

}
