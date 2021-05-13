
package org.eyeseetea.malariacare.layout.adapters.dashboard;

import static org.eyeseetea.malariacare.DashboardActivity.dashboardActivity;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedSurveyByOrgUnit;
import org.eyeseetea.malariacare.data.database.utils.planning.ScheduleListener;
import org.eyeseetea.malariacare.fragments.PlannedPerOrgUnitFragment;
import org.eyeseetea.malariacare.utils.DateParser;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.ArrayList;
import java.util.List;

public class PlanningPerOrgUnitAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private PlannedPerOrgUnitFragment.Callback callback;

    private Context context;

    List<PlannedSurveyByOrgUnit> items = new ArrayList<>();

    public PlanningPerOrgUnitAdapter(Context context, PlannedPerOrgUnitFragment.Callback callback) {
        this.context = context;
        this.callback = callback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.assessment_planning_record, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ((ViewHolder) viewHolder).bindView(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<PlannedSurveyByOrgUnit> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public PlannedSurveyByOrgUnit getItem(int position) {
        return items.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CheckBox surveyCheckBox;
        private CustomTextView scheduleTextView;
        private CustomTextView completionDateTextView;
        private CustomTextView programTextView;
        private CustomTextView orgUnitTextView;
        private ImageView menuDots;

        public ViewHolder(View itemView) {
            super(itemView);

            surveyCheckBox = itemView.findViewById(R.id.survey_type);
            scheduleTextView = itemView.findViewById(R.id.schedule);
            completionDateTextView = itemView.findViewById(R.id.completionDate);
            programTextView = itemView.findViewById(R.id.program);
            orgUnitTextView = itemView.findViewById(R.id.org_unit);
            menuDots = itemView.findViewById(R.id.menu_dots);
        }

        void bindView(int position) {
            final PlannedSurveyByOrgUnit plannedSurvey = getItem(position);
            SurveyDB survey = plannedSurvey.getSurvey();
            float density = itemView.getContext().getResources().getDisplayMetrics().density;
            int paddingDp = (int) (5 * density);

            itemView.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);

            surveyCheckBox.setOnCheckedChangeListener(null);
            surveyCheckBox.setChecked(plannedSurvey.getChecked());

            surveyCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                plannedSurvey.setChecked(isChecked);

                callback.onItemCheckboxChanged();
            }
            );

            DateParser dateParser = new DateParser();

            if (survey.getScheduledDate() != null) {
                scheduleTextView.setText(
                        dateParser.getEuropeanFormattedDate(survey.getScheduledDate()));
            } else {
                scheduleTextView.setText(R.string.assessment_no_schedule_date);
            }

            completionDateTextView.setText(
                    dateParser.getEuropeanFormattedDate(survey.getCreationDate()));
            programTextView.setText(survey.getProgram().getName());
            orgUnitTextView.setText(survey.getOrgUnit().getName());

            assignBackgroundColor(position);

            if (plannedSurvey.isHideMenu()) {
                menuDots.setVisibility(View.INVISIBLE);
            } else {
                menuDots.setVisibility(View.VISIBLE);
                menuDots.setOnClickListener(view -> {
                    // TODO : review after merge questmark cosmetics and remove or create a
                    //  strategy if is necessary
                    dashboardActivity.onPlannedSurvey(plannedSurvey.getSurvey(),
                            new ScheduleListener(plannedSurvey.getSurvey(), context));
                });
            }

        }

        private void assignBackgroundColor(int position) {
            if (position == 0 || position % 2 == 0) {
                itemView.setBackgroundColor(
                        PreferencesState.getInstance().getContext().getResources().getColor(
                                R.color.white_grey));
            } else {
                itemView.setBackgroundColor(
                        PreferencesState.getInstance().getContext().getResources().getColor(
                                R.color.white));
            }
        }
    }

}