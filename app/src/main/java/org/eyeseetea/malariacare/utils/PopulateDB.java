package org.eyeseetea.malariacare.utils;

import android.content.res.AssetManager;

import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;

import org.eyeseetea.malariacare.data.Answer;
import org.eyeseetea.malariacare.data.Header;
import org.eyeseetea.malariacare.data.Option;
import org.eyeseetea.malariacare.data.Question;
import org.eyeseetea.malariacare.data.Tab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by adrian on 15/02/15.
 */
public class PopulateDB {

    static List<Tab> tabList = new ArrayList<Tab>();
    static List<Header> headerList = new ArrayList<Header>();
    static List<Question> questionList = new ArrayList<Question>();
    static List<Option> optionList = new ArrayList<Option>();
    static List<Answer> answerList = new ArrayList<Answer>();

    static List<Header> headerCustomList = new ArrayList<Header>();
    static List<Question> questionCustomList = new ArrayList<Question>();

    public static void populateDB(AssetManager assetManager) throws IOException {


        List<String> tables2populate = Arrays.asList("Tabs.csv", "Headers.csv", "Answers.csv", "Options.csv", "Questions.csv", "HeadersCustom.csv", "QuestionsCustom.csv");

        CSVReader reader = null;
        for (String table : tables2populate) {
            reader = new CSVReader(new InputStreamReader(assetManager.open(table)), ';', '\'');

            String[] line;
            while ((line = reader.readNext()) != null) {
                switch (table) {
                    case "Tabs.csv":
                        Tab tab = new Tab();
                        tab.setName(line[1]);
                        tab.setOrder_tab(Integer.valueOf(line[2]));
                        tabList.add(tab);
                        break;
                    case "Headers.csv":
                        Header header = new Header();
                        header.setShort_name(line[1]);
                        header.setName(line[2]);
                        header.setOrder_header(Integer.valueOf(line[3]));
                        header.setMaster(Integer.valueOf(line[4]));
                        header.setTab(tabList.get(Integer.valueOf(line[5])-1));
                        headerList.add(header);
                        break;
                    case "Answers.csv":
                        Answer answer = new Answer();
                        answer.setName(line[1]);
                        answer.setOutput(Integer.valueOf(line[2]));
                        answerList.add(answer);
                        break;
                    case "Options.csv":
                        Option option = new Option();
                        option.setName(line[1]);
                        option.setFactor(Float.valueOf(line[2]));
                        option.setAnswer(answerList.get(Integer.valueOf(line[3]) - 1));
                        optionList.add(option);
                        break;
                    case "Questions.csv":
                        Question question = new Question();
                        question.setCode(line[1]);
                        question.setDe_name(line[2]);
                        question.setShort_name(line[3]);
                        question.setForm_name(line[4]);
                        question.setUid(line[5]);
                        question.setOrder_question(Integer.valueOf(line[6]));
                        question.setNumerator_w(Float.valueOf(line[7]));
                        question.setDenominator_w(Float.valueOf(line[8]));
                        question.setHeader(headerList.get(Integer.valueOf(line[9])-1));
                        question.setAnswer(answerList.get(Integer.valueOf(line[10])-1));
                        if (!line[11].equals("")) question.setQuestion(questionList.get(Integer.valueOf(line[11])-1));
                        questionList.add(question);
                        break;
                    case "HeadersCustom.csv":
                        Header headerCustom = new Header();
                        headerCustom.setShort_name(line[1]);
                        headerCustom.setName(line[2]);
                        headerCustom.setOrder_header(Integer.valueOf(line[3]));
                        headerCustom.setMaster(Integer.valueOf(line[4]));
                        headerCustom.setTab(tabList.get(Integer.valueOf(line[5])-1));
                        headerCustomList.add(headerCustom);
                        break;
                    case "QuestionsCustom.csv":
                        Question questionCustom = new Question();
                        questionCustom.setCode(line[1]);
                        questionCustom.setDe_name(line[2]);
                        questionCustom.setShort_name(line[3]);
                        questionCustom.setForm_name(line[4]);
                        questionCustom.setUid(line[5]);
                        questionCustom.setOrder_question(Integer.valueOf(line[6]));
                        questionCustom.setNumerator_w(Float.valueOf(line[7]));
                        questionCustom.setDenominator_w(Float.valueOf(line[8]));
                        questionCustom.setHeader(headerCustomList.get(Integer.valueOf(line[9])-1));
                        if (!line[10].equals("")) questionCustom.setAnswer(answerList.get(Integer.valueOf(line[10])-1));
                        if (!line[11].equals("")) questionCustom.setQuestion(questionCustomList.get(Integer.valueOf(line[11])-1));
                        questionCustomList.add(questionCustom);
                        break;
                }
            }
            reader.close();
        }

        Tab.saveInTx(tabList);
        Header.saveInTx(headerList);
        Answer.saveInTx(answerList);
        Option.saveInTx(optionList);
        Question.saveInTx(questionList);

        Header.saveInTx(headerCustomList);
        Header.saveInTx(questionCustomList);

    }

//    public static String trimText(String text){
//        return text.replaceAll("^'|'$", "");
//    }
}
