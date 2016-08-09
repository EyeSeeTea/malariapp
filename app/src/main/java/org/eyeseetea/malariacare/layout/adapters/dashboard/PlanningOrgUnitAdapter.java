package org.eyeseetea.malariacare.layout.adapters.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.planning.PlannedItem;
import org.eyeseetea.malariacare.database.utils.planning.PlannedSurvey;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.Date;
import java.util.List;

/**
 * Created by idelcano on 09/08/2016.
 */
public class PlanningOrgUnitAdapter extends ADashboardAdapter implements IDashboardAdapter  {
    List<PlannedItem> items;
    protected int backIndex = 0;
    protected boolean showNextFacilityName = true;
    protected boolean multipleTabGroups = new Select().count().from(TabGroup.class).count() != 1;
        public PlanningOrgUnitAdapter(List<PlannedItem> items, Context context) {
            this.items = items;
            this.context = context;
            this.lInflater = LayoutInflater.from(context);
            this.headerLayout = R.layout.assessment_planning_header;
            this.recordLayout = R.layout.assessment_planning_record;
        }

        @Override
        public IDashboardAdapter newInstance(List items, Context context) {
            return new PlanningOrgUnitAdapter((List<PlannedItem>) items, context);
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
        PlannedSurvey plannedSurvey = (PlannedSurvey) getItem(position);
        Survey survey=plannedSurvey.getSurvey();
        float density = getContext().getResources().getDisplayMetrics().density;
        int paddingDp = (int)(5 * density);

        // Get the row layout
        View rowView = this.lInflater.inflate(getRecordLayout(), parent, false);
        rowView.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);

        // Org Unit Cell
        CheckBox surveyType = (CheckBox) rowView.findViewById(R.id.survey_type);
        CustomTextView schedule = (CustomTextView) rowView.findViewById(R.id.schedule);
        CustomTextView dueDate = (CustomTextView) rowView.findViewById(R.id.dueDate);
        if(survey.getScheduledDate()!=null)
            schedule.setText(survey.getScheduledDate().toString());
        if(survey.getCompletionDate()!=null)
        dueDate.setText(survey.getCompletionDate().toString());

        String surveyDescription = survey.getTabGroup().getProgram().getName();
        surveyType.setText(surveyDescription);

        rowView.setBackgroundColor(PreferencesState.getInstance().getContext().getResources().getColor(plannedSurvey.getHeader().getBackgroundColor()));
        return rowView;
    }

    @Override
    public void notifyDataSetChanged(){
        this.showNextFacilityName = true;
        super.notifyDataSetChanged();
    }

}
