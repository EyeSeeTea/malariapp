/*
 * Copyright (c) 2017.
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

package org.eyeseetea.malariacare.utils;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.exception.DateFormatException;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class DateQuestionFormatter {

    //Format a date to value using server format
    public static String formatDateToValue(Date date) {
        if(date==null){
            return "-";
        }
        try {
            return EventExtended.format(date, EventExtended.AMERICAN_DATE_FORMAT);
        }catch (Exception e){
            e.printStackTrace();
            return "-";
        }
    }

    //Format a date to view using locale format
    public static String formatDateToView(Date date) {
        if(date==null){
            return "-";
        }
        try {
            return formatLocaleDateToString(date);
        }catch (DateFormatException ex){
            ex.printStackTrace();
            return "-";
        }
    }

    //Parse the saved date
    public static Date formatDateOutput(String value) {
        Date date;
        try {
            date = formatFromNewOutput(value);
        }catch (DateFormatException ex){
            ex.printStackTrace();
            try {
                date = formatFromOldOutput(value);
            } catch (DateFormatException e) {
                e.printStackTrace();
                return null;
            }
        }
        return date;
    }

    //Parse the saved date from server format
    public static Date formatFromNewOutput(String value) throws DateFormatException{
        try{
            return EventExtended.parseDate(value, EventExtended.AMERICAN_DATE_FORMAT);
        }catch(ParseException ex){
            ex.printStackTrace();
            throw new DateFormatException();
        }
    }

    //Parse the saved date from locale formatt
    public static Date formatFromOldOutput(String value) throws DateFormatException{
        try{
            return formatLocaleDateStringToDate(value);
        }catch(ParseException ex){
            ex.printStackTrace();
            throw new DateFormatException();
        }
    }


    //Returns a string formatted date with the locale format
    public static String formatLocaleDateToString(Date date) throws DateFormatException {
        String dateAsLocaleString;
        try {
            Locale locale =
                    PreferencesState.getInstance().getContext().getResources().getConfiguration().locale;

            java.text.DateFormat dateFormatter = java.text.DateFormat.getDateInstance(
                    java.text.DateFormat.DEFAULT, locale);
            dateAsLocaleString = dateFormatter.format(date);
        }catch (Exception e){
            e.printStackTrace();
            throw  new DateFormatException();
        }
        return dateAsLocaleString;
    }

    //Returns a date parse from a string locale formatted date
    public static Date formatLocaleDateStringToDate(String value) throws ParseException {
        Locale locale = PreferencesState.getInstance().getContext().getResources().getConfiguration().locale;
        java.text.DateFormat dateFormatter = java.text.DateFormat.getDateInstance(
                java.text.DateFormat.DEFAULT, locale);
        return dateFormatter.parse(value);
    }
}
