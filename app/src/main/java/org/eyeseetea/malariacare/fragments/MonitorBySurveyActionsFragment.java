package org.eyeseetea.malariacare.fragments;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.usecase.GetOrgUnitsUseCase;
import org.eyeseetea.malariacare.domain.usecase.GetProgramsUseCase;
import org.eyeseetea.malariacare.domain.usecase.GetSentObservationsUseCase;
import org.eyeseetea.malariacare.domain.usecase.GetServerMetadataUseCase;
import org.eyeseetea.malariacare.domain.usecase.GetSurveysUseCase;
import org.eyeseetea.malariacare.factories.DataFactory;
import org.eyeseetea.malariacare.factories.MetadataFactory;
import org.eyeseetea.malariacare.layout.adapters.monitor.MonitorBySurveyActionsAdapter;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.presentation.presenters.monitoring.MonitorBySurveyActionsPresenter;
import org.eyeseetea.malariacare.presentation.viewmodels.SurveyViewModel;

import java.util.List;

public class MonitorBySurveyActionsFragment extends FiltersFragment implements
        MonitorBySurveyActionsPresenter.View {
    public static final String TAG = ".PlannedFragment";

    private View rootView;
    private RecyclerView surveysByActionsView;
    private ProgressBar progressView;
    private MonitorBySurveyActionsAdapter adapter;
    private MonitorBySurveyActionsPresenter presenter;

    private ImageView backButton;

    public static MonitorBySurveyActionsFragment newInstance(){
        return new MonitorBySurveyActionsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_monitor_by_survey_actions, container, false);

        hideBackButton();
        initializeRecyclerView();
        initializeProgressView();
        initializePresenter();

        return rootView;
    }

    private void hideBackButton() {
        if (backButton == null){
            backButton = getActivity().findViewById(R.id.back_to_monitoring_by_actions_view);
        }

        backButton.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    @Override
    protected void onFiltersChanged() {
        presenter.refresh (selectedProgramUidFilter, selectedOrgUnitUidFilter);
    }


    @Override
    public void reloadData() {
        super.reloadData();
        hideBackButton();
        presenter.refresh (selectedProgramUidFilter, selectedOrgUnitUidFilter);
    }


    @Override
    public void showSurveysByActions(List<SurveyViewModel> incompleteSurveys,
            List<SurveyViewModel> completeSurveys) {
        adapter.setSurveys(incompleteSurveys, completeSurveys);
    }

    @Override
    public void showLoading() {
        progressView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        progressView.setVisibility(View.GONE);
    }

    private void initializeProgressView() {
        progressView = rootView.findViewById(R.id.progress_view);
    }

    private void initializeRecyclerView() {
        surveysByActionsView = rootView.findViewById(R.id.surveys_by_action_view);

        adapter = new MonitorBySurveyActionsAdapter(getActivity());

        surveysByActionsView.setAdapter(adapter);
    }

    private void initializePresenter() {
        IAsyncExecutor asyncExecutor = new AsyncExecutor();
        IMainExecutor mainExecutor = new UIThreadExecutor();

        GetProgramsUseCase getProgramsUseCase =
                MetadataFactory.INSTANCE.provideProgramsUseCase();

        GetOrgUnitsUseCase getOrgUnitsUseCase =
                MetadataFactory.INSTANCE.provideOrgUnitsUseCase();

        GetServerMetadataUseCase getServerMetadataUseCase =
                MetadataFactory.INSTANCE.provideServerMetadataUseCase(getActivity());

        GetSentObservationsUseCase getSentObservationsUseCase =
                DataFactory.INSTANCE.provideSentObservationsUseCase();

        GetSurveysUseCase getSurveysUseCase = DataFactory.INSTANCE.provideSurveysUseCase();

        presenter = new MonitorBySurveyActionsPresenter(asyncExecutor, mainExecutor,
                getProgramsUseCase, getOrgUnitsUseCase, getServerMetadataUseCase,
                getSentObservationsUseCase, getSurveysUseCase);

        presenter.attachView(this, selectedProgramUidFilter, selectedOrgUnitUidFilter);
    }
}