package org.eyeseetea.malariacare.views;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;
import org.eyeseetea.malariacare.layout.adapters.monitor.SurveysMonitorAdapter;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;

import java.util.ArrayList;
import java.util.List;

public class MonitorSurveysDialogFragment extends DialogFragment {

    private static String SURVEY_IDS = "SurveyIds";
    private static String SERVER_CLASSIFICATION = "ServerClassification";

    private List<SurveyDB> surveys = new ArrayList<>();

    private LayoutInflater inflater;
    private View rootView;
    private ServerClassification serverClassification;

    public static MonitorSurveysDialogFragment newInstance(String surveyIds,
            ServerClassification serverClassification) {
        MonitorSurveysDialogFragment fragment = new MonitorSurveysDialogFragment();

        Bundle args = new Bundle();
        args.putString(SURVEY_IDS, surveyIds);
        args.putInt(SERVER_CLASSIFICATION, serverClassification.getCode());
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
        serverClassification = ServerClassification.Companion.get(
                getArguments().getInt(SERVER_CLASSIFICATION));

        if (surveyIds != null && !surveyIds.isEmpty()) {
            String surveyIdsArray[] = surveyIds.split(";");
            for (String uid : surveyIdsArray) {
                surveys.add(SurveyDB.getSurveyByUId(uid));
            }
        }

        this.inflater = inflater;

        rootView = inflater.inflate(R.layout.dialog_survey_list_monitoring, container);

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
        TextView surveyCompetencyHeaderView = rootView.findViewById(R.id.survey_competency_header_view);

        if (serverClassification == ServerClassification.COMPETENCIES){
            surveyCompetencyHeaderView.setVisibility(View.VISIBLE);
        } else {
            surveyCompetencyHeaderView.setVisibility(View.GONE);
        }

        RecyclerView recyclerView = rootView.findViewById(R.id.monitor_surveys_list);
        SurveysMonitorAdapter adapter = new SurveysMonitorAdapter(serverClassification);
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