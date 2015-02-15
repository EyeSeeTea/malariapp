package org.psi.malariacare.utils;

import android.content.res.AssetManager;

import org.psi.malariacare.data.Answer;
import org.psi.malariacare.data.Header;
import org.psi.malariacare.data.Option;
import org.psi.malariacare.data.Question;
import org.psi.malariacare.data.Tab;

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


    public static void populateDB(AssetManager assetManager){
        List<Tab> tabList = new ArrayList<Tab>();
        List<Header> headerList = new ArrayList<Header>();
        List<Question> questionList = new ArrayList<Question>();
        List<Option> optionList = new ArrayList<Option>();
        List<Answer> answerList = new ArrayList<Answer>();


        List<String> tables2populate = Arrays.asList("Tabs.csv", "Headers.csv", "Answers.csv", "Options.csv", "Questions.csv");
        InputStream is = null;
        BufferedReader reader = null;

        for (String table : tables2populate) {
            try {
                is = assetManager.open(table);
            } catch (IOException e) {
                e.printStackTrace();
            }

            reader = new BufferedReader(new InputStreamReader(is));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] RowData = line.split(";");
                    switch (table) {
                        case "Tabs.csv":
                            Tab tab = new Tab();
                            tab.setName(RowData[1]);
                            tab.setOrder_tab(Integer.valueOf(RowData[2]));
                            tabList.add(tab);
                            break;
                        case "Headers.csv":
                            Header header = new Header();
                            header.setShort_name(RowData[1]);
                            header.setName(RowData[2]);
                            header.setOrder_header(Integer.valueOf(RowData[3]));
                            header.setMaster(Integer.valueOf(RowData[4]));
                            header.setTab(tabList.get(Integer.valueOf(RowData[5])-1));
                            headerList.add(header);
                            break;
                        case "Answers.csv":
                            Answer answer = new Answer();
                            answer.setName(RowData[1]);
                            answer.setOutput(Integer.valueOf(RowData[2]));
                            answerList.add(answer);
                            break;
                        case "Options.csv":
                            Option option = new Option();
                            option.setName(RowData[1]);
                            option.setFactor(Float.valueOf(RowData[2]));
                            option.setAnswer(answerList.get(Integer.valueOf(RowData[3]) - 1));
                            optionList.add(option);
                            break;
                        case "Questions.csv":
                            Question question = new Question();
                            question.setCode(RowData[1]);
                            question.setDe_name(RowData[2]);
                            question.setShort_name(RowData[3]);
                            question.setForm_name(RowData[4]);
                            question.setUid(RowData[5]);
                            question.setOrder_question(Integer.valueOf(RowData[6]));
                            question.setNumerator_w(Float.valueOf(RowData[7]));
                            question.setDenominator_w(Float.valueOf(RowData[8]));
                            question.setHeader(headerList.get(Integer.valueOf(RowData[9])-1));
                            question.setAnswer(answerList.get(Integer.valueOf(RowData[10])-1));
                            if (RowData.length == 13){
                                question.setQuestion(questionList.get(Integer.valueOf(RowData[12])-1));
                            }
                            questionList.add(question);
                            break;
                    }
                }
            } catch (IOException ex) {
                // handle exception
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    // handle exception
                }
            }

        }

        Tab.saveInTx(tabList);
        Header.saveInTx(headerList);
        Answer.saveInTx(answerList);
        Option.saveInTx(optionList);
        Question.saveInTx(questionList);

    }
}
