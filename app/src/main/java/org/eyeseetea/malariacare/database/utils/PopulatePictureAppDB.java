/*
 * Copyright (c) 2015.
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

package org.eyeseetea.malariacare.database.utils;

import android.content.res.AssetManager;

import com.opencsv.CSVReader;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Match;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.OptionAttribute;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.OrgUnitLevel;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.QuestionOption;
import org.eyeseetea.malariacare.database.model.QuestionRelation;
import org.eyeseetea.malariacare.database.model.Score;
import org.eyeseetea.malariacare.database.model.Survey;
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

/**
 * Created by ignac on 06/12/2015.
 */
public class PopulatePictureAppDB {

    public static final String PROGRAMS_CSV = "Programs_pictureapp.csv";
    public static final String TAB_GROUPS_CSV = "TabGroups_pictureapp.csv";
    public static final String TABS_CSV = "Tabs_pictureapp.csv";
    public static final String HEADERS_CSV = "Headers_pictureapp.csv";
    public static final String ANSWERS_CSV = "Answers_pictureapp.csv";
    public static final String OPTION_ATTRIBUTES_CSV = "OptionAttributes_pictureapp.csv";
    public static final String OPTIONS_CSV = "Options_pictureapp.csv";
    public static final String QUESTIONS_CSV = "Questions_pictureapp.csv";
    public static final String ORG_UNIT_LEVELS_CSV = "OrgUnitLevels_pictureapp.csv";
    public static final String ORG_UNITS_CSV = "OrgUnits_pictureapp.csv";
    static Map<Integer, Program> programList = new LinkedHashMap<Integer, Program>();
    static Map<Integer, TabGroup> tabGroupList = new LinkedHashMap<Integer, TabGroup>();
    static Map<Integer, Tab> tabList = new LinkedHashMap<Integer, Tab>();
    static Map<Integer, Header> headerList = new LinkedHashMap<Integer, Header>();
    static Map<Integer, Question> questionList = new LinkedHashMap<Integer, Question>();
    static Map<Integer, OptionAttribute> optionAttributeList = new LinkedHashMap<Integer, OptionAttribute>();
    static Map<Integer, Option> optionList = new LinkedHashMap<Integer, Option>();
    static Map<Integer, Answer> answerList = new LinkedHashMap<Integer, Answer>();
    static Map<Integer, OrgUnitLevel> orgUnitLevelList = new LinkedHashMap<>();
    static Map<Integer, OrgUnit> orgUnitList = new LinkedHashMap<>();


    /**
     * Deletes all data from the app database
     */
    public static void wipeDatabase() {
        Delete.tables(
                Program.class,
                TabGroup.class,
                Tab.class,
                Header.class,
                Question.class,
                OptionAttribute.class,
                Option.class,
                Answer.class,
                OrgUnitLevel.class,
                OrgUnit.class,
                Value.class,
                Score.class,
                Survey.class,
                QuestionOption.class,
                Match.class,
                QuestionRelation.class,
                CompositeScore.class
        );
    }
    public static void populateDB(AssetManager assetManager) throws IOException {

        List<String> tables2populate = Arrays.asList(PROGRAMS_CSV, TAB_GROUPS_CSV,  TABS_CSV, HEADERS_CSV, ANSWERS_CSV, OPTION_ATTRIBUTES_CSV, OPTIONS_CSV, QUESTIONS_CSV, ORG_UNIT_LEVELS_CSV, ORG_UNITS_CSV);

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
                        saveItem(programList, program, Integer.valueOf(line[0]));
                        break;
                    case TAB_GROUPS_CSV:
                        TabGroup tabGroup = new TabGroup();
                        tabGroup.setName(line[1]);
                        tabGroup.setProgram(programList.get(Integer.valueOf(line[2])));
                        saveItem(tabGroupList,tabGroup,Integer.valueOf(line[0]));
                        break;
                    case TABS_CSV:
                        Tab tab = new Tab();
                        tab.setName(line[1]);
                        tab.setOrder_pos(Integer.valueOf(line[2]));
                        tab.setProgram(programList.get(Integer.valueOf(line[3])));
                        tab.setType(Integer.valueOf(line[4]));
                        tab.setTabGroup(tabGroupList.get(Integer.valueOf(line[5])));//new value
                        saveItem(tabList,tab,Integer.valueOf(line[0]));
                        break;
                    case HEADERS_CSV:
                        Header header = new Header();
                        header.setShort_name(line[1]);
                        header.setName(line[2]);
                        header.setOrder_pos(Integer.valueOf(line[3]));
                        header.setTab(tabList.get(Integer.valueOf(line[4])));
                        saveItem(headerList,header,Integer.valueOf(line[0]));
                        break;
                    case ANSWERS_CSV:
                        Answer answer = new Answer();
                        answer.setName(line[1]);
                        answer.setOutput(Integer.valueOf(line[2]));
                        saveItem(answerList,answer,Integer.valueOf(line[0]));
                        break;
                    case OPTION_ATTRIBUTES_CSV:
                        OptionAttribute optionAttribute = new OptionAttribute();
                        optionAttribute.setBackground_colour(line[1]);
                        saveItem(optionAttributeList,optionAttribute,Integer.valueOf(line[0]));
                        break;
                    case OPTIONS_CSV:
                        Option option = new Option();
                        option.setCode(line[1]);
                        option.setName(line[2]);
                        option.setFactor(Float.valueOf(line[3]));
                        option.setAnswer(answerList.get(Integer.valueOf(line[4])));
                        option.setPath(line[5]);
                        if (!line[6].equals(""))
                            option.setOptionAttribute(optionAttributeList.get(Integer.valueOf(line[6])));
                        option.setBackground_colour(line[7]);
                        saveItem(optionList,option,Integer.valueOf(line[0]));
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
                        if (!line[10].equals(""))
                            question.setAnswer(answerList.get(Integer.valueOf(line[10])));
                        if (!line[11].equals(""))
                            question.setQuestion(questionList.get(Integer.valueOf(line[11])));
                        saveItem(questionList,question,Integer.valueOf(line[0]));
                        break;

                    case ORG_UNIT_LEVELS_CSV:
                        OrgUnitLevel orgUnitLevel = new OrgUnitLevel();
                        orgUnitLevel.setName(line[1]);
                        orgUnitLevelList.put(Integer.valueOf(line[0]), orgUnitLevel);
                        saveItem(orgUnitLevelList,orgUnitLevel,Integer.valueOf(line[0]));
                        break;
                    case ORG_UNITS_CSV:
                        OrgUnit orgUnit = new OrgUnit();
                        orgUnit.setUid(line[1]);
                        orgUnit.setName(line[2]);
                        if (!line[3].equals(""))
                            orgUnit.setOrgUnit(orgUnitList.get(Integer.valueOf(line[3])));
                        if (!line[4].equals(""))
                            orgUnit.setOrgUnitLevel(orgUnitLevelList.get(Integer.valueOf(line[4])));
                        orgUnitList.put(Integer.valueOf(line[0]), orgUnit);
                        saveItem(orgUnitList,orgUnit,Integer.valueOf(line[0]));
                        break;
                }
            }
            reader.close();
        }

        TransactionManager.getInstance().saveOnSaveQueue(programList.values());
        TransactionManager.getInstance().saveOnSaveQueue(tabGroupList.values());
        TransactionManager.getInstance().saveOnSaveQueue(tabList.values());
        TransactionManager.getInstance().saveOnSaveQueue(headerList.values());
        TransactionManager.getInstance().saveOnSaveQueue(answerList.values());
        TransactionManager.getInstance().saveOnSaveQueue(optionAttributeList.values());
        TransactionManager.getInstance().saveOnSaveQueue(optionList.values());
        TransactionManager.getInstance().saveOnSaveQueue(questionList.values());
        TransactionManager.getInstance().saveOnSaveQueue(orgUnitLevelList.values());
        TransactionManager.getInstance().saveOnSaveQueue(orgUnitList.values());
    }

    protected static void saveItem(Map items, BaseModel model, Integer pk){
        items.put(pk,model);
        model.save();
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
