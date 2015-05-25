package org.eyeseetea.malariacare.database.utils;

import android.content.res.AssetManager;

import com.opencsv.CSVReader;

import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositiveScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Relative;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.User;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PopulateDB {

    static Map<Integer, Program> programList = new LinkedHashMap<Integer, Program>();
    static Map<Integer, Tab> tabList = new LinkedHashMap<Integer, Tab>();
    static Map<Integer, Header> headerList = new LinkedHashMap<Integer, Header>();
    static Map<Integer, Question> questionList = new LinkedHashMap<Integer, Question>();
    static Map<Integer, Option> optionList = new LinkedHashMap<Integer, Option>();
    static Map<Integer, Answer> answerList = new LinkedHashMap<Integer, Answer>();
    static Map<Integer, CompositiveScore> compositiveScoreList = new LinkedHashMap<Integer, CompositiveScore>();
    static Map<Integer, Relative> relativeList = new LinkedHashMap<Integer, Relative>();

    //static Map<Integer, Header> headerCustomList = new LinkedHashMap<Integer, Header>();
    //static Map<Integer, Question> questionCustomList = new LinkedHashMap<Integer, Question>();

    public static void populateDB(AssetManager assetManager) throws IOException {


        List<String> tables2populate = Arrays.asList("Programs.csv", "Tabs.csv", "Headers.csv", "Answers.csv", "Options.csv", "CompositiveScores.csv", "Questions.csv", "Relatives.csv"); //"HeadersCustom.csv", "QuestionsCustom.csv");

        CSVReader reader = null;
        for (String table : tables2populate) {
            reader = new CSVReader(new InputStreamReader(assetManager.open(table)), ';', '\'');

            String[] line;
            while ((line = reader.readNext()) != null) {
                switch (table) {
                    case "Programs.csv":
                        Program program = new Program();
                        program.setUid(line[1]);
                        program.setName(line[2]);
                        programList.put(Integer.valueOf(line[0]), program);
                        break;
                    case "Tabs.csv":
                        Tab tab = new Tab();
                        tab.setName(line[1]);
                        tab.setOrder_pos(Integer.valueOf(line[2]));
                        tab.setProgram(programList.get(Integer.valueOf(line[3])));
                        tab.setType(Integer.valueOf(line[4]));
                        tabList.put(Integer.valueOf(line[0]), tab);
                        break;
                    case "Headers.csv":
                        Header header = new Header();
                        header.setShort_name(line[1]);
                        header.setName(line[2]);
                        header.setOrder_pos(Integer.valueOf(line[3]));
                        header.setTab(tabList.get(Integer.valueOf(line[4])));
                        headerList.put(Integer.valueOf(line[0]), header);
                        break;
                    case "Answers.csv":
                        Answer answer = new Answer();
                        answer.setName(line[1]);
                        answer.setOutput(Integer.valueOf(line[2]));
                        answerList.put(Integer.valueOf(line[0]), answer);
                        break;
                    case "Options.csv":
                        Option option = new Option();
                        option.setName(line[1]);
                        option.setFactor(Float.valueOf(line[2]));
                        option.setAnswer(answerList.get(Integer.valueOf(line[3])));
                        optionList.put(Integer.valueOf(line[0]), option);
                        break;
                    case "CompositiveScores.csv":
                        CompositiveScore compositiveScore = new CompositiveScore();
                        compositiveScore.setCode(line[1]);
                        compositiveScore.setLabel(line[2]);
                        if (!line[3].equals("")) compositiveScore.setCompositive_score(compositiveScoreList.get(Integer.valueOf(line[3])));
                        compositiveScoreList.put(Integer.valueOf(line[0]), compositiveScore);
                        break;
                    case "Questions.csv":
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
                        if (line.length == 13 && !line[12].equals("")) question.setCompositiveScore(compositiveScoreList.get(Integer.valueOf(line[12])));
                        questionList.put(Integer.valueOf(line[0]), question);
                        break;
                    case "Relatives.csv":
                        Relative relative = new Relative();
                        relative.setMaster(questionList.get(Integer.valueOf(line[1])));
                        relative.setRelative(questionList.get(Integer.valueOf(line[2])));
                        relativeList.put(Integer.valueOf(line[0]),relative);
                        break;

/*                    case "HeadersCustom.csv":
                        Header headerCustom = new Header();
                        headerCustom.setShort_name(line[1]);
                        headerCustom.setName(line[2]);
                        headerCustom.setOrder_pos(Integer.valueOf(line[3]));
                        headerCustom.setTab(tabList.get(Integer.valueOf(line[4])));
                        headerCustomList.put(Integer.valueOf(line[0]), headerCustom);
                        break;
                    case "QuestionsCustom.csv":
                        Question questionCustom = new Question();
                        questionCustom.setCode(line[1]);
                        questionCustom.setDe_name(line[2]);
                        questionCustom.setShort_name(line[3]);
                        questionCustom.setForm_name(line[4]);
                        questionCustom.setUid(line[5]);
                        questionCustom.setOrder_pos(Integer.valueOf(line[6]));
                        questionCustom.setNumerator_w(Float.valueOf(line[7]));
                        questionCustom.setDenominator_w(Float.valueOf(line[8]));
                        questionCustom.setHeader(headerCustomList.get(Integer.valueOf(line[9])));
                        if (!line[10].equals("")) questionCustom.setAnswer(answerList.get(Integer.valueOf(line[10])));
                        if (!line[11].equals("")) questionCustom.setQuestion(questionCustomList.get(Integer.valueOf(line[11])));
                        questionCustomList.put(Integer.valueOf(line[0]), questionCustom);
                        break;*/
                }
            }
            reader.close();
        }

        Program.saveInTx(programList.values());
        Tab.saveInTx(tabList.values());
        Header.saveInTx(headerList.values());
        Answer.saveInTx(answerList.values());
        Option.saveInTx(optionList.values());
        CompositiveScore.saveInTx(compositiveScoreList.values());
        Question.saveInTx(questionList.values());
        Relative.saveInTx(relativeList.values());

        //Header.saveInTx(headerCustomList.values());
        //Question.saveInTx(questionCustomList.values());

    }

    public static void populateDummyData(){
        for (int i=0; i<10; i++) {
            OrgUnit orgUnit = new OrgUnit("123" + i, "Health Facility " + i);
            orgUnit.save();
        }
    }

}
