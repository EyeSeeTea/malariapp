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

package org.eyeseetea.malariacare.layout.adapters.survey;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.BaseActivity;
import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.SurveyActivity;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.utils.planning.PlannedHeader;
import org.eyeseetea.malariacare.database.utils.planning.PlannedItem;
import org.eyeseetea.malariacare.database.utils.planning.PlannedSurvey;
import org.eyeseetea.malariacare.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.utils.Constants;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by arrizabalaga on 14/09/15.
 */
public class PlannedAdapter extends BaseAdapter {

    private final String TAG=".PlannedAdapter";

    private List<PlannedItem> items;

    private Context context;

    /**
     * Require to start or edit a selected survey
     */
    SurveyPlanner surveyPlanner;

    /**
     * Items filtered by this program
     */
    Program programFilter;

    /**
     * Current selected header (working like an accordeon)
     */
    PlannedHeader currentHeader;

    /**
     * Number of items shown according to the selected section and filter
     */
    int numShown;

    public PlannedAdapter(List<PlannedItem> items, Context context){
        this.items=items;
        this.context=context;
        this.surveyPlanner = new SurveyPlanner();
        initDefaultSection();
    }

    private void initDefaultSection(){
        if(items!=null && items.size()>0){
            openSection((PlannedHeader)items.get(0));
        }
    }

    private void openSection(PlannedHeader header){
        //Annotate currentHeader
        Log.d(TAG, "openSection: " + header);
        currentHeader=header;
        applyFilter(programFilter);
    }

    @Override
    public int getCount() {
        return numShown;
    }

    public void reloadItems(List<PlannedItem> newItems){
        Log.d(TAG, "reloadItems: " + newItems.size());
        this.items.clear();
        this.items.addAll(newItems);
        initDefaultSection();
        applyFilter(null);
    }

    public void applyFilter(Program program){
        Log.d(TAG,"applyFilter:"+program);

        //Annotate filter
        programFilter=program;

        //Update header counters according to new program filter
        updateHeaderCounters();

        //Update counter
        updateNumShown();

        //Refresh view
        notifyDataSetChanged();
    }

    private void updateNumShown(){
        Log.d(TAG, "updateNumShown");
        //No list -> nothing to update
        if(items==null){
            numShown=0;
            return;
        }

        //Loop over list annotating items that can be shown
        int numItems=0;
        for(PlannedItem plannedItem:items){
            //Headers are always shown
            if(plannedItem instanceof PlannedHeader){
                numItems++;
            }else{
                //Surveys are shown
                if(plannedItem.isShownByProgram(programFilter) && plannedItem.isShownByHeader(currentHeader)){
                    numItems++;
                }
            }
        }
        //Contar x filtro
        Log.d(TAG, "updateNumShown:" + numItems);
        numShown=numItems;
    }

    private void updateHeaderCounters(){
        //Update counters in header
        for(PlannedItem plannedItem:items){
            //Headers are always shown
            if(plannedItem instanceof PlannedHeader){
                ((PlannedHeader) plannedItem).resetCounter();
                continue;
            }
            //Check match survey/program -> update header.counter
            PlannedSurvey plannedSurvey = (PlannedSurvey)plannedItem;
            if(plannedSurvey.isShownByProgram(programFilter)){
                plannedSurvey.incHeaderCounter();
            }
        }
    }

    @Override
    public Object getItem(int position) {
        //No filter
        Log.d(TAG, "getItem: "+position);

        //Loop and count
        int numShownItems=0;
        for(int i=0;i<items.size();i++){
            PlannedItem plannedItem=items.get(i);

            if(plannedItem.isShownByProgram(programFilter) && plannedItem.isShownByHeader(currentHeader)){
                numShownItems++;
                if(position==(numShownItems-1)) {
                    return plannedItem;
                }
            }
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG,"getView: "+position);
        PlannedItem plannedItem=(PlannedItem)getItem(position);
        if (plannedItem instanceof PlannedHeader){
            return getViewByPlannedHeader((PlannedHeader) plannedItem, parent);
        }else{
            return getViewByPlannedSurvey(position,(PlannedSurvey) plannedItem, parent);
        }
    }

    private View getViewByPlannedHeader(PlannedHeader plannedHeader, ViewGroup parent){
        LayoutInflater inflater=LayoutInflater.from(context);
        LinearLayout rowLayout = (LinearLayout)inflater.inflate(R.layout.planning_header_row, parent, false);
        rowLayout.setBackgroundResource(plannedHeader.getBackgroundColor());

        //Title
        TextView textView=(TextView)rowLayout.findViewById(R.id.planning_title);
        textView.setText(plannedHeader.getTitleHeader());
        if(plannedHeader.equals(currentHeader)){
            boldHeader(textView);
        }

        //Productivity
        textView=(TextView)rowLayout.findViewById(R.id.planning_prod);
        textView.setText(plannedHeader.getProductivityHeader());
        if(plannedHeader.equals(currentHeader)){
            boldHeader(textView);
        }

        //Quality of Care
        textView=(TextView)rowLayout.findViewById(R.id.planning_qoc);
        textView.setText(plannedHeader.getQualityOfCareHeader());
        if(plannedHeader.equals(currentHeader)){
            boldHeader(textView);
        }

        //Next
        textView=(TextView)rowLayout.findViewById(R.id.planning_next);
        textView.setText(plannedHeader.getNextHeader());
        if(plannedHeader.equals(currentHeader)){
            boldHeader(textView);
        }

        //Planned header -> openSection
        rowLayout.setOnClickListener(new OpenHeaderListener(plannedHeader));
        return rowLayout;
    }

    private void boldHeader(TextView textView){
        textView.setTextColor(context.getResources().getColor(R.color.black));
    }

    private View getViewByPlannedSurvey(int position,final PlannedSurvey plannedSurvey, ViewGroup parent){

        LayoutInflater inflater=LayoutInflater.from(context);
        LinearLayout rowLayout = (LinearLayout)inflater.inflate(R.layout.planning_survey_row, parent, false);

        //OrgUnit
        TextView textView=(TextView)rowLayout.findViewById(R.id.planning_org_unit);
        textView.setText(plannedSurvey.getOrgUnit());
        if(isSameOrgUnit(plannedSurvey,position)){
            textView.setVisibility(View.GONE);
        }

        //Program
        textView=(TextView)rowLayout.findViewById(R.id.planning_program);
        textView.setText(String.format("   - %s",plannedSurvey.getProgram()));

        //Productivity
        textView=(TextView)rowLayout.findViewById(R.id.planning_survey_prod);
        textView.setText(plannedSurvey.getProductivity());

        //QualityOfCare
        textView=(TextView)rowLayout.findViewById(R.id.planning_survey_qoc);
        textView.setText(plannedSurvey.getQualityOfCare());

        //ScheduledDate
        textView=(TextView)rowLayout.findViewById(R.id.planning_survey_schedule_date);
        textView.setText(formatScheduledDate(plannedSurvey.getNextAssesment()));

        //Action
        ImageButton actionButton = (ImageButton)rowLayout.findViewById(R.id.planning_survey_action);
        if(plannedSurvey.getSurvey().isInProgress()){
            actionButton.setImageResource(R.drawable.ic_edit);
        }

        //Planned survey -> onclick startSurvey
        actionButton.setOnClickListener(new CreateOrEditSurveyListener(plannedSurvey.getSurvey()));

        return rowLayout;
    }

    private boolean isSameOrgUnit(PlannedSurvey plannedSurvey, int currentPosition){
        PlannedItem plannedItem=(PlannedItem)getItem(currentPosition-1);

        if(plannedItem instanceof PlannedHeader){
            return false;
        }
        PlannedSurvey previousPlannedSurvey = (PlannedSurvey)plannedItem;
        return plannedSurvey.getOrgUnit().equals(previousPlannedSurvey.getOrgUnit());
    }

    private String formatScheduledDate(Date date){
        if(date==null){
            return "-";
        }
        Locale locale = context.getResources().getConfiguration().locale;
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
        return dateFormatter.format(date);
    }

    /**
     * Listener that starts the given planned survey and goes to surveyActivity to start with edition
     */
    class CreateOrEditSurveyListener implements View.OnClickListener {

        Survey survey;

        CreateOrEditSurveyListener(Survey survey){
            this.survey=survey;
        }

        @Override
        public void onClick(View v) {
            BaseActivity activity = ((DashboardActivity) context);
            if(survey.getStatus()==Constants.SURVEY_PLANNED){
                survey=surveyPlanner.startSurvey(survey);
            }

            Session.setSurvey(survey);
            activity.prepareLocationListener(survey);
            activity.finishAndGo(SurveyActivity.class);
        }
    }

    /**
     * Listener that opens a section
     */
    class OpenHeaderListener implements View.OnClickListener {

        PlannedHeader plannedHeader;

        OpenHeaderListener(PlannedHeader plannedHeader){
            this.plannedHeader=plannedHeader;
        }

        @Override
        public void onClick(View v) {
            openSection(plannedHeader);
        }
    }

}
