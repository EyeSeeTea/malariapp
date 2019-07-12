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
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedHeader;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedItem;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedSurvey;
import org.eyeseetea.malariacare.data.database.utils.planning.ScheduleListener;
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.utils.DateParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlannedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int PLAN_ITEM_TYPE = 1;
    private static final int PLAN_HEADER_TYPE = 2;

    private final String TAG = ".PlannedAdapter";

    private List<PlannedItem> items = new ArrayList<>();

    private Context context;

    ProgramDB programFilter;
    PlannedHeader currentHeader;

    int numShown;

    public PlannedAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        PlannedItem plannedItem = getItem(position);
        if (plannedItem instanceof PlannedHeader) {
            return PLAN_HEADER_TYPE;
        } else {
            return PLAN_ITEM_TYPE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case PLAN_HEADER_TYPE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.planning_header_row, parent, false);
                return new PlanHeaderViewHolder(itemView);
            default: // PLAN_ITEM_TYPE
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.planning_survey_row, parent, false);
                return new PlanItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        PlannedItem plannedItem = getItem(position);

        switch (getItemViewType(position)) {
            case PLAN_HEADER_TYPE:
                ((PlanHeaderViewHolder) viewHolder).bindView(position);
                break;
            case PLAN_ITEM_TYPE:
                ((PlanItemViewHolder) viewHolder).bindView(position);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return numShown;
    }

    private PlannedItem getItem(int position) {
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

    public void setItems(List<PlannedItem> newItems) {
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
    public long getItemId(int position) {
        return getItem(position) != null ? getItem(position).hashCode() : 0L;
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

    class PlanItemViewHolder extends RecyclerView.ViewHolder {
        int itemOrder;

        private TextView orgUnitTextView;
        private TextView programTextView;
        private TextView productivityTextView;
        private TextView competencyTextView;
        private TextView scheduledDateTextView;

        public PlanItemViewHolder(View itemView) {
            super(itemView);

            orgUnitTextView = itemView.findViewById(R.id.planning_org_unit);
            programTextView = itemView.findViewById(R.id.planning_program);
            productivityTextView = itemView.findViewById(R.id.planning_survey_prod);
            competencyTextView = itemView.findViewById(R.id.planning_survey_competency);
            scheduledDateTextView = itemView.findViewById(R.id.planning_survey_schedule_date);
        }

        void bindView(int position) {
            PlannedSurvey plannedSurvey = (PlannedSurvey) getItem(position);
            itemOrder = position;

            orgUnitTextView.setText(plannedSurvey.getOrgUnit());
            programTextView.setText(String.format("%s", plannedSurvey.getProgram()));
            productivityTextView.setText(plannedSurvey.getProductivity());

            if (plannedSurvey.getSurvey().getCompetencyScoreClassification() ==
                    CompetencyScoreClassification.NOT_AVAILABLE.getCode()) {
                competencyTextView.setText(
                        context.getString(R.string.competency_classification_not_available));
            } else if (plannedSurvey.getSurvey().getCompetencyScoreClassification() ==
                    CompetencyScoreClassification.COMPETENT.getCode()) {
                competencyTextView.setText(
                        context.getString(R.string.competency_classification_competent));
            } else if (plannedSurvey.getSurvey().getCompetencyScoreClassification() ==
                    CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT.getCode()) {
                competencyTextView.setText(
                        context.getString(R.string.competency_classification_competent_improvement));
            } else if (plannedSurvey.getSurvey().getCompetencyScoreClassification() ==
                    CompetencyScoreClassification.NOT_COMPETENT.getCode()) {
                competencyTextView.setText(
                        context.getString(R.string.competency_classification_not_competent));
            }

            DateParser dateParser = new DateParser();
            scheduledDateTextView.setText(
                    dateParser.getEuropeanFormattedDate(plannedSurvey.getNextAssesment()));
            scheduledDateTextView.setOnClickListener(
                    new ScheduleListener(plannedSurvey.getSurvey(), context));

            ImageView dotsMenu = itemView.findViewById(R.id.menu_dots);

            dotsMenu.setOnClickListener(view -> DashboardActivity.dashboardActivity.onPlannedSurvey(
                    plannedSurvey.getSurvey(),
                    new ScheduleListener(plannedSurvey.getSurvey(), context)));

            assignBackgroundColor();
            setUpActionButton(plannedSurvey);
        }

        private void setUpActionButton(PlannedSurvey plannedSurvey) {
            ImageButton actionButton = itemView.findViewById(R.id.planning_survey_action);
            if (plannedSurvey.getSurvey().isInProgress()) {
                actionButton.setImageResource(R.drawable.ic_edit);
                actionButton.setColorFilter(
                        PreferencesState.getInstance().getContext().getResources().getColor(
                                R.color.assess_yellow));
            } else {
                actionButton.setImageResource(R.drawable.red_circle_cross);
                actionButton.setColorFilter(
                        PreferencesState.getInstance().getContext().getResources().getColor(
                                R.color.plan_grey_light));
            }

            //Planned survey -> onclick startSurvey
            actionButton.setOnClickListener(
                    new CreateOrEditSurveyListener(plannedSurvey.getSurvey()));
        }

        private void assignBackgroundColor() {
            int colorId;
            if (itemOrder == 0 || itemOrder % 2 == 0) {
                colorId = PreferencesState.getInstance().getContext().getResources().getColor(
                        R.color.white);
                itemView.setBackgroundColor(colorId);
            } else {
                colorId = PreferencesState.getInstance().getContext().getResources().getColor(
                        R.color.white_grey);
                itemView.setBackgroundColor(colorId);
            }
        }
    }

    class PlanHeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private ImageView img;

        public PlanHeaderViewHolder(View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.planning_title);
            img = itemView.findViewById(R.id.planning_image_cross);
        }

        void bindView(int position) {
            PlannedHeader plannedHeader = (PlannedHeader) getItem(position);
            itemView.setBackgroundResource(plannedHeader.getBackgroundColor());


            String titleHeader = String.format("%s (%d)",
                    context.getString(plannedHeader.getTitleHeader()), plannedHeader.getCounter());
            textView.setText(titleHeader);


            int color = PreferencesState.getInstance().getContext().getResources().getColor(
                    R.color.black);
            //Set image color
            if (plannedHeader.equals(currentHeader)) {
                img.setImageResource(R.drawable.ic_media_arrow_up);
                img.setColorFilter(
                        PreferencesState.getInstance().getContext().getResources().getColor(
                                R.color.white));
            } else {
                img.setImageResource(R.drawable.ic_media_arrow);
                if (plannedHeader.getTitleHeader() == R.string.dashboard_title_planned_type_never) {
                    color = PreferencesState.getInstance().getContext().getResources().getColor(
                            R.color.white);
                    Typeface font = Typeface.createFromAsset(context.getAssets(),
                            "fonts/" + context.getString(R.string.medium_font_name));
                    textView.setTypeface(font);
                } else {
                    color = PreferencesState.getInstance().getContext().getResources().getColor(
                            R.color.black);
                }
            }
            img.setColorFilter(color);
            textView.setTextColor(color);

            itemView.setOnClickListener(new OpenHeaderListener(plannedHeader));
        }
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
