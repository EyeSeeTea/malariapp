
package org.eyeseetea.malariacare.layout.adapters.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.planning.PlannedSurveyByOrgUnit;
import org.eyeseetea.malariacare.fragments.PlannedPerOrgUnitFragment;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by idelcano on 09/08/2016.
 */
public class PlanningPerOrgUnitAdapter extends ADashboardAdapter {
    List<PlannedSurveyByOrgUnit> items;

    public PlanningPerOrgUnitAdapter(List<PlannedSurveyByOrgUnit> items, Context context) {
        this.items= new ArrayList<>();
        this.items = items;
        this.context = context;
        this.lInflater = LayoutInflater.from(context);
        this.headerLayout = R.layout.assessment_planning_header;
        this.recordLayout = R.layout.assessment_planning_record;
    }


    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PlannedSurveyByOrgUnit plannedSurvey = (PlannedSurveyByOrgUnit) getItem(position);
        Survey survey = plannedSurvey.getSurvey();
        float density = getContext().getResources().getDisplayMetrics().density;
        int paddingDp = (int) (5 * density);

        // Get the row layout
        View rowView = this.lInflater.inflate(getRecordLayout(), parent, false);
        rowView.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);

        //config row checkbox
        final CheckBox surveyCheckBox = (CheckBox) rowView.findViewById(R.id.survey_type);
        surveyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                      @Override
                                                      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                          plannedSurvey.setChecked(isChecked);
                                                          PlannedPerOrgUnitFragment.reloadButtonState(isChecked);
                                                      }
                                                  }
        );

        //set checkbox as checked if the planned is checked
        if (plannedSurvey.getChecked()) {
            surveyCheckBox.setChecked(true);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");

        //set schedule date
        if (survey.getScheduledDate() != null) {
            CustomTextView schedule = (CustomTextView) rowView.findViewById(R.id.schedule);
            schedule.setText(sdf.format(survey.getScheduledDate()));
        }
        //set creation date
        if (survey.getCreationDate() != null) {
            CustomTextView dueDate = (CustomTextView) rowView.findViewById(R.id.dueDate);
            dueDate.setText(sdf.format(survey.getCreationDate()));
        }

        //set row survey name
        String surveyDescription = survey.getProgram().getName();
        surveyCheckBox.setText(surveyDescription);

        //set background color from header(type of planning survey)
        rowView.setBackgroundColor(PreferencesState.getInstance().getContext().getResources().getColor(plannedSurvey.getHeader().getBackgroundColor()));
        return rowView;
    }

    @Override
    protected void decorateCustomColumns(Survey survey, View rowView) {

    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

}