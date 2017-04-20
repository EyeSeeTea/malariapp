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

package org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models;

import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.VisitableFromSDK;
import org.eyeseetea.malariacare.data.database.model.AnswerDB;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.TrackedEntityDataValueFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.TrackedEntityDataValueFlow_Table;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by arrizabalaga on 5/11/15.
 */
public class DataValueExtended implements VisitableFromSDK {

    private final static String TAG = ".DataValueExtended";
    private final static String REGEXP_FACTOR = ".*\\[([0-9]*)\\]";

    TrackedEntityDataValueFlow dataValue;

    String programUid;

    public DataValueExtended() {
        dataValue = new TrackedEntityDataValueFlow();
    }

    public DataValueExtended(TrackedEntityDataValueFlow dataValue) {
        this.dataValue = dataValue;
    }

    public DataValueExtended(DataValueExtended dataValueExtended) {
        this.dataValue = dataValueExtended.getDataValue();
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }

    public TrackedEntityDataValueFlow getDataValue() {
        return dataValue;
    }

    public OptionDB findOptionByQuestion(QuestionDB question) {
        if (question == null) {
            return null;
        }

        AnswerDB answer = question.getAnswer();
        if (answer == null) {
            return null;
        }

        List<OptionDB> options = answer.getOptions();
        List<String> optionCodes = new ArrayList<>();
        for (OptionDB option : options) {
            optionCodes.add(option.getCode());
            if (option.getCode() == null) {
                continue;
            }
            String optionCleaned = extractValue(option.getCode());
            String valueCleaned = extractValue(dataValue.getValue());
            //Yes[1]==Yes || Yes==Yes || Yes==Yes[1]
//            if(option.getCode().equals(dataValue.getValue()) || optionCleaned.equals(dataValue
// .getValue()) || option.getCode().equals(valueCleaned)){
            //Option code no longer will be 'Yes[1]' but a real code for the option
            if (option.getCode().equals(dataValue.getValue())) {
                return option;
            }
        }

        Log.w(TAG, String.format("Cannot find option '%s' in %s and dataElement %s event %s",
                dataValue.getValue(), optionCodes.toString(), dataValue.getDataElement(),
                dataValue.getEvent()));
        return null;
    }

    /**
     * Turns a value with a code 'Yes[1]' into its proper aprox value 'Yes'.
     * This might not be an exact translation when it comes to translated values such as 'Oui' but
     * it better than nothing
     */
    private String extractValue(String code) {
        if (code == null || code.isEmpty()) {
            return code;
        }

        Pattern pattern = Pattern.compile(REGEXP_FACTOR);
        Matcher matcher = pattern.matcher(code);

        //No match
        if (!matcher.matches()) {
            return code;
        }

        //Found a match
        String factorStr = "[" + matcher.group(1) + "]";

        return code.replace(factorStr, "").trim();
    }

    /**
     *
     * @return
     */
    public String extractValue() {
        return extractValue(dataValue.getValue());
    }

    /**
     * The factor of an option is codified inside its code. Ex: Yes[1]
     */
    public static Float extractFactor(String code) {
        if (code == null || code.isEmpty()) {
            return 0f;
        }

        Pattern pattern = Pattern.compile(REGEXP_FACTOR);
        Matcher matcher = pattern.matcher(code);

        //No match
        if (!matcher.matches()) {
            return 0f;
        }

        //Found a match
        String factorStr = matcher.group(1);

        return Float.parseFloat(factorStr);
    }

    public static long count() {
        return new SQLite().selectCountOf()
                .from(TrackedEntityDataValueFlow.class)
                .count();
    }

    public static DataValueExtended findByEventAndUID(EventFlow event, String dataElementUID) {
        return new DataValueExtended(new Select()
                .from(TrackedEntityDataValueFlow.class)
                .where(TrackedEntityDataValueFlow_Table.event.eq(event.getUId()))
                .and(TrackedEntityDataValueFlow_Table.dataElement.eq(dataElementUID))
                .querySingle());
    }

    public String getProgramUid() {
        return programUid;
    }

    public void setProgramUid(String programUid) {
        this.programUid = programUid;
    }

    public String getEvent() {
        return dataValue.getEvent().getUId();
    }

    public String getDataElement() {
        return dataValue.getDataElement();
    }

    public String getValue() {
        return dataValue.getValue();
    }

    public void setDataElement(String uid) {
        dataValue.setDataElement(uid);
    }

    public void setEvent(EventFlow event) {
        dataValue.setEvent(event);
    }

    public void setStoredBy(String safeUsername) {
        dataValue.setStoredBy(safeUsername);
    }

    public void setValue(String round) {
        dataValue.setValue(round);
    }

    public void save() {
        System.out.println("Saving "+dataValue.getValue());
        String dataElement = dataValue.getDataElement();
        String value = dataValue.getValue();
        System.out.println(dataElement + " val " + value);
        dataValue.save();
    }

    public void update() {
        System.out.println("Updating"+dataValue.getValue());
        String dataElement = dataValue.getDataElement();
        String value = dataValue.getValue();
        System.out.println(dataElement + "val " + value);
        dataValue.update();
    }

    public static List<DataValueExtended> getExtendedList(
            List<TrackedEntityDataValueFlow> flowList) {
        List<DataValueExtended> extendedsList = new ArrayList<>();
        for (TrackedEntityDataValueFlow flowPojo : flowList) {
            extendedsList.add(new DataValueExtended(flowPojo));
        }
        return extendedsList;
    }
}
