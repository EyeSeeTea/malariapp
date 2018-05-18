package org.eyeseetea.malariacare.fragments;


import android.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.ScoreType;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.sdk.presentation.views.DoubleRectChart;


import java.util.ArrayList;
import java.util.List;

public class ListOfSurveysDialogFragment extends DialogFragment {

    public static final String UIDS_LIST = "uidsList";
    public static final String TAG = "ListOfSurveysDialogFragment";

    private String mUidsList;
    private View mView;

    static ListOfSurveysDialogFragment newInstance(String uidsList) {
        ListOfSurveysDialogFragment listOfSurveysDialogFragment = new ListOfSurveysDialogFragment();
        Bundle args = new Bundle();
        args.putString(UIDS_LIST, uidsList);
        listOfSurveysDialogFragment.setArguments(args);

        return listOfSurveysDialogFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUidsList = getArguments().getString(UIDS_LIST);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.monitoring_survey_list_dialog, container, false);
        ArrayList<SurveyDB> surveys = getSurveysFromUids();
        showListOfSurveys(surveys, inflater);
        return mView;
    }

    private ArrayList<SurveyDB> getSurveysFromUids() {
        ArrayList<SurveyDB> surveys = new ArrayList<>();
        if (mUidsList.length() > 0) {
            String uids[] = mUidsList.split(";");
            for (String uid : uids) {
                surveys.add(SurveyDB.findById(Long.parseLong(uid)));
            }
        }
        return surveys;
    }

    public void showListOfSurveys(final ArrayList<SurveyDB> surveys, LayoutInflater inflater) {
        TextView orgUnit = (TextView) mView.findViewById(R.id.org_unitName);
        TextView program = (TextView) mView.findViewById(R.id.programName);
        program.setText(surveys.get(0).getProgram().getName());
        orgUnit.setText(surveys.get(0).getOrgUnit().getName());
        Button cancel = (Button) mView.findViewById(R.id.cancel);
        createRows(inflater, surveys);
        cancel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                }
        );
    }

    private void createRows(LayoutInflater inflater, List<SurveyDB> surveys) {
        LinearLayout linearLayout = (LinearLayout) mView.findViewById(R.id.log_content);
        View row = inflater.inflate(R.layout.survey_list_dialog_header, null);
        ((TextView) row.findViewById(R.id.first_column)).setText(R.string.assessment_sent_date);
        ((TextView) row.findViewById(R.id.second_column)).setText(R.string.score);
        linearLayout.addView(row);
        for (final SurveyDB survey : surveys) {
            row = inflater.inflate(R.layout.survey_list_row, null);
            TextView completionDate = (TextView) row.findViewById(R.id.first_column);
            completionDate.setText(AUtils.getEuropeanFormatedDate(survey.getCompletionDate()));
            if (row.findViewById(R.id.second_column) instanceof DoubleRectChart) {
                DoubleRectChart mDoubleRectChart = (DoubleRectChart) row.findViewById(
                        R.id.second_column);
                LayoutUtils.drawScore(survey.getMainScore(), mDoubleRectChart);
            } else {
                TextView score = (TextView) row.findViewById(R.id.second_column);
                score.setText(survey.getMainScore() + "");
                Resources resources = PreferencesState.getInstance().getContext().getResources();

                ScoreType scoreType = new ScoreType(survey.getMainScore());
                if (scoreType.isTypeA()) {
                    score.setBackgroundColor(resources.getColor(R.color.lightGreen));
                } else if (scoreType.isTypeB()) {
                    score.setBackgroundColor(resources.getColor(R.color.assess_yellow));
                } else if (scoreType.isTypeC()) {
                    score.setBackgroundColor(resources.getColor(R.color.darkRed));
                }
            }

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new UIThreadExecutor().run(new Runnable() {
                        @Override
                        public void run() {
                            DashboardActivity.dashboardActivity.openFeedback(survey, false);
                        }
                    });
                    dismiss();
                }
            });
            linearLayout.addView(row);
        }
    }
}
