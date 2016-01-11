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

package org.eyeseetea.malariacare.database.utils.planning;

import android.content.Context;

import com.raizlabs.android.dbflow.structure.InternalAdapter;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Program;

/**
 * Simple VO to model the headers  the planned listview
 * Created by arrizabalaga on 15/12/15.
 */
public class PlannedHeader implements PlannedItem {
    private String titleHeader;
    private final String productivityHeader;
    private final String qualityOfCareHeader;
    private final String nextHeader;
    private final Integer backgroundColor;

    /**
     * Number of planned surveys under this category
     */
    private Integer counter;

    public PlannedHeader(String titleHeader, String productivityHeader, String qualityOfCareHeader, String nextHeader, Integer backgroundColor) {
        this.titleHeader = titleHeader;
        this.productivityHeader = productivityHeader;
        this.qualityOfCareHeader = qualityOfCareHeader;
        this.nextHeader = nextHeader;
        this.backgroundColor = backgroundColor;
        this.counter=0;
    }

    public String getTitleHeader() {
        return String.format("%s (%d)",titleHeader,counter);
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

    public String getProductivityHeader() {
        return productivityHeader;
    }

    public String getQualityOfCareHeader() {
        return qualityOfCareHeader;
    }

    public String getNextHeader() {
        return nextHeader;
    }

    public Integer getBackgroundColor(){
        return backgroundColor;
    }

    /**
     * Headers are always shown
     * @param filterProgram
     * @return
     */
    @Override
    public boolean isShownByProgram(Program filterProgram){
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
     * @param ctx
     * @return
     */
    public static PlannedHeader buildNeverHeader(Context ctx){
        return new PlannedHeader(
                ctx.getString(R.string.dashboard_title_planned_type_never),
                ctx.getString(R.string.dashboard_title_planned_productivity),
                ctx.getString(R.string.dashboard_title_planned_quality_of_care),
                ctx.getString(R.string.dashboard_title_planned_next_qa),
                R.color.red);
    }

    /**
     * Builds the header for the overdue accordion
     * @param ctx
     * @return
     */
    public static PlannedHeader buildOverdueHeader(Context ctx){
        return new PlannedHeader(
                ctx.getString(R.string.dashboard_title_planned_type_overdue),
                ctx.getString(R.string.dashboard_title_planned_productivity),
                ctx.getString(R.string.dashboard_title_planned_quality_of_care),
                ctx.getString(R.string.dashboard_title_planned_next_qa),
                R.color.amber);
    }

    /**
     * Builds the header for the overdue accordion
     * @param ctx
     * @return
     */
    public static PlannedHeader buildNext30Header(Context ctx){
        return new PlannedHeader(
                ctx.getString(R.string.dashboard_title_planned_type_next_30),
                ctx.getString(R.string.dashboard_title_planned_productivity),
                ctx.getString(R.string.dashboard_title_planned_quality_of_care),
                ctx.getString(R.string.dashboard_title_planned_next_qa),
                R.color.green);
    }

    /**
     * Builds the header for the overdue accordion
     * @param ctx
     * @return
     */
    public static PlannedHeader buildFutureHeader(Context ctx){
        return new PlannedHeader(
                ctx.getString(R.string.dashboard_title_planned_type_future),
                ctx.getString(R.string.dashboard_title_planned_productivity),
                ctx.getString(R.string.dashboard_title_planned_quality_of_care),
                ctx.getString(R.string.dashboard_title_planned_next_qa),
                R.color.scoreGrandson);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlannedHeader that = (PlannedHeader) o;

        if (titleHeader != null ? !titleHeader.equals(that.titleHeader) : that.titleHeader != null)
            return false;
        if (productivityHeader != null ? !productivityHeader.equals(that.productivityHeader) : that.productivityHeader != null)
            return false;
        if (qualityOfCareHeader != null ? !qualityOfCareHeader.equals(that.qualityOfCareHeader) : that.qualityOfCareHeader != null)
            return false;
        if (nextHeader != null ? !nextHeader.equals(that.nextHeader) : that.nextHeader != null)
            return false;
        return !(backgroundColor != null ? !backgroundColor.equals(that.backgroundColor) : that.backgroundColor != null);

    }

    @Override
    public int hashCode() {
        int result = titleHeader != null ? titleHeader.hashCode() : 0;
        result = 31 * result + (productivityHeader != null ? productivityHeader.hashCode() : 0);
        result = 31 * result + (qualityOfCareHeader != null ? qualityOfCareHeader.hashCode() : 0);
        result = 31 * result + (nextHeader != null ? nextHeader.hashCode() : 0);
        result = 31 * result + (backgroundColor != null ? backgroundColor.hashCode() : 0);
        return result;
    }

    @Override
    public String toString(){
        return titleHeader;
    }
}
