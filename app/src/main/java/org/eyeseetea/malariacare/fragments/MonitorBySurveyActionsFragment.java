package org.eyeseetea.malariacare.fragments;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;
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
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

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

    private static String SERVER_CLASSIFICATION = "ServerClassification";
    private ServerClassification serverClassification;

    public static MonitorBySurveyActionsFragment newInstance(ServerClassification serverClassification) {
        MonitorBySurveyActionsFragment fragment = new MonitorBySurveyActionsFragment();

        Bundle args = new Bundle();
        args.putInt(SERVER_CLASSIFICATION, serverClassification.getCode());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_monitor_by_survey_actions, container, false);

        serverClassification = ServerClassification.Companion.get(
                getArguments().getInt(SERVER_CLASSIFICATION));

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

    @Override
    protected OrgUnitProgramFilterView.FilterType getFilterType() {
        return OrgUnitProgramFilterView.FilterType.EXCLUSIVE;
    }

    @Override
    protected int getOrgUnitProgramFilterViewId() {
        return R.id.monitor_org_unit_program_filter_view;
    }

    private boolean existFilter() {
        return !getSelectedProgramUidFilter().isEmpty() || !getSelectedOrgUnitUidFilter().isEmpty();
    }


    @Override
    public void reloadData() {
        super.reloadData();

        if (presenter != null && !existFilter()) {
            presenter.refresh(getSelectedProgramUidFilter(), getSelectedOrgUnitUidFilter());
        }
    }


    @Override
    public void showSurveysByActions(List<SurveyViewModel> incompleteSurveys,
            List<SurveyViewModel> completeSurveys) {
        adapter.setSurveys(incompleteSurveys, completeSurveys, serverClassification);
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
            presenter.refresh(getSelectedProgramUidFilter(), getSelectedOrgUnitUidFilter());
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

        presenter.attachView(this, getSelectedProgramUidFilter(), getSelectedOrgUnitUidFilter());
    }
}