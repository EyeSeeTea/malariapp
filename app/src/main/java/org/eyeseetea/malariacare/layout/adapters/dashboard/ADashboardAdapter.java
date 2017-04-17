/*
 * Copyright (c) 2015.
 *
 * This file is part of Health Network QIS App.
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

package org.eyeseetea.malariacare.layout.adapters.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import org.eyeseetea.malariacare.domain.entity.SurveyEntity;

import java.util.List;

public abstract class ADashboardAdapter extends BaseAdapter implements IDashboardAdapter {

    List<SurveyEntity> items;
    protected LayoutInflater lInflater;
    protected Context context;
    protected Integer headerLayout;
    protected Integer footerLayout;
    protected Integer recordLayout;
    protected String title;

    public ADashboardAdapter(){

    }

    public ADashboardAdapter(List<SurveyEntity> items, Context context, Integer headerLayout, Integer footerLayout, Integer recordLayout, String title) {
        this.items = items;
        this.context = context;
        this.lInflater = LayoutInflater.from(context);
        this.headerLayout = headerLayout;
        this.footerLayout = footerLayout;
        this.recordLayout = recordLayout;
        this.title = title;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void setItems(List items) {
        this.items = (List<SurveyEntity>) items;
    }

    @Override
    public void setHeaderLayout(Integer headerLayout){
        this.headerLayout = headerLayout;
    }

    @Override
    public Integer getHeaderLayout() {
        return this.headerLayout;
    }

    @Override
    public void setFooterLayout(Integer footerLayout) {
        this.footerLayout = footerLayout;
    }

    @Override
    public Integer getFooterLayout() {
        return footerLayout;
    }

    @Override
    public void setRecordLayout(Integer recordLayout){
        this.recordLayout = recordLayout;
    }

    @Override
    public Integer getRecordLayout() {
        return this.recordLayout;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setContext(Context context){
        this.context = context;
    }

    @Override
    public Context getContext(){
        return this.context;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void remove(Object item) {
        this.items.remove(item);
    }

}