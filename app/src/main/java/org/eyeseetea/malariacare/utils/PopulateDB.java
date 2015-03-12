package org.eyeseetea.malariacare.utils;

import android.content.res.AssetManager;
import android.util.SparseArray;

import com.opencsv.CSVReader;

import org.eyeseetea.malariacare.data.Answer;
import org.eyeseetea.malariacare.data.CompositiveScore;
import org.eyeseetea.malariacare.data.Header;
import org.eyeseetea.malariacare.data.Option;
import org.eyeseetea.malariacare.data.Question;
import org.eyeseetea.malariacare.data.Tab;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by adrian on 15/02/15.
 */
public class PopulateDB {

    static SparseArray<Tab> tabList = new SparseArray<Tab>();
    static SparseArray<Header> headerList = new SparseArray<Header>();
    static SparseArray<Question> questionList = new SparseArray<Question>();
    static SparseArray<Option> optionList = new SparseArray<Option>();
    static SparseArray<Answer> answerList = new SparseArray<Answer>();
    static SparseArray<CompositiveScore> compositiveScoreList = new SparseArray<CompositiveScore>();

    static SparseArray<Header> headerCustomList = new SparseArray<Header>();
    static SparseArray<Question> questionCustomList = new SparseArray<Question>();

    public static void populateDB(AssetManager assetManager) throws IOException {


        List<String> tables2populate = Arrays.asList("Tabs.csv", "Headers.csv", "Answers.csv", "Options.csv", "CompositiveScores.csv", "Questions.csv", "HeadersCustom.csv", "QuestionsCustom.csv");

        CSVReader reader = null;
        for (String table : tables2populate) {
            reader = new CSVReader(new InputStreamReader(assetManager.open(table)), ';', '\'');

            String[] line;
            while ((line = reader.readNext()) != null) {
                switch (table) {
                    case "Tabs.csv":
                        Tab tab = new Tab();
                        tab.setName(line[1]);
                        tab.setOrder_pos(Integer.valueOf(line[2]));
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
                        if (!line[3].equals("")) compositiveScore.setCompositiveScore(compositiveScoreList.get(Integer.valueOf(line[3])));
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
                    case "HeadersCustom.csv":
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
                        break;
                }
            }
            reader.close();
        }

        Tab.saveInTx(asList(tabList));
        Header.saveInTx(asList(headerList));
        Answer.saveInTx(asList(answerList));
        Option.saveInTx(asList(optionList));
        CompositiveScore.saveInTx(asList(compositiveScoreList));
        Question.saveInTx(asList(questionList));

        Header.saveInTx(asList(headerCustomList));
        Question.saveInTx(asList(questionCustomList));

    }

    public static <C> List <C> asList(SparseArray<C> sparseArray){
        if (sparseArray == null) return null;
        List<C> arrayList = new ArrayList<C>(sparseArray.size());
        for (int i = 0; i < sparseArray.size(); i++)
            arrayList.add(sparseArray.valueAt(i));
        return arrayList;
    }

//    public static String trimText(String text){
//        return text.replaceAll("^'|'$", "");
//    }
}
