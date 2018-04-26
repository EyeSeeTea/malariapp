
package org.eyeseetea.malariacare.layout.adapters.dashboard;

import static org.eyeseetea.malariacare.DashboardActivity.dashboardActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedSurveyByOrgUnit;
import org.eyeseetea.malariacare.data.database.utils.planning.ScheduleListener;
import org.eyeseetea.malariacare.fragments.PlannedPerOrgUnitFragment;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.List;

public class PlanningPerOrgUnitAdapter extends ABaseAdapter {
    PlannedPerOrgUnitFragment.Callback callback;
    public static boolean  greyBackground=false;
    public PlanningPerOrgUnitAdapter(List<PlannedSurveyByOrgUnit> newItems, Context context, PlannedPerOrgUnitFragment.Callback callback) {
        super(context);
        items = newItems;
        this.context = context;
        this.lInflater = LayoutInflater.from(context);
        this.recordLayout = R.layout.assessment_planning_record;
        this.callback = callback;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PlannedSurveyByOrgUnit plannedSurvey = (PlannedSurveyByOrgUnit) getItem(position);
        SurveyDB survey = plannedSurvey.getSurvey();
        float density = getContext().getResources().getDisplayMetrics().density;
        int paddingDp = (int) (5 * density);

        // Get the row layout
        View rowView = this.lInflater.inflate(getRecordLayout(), parent, false);
        rowView.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);

        //config row checkbox
        final CheckBox surveyCheckBox = (CheckBox) rowView.findViewById(R.id.survey_type);
        surveyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                      @Override
                                                      public void onCheckedChanged(CompoundButton
                                                              buttonView, boolean isChecked) {
                                                          boolean isChanged = false;
                                                          if(plannedSurvey.getChecked()!=isChecked) {
                                                              isChanged = true;
                                                          }
                                                          plannedSurvey.setChecked(isChecked);
                                                          if(isChanged) {
                                                              callback.onItemCheckboxChanged();
                                                          }
                                                          PlannedPerOrgUnitFragment
                                                                  .reloadButtonState(isChecked);
                                                      }
                                                  }
        );

        //set checkbox as checked if the planned is checked
        if (plannedSurvey.getChecked()) {
            surveyCheckBox.setChecked(true);
        }

        //set schedule date
        CustomTextView schedule = (CustomTextView) rowView.findViewById(R.id.schedule);
        if (survey.getScheduledDate() != null) {
            schedule.setText(AUtils.getEuropeanFormatedDate(survey.getScheduledDate()));
        } else {
            schedule.setText(R.string.assessment_no_schedule_date);
        }
        //set creation date
        if (survey.getCreationDate() != null) {
            CustomTextView dueDate = (CustomTextView) rowView.findViewById(R.id.dueDate);
            dueDate.setText(AUtils.getEuropeanFormatedDate(survey.getCreationDate()));
        }

        //set row survey name
        String surveyDescription = survey.getProgram().getName();
        CustomTextView program = (CustomTextView) rowView.findViewById(R.id.program);
        program.setText(survey.getProgram().getName());
        CustomTextView orgUnit = (CustomTextView) rowView.findViewById(R.id.org_unit);
        orgUnit.setText(survey.getOrgUnit().getName());

        //set background color from header(type of planning survey)
        if(position==0 || position%2==0) {
            rowView.setBackgroundColor(
                    PreferencesState.getInstance().getContext().getResources().getColor(
                            R.color.white_grey));
        }else{
            rowView.setBackgroundColor(
                    PreferencesState.getInstance().getContext().getResources().getColor(
                            R.color.white));
        }
        greyBackground=!greyBackground;
        ImageView menuDots = (ImageView) rowView.findViewById(R.id.menu_dots);
        if(plannedSurvey.isHideMenu()){
            menuDots.setVisibility(View.INVISIBLE);
        }else {
            menuDots.setVisibility(View.VISIBLE);
            menuDots.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO : review after merge questmark cosmetics and remove or create a strategy if is necessary

                    // dashboardActivity.onPlanPerOrgUnitMenuClicked(plannedSurvey.getSurvey());
                    dashboardActivity.onPlannedSurvey(plannedSurvey.getSurvey(),
                            new ScheduleListener(plannedSurvey.getSurvey(), context));
                }
            });
        }

        return rowView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

}