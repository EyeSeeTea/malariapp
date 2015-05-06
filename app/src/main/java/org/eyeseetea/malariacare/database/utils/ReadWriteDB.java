/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.database.utils;

import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.List;

/**
 * Created by Jose on 26/04/2015.
 */
public class ReadWriteDB {

    public static String readValueQuestion(Question question){
        String result = null;

        Value value = question.getValueBySession();

        if (value != null)
            result = value.getValue();

        return result;
    }

    public static int readPositionOption (Question question) {
        int result = 0;

        Value value = question.getValueBySession();
        if (value!=null) {

            List<Option> optionList = question.getAnswer().getOptions();
            optionList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));
            result = optionList.indexOf(value.getOption());
        }

        return result;
    }

    public static void saveValuesDDL(Question question, Option option) {

        Value value = question.getValueBySession();

        if (!option.getName().equals(Constants.DEFAULT_SELECT_OPTION)) {
            if (value == null) {
                value = new Value(option, question, Session.getSurvey());
            } else {
                value.setOption(option);
                value.setValue(option.getName());
            }
            value.save();
        }
        else {
            if (value != null) value.delete();
        }
    }

    public static void saveValuesText(Question question, String answer) {

        Value value = question.getValueBySession();

        // If the value is not found we create one
        if (value == null) {
            value = new Value(answer, question, Session.getSurvey());
        } else {
            value.setOption(null);
            value.setValue(answer);
        }
        value.save();
    }

    public static void resetValue(Question question) {

        Value value = question.getValueBySession();

        if (value != null)
            value.delete();
    }



}
