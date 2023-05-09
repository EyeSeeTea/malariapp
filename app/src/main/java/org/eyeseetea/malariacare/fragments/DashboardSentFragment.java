/*
 * Copyright (c) 2015.
 *
 * This file is part of Health Network QIS App.
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

package org.eyeseetea.malariacare.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;
import org.eyeseetea.malariacare.domain.entity.SurveyStatusFilter;
import org.eyeseetea.malariacare.factories.DataFactory;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AssessmentSentAdapter;
import org.eyeseetea.malariacare.presentation.presenters.surveys.SurveysPresenter;
import org.eyeseetea.malariacare.presentation.viewmodels.SurveyViewModel;
import org.eyeseetea.malariacare.views.CustomRadioButton;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardSentFragment extends FiltersFragment implements IModuleFragment, SurveysPresenter.View {
    public static final String TAG = ".SentFragment";
    private final static int WITHOUT_ORDER = 0;
    private final static int FACILITY_ORDER = 1;
    private final static int DATE_ORDER = 2;
    private final static int COMPETENCY_ORDER = 3;
    private static int LAST_ORDER = WITHOUT_ORDER;

    private SurveysPresenter surveysPresenter;

    protected AssessmentSentAdapter adapter;
    //surveys contains all the surveys without filter
    private List<SurveyViewModel> surveys;
    //oneSurveyForOrgUnit contains the filtered orgunit list
    List<SurveyViewModel> oneSurveyForOrgUnit;

    Map<String, SurveyViewModel> surveysMap = new HashMap<>();


    //orderBy contains the selected order
    int orderBy = WITHOUT_ORDER;
    //reverse contains the selected order asc or desc
    static boolean reverse = false;

    boolean forceAllSurveys;

    CustomRadioButton customRadioButton;
    TextView noSurveysText;

    private RecyclerView recyclerView;
    private View rootView;

    public void toggleForceAllSurveys() {
        this.forceAllSurveys = !this.forceAllSurveys;
    }

    public boolean isForceAllSurveys() {
        return forceAllSurveys;
    }

    private static String SERVER_CLASSIFICATION = "ServerClassification";
    private ServerClassification serverClassification;

    public static DashboardSentFragment newInstance(ServerClassification serverClassification) {
        DashboardSentFragment fragment = new DashboardSentFragment();

        Bundle args = new Bundle();
        args.putInt(SERVER_CLASSIFICATION, serverClassification.getCode());
        fragment.setArguments(args);

        return fragment;
    }

    public DashboardSentFragment() {
        oneSurveyForOrgUnit = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onFiltersChanged() {
        reloadData();
    }

    @Override
    protected OrgUnitProgramFilterView.FilterType getFilterType() {
        return OrgUnitProgramFilterView.FilterType.NON_EXCLUSIVE;
    }

    @Override
    protected int getOrgUnitProgramFilterViewId() {
        return R.id.improve_org_unit_program_filter_view;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        serverClassification = ServerClassification.Companion.get(
                getArguments().getInt(SERVER_CLASSIFICATION));

        rootView = inflater.inflate(R.layout.improve_listview, null);
        noSurveysText = rootView.findViewById(R.id.no_surveys_improve);

        initComponents();
        initRecyclerView();
        initPresenter();

        return rootView;
    }

    private void initPresenter() {
        surveysPresenter = DataFactory.INSTANCE.provideSurveysPresenter();

        surveysPresenter.attachView(this, SurveyStatusFilter.SENT, getSelectedProgramUidFilter(), getSelectedOrgUnitUidFilter());
    }


    private void initComponents() {
        customRadioButton = (CustomRadioButton) rootView.findViewById(
                R.id.check_show_all_surveys);
        forceAllSurveys = false;
        PreferencesState.getInstance().setForceAllSentSurveys(forceAllSurveys);
        customRadioButton.setChecked(true);
        customRadioButton.setOnClickListener(v -> {
                    toggleForceAllSurveys();
                    PreferencesState.getInstance().setForceAllSentSurveys(isForceAllSurveys());
                    ((CustomRadioButton) v).setChecked(!isForceAllSurveys());
                    reloadData();
                }
        );
    }

    private void initRecyclerView() {
        recyclerView = rootView.findViewById(R.id.sentSurveyList);

        TextView classificationHeader = rootView.findViewById(R.id.scoreHeader);

        if (serverClassification == ServerClassification.COMPETENCIES) {
            classificationHeader.setText(getActivity().getString(R.string.competency_title));
        } else {
            classificationHeader.setText(
                    getActivity().getString(R.string.dashboard_title_planned_quality_of_care));
        }

        adapter = new AssessmentSentAdapter(serverClassification);
        recyclerView.setAdapter(adapter);
        initFilterOrder();
    }

    public void setCompetencyOrder() {
        orderBy = COMPETENCY_ORDER;
        reloadSentSurveys();
    }

    public void setFacilityOrder() {
        orderBy = FACILITY_ORDER;
        reloadSentSurveys();
    }

    public void setDateOrder() {
        orderBy = DATE_ORDER;
        reloadSentSurveys();
    }

    @Override
    public void onDestroy() {
        surveysPresenter.detachView();

        super.onDestroy();
    }

    //Adds the clicklistener to the header CustomTextView.
    private void initFilterOrder() {

        CustomTextView statusHeader = rootView.findViewById(R.id.statusHeader);
        statusHeader.setOnClickListener(v -> setDateOrder());

        CustomTextView scoreHeader = rootView.findViewById(R.id.scoreHeader);
        scoreHeader.setOnClickListener(v -> setCompetencyOrder());

        CustomTextView facilityHeader = rootView.findViewById(R.id.idHeader);
        facilityHeader.setOnClickListener(v -> setFacilityOrder());
    }

    public void refreshScreen(List<SurveyViewModel> newListSurveys) {
        Log.d(TAG, "refreshScreen (Thread: " + Thread.currentThread().getId() + "): "
                + newListSurveys.size());
        if (!this.isAdded()) {
            return;
        }

        if (adapter != null) {
            adapter.setSurveys(newListSurveys);

            if (newListSurveys.isEmpty()) {
                noSurveysText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                noSurveysText.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void reloadData() {
        super.reloadData();

        if (surveysPresenter != null) {
            surveysPresenter.refresh(getSelectedProgramUidFilter(), getSelectedOrgUnitUidFilter());
        }
    }

    /**
     * filter the surveys for last survey in org unit, and set surveysForGraphic for the statistics
     */
    private void reloadSentSurveys() {
        // To prevent from reloading too fast, before service has finished its job
        if (surveys == null || surveys.size() == 0){
            refreshScreen(new ArrayList<>());
        };

        HashMap<String, SurveyViewModel> orgUnits = new HashMap<>();

        oneSurveyForOrgUnit = new ArrayList<>();
        surveysMap = new HashMap<>();
        if (PreferencesState.getInstance().isNoneFilter()) {
            for (SurveyViewModel survey : surveys) {
                oneSurveyForOrgUnit.add(survey);
            }
        } else if (isForceAllSurveys()) {
            for (SurveyViewModel survey : surveys) {

                oneSurveyForOrgUnit.add(survey);

            }
        } else if (PreferencesState.getInstance().isLastForOrgUnit()) {
            for (SurveyViewModel survey : surveys) {
                if (survey.getOrgUnit() != null && survey.getProgram() != null) {
                    if (!surveysMap.containsKey(survey.getProgram() + "-" +
                            survey.getOrgUnit())) {
                        addSurveyIfIsLast(survey);
                    } else {
                        SurveyViewModel surveyMapped = surveysMap.get(survey.getProgram() + "-" +
                                survey.getOrgUnit());

                        if ((surveyMapped.getDate() != null
                                && survey.getDate() != null)
                                && surveyMapped.getDate().before(
                                survey.getDate())) {
                            addSurveyIfIsLast(survey);
                        }
                    }
                }
            }
            oneSurveyForOrgUnit = new ArrayList<>(surveysMap.values());
        }

        //Order the surveys, and reverse if is needed, taking the last order from LAST_ORDER
        if (orderBy != WITHOUT_ORDER) {
            reverse = false;
            if (orderBy == LAST_ORDER) {
                reverse = true;
            }

            Collections.sort(oneSurveyForOrgUnit, new Comparator<SurveyViewModel>() {
                public int compare(SurveyViewModel survey1, SurveyViewModel survey2) {
                    int compare;
                    switch (orderBy) {
                        case FACILITY_ORDER:
                            String surveyA = survey1.getOrgUnit();
                            String surveyB = survey2.getOrgUnit();
                            compare = surveyA.compareTo(surveyB);
                            break;
                        case COMPETENCY_ORDER:
                            CompetencyScoreClassification classification1 = survey1.getCompetency();
                            CompetencyScoreClassification classification2 = survey2.getCompetency();

                            compare = classification1.toString().compareTo(
                                    classification2.toString());
                            break;
                        default:
                            //By Date
                            compare = survey1.getDate().compareTo(survey2.getDate());
                            break;
                    }

                    if (reverse) {
                        return (compare * -1);
                    }
                    return compare;
                }
            });
        }
        if (reverse) {
            LAST_ORDER = WITHOUT_ORDER;
        } else {
            LAST_ORDER = orderBy;
        }
        refreshScreen(oneSurveyForOrgUnit);
    }

    private void addSurveyIfIsLast(SurveyViewModel survey) {

        SurveyViewModel previousSurvey = surveysMap.get(survey.getProgram() + "-" +
                survey.getOrgUnit());
        if (previousSurvey == null || previousSurvey.getDate().compareTo(
                survey.getDate()) < 0) {
            surveysMap.put(survey.getProgram() + "-" + survey.getOrgUnit(), survey);
        }
    }

    @Override
    public void showSurveys(@NonNull List<SurveyViewModel> surveys) {
        this.surveys = surveys;
        reloadSentSurveys();
    }


    @Override
    public void showNetworkError() {
        Log.e(this.getClass().getSimpleName(), "Network Error");
    }
}