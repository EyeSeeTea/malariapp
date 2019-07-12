package org.eyeseetea.malariacare.views;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
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
import org.eyeseetea.malariacare.layout.adapters.monitor.SurveysMonitorAdapter;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.utils.CompetencyUtils;
import org.eyeseetea.malariacare.utils.DateParser;

import java.util.ArrayList;
import java.util.List;

public class MonitorSurveysDialogFragment extends DialogFragment {

    private static String SURVEY_IDS = "SurveyIds";

    private List<SurveyDB> surveys = new ArrayList<>();

    private LayoutInflater inflater;
    private View rootView;

    public static MonitorSurveysDialogFragment newInstance(String surveyIds) {
        MonitorSurveysDialogFragment fragment = new MonitorSurveysDialogFragment();

        Bundle args = new Bundle();
        args.putString(SURVEY_IDS, surveyIds);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onStart() {
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        super.onStart();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        String surveyIds = getArguments().getString(SURVEY_IDS);

        if (surveyIds != null && !surveyIds.isEmpty()) {
            String surveyIdsArray[] = surveyIds.split(";");
            for (String uid : surveyIdsArray) {
                surveys.add(SurveyDB.findById(Long.parseLong(uid)));
            }
        }

        this.inflater = inflater;

        rootView =  inflater.inflate(R.layout.dialog_survey_list_monitoring, container);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeOrgUnitAndProgramViews();
        initializeCancelButton();
        initializeRecyclerView();
    }

    private void initializeRecyclerView() {
        RecyclerView recyclerView = rootView.findViewById(R.id.monitor_surveys_list);
        SurveysMonitorAdapter adapter = new SurveysMonitorAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnSurveyClickListener(survey -> {
            dismiss();
            new UIThreadExecutor().run(
                    () -> DashboardActivity.dashboardActivity.openFeedback(survey, false));
        });

        adapter.setSurveys(surveys);
    }

    private void initializeOrgUnitAndProgramViews() {
        TextView orgUnit = rootView.findViewById(R.id.org_unitName);
        TextView program = rootView.findViewById(R.id.programName);
        program.setText(surveys.get(0).getProgram().getName());
        orgUnit.setText(surveys.get(0).getOrgUnit().getName());
    }

    private void initializeCancelButton() {
        Button cancel = rootView.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> dismiss());
    }
}