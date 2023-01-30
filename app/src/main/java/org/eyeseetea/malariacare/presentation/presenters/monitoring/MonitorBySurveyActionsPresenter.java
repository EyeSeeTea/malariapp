package org.eyeseetea.malariacare.presentation.presenters.monitoring;

import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.common.Either;
import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationValue;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;
import org.eyeseetea.malariacare.domain.entity.Program;
import org.eyeseetea.malariacare.domain.entity.Server;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.usecase.GetSentObservationsUseCase;
import org.eyeseetea.malariacare.domain.usecase.GetOrgUnitsUseCase;
import org.eyeseetea.malariacare.domain.usecase.GetProgramsUseCase;
import org.eyeseetea.malariacare.domain.usecase.GetServerFailure;
import org.eyeseetea.malariacare.domain.usecase.GetServerMetadataUseCase;
import org.eyeseetea.malariacare.domain.usecase.GetServerUseCase;
import org.eyeseetea.malariacare.domain.usecase.GetSurveysUseCase;
import org.eyeseetea.malariacare.presentation.viewmodels.SurveyViewModel;
import org.eyeseetea.malariacare.utils.AUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitorBySurveyActionsPresenter {
    private View view;

    private final GetProgramsUseCase getProgramsUseCase;
    private final GetOrgUnitsUseCase getOrgUnitsUseCase;
    private final GetServerMetadataUseCase getServerMetadataUseCase;
    private final GetSentObservationsUseCase getSentObservationsUseCase;
    private final GetSurveysUseCase getSurveysUseCase;
    private final IAsyncExecutor asyncExecutor;
    private final IMainExecutor mainExecutor;

    private Map<String, Program> programsMap = new HashMap<>();
    private Map<String, OrgUnit> orgUnitsMap = new HashMap<>();
    private Map<String, Observation> observationsMap = new HashMap<>();
    private List<Survey> surveys;
    private ServerMetadata serverMetadata;

    private String programUid;
    private String orgUnitUid;

    public MonitorBySurveyActionsPresenter(
            IAsyncExecutor asyncExecutor,
            IMainExecutor mainExecutor,
            GetProgramsUseCase getProgramsUseCase,
            GetOrgUnitsUseCase getOrgUnitsUseCase,
            GetServerMetadataUseCase getServerMetadataUseCase,
            GetSentObservationsUseCase getSentObservationsUseCase,
            GetSurveysUseCase getSurveysUseCase) {

        this.asyncExecutor = asyncExecutor;
        this.mainExecutor = mainExecutor;
        this.getProgramsUseCase = getProgramsUseCase;
        this.getOrgUnitsUseCase = getOrgUnitsUseCase;
        this.getServerMetadataUseCase = getServerMetadataUseCase;
        this.getSentObservationsUseCase = getSentObservationsUseCase;
        this.getSurveysUseCase = getSurveysUseCase;
    }

    public void attachView(View view, String programUid, String orgUnitUid) {
        this.view = view;

        this.programUid = programUid;
        this.orgUnitUid = orgUnitUid;

        loadAll();
    }

    public void detachView() {
        view = null;
    }

    public void refresh(String programUid, String orgUnitUid) {

        this.programUid = programUid;
        this.orgUnitUid = orgUnitUid;

        asyncExecutor.run(() -> {
            showLoading();
            loadData();
            hideLoading();
        });
    }

    private void loadAll() {
        asyncExecutor.run(() -> {
            showLoading();
            loadMetadata();
            loadData();
            hideLoading();
        });
    }


    private void loadMetadata() {
        try {

            loadPrograms();
            loadOrgUnits();
            serverMetadata = getServerMetadataUseCase.execute();
        } catch (Exception e) {
            showLoadingErrorMessage(e.getMessage());
        }
    }

    private void loadData() {
        List<SurveyViewModel> incompleteSurveys = new ArrayList<>();
        List<SurveyViewModel> completeSurveys = new ArrayList<>();

        try {
            loadSentObservations();
            loadSurveysOfSentObservations();

            for (Survey survey : surveys) {
                SurveyViewModel surveyViewModel = mapToViewModel(survey);

                if (hasAllObservationActionsCompleted(survey)) {
                    completeSurveys.add(surveyViewModel);
                } else {
                    incompleteSurveys.add(surveyViewModel);
                }
            }

            showSurveysByActions(incompleteSurveys, completeSurveys);
        } catch (Exception e) {
            showLoadingErrorMessage(e.getMessage());
        }
    }

    private SurveyViewModel mapToViewModel(Survey survey) {
        String programName = "";
        String orgUnitName = "";

        Program program = programsMap.get(survey.getProgramUId());

        if (program != null) {
            programName = program.getName();
        }

        OrgUnit orgUnit = orgUnitsMap.get(survey.getOrgUnitUId());

        if (program != null) {
            orgUnitName = orgUnit.getName();
        }

        String qualityOfCare = "-";

        if (survey.getScore() != null) {
            qualityOfCare = AUtils.round(survey.getScore().getScore(), 2) + " %";
        }

        return new SurveyViewModel(survey.getSurveyUid(), programName, orgUnitName,
                survey.getCompletionDate(), survey.getCompetency(), qualityOfCare);
    }

    private boolean hasAllObservationActionsCompleted(Survey survey) {
        Observation observation = observationsMap.get(survey.getUId());

        if (observation != null) {
            boolean action1Exists = false;
            boolean action1IsCompleted = false;
            boolean action2Exists = false;
            boolean action2IsCompleted = false;
            boolean action3Exists = false;
            boolean action3IsCompleted = false;

            for (ObservationValue observationValue : observation.getValues()) {
                if (observationValue.getObservationValueUid().equals(
                        serverMetadata.getAction1().getUId())) {
                    action1Exists = true;
                } else if (observationValue.getObservationValueUid().equals(
                        serverMetadata.getCompletionDateAction1().getUId())) {
                    action1IsCompleted = true;
                } else if (observationValue.getObservationValueUid().equals(
                        serverMetadata.getAction2().getUId())) {
                    action2Exists = true;
                } else if (observationValue.getObservationValueUid().equals(
                        serverMetadata.getCompletionDateAction2().getUId())) {
                    action2IsCompleted = true;
                } else if (observationValue.getObservationValueUid().equals(
                        serverMetadata.getAction3().getUId())) {
                    action3Exists = true;
                } else if (observationValue.getObservationValueUid().equals(
                        serverMetadata.getCompletionDateAction3().getUId())) {
                    action3IsCompleted = true;
                }
            }

            return (!action1Exists || action1IsCompleted) &&
                    (!action2Exists || action2IsCompleted) &&
                    (!action3Exists || action3IsCompleted);
        } else {
            throw new IllegalArgumentException(
                    "No exists observations for survey: " + survey.getUId());
        }
    }

    private void loadPrograms() throws Exception {
        List<Program> programs = getProgramsUseCase.execute();

        for (Program program : programs) {
            programsMap.put(program.getUid(), program);
        }
    }

    private void loadOrgUnits() throws Exception {
        List<OrgUnit> orgUnits = getOrgUnitsUseCase.execute();

        for (OrgUnit orgUnit : orgUnits) {
            orgUnitsMap.put(orgUnit.getUid(), orgUnit);
        }
    }

    private void loadSentObservations() throws Exception {
        observationsMap = new HashMap<>();
        List<Observation> observations =
                getSentObservationsUseCase.execute(programUid, orgUnitUid);

        for (Observation observation : observations) {
            observationsMap.put(observation.getSurveyUid(), observation);
        }
    }

    private void loadSurveysOfSentObservations() throws Exception {
        List<String> surveyUids = new ArrayList<>(observationsMap.keySet());

        surveys = getSurveysUseCase.execute(surveyUids);
    }

    private void showSurveysByActions(
            List<SurveyViewModel> incompleteSurveys,
            List<SurveyViewModel> completeSurveys) {
        if (view != null) {
            mainExecutor.run(() ->
                    view.showSurveysByActions(incompleteSurveys, completeSurveys));
        }
    }

    private void showLoadingErrorMessage(String message) {
        System.out.println("An error has occur loading monitor: " + message);
    }

    private void showLoading() {
        if (view != null) {
            mainExecutor.run(() -> view.showLoading());
        }
    }

    private void hideLoading() {
        if (view != null) {
            mainExecutor.run(() -> view.hideLoading());
        }
    }

    public interface View {
        void showSurveysByActions(
                List<SurveyViewModel> incompleteSurveys,
                List<SurveyViewModel> completeSurveys);

        void showLoading();

        void hideLoading();
    }
}
