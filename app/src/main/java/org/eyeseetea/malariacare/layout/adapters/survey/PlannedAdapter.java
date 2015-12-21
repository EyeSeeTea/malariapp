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
     * Require to hide/show org_unit according to last one
     */
    String lastOrgUnit;

    /**
     * Items filtered by this program
     */
    Program programFilter;

    public PlannedAdapter(Context context){
        this(new ArrayList<PlannedItem>(), context);
    }

    public PlannedAdapter(List<PlannedItem> items, Context context){
        this.items=items;
        this.context=context;
        this.surveyPlanner = new SurveyPlanner();
    }

    @Override
    public int getCount() {
        int numItems=0;
        for(PlannedItem plannedItem:items){
            //Headers are always shown
            if(plannedItem instanceof PlannedHeader){
                numItems=numItems+1+((PlannedHeader)plannedItem).getCounter();
            }
        }
        //Contar x filtro
        Log.d(TAG,"getCount "+numItems);
        return numItems;
    }

    public void applyFilter(Program program){
        programFilter=program;

        //Update counters in header
        for(PlannedItem plannedItem:items){
            //Headers are always shown
            if(plannedItem instanceof PlannedHeader){
                ((PlannedHeader) plannedItem).resetCounter();
                continue;
            }
            //Check match survey/program -> update header.counter
            PlannedSurvey plannedSurvey = (PlannedSurvey)plannedItem;
            if(plannedSurvey.isShownByProgram(program)){
                plannedSurvey.incHeaderCounter();
            }
        }

        //Refresh view
        notifyDataSetChanged();

    }

    @Override
    public Object getItem(int position) {
        //No filter
        Log.d(TAG,"getItem pos:"+position);
        if(programFilter==null) {
            Log.d(TAG,"       pos:"+position+" -> No filter direct access");
            return this.items.get(position);
        }

        //Loop and count
        int numShownItems=0;
        for(PlannedItem plannedItem:items){
            //Found
            Log.d(TAG,"       pos:"+position+" numShown:"+numShownItems);
            if(position==numShownItems){
                return plannedItem;
            }
            //Not found -> increase counter if shown
            if (plannedItem.isShownByProgram(programFilter)){
                numShownItems++;
            }
        }
        Log.d(TAG,"       pos:"+position+" -> Not found");
        return null;
    }

    @Override
    public long getItemId(int position) {
        Log.d(TAG,"getItemId pos:"+position);
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG,"getView pos:"+position);
        PlannedItem plannedItem=(PlannedItem)getItem(position);
        if (plannedItem instanceof PlannedHeader){
            return getViewByPlannedHeader((PlannedHeader) plannedItem, convertView, parent);
        }else{
            return getViewByPlannedSurvey((PlannedSurvey) plannedItem, convertView, parent);
        }
    }

    private View getViewByPlannedHeader(PlannedHeader plannedHeader, View convertView, ViewGroup parent){
        LayoutInflater inflater=LayoutInflater.from(context);
        LinearLayout rowLayout = (LinearLayout)inflater.inflate(R.layout.planning_header_row, parent, false);
        rowLayout.setBackgroundResource(plannedHeader.getBackgroundColor());

        //Title
        TextView textView=(TextView)rowLayout.findViewById(R.id.planning_title);
        textView.setText(plannedHeader.getTitleHeader());

        //Productivity
        textView=(TextView)rowLayout.findViewById(R.id.planning_prod);
        textView.setText(plannedHeader.getProductivityHeader());

        //Quality of Care
        textView=(TextView)rowLayout.findViewById(R.id.planning_qoc);
        textView.setText(plannedHeader.getQualityOfCareHeader());

        //Next
        textView=(TextView)rowLayout.findViewById(R.id.planning_next);
        textView.setText(plannedHeader.getNextHeader());

        //Resets last orgunit
        lastOrgUnit=null;
        return rowLayout;
    }

    private View getViewByPlannedSurvey(final PlannedSurvey plannedSurvey, View convertView, ViewGroup parent){

        LayoutInflater inflater=LayoutInflater.from(context);
        LinearLayout rowLayout = (LinearLayout)inflater.inflate(R.layout.planning_survey_row, parent, false);

        //OrgUnit
        TextView textView=(TextView)rowLayout.findViewById(R.id.planning_org_unit);
        textView.setText(plannedSurvey.getOrgUnit());
        if(lastOrgUnit!=null && lastOrgUnit.equals(plannedSurvey.getOrgUnit())){
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

        //Annotate last orgunit
        lastOrgUnit=plannedSurvey.getOrgUnit();

        return rowLayout;
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

}
