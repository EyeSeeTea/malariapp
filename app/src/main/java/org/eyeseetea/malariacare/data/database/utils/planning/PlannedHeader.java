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

package org.eyeseetea.malariacare.data.database.utils.planning;

import android.content.Context;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;

/**
 * Simple VO to model the headers  the planned listview
 * Created by arrizabalaga on 15/12/15.
 */
public class PlannedHeader implements PlannedItem {
    private int titleHeader;
    private final Integer backgroundColor;
    private final Integer secondaryColor;
    private final Integer gaudyBackgroundColor;

    /**
     * Number of planned surveys under this category
     */
    private Integer counter;

    public PlannedHeader(int titleHeader, Integer backgroundColor, Integer secondaryColor,
            Integer gaudyBackgroundColor) {
        this.titleHeader = titleHeader;
        this.backgroundColor = backgroundColor;
        this.secondaryColor = secondaryColor;
        this.gaudyBackgroundColor = gaudyBackgroundColor;
        this.counter=0;
    }

    public Integer getGaudyBackgroundColor() {
        return gaudyBackgroundColor;
    }

    public int getTitleHeader() {
        return titleHeader;
    }

    public Integer getCounter() {
        return counter;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    /**
     * Resets counter
     */
    public void resetCounter(){
        setCounter(0);
    }

    public void incCounter(){
        counter++;
    }

    public Integer getBackgroundColor(){
        return backgroundColor;
    }

    public Integer getSecondaryColor() {
        return secondaryColor;
    }
    /**
     * Headers are always shown
     * @param programUidFilter
     * @return
     */
    @Override
    public boolean isShownByProgram(String programUidFilter){
        return true;
    }

    /**
     * Headers are always shown
     * @param plannedHeader
     * @return
     */
    @Override
    public boolean isShownByHeader(PlannedHeader plannedHeader){
        return true;
    }

    /**
     * Builds the header for the never accordion
     * @return
     */
    public static PlannedHeader buildNeverHeader(){
        return new PlannedHeader(R.string.dashboard_title_planned_type_never,
                R.color.red,
                R.color.white_grey,
                R.color.white);
    }

    /**
     * Builds the header for the overdue accordion
     * @return
     */
    public static PlannedHeader buildOverdueHeader(){
        return new PlannedHeader(R.string.dashboard_title_planned_type_overdue,
                R.color.amber,
                R.color.white_grey,
                R.color.white);
    }

    /**
     * Builds the header for the overdue accordion
     * @return
     */
    public static PlannedHeader buildNext30Header(){
        return new PlannedHeader(R.string.dashboard_title_planned_type_next_30,
                R.color.green,
                R.color.white_grey,
                R.color.white);
    }

    /**
     * Builds the header for the overdue accordion
     * @return
     */
    public static PlannedHeader buildFutureHeader(){
        return new PlannedHeader(R.string.dashboard_title_planned_type_future,
                R.color.scoreGrandson,
                R.color.white_grey,
                R.color.white);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlannedHeader that = (PlannedHeader) o;

        if (titleHeader != that.titleHeader)
            return false;
        return !(backgroundColor != null ? !backgroundColor.equals(that.backgroundColor) : that.backgroundColor != null);

    }

    @Override
    public int hashCode() {
        int result = titleHeader;
        result = 31 * result + (backgroundColor != null ? backgroundColor.hashCode() : 0);
        return result;
    }
}
