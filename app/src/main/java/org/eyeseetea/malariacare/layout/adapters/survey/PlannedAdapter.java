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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedHeader;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedItem;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedSurvey;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedSurveyHeader;
import org.eyeseetea.malariacare.data.database.utils.planning.ScheduleListener;
import org.eyeseetea.malariacare.strategies.PlannedStyleStrategy;

import java.util.List;

/**
 * Created by arrizabalaga on 14/09/15.
 */
public class PlannedAdapter extends BaseAdapter {

    private final String TAG = ".PlannedAdapter";

    private List<PlannedItem> items;

    private Context context;

    /**
     * Items filtered by this program
     */
    ProgramDB programFilter;

    /**
     * Current selected header (working like an accordeon)
     */
    PlannedHeader currentHeader;

    /**
     * Number of items shown according to the selected section and filter
     */
    int numShown;

    /**
     * Item order in the block tab(init in the header tab).
     */
    int itemOrder;

    public PlannedAdapter(List<PlannedItem> items, Context context) {
        this.items = items;
        this.context = context;
    }

    private void toggleSection(PlannedHeader header) {

        //An empty section cannot be open
        if (header == null || header.getCounter() == 0) {
            return;
        }

        //Annotate currentHeader
        Log.d(TAG, "toggleSection: " + header);
        currentHeader = (currentHeader == header) ? null : header;
        applyFilter(programFilter);
    }

    @Override
    public int getCount() {
        return numShown;
    }

    public void reloadItems(List<PlannedItem> newItems) {
        Log.d(TAG, "reloadItems: " + newItems.size());
        this.items.clear();
        this.items.addAll(newItems);
        applyFilter(null);
    }

    public void applyFilter(ProgramDB program) {
        Log.d(TAG, "applyFilter:" + program);

        //Annotate filter
        programFilter = program;

        //Update header counters according to new program filter
        updateHeaderCounters();

        //Update counter
        updateNumShown();

        //Refresh view
        notifyDataSetChanged();
    }

    private void updateNumShown() {
        Log.d(TAG, "updateNumShown");
        //No list -> nothing to update
        if (items == null) {
            numShown = 0;
            return;
        }

        //Loop over list annotating items that can be shown
        int numItems = 0;
        for (PlannedItem plannedItem : items) {
            //Headers are always shown
            if (plannedItem instanceof PlannedHeader) {
                numItems++;
            } else {
                //Surveys are shown
                if ((plannedItem.isShownByProgram(programFilter) || programFilter.getName().equals(
                        PreferencesState.getInstance().getContext().getResources().getString(
                                R.string.filter_all_org_assessments)))
                        && plannedItem.isShownByHeader(currentHeader)) {
                    numItems++;
                }
            }
        }
        //Contar x filtro
        Log.d(TAG, "updateNumShown:" + numItems);
        numShown = numItems;
    }

    private void updateHeaderCounters() {
        //Update counters in header
        for (PlannedItem plannedItem : items) {
            //Headers are always shown
            if (plannedItem instanceof PlannedHeader) {
                ((PlannedHeader) plannedItem).resetCounter();
                continue;
            }
            if (plannedItem instanceof PlannedSurveyHeader) {
                continue;
            }
            //Check match survey/program -> update header.counter
            PlannedSurvey plannedSurvey = (PlannedSurvey) plannedItem;
            if (plannedSurvey.isShownByProgram(programFilter) || programFilter.getName().equals(
                    PreferencesState.getInstance().getContext().getResources().getString(
                            R.string.filter_all_org_assessments))) {
                plannedSurvey.incHeaderCounter();
            }
        }
    }

    @Override
    public Object getItem(int position) {
        //No filter
        Log.d(TAG, "getItem: " + position);

        //Loop and count
        int numShownItems = 0;
        for (int i = 0; i < items.size(); i++) {
            PlannedItem plannedItem = items.get(i);

            if ((plannedItem.isShownByProgram(programFilter) || programFilter.getName().equals(
                    PreferencesState.getInstance().getContext().getResources().getString(
                            R.string.filter_all_org_assessments)))
                    && plannedItem.isShownByHeader(currentHeader)) {
                numShownItems++;
                if (position == (numShownItems - 1)) {
                    return plannedItem;
                }
            }
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position) != null ? getItem(position).hashCode() : 0L;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getView: " + position);
        PlannedItem plannedItem = (PlannedItem) getItem(position);
        if (plannedItem instanceof PlannedHeader) {
            itemOrder = 0;
            return getViewByPlannedHeader((PlannedHeader) plannedItem, parent);
        } else if (plannedItem instanceof PlannedSurveyHeader) {
            itemOrder++;
            return PlannedStyleStrategy.getViewByPlannedSurveyHeader(parent);
        } else {
            return getViewByPlannedSurvey(position, (PlannedSurvey) plannedItem, parent);
        }
    }

    private View getViewByPlannedHeader(PlannedHeader plannedHeader, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout rowLayout = (LinearLayout) inflater.inflate(R.layout.planning_header_row,
                parent, false);
        rowLayout.setBackgroundResource(plannedHeader.getBackgroundColor());

        //Title
        TextView textView = (TextView) rowLayout.findViewById(R.id.planning_title);
        textView.setText(plannedHeader.getTitleHeader());
        ImageView img = (ImageView) rowLayout.findViewById(R.id.planning_image_cross);

        int color  = PreferencesState.getInstance().getContext().getResources().getColor(
                R.color.black);
        //Set image color
        color = new PlannedStyleStrategy(plannedHeader, textView, img, color).draw(currentHeader);
        img.setColorFilter(color);
        textView.setTextColor(color);
        PlannedStyleStrategy.drawNumber(rowLayout, plannedHeader.getCounter());
/*
        //Productivity
        textView=(TextView)rowLayout.findViewById(R.id.planning_prod);
        textView.setText(plannedHeader.getProductivityHeader());

        //Quality of Care
        textView=(TextView)rowLayout.findViewById(R.id.planning_qoc);
        textView.setText(plannedHeader.getQualityOfCareHeader());

        //Next
        textView=(TextView)rowLayout.findViewById(R.id.planning_next);
        textView.setText(plannedHeader.getNextHeader());*/

        //Planned header -> toggleSection
        rowLayout.setOnClickListener(new OpenHeaderListener(plannedHeader));
        return rowLayout;
    }

    private View getViewByPlannedSurvey(int position, final PlannedSurvey plannedSurvey,
            ViewGroup parent) {
        itemOrder++;
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout rowLayout = (LinearLayout) inflater.inflate(R.layout.planning_survey_row,
                parent, false);

        //OrgUnit
        TextView textView = (TextView) rowLayout.findViewById(R.id.planning_org_unit);
        textView.setText(plannedSurvey.getOrgUnit());

        //Program
        textView = (TextView) rowLayout.findViewById(R.id.planning_program);
        textView.setText(String.format("%s", plannedSurvey.getProgram()));

        //Productivity
        textView = (TextView) rowLayout.findViewById(R.id.planning_survey_prod);
        textView.setText(plannedSurvey.getProductivity());

        //QualityOfCare
        PlannedStyleStrategy.drawQualityOfCare(rowLayout, plannedSurvey);

        //ScheduledDate
        textView = (TextView) rowLayout.findViewById(R.id.planning_survey_schedule_date);
        textView.setText(PlannedStyleStrategy.formatDate(plannedSurvey.getNextAssesment()));
        textView.setOnClickListener(new ScheduleListener(plannedSurvey.getSurvey(), context));

        ImageView dotsMenu = (ImageView) rowLayout.findViewById(R.id.menu_dots);
        dotsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DashboardActivity.dashboardActivity.onPlannedSurvey(plannedSurvey.getSurvey(), new ScheduleListener(plannedSurvey.getSurvey(), context));
            }
        });
        //background color
        int colorId = plannedSurvey.getPlannedHeader().getSecondaryColor();
        int fixposition = itemOrder - 1;
        if (fixposition == 0 || fixposition % 2 == 0) {
            colorId = PreferencesState.getInstance().getContext().getResources().getColor(
                    R.color.white);
            rowLayout.setBackgroundColor(colorId);
        } else {
            colorId = PreferencesState.getInstance().getContext().getResources().getColor(
                    R.color.even_row_background);
            rowLayout.setBackgroundColor(colorId);
        }
        //Action
        ImageButton actionButton = (ImageButton) rowLayout.findViewById(
                R.id.planning_survey_action);
        if (plannedSurvey.getSurvey().isInProgress()) {
            actionButton.setImageResource(R.drawable.ic_edit_light);
        } else {
            actionButton.setImageResource(R.drawable.ic_plus_light);
        }
         PlannedStyleStrategy.drawActionButtonTint(actionButton, plannedSurvey.getSurvey().isInProgress());

        //Planned survey -> onclick startSurvey
        actionButton.setOnClickListener(new CreateOrEditSurveyListener(plannedSurvey.getSurvey()));
        rowLayout.setOnClickListener(new CreateOrEditSurveyListener(plannedSurvey.getSurvey()));

        return rowLayout;
    }

    private boolean isSameOrgUnit(PlannedSurvey plannedSurvey, int currentPosition) {
        PlannedItem plannedItem = (PlannedItem) getItem(currentPosition - 1);

        if (plannedItem instanceof PlannedHeader) {
            return false;
        }
        PlannedSurvey previousPlannedSurvey = (PlannedSurvey) plannedItem;
        return plannedSurvey.getOrgUnit().equals(previousPlannedSurvey.getOrgUnit());
    }

    /**
     * Listener that starts the given planned survey and goes to surveyActivity to start with
     * edition
     */
    class CreateOrEditSurveyListener implements View.OnClickListener {

        SurveyDB survey;

        CreateOrEditSurveyListener(SurveyDB survey) {
            this.survey = survey;
        }

        @Override
        public void onClick(View v) {
            DashboardActivity activity = ((DashboardActivity) context);
            activity.onSurveySelected(survey);
//            if(survey.getStatus()==Constants.SURVEY_PLANNED){
//                survey=SurveyPlanner.getInstance().startSurvey(survey);
//            }
//
//            Session.setSurveyByModule(survey);
//            activity.prepareLocationListener(survey);
//            //FIXME
//
//            activity.initSurveyFromPlanning();

            // de development
//            DashboardActivity activity = ((DashboardActivity) context);
//            if(survey.getStatus()==Constants.SURVEY_PLANNED){
//                survey=SurveyPlanner.getInstance().startSurvey(survey);
//            }
//            activity.prepareLocationListener(survey);
//            //FIXME
//
//            activity.initSurveyFromPlanning(survey);
        }
    }

    /**
     * Listener that opens a section
     */
    class OpenHeaderListener implements View.OnClickListener {

        PlannedHeader plannedHeader;

        OpenHeaderListener(PlannedHeader plannedHeader) {
            this.plannedHeader = plannedHeader;
        }

        @Override
        public void onClick(View v) {
            toggleSection(plannedHeader);
        }
    }


}
