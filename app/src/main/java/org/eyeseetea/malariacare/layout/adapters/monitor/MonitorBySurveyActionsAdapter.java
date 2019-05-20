package org.eyeseetea.malariacare.layout.adapters.monitor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.layout.adapters.SectionDetailAdapter;
import org.eyeseetea.malariacare.presentation.viewmodels.SurveyViewModel;
import org.eyeseetea.malariacare.utils.CompetencyUtils;
import org.eyeseetea.malariacare.utils.DateParser;

import java.util.ArrayList;
import java.util.List;

public class MonitorBySurveyActionsAdapter extends SectionDetailAdapter {
    private List<SurveyViewModel> incompleteSurveys = new ArrayList<>();
    private List<SurveyViewModel> completedSurveys = new ArrayList<>();
    private Context context;

    public MonitorBySurveyActionsAdapter(Context context){
        this.context =  context;
    }

    public void setSurveys (
            List<SurveyViewModel> incompleteSurveys,
            List<SurveyViewModel> completedSurveys){
        this.incompleteSurveys = incompleteSurveys;
        this.completedSurveys = completedSurveys;
        super.refreshData();
    }

    @Override
    protected int getSectionsCount() {
        return 2;
    }

    @Override
    protected int getItemsCountInSection(int section) {
        if (section == 0){
            return incompleteSurveys.size();
        } else {
            return completedSurveys.size();
        }
    }

    @Override
    protected RecyclerView.ViewHolder onCreateSectionViewHolder(ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_survey_section, parent, false);
        return new SurveySectionViewHolder(itemView);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateRowViewHolder(ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_survey_detail, parent, false);
        return new SurveyDetailViewHolder(itemView);
    }

    @Override
    protected void onBindSectionViewHolder(RecyclerView.ViewHolder holder, int sectionPosition) {
        String sectionName;
        int sectionCount;
        int color;

        if (sectionPosition == 0){
            sectionName = context.getString(R.string.survey_imcomplete_text);
            sectionCount = incompleteSurveys.size();
            color = R.color.red;
        } else {
            sectionName = context.getString(R.string.survey_complete_text);
            sectionCount = completedSurveys.size();
            color = R.color.green;
        }

        ((SurveySectionViewHolder) holder).bindView(sectionName, sectionCount, color);
    }

    @Override
    protected void onBindRowViewHolder(RecyclerView.ViewHolder holder, int sectionPosition,
            int rowPositionInSection) {
        if (sectionPosition == 0){
            ((SurveyDetailViewHolder) holder).bindView(incompleteSurveys, rowPositionInSection);
        } else {
            ((SurveyDetailViewHolder) holder).bindView(incompleteSurveys, rowPositionInSection);
        }
    }

    class SurveySectionViewHolder extends RecyclerView.ViewHolder {

        private TextView surveySectionNameView;
        private ImageView explandCollapseView;
        private LinearLayout surveySectionContainer;

        public SurveySectionViewHolder(View itemView) {
            super(itemView);

            surveySectionNameView = itemView.findViewById(R.id.survey_section_name);
            explandCollapseView = itemView.findViewById(R.id.expand_collapse_view);
            surveySectionContainer = itemView.findViewById((R.id.survey_section_container));
        }

        void bindView(String sectionName, int sectionCount, int color) {
            String titleHeader = String.format("%s (%d)", sectionName, sectionCount);
            surveySectionNameView.setText(titleHeader);
            surveySectionContainer.setBackgroundResource(color);
        }
    }

    class SurveyDetailViewHolder extends RecyclerView.ViewHolder {
        int itemOrder;

        private TextView orgUnitTextView;
        private TextView programTextView;
        private TextView competencyView;
        private TextView scheduledDateTextView;
        private ImageView dotsMenu;

        public SurveyDetailViewHolder(View itemView) {
            super(itemView);

            orgUnitTextView = itemView.findViewById(R.id.survey_org_unit_name);
            programTextView = itemView.findViewById(R.id.survey_program_name);
            competencyView = itemView.findViewById(R.id.survey_competency_view);
            scheduledDateTextView = itemView.findViewById(R.id.survey_schedule_date_view);
            dotsMenu = itemView.findViewById(R.id.menu_dots);
        }

        void bindView(List<SurveyViewModel> surveys, int position) {
            SurveyViewModel survey = surveys.get(position);
            itemOrder = position;

            orgUnitTextView.setText(survey.getOrgUnit());
            programTextView.setText(survey.getProgram());

            CompetencyUtils.setTextByCompetencyAbbreviation(competencyView,survey.getCompetency());

            scheduledDateTextView.setText(DateParser.getEuropeanFormattedDate(survey.getDate()));

            //dotsMenu.setOnClickListener(view -> );

            assignBackgroundColor();
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
}
