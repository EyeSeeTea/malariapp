package org.eyeseetea.malariacare.views;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.ScoreType;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.utils.CompetencyUtils;
import org.eyeseetea.malariacare.utils.DateParser;

import java.util.ArrayList;
import java.util.List;

public class MonitorSurveysDialogFragment extends DialogFragment {

    private static String SURVEY_IDS = "SurveyIds";

    private List<SurveyDB> surveys = new ArrayList<>();

    private LayoutInflater inflater;

    public static MonitorSurveysDialogFragment newInstance(String surveyIds) {
        MonitorSurveysDialogFragment fragment = new MonitorSurveysDialogFragment();

        Bundle args = new Bundle();
        args.putString(SURVEY_IDS, surveyIds);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onStart() {
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);

        super.onStart();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        String surveyIds = getArguments().getString(SURVEY_IDS);

        if (surveyIds != null && !surveyIds.isEmpty()){
                String surveyIdsArray[] = surveyIds.split(";");
                for (String uid : surveyIdsArray) {
                    surveys.add(SurveyDB.findById(Long.parseLong(uid)));
                }
        }

        this.inflater = inflater;

        return inflater.inflate(R.layout.dialog_survey_list_monitoring, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView orgUnit = view.findViewById(R.id.org_unitName);
        TextView program = view.findViewById(R.id.programName);
        program.setText(surveys.get(0).getProgram().getName());
        orgUnit.setText(surveys.get(0).getOrgUnit().getName());
        Button cancel = view.findViewById(R.id.cancel);
        LinearLayout linearLayout = view.findViewById(R.id.dialog_content);

        View row;

        for(final SurveyDB survey: surveys){
            row = inflater.inflate(R.layout.item_survey_monitoring, null);
            TextView completionDateView = row.findViewById(R.id.survey_completion_date_item_view);
            TextView competencyView = row.findViewById(R.id.survey_competency_item_view);
            TextView scoreView = row.findViewById(R.id.survey_score_item_view);
            DateParser dateParser = new DateParser();
            completionDateView.setText(dateParser.getEuropeanFormattedDate(survey.getCompletionDate()));
            scoreView.setText(Math.round(survey.getMainScore())+"");
            Resources resources = PreferencesState.getInstance().getContext().getResources();

            ScoreType scoreType = new ScoreType(survey.getMainScore());
            if (scoreType.isTypeA()) {
                scoreView.setBackgroundColor(resources.getColor(R.color.high_score_color));
            }else if (scoreType.isTypeB()){
                scoreView.setBackgroundColor(resources.getColor(R.color.medium_score_color));
            }else if (scoreType.isTypeC()){
                scoreView.setBackgroundColor(resources.getColor(R.color.low_score_color));
            }

            CompetencyScoreClassification classification =
                    CompetencyScoreClassification.get(
                            survey.getCompetencyScoreClassification());

            CompetencyUtils.setBackgroundByCompetency(competencyView, classification);
            CompetencyUtils.setTextByCompetency(competencyView, classification);
            CompetencyUtils.setTextColorByCompetency(competencyView, classification);

            row.setOnClickListener(view1 -> {
                dismiss();
                new UIThreadExecutor().run(
                        () -> DashboardActivity.dashboardActivity.openFeedback(survey, false));
            });
            linearLayout.addView(row );
        }

        cancel.setOnClickListener(v -> dismiss());
    }
}