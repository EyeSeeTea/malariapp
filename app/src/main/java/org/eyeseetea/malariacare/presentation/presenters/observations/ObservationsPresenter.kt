package org.eyeseetea.malariacare.presentation.presenters.observations

import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB
import org.eyeseetea.malariacare.data.database.model.QuestionDB
import org.eyeseetea.malariacare.data.database.model.SurveyDB
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification
import org.eyeseetea.malariacare.domain.entity.NextScheduleDateConfiguration
import org.eyeseetea.malariacare.domain.entity.Observation
import org.eyeseetea.malariacare.domain.entity.ObservationStatus
import org.eyeseetea.malariacare.domain.entity.ServerClassification
import org.eyeseetea.malariacare.domain.entity.ServerMetadata
import org.eyeseetea.malariacare.domain.exception.InvalidServerMetadataException
import org.eyeseetea.malariacare.domain.exception.ObservationNotFoundException
import org.eyeseetea.malariacare.domain.service.SurveyNextScheduleDomainService
import org.eyeseetea.malariacare.domain.usecase.GetObservationBySurveyUidUseCase
import org.eyeseetea.malariacare.domain.usecase.GetServerMetadataUseCase
import org.eyeseetea.malariacare.domain.usecase.SaveObservationUseCase
import org.eyeseetea.malariacare.observables.ObservablePush
import org.eyeseetea.malariacare.presentation.boundary.Executor
import org.eyeseetea.malariacare.presentation.mapper.observations.MissedStepMapper
import org.eyeseetea.malariacare.presentation.mapper.observations.ObservationMapper
import org.eyeseetea.malariacare.presentation.viewmodels.observations.ActionViewModel
import org.eyeseetea.malariacare.presentation.viewmodels.observations.MissedStepViewModel
import org.eyeseetea.malariacare.presentation.viewmodels.observations.ObservationViewModel
import org.eyeseetea.malariacare.utils.Constants
import org.eyeseetea.malariacare.utils.DateParser
import java.util.ArrayList

class ObservationsPresenter(
    private val executor: Executor,
    private val getObservationBySurveyUidUseCase: GetObservationBySurveyUidUseCase,
    private val getServerMetadataUseCase: GetServerMetadataUseCase,
    private val saveObservationUseCase: SaveObservationUseCase,
    private val serverClassification: ServerClassification
) {
    private var view: View? = null

    private lateinit var survey: SurveyDB
    private lateinit var serverMetadata: ServerMetadata
    private lateinit var surveyUid: String
    private lateinit var observationViewModel: ObservationViewModel
    private var formattedNextScheduleDate: String = "NaN"

    private var missedCriticalSteps: List<MissedStepViewModel> = mutableListOf()
    private var missedNonCriticalSteps: List<MissedStepViewModel> = mutableListOf()

    init {
        ObservablePush.getInstance().addObserver { observable, o ->
            if (view != null) {
                refreshObservation()
            }
        }
    }

    fun attachView(view: View, surveyUid: String) {
        this.view = view
        this.surveyUid = surveyUid

        observationViewModel = ObservationViewModel(surveyUid)

        loadData()
    }

    fun detachView() {
        this.view = null
    }

    private fun loadData() = executor.asyncExecute {
        try {
            serverMetadata = getServerMetadataUseCase.execute()
            loadObservation()
        } catch (e: InvalidServerMetadataException) {
            showInvalidServerMetadataErrorMessage()

            println(
                "InvalidServerMetadataException has occur retrieving server metadata: " + e.message
            )
        } catch (e: Exception) {
            println(
                "An error has occur retrieving the data: " + e.message
            )
        }
    }

    private fun loadObservation() {
        try {
            val observation = getObservationBySurveyUidUseCase.execute(surveyUid)

            observationViewModel = ObservationMapper.mapToViewModel(observation, serverMetadata)

            loadSurvey()
            loadMissedSteps()
            updateStatus()
            showObservation()
        } catch (exception: ObservationNotFoundException) {
            observationViewModel = ObservationViewModel(surveyUid)
            saveObservation()

            loadSurvey()
            loadMissedSteps()
            updateStatus()
            showObservation()
        } catch (e: Exception) {
            println(
                "An error has occur retrieving observation: " + e.message
            )
        }
    }

    private fun loadSurvey() {
        survey = SurveyDB.getSurveyByUId(surveyUid)

        val dateParser = DateParser()
        var formattedCompletionDate = "NaN"
        if (survey.completionDate != null) {
            formattedCompletionDate = dateParser.format(
                survey.completionDate,
                DateParser.EUROPEAN_DATE_FORMAT
            )
        }

        if (survey != null) {
            val eventDate = survey.getCompletionDate()

            val competencyScoreClassification =
                CompetencyScoreClassification.get(survey.competencyScoreClassification!!)

            val nextScheduleDateConfiguration =
                NextScheduleDateConfiguration(survey.program!!.nextScheduleDeltaMatrix)

            val surveyNextScheduleDomainService = SurveyNextScheduleDomainService()

            val nextScheduleDate = surveyNextScheduleDomainService.calculate(
                nextScheduleDateConfiguration,
                eventDate,
                competencyScoreClassification,
                survey.isLowProductivity,
                survey.mainScoreValue,
                serverClassification
            )

            formattedNextScheduleDate = dateParser.format(
                nextScheduleDate,
                DateParser.EUROPEAN_DATE_FORMAT
            )
        }

        val classification = CompetencyScoreClassification.get(
            survey.competencyScoreClassification
        )

        showHeaderInfo(formattedCompletionDate, formattedNextScheduleDate, classification)
    }

    private fun loadMissedSteps() {
        val criticalQuestions = QuestionDB.getFailedQuestions(
            survey.id_survey, true
        )

        val compositeScoresOfCriticalFailedQuestions = getValidTreeOfCompositeScores(true)

        missedCriticalSteps = MissedStepMapper.mapToViewModel(
            criticalQuestions,
            compositeScoresOfCriticalFailedQuestions
        )

        val nonCriticalQuestions = QuestionDB.getFailedQuestions(
            survey.id_survey, false
        )

        val compositeScoresOfNonCriticalFailedQuestions = getValidTreeOfCompositeScores(false)

        missedNonCriticalSteps = MissedStepMapper.mapToViewModel(
            nonCriticalQuestions,
            compositeScoresOfNonCriticalFailedQuestions
        )
    }

    fun providerChanged(provider: String) {
        if (provider != observationViewModel.provider) {
            observationViewModel.provider = provider
            saveObservation()
        }
    }

    private fun saveObservation() = executor.asyncExecute {
        try {
            val observation =
                ObservationMapper.mapToObservation(observationViewModel, serverMetadata)

            saveObservationUseCase.execute(observation)
        } catch (e: Exception) {
            println(
                "An error has occur saving Observation: " + e.message
            )
        }
    }

    fun completeObservation() {
        if (observationViewModel.isValid) {
            observationViewModel.status = ObservationStatus.COMPLETED

            saveObservation()
            updateStatus()
        } else {
            if (view != null) {
                view!!.showInvalidObservationErrorMessage()
            }
        }
    }

    fun onAction1Changed(actionViewModel: ActionViewModel) {
        observationViewModel.action1 = actionViewModel
        saveObservation()
    }

    fun onAction2Changed(actionViewModel: ActionViewModel) {
        observationViewModel.action2 = actionViewModel
        saveObservation()
    }

    fun onAction3Changed(actionViewModel: ActionViewModel) {
        observationViewModel.action3 = actionViewModel
        saveObservation()
    }

    private fun getValidTreeOfCompositeScores(critical: Boolean): List<CompositeScoreDB> {
        val compositeScoreList = QuestionDB.getCompositeScoreOfFailedQuestions(
            survey.id_survey!!, critical
        )

        val compositeScoresTree = ArrayList<CompositeScoreDB>()
        for (compositeScore in compositeScoreList) {
            buildCompositeScoreTree(compositeScore, compositeScoresTree)
        }

        compositeScoresTree.sortBy { compositeScoreDB -> compositeScoreDB.order_pos }

        return compositeScoresTree
    }

    // Recursive compositescore parent builder
    private fun buildCompositeScoreTree(
        compositeScore: CompositeScoreDB,
        compositeScoresTree: MutableList<CompositeScoreDB>
    ) {
        if (compositeScore.hierarchical_code == "0") {
            // ignore composite score root
            return
        }
        if (!compositeScoresTree.contains(compositeScore)) {
            compositeScoresTree.add(compositeScore)
        }
        if (compositeScore.hasParent()) {
            buildCompositeScoreTree(compositeScore.composite_score!!, compositeScoresTree)
        }
    }

    private fun refreshObservation() {
        var observation: Observation
        try {
            executor.asyncExecute {
                observation = getObservationBySurveyUidUseCase.execute(surveyUid)

                observationViewModel = ObservationMapper.mapToViewModel(observation, serverMetadata)

                updateStatus()
            }
        } catch (e: Exception) {
            println("An error has occur refreshing observation: " + e.message)
        }
    }

    private fun updateStatus() = executor.uiExecute {
        view?.let { view ->

            view.updateStatusView(observationViewModel.status)

            when (observationViewModel.status) {
                ObservationStatus.COMPLETED, ObservationStatus.SENT -> {
                    view.enableShareButton()
                    view.changeToReadOnlyMode()
                }

                ObservationStatus.IN_PROGRESS -> view.disableShareButton()
            }
        }
    }

    fun shareObsActionPlan() = executor.uiExecute {
        view?.let { view ->

            if (survey.status != Constants.SURVEY_SENT) {
                view.shareNotSent()
            } else {
                view.shareByText(
                    observationViewModel,
                    survey,
                    formattedNextScheduleDate,
                    missedCriticalSteps,
                    missedNonCriticalSteps
                )
            }
        }
    }

    private fun showHeaderInfo(
        formattedCompletionDate: String,
        formattedNextDate: String,
        classification: CompetencyScoreClassification
    ) = executor.uiExecute {
        view?.showHeaderInfo(
            survey.orgUnit!!.name, survey.mainScoreValue,
            formattedCompletionDate, formattedNextDate, classification
        )
    }

    private fun showObservation() = executor.uiExecute {
        view?.let { view ->
            if (missedCriticalSteps.isNotEmpty()) {
                view.showMissedCriticalSteps(missedCriticalSteps)
            } else {
                view.showNoCriticalStepsMissedText()
            }

            if (missedNonCriticalSteps.isNotEmpty()) {
                view.showMissedNonCriticalSteps(missedNonCriticalSteps)
            } else {
                view.showNoNonCriticalStepsMissedText()
            }

            view.showProvider(observationViewModel.provider)
            view.showAction1(observationViewModel.action1)
            view.showAction2(observationViewModel.action2)
            view.showAction3(observationViewModel.action3)
        }
    }

    private fun showInvalidServerMetadataErrorMessage() = executor.uiExecute {
        view?.let { view ->
            view.showInvalidServerMetadataErrorMessage()
            view.changeToReadOnlyMode()
        }
    }

    interface View {
        fun showHeaderInfo(
            orgUnitName: String,
            mainScore: Float?,
            completionDate: String,
            nextDate: String?,
            classification: CompetencyScoreClassification
        )

        fun showMissedCriticalSteps(missedCriticalSteps: List<@JvmSuppressWildcards MissedStepViewModel>)
        fun showMissedNonCriticalSteps(missedNonCriticalSteps: List<@JvmSuppressWildcards MissedStepViewModel>)
        fun showNoCriticalStepsMissedText()
        fun showNoNonCriticalStepsMissedText()
        fun showProvider(provider: String)
        fun showAction1(action1: ActionViewModel)
        fun showAction2(action2: ActionViewModel)
        fun showAction3(action3: ActionViewModel)

        fun updateStatusView(status: ObservationStatus)

        fun changeToReadOnlyMode()

        fun shareByText(
            observationViewModel: ObservationViewModel?,
            survey: SurveyDB,
            formattedNextScheduleDate: String,
            missedCriticalStepViewModels: List<@JvmSuppressWildcards MissedStepViewModel>?,
            missedNonCriticalStepViewModels: List<@JvmSuppressWildcards MissedStepViewModel>?
        )

        fun shareNotSent()

        fun enableShareButton()

        fun disableShareButton()

        fun showInvalidObservationErrorMessage()
        fun showInvalidServerMetadataErrorMessage()
    }
}
