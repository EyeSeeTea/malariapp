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

import android.content.Context;

import com.orm.query.Select;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.layout.adapters.dashboard.IDashboardAdapter;
import org.eyeseetea.malariacare.utils.Constants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Session {

    private static Survey survey;
    private static User user;
    private static IDashboardAdapter adapter;
    private static String fontSize;
    private static Map<String, Map<String, Float>> fontMap;

    public static Survey getSurvey() {
        return survey;
    }

    public static void setSurvey(Survey survey) {
        Session.survey = survey;
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        Session.user = user;
    }

    public static IDashboardAdapter getAdapter() {
        return adapter;
    }

    public static void setAdapter(IDashboardAdapter adapter) {
        Session.adapter = adapter;
    }

    public static String getFontSize() {
        return fontSize;
    }

    public static void setFontSize(String fontSize) {
        Session.fontSize = fontSize;
    }

    public static Map<String, Map<String, Float>> getFontMap() {
        return fontMap;
    }

    public static void setFontMap(Map<String, Map<String, Float>> fontMap) {
        Session.fontMap = fontMap;
    }

    public static void initMap(Context context){
        Map<String, Float> xsmall = new HashMap<>();
        xsmall.put(Constants.FONTS_XSMALL, context.getResources().getDimension(R.dimen.xsmall_xsmall_text_size));
        xsmall.put(Constants.FONTS_SMALL, context.getResources().getDimension(R.dimen.xsmall_small_text_size));
        xsmall.put(Constants.FONTS_MEDIUM, context.getResources().getDimension(R.dimen.xsmall_medium_text_size));
        xsmall.put(Constants.FONTS_LARGE, context.getResources().getDimension(R.dimen.xsmall_large_text_size));
        xsmall.put(Constants.FONTS_XLARGE, context.getResources().getDimension(R.dimen.xsmall_xlarge_text_size));
        Map<String, Float> small = new HashMap<>();
        small.put(Constants.FONTS_XSMALL, context.getResources().getDimension(R.dimen.small_xsmall_text_size));
        small.put(Constants.FONTS_SMALL, context.getResources().getDimension(R.dimen.small_small_text_size));
        small.put(Constants.FONTS_MEDIUM, context.getResources().getDimension(R.dimen.small_medium_text_size));
        small.put(Constants.FONTS_LARGE, context.getResources().getDimension(R.dimen.small_large_text_size));
        small.put(Constants.FONTS_XLARGE, context.getResources().getDimension(R.dimen.small_xlarge_text_size));
        Map<String, Float> medium = new HashMap<>();
        medium.put(Constants.FONTS_XSMALL, context.getResources().getDimension(R.dimen.medium_xsmall_text_size));
        medium.put(Constants.FONTS_SMALL, context.getResources().getDimension(R.dimen.medium_small_text_size));
        medium.put(Constants.FONTS_MEDIUM, context.getResources().getDimension(R.dimen.medium_medium_text_size));
        medium.put(Constants.FONTS_LARGE, context.getResources().getDimension(R.dimen.medium_large_text_size));
        medium.put(Constants.FONTS_XLARGE, context.getResources().getDimension(R.dimen.medium_xlarge_text_size));
        Map<String, Float> large = new HashMap<>();
        large.put(Constants.FONTS_XSMALL, context.getResources().getDimension(R.dimen.large_xsmall_text_size));
        large.put(Constants.FONTS_SMALL, context.getResources().getDimension(R.dimen.large_small_text_size));
        large.put(Constants.FONTS_MEDIUM, context.getResources().getDimension(R.dimen.large_medium_text_size));
        large.put(Constants.FONTS_LARGE, context.getResources().getDimension(R.dimen.large_large_text_size));
        large.put(Constants.FONTS_XLARGE, context.getResources().getDimension(R.dimen.large_xlarge_text_size));
        Map<String, Float> xlarge = new HashMap<>();
        xlarge.put(Constants.FONTS_XSMALL, context.getResources().getDimension(R.dimen.extra_xsmall_text_size));
        xlarge.put(Constants.FONTS_SMALL, context.getResources().getDimension(R.dimen.extra_small_text_size));
        xlarge.put(Constants.FONTS_MEDIUM, context.getResources().getDimension(R.dimen.extra_medium_text_size));
        xlarge.put(Constants.FONTS_LARGE, context.getResources().getDimension(R.dimen.extra_large_text_size));
        xlarge.put(Constants.FONTS_XLARGE, context.getResources().getDimension(R.dimen.extra_xlarge_text_size));
        Session.fontMap = new HashMap<>();
        Session.fontMap.put(Constants.FONTS_XSMALL, xsmall);
        Session.fontMap.put(Constants.FONTS_SMALL, small);
        Session.fontMap.put(Constants.FONTS_MEDIUM, medium);
        Session.fontMap.put(Constants.FONTS_LARGE, large);
        Session.fontMap.put(Constants.FONTS_XLARGE, xlarge);
    }

    /**
     * Closes the current session when the user logs out
     */
    public static void logout(){
        List<Survey> surveys = Survey.getAllUnsentSurveys();
        for (Survey survey : surveys) {
            survey.delete();
        }
        Session.getUser().delete();
        Session.setUser(null);
        Session.setSurvey(null);
        Session.setAdapter(null);
    }

}
