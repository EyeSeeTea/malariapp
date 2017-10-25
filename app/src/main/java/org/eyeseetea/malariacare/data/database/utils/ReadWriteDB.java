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

package org.eyeseetea.malariacare.data.database.utils;

import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.domain.subscriber.DomainEventPublisher;
import org.eyeseetea.malariacare.domain.subscriber.event.ValueChangedEvent;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Jose on 26/04/2015.
 */
public class ReadWriteDB {

    public static String readValueQuestion(QuestionDB question, String module) {
        String result = null;

        ValueDB value = question.getValueBySession(module);

        if (value != null)
            result = value.getValue();

        return result;
    }

    public static int readPositionOption(QuestionDB question, String module) {
        int result = 0;

        ValueDB value = question.getValueBySession(module);
        if (value != null) {

            List<OptionDB> optionList = new ArrayList<>(question.getAnswer().getOptions());
            optionList.add(0, new OptionDB(Constants.DEFAULT_SELECT_OPTION));
            result = optionList.indexOf(value.getOption());
        }

        return result;
    }

    public static OptionDB readOptionAnswered(QuestionDB question, String module) {
        OptionDB option = null;

        ValueDB value = question.getValueBySession(module);

        if (value != null)
            option = value.getOption();

        return option;
    }

    public static void saveValuesDDL(QuestionDB question, OptionDB option, String module) {

        ValueDB value = question.getValueBySession(module);

        if (!option.getName().equals(Constants.DEFAULT_SELECT_OPTION)) {
            if (value == null) {
                value = new ValueDB(option, question, Session.getSurveyByModule(module));
                value.save();
                DomainEventPublisher
                        .instance()
                        .publish(new ValueChangedEvent(Session.getSurveyByModule(module).getId_survey(), question.getCompulsory(),ValueChangedEvent.Action.SAVE));
            } else {
                value.setOption(option);
                value.setValue(option.getName());
                value.setUploadDate(new Date());
                value.update();
            }
        } else {
            if (value != null) {
                value.delete();
                DomainEventPublisher
                        .instance()
                        .publish(new ValueChangedEvent(Session.getSurveyByModule(module).getId_survey(), question.getCompulsory(),ValueChangedEvent.Action.DELETE));
            }
        }
    }

    public static void saveValuesText(QuestionDB question, String answer, String module) {

        ValueDB value = question.getValueBySession(module);

        // If the value is not found we create one
        if (value == null) {
            value = new ValueDB(answer, question, Session.getSurveyByModule(module));
            value.save();
            DomainEventPublisher
                    .instance()
                    .publish(new ValueChangedEvent(Session.getSurveyByModule(module).getId_survey(), question.getCompulsory(),ValueChangedEvent.Action.SAVE));
        } else {
            value.setOption((Long)null);
            value.setValue(answer);
            value.setUploadDate(new Date());
            value.update();
        }
    }

    public static void deleteValue(QuestionDB question, String module) {

        ValueDB value = question.getValueBySession(module);

        if (value != null) {
            value.delete();
            DomainEventPublisher
                    .instance()
                    .publish(new ValueChangedEvent(Session.getSurveyByModule(module).getId_survey(), question.getCompulsory(),ValueChangedEvent.Action.DELETE));
        }
    }

}
