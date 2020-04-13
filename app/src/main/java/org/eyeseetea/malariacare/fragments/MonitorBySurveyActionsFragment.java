package org.eyeseetea.malariacare.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import org.eyeseetea.malariacare.presentation.views.MonitorActionsDialogFragment;

import java.util.List;

import kotlin.Unit;

public class MonitorBySurveyActionsFragment extends FiltersFragment implements
        MonitorBySurveyActionsPresenter.View {
    public static final String TAG = ".PlannedFragment";

    private View rootView;
    private RecyclerView surveysByActionsView;
    private ProgressBar progressView;
    private MonitorBySurveyActionsAdapter adapter;
    private MonitorBySurveyActionsPresenter presenter;

    public static MonitorBySurveyActionsFragment newInstance() {
        return new MonitorBySurveyActionsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_monitor_by_survey_actions, container, false);

        initializeRecyclerView();
        initializeProgressView();
        initializePresenter();

        return rootView;
    }

    @Override
    public void onPause() {
        presenter.detachView();
        super.onPause();
    }

    @Override
    protected void onFiltersChanged() {
        if (existFilter()) {
            DashboardActivity.dashboardActivity.openMonitoringByCalendar();
        }
    }

    private boolean existFilter() {
        return !selectedProgramUidFilter.isEmpty() || !selectedOrgUnitUidFilter.isEmpty();
    }


    @Override
    public void reloadData() {
        super.reloadData();

        if (presenter != null && !existFilter()) {
            presenter.refresh(selectedProgramUidFilter, selectedOrgUnitUidFilter);
        }
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

        adapter.setOnItemMenuClickListener(
                surveyViewModel -> openSurveyActionsDialog(surveyViewModel));

        surveysByActionsView.setAdapter(adapter);
    }

    private void openSurveyActionsDialog(SurveyViewModel surveyViewModel) {
        FragmentManager fm = getActivity().getSupportFragmentManager();

        MonitorActionsDialogFragment monitorActionsDialogFragment =
                MonitorActionsDialogFragment.newInstance(surveyViewModel.getSurveyUid());

        monitorActionsDialogFragment.setOnActionsSaved(() -> {
            presenter.refresh(selectedProgramUidFilter, selectedOrgUnitUidFilter);
            return Unit.INSTANCE;
        });

        monitorActionsDialogFragment.show(fm, MonitorActionsDialogFragment.class.getSimpleName());
    }

    private void initializePresenter() {
        IAsyncExecutor asyncExecutor = new AsyncExecutor();
        IMainExecutor mainExecutor = new UIThreadExecutor();

        GetProgramsUseCase getProgramsUseCase =
                MetadataFactory.INSTANCE.provideGetProgramsUseCase();

        GetOrgUnitsUseCase getOrgUnitsUseCase =
                MetadataFactory.INSTANCE.provideGetOrgUnitsUseCase();

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