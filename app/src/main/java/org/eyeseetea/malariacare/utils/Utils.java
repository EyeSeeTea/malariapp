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

package org.eyeseetea.malariacare.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    static final int numberOfDecimals = 0; // Number of decimals outputs will have

    public static String round(float base, int decimalPlace){
        BigDecimal bd = new BigDecimal(Float.toString(base));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        if (decimalPlace == 0) return Integer.toString((int) bd.floatValue());
        return Float.toString(bd.floatValue());
    }

    public static String round(float base){
        return round(base, Utils.numberOfDecimals);
    }

    public static List<BaseModel> convertTabToArrayCustom(Tab tab) {
        List<BaseModel> result = new ArrayList<BaseModel>();

        for (Header header : tab.getHeaders()) {
            result.add(header);
            for (Question question : header.getQuestions()) {
                if (tab.getType().equals(Constants.TAB_AUTOMATIC) || tab.getType().equals(Constants.TAB_AUTOMATIC_NON_SCORED) || question.hasChildren())
                    result.add(question);
            }
        }

        return result;
    }

    public static List<Object> convertPictureTabToArrayCustom(Tab tab) {
        List<Object> result = new ArrayList<Object>();

        for (Header header : tab.getHeaders()) {
            result.add(header);
            for (Question question : header.getQuestions()) {
                if (question.hasChildren())
                    result.add(question);
            }
        }

        return result;
    }
    public static List<? extends BaseModel> preloadTabItems(Tab tab){
        List<? extends BaseModel> items = Session.getTabsCache().get(tab.getId_tab());

        if (tab.isCompositeScore())
            items = CompositeScore.listByTabGroup(Session.getSurvey().getTabGroup());

        else{

            items=Session.getTabsCache().get(tab.getId_tab());

            if (items == null) {
                items = convertTabToArrayCustom(tab);
            }
            Session.getTabsCache().put(tab.getId_tab(), items);
        }
        return items;
    }

    public static StringBuilder convertFromInputStreamToString(InputStream inputStream){
        StringBuilder stringBuilder = new StringBuilder();

        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            while ((line = r.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder;
    }
    public static List<Object> convertTabToArray(Tab tab) {
        List<Object> result = new ArrayList<Object>();

        for (Header header : tab.getHeaders()) {
            result.add(header);
            for (Question question : header.getQuestions())
                result.add(question);

        }
        return result;
    }
    public static boolean isPictureQuestion(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(PreferencesState.getInstance().getContext());
        return sharedPreferences.getBoolean(PreferencesState.getInstance().getContext().getResources().getString(R.string.picturebuild), true);
    }
}
