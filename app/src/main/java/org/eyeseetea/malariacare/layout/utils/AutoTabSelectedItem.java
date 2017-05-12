/*
 * Copyright (c) 2016.
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

package org.eyeseetea.malariacare.layout.utils;

import android.content.Context;

import org.eyeseetea.malariacare.data.database.model.Option;
import org.eyeseetea.malariacare.data.database.model.Question;
import org.eyeseetea.malariacare.layout.adapters.survey.AutoTabAdapter;

/**
 * Holds info related to a selected Item in the AutoTabAdapter.
 * Required to reduce the signature of AutoTabUtils.itemSelected and improve its readability
 * Created by arrizabalaga on 19/04/16.
 */
public class AutoTabSelectedItem {
    private Context context;
    private AutoTabAdapter autoTabAdapter;
    private AutoTabInVisibilityState inVisibilityState;
    private Question question;
    private Option option;
    private AutoTabViewHolder viewHolder;
    private float idSurvey;
    private String module;

    /**
     * Public constructor that gets the info that remains the same between different 'click' events
     * @param autoTabAdapter
     * @param inVisibilityState
     */
    public AutoTabSelectedItem(AutoTabAdapter autoTabAdapter,AutoTabInVisibilityState inVisibilityState, float idSurvey, String module){
        this.autoTabAdapter=autoTabAdapter;
        this.context = autoTabAdapter.getContext();
        this.inVisibilityState = inVisibilityState;
        this.idSurvey=idSurvey;
        this.module=module;
    }

    /**
     * Returns a new 'AutoTabSelectedItem' reusing the common info.
     * @param question
     * @param option
     * @param viewHolder
     * @return
     */
    public AutoTabSelectedItem buildSelectedItem(Question question, Option option, AutoTabViewHolder viewHolder, float idSurvey, String module){
        AutoTabSelectedItem autoTabSelectedItem = new AutoTabSelectedItem(autoTabAdapter,inVisibilityState,idSurvey, module);
        autoTabSelectedItem.question = question;
        autoTabSelectedItem.option = option;
        autoTabSelectedItem.viewHolder = viewHolder;
        this.idSurvey=idSurvey;
        this.module=module;
        return autoTabSelectedItem;
    }

    public Context getContext() {
        return context;
    }

    public Question getQuestion() {
        return question;
    }

    public Option getOption() {
        return option;
    }

    public void setOption(Option option){
        this.option=option;
    }

    public AutoTabViewHolder getViewHolder() {
        return viewHolder;
    }

    public void toggleChildrenVisibility(float idSurvey, String module){
        this.inVisibilityState.toggleChildrenVisibility(this,idSurvey, module);
    }

    public void notifyDataSetChanged(){
        this.autoTabAdapter.notifyDataSetChanged();
    }
}
