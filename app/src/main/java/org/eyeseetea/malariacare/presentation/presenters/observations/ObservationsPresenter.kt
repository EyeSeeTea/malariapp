package org.eyeseetea.malariacare.presentation.presenters.observations

import android.content.Context

import org.eyeseetea.malariacare.R
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB
import org.eyeseetea.malariacare.data.database.model.QuestionDB
import org.eyeseetea.malariacare.data.database.model.SurveyDB
import org.eyeseetea.malariacare.data.database.utils.planning.SurveyPlanner
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification
import org.eyeseetea.malariacare.domain.entity.Observation
import org.eyeseetea.malariacare.domain.entity.ObservationStatus
import org.eyeseetea.malariacare.domain.entity.ServerMetadata
import org.eyeseetea.malariacare.domain.exception.InvalidServerMetadataException
import org.eyeseetea.malariacare.domain.exception.ObservationNotFoundException
import org.eyeseetea.malariacare.domain.usecase.GetObservationBySurveyUidUseCase
import org.eyeseetea.malariacare.domain.usecase.GetServerMetadataUseCase
import org.eyeseetea.malariacare.domain.usecase.SaveObservationUseCase
import org.eyeseetea.malariacare.observables.ObservablePush
import org.eyeseetea.malariacare.presentation.mapper.observations.MissedStepMapper
import org.eyeseetea.malariacare.presentation.mapper.observations.ObservationMapper
import org.eyeseetea.malariacare.presentation.viewmodels.observations.ActionViewModel
import org.eyeseetea.malariacare.presentation.viewmodels.observations.MissedStepViewModel
import org.eyeseetea.malariacare.presentation.viewmodels.observations.ObservationViewModel
import org.eyeseetea.malariacare.utils.DateParser
import org.eyeseetea.malariacare.utils.Constants

import java.util.ArrayList

class ObservationsPresenter(
    private val context: Context,
    private val getObservationBySurveyUidUseCase: GetObservationBySurveyUidUseCase,
    private val getServerMetadataUseCase: GetServerMetadataUseCase,
    private val saveObservationUseCase: SaveObservationUseCase
) {
    private var view: View? = null

    private lateinit var survey: SurveyDB
    private lateinit var serverMetadata: ServerMetadata
    private lateinit var surveyUid: String
    private lateinit var observationViewModel: ObservationViewModel

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

        loadData()
    }

    fun detachView() {
        this.view = null
    }

    private fun loadData() {
        try {
            val serverMetadata = getServerMetadataUseCase.execute()
            this@ObservationsPresenter.serverMetadata = serverMetadata
            loadObservation()
        } catch (e: InvalidServerMetadataException) {
            view?.let { view ->
                view.showInvalidServerMetadataErrorMessage()
                view.changeToReadOnlyMode()
            }

            println(
                "InvalidServerMetadataException has occur retrieving server metadata: " + e.message
            )
        } catch (e: Exception) {
            println(
                "An error has occur retrieving server metadata: " + e.message
            )
        }
    }

    private fun loadObservation() {
        try {
            val observation = getObservationBySurveyUidUseCase.execute(surveyUid!!)

            observationViewModel = ObservationMapper.mapToViewModel(observation, serverMetadata)

            loadSurvey()
            loadMissedSteps()
            updateStatus()
            showObservation()
        } catch (exception: ObservationNotFoundException) {
            observationViewModel = ObservationViewModel(surveyUid!!)
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

        if (view != null) {
            val dateParser = DateParser()
            var formattedCompletionDate = "NaN"
            if (survey.completionDate != null) {
                formattedCompletionDate = dateParser.format(
                    survey.completionDate,
                    DateParser.EUROPEAN_DATE_FORMAT
                )
            }

            var formattedNextDate = "NaN"

            formattedNextDate = dateParser.format(
                SurveyPlanner.getInstance().findScheduledDateBySurvey(survey),
                DateParser.EUROPEAN_DATE_FORMAT
            )

            val classification = CompetencyScoreClassification.get(
                survey.competencyScoreClassification
            )

            view?.renderHeaderInfo(
                survey.orgUnit!!.name, survey.mainScoreValue,
                formattedCompletionDate, formattedNextDate, classification
            )
        }
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

    private fun showObservation() {
        view?.let { view ->
            if (missedCriticalSteps.isNotEmpty()) {
                view.renderMissedCriticalSteps(missedCriticalSteps)
            } else {
                view.showNoCriticalStepsMissedText()
            }

            if (missedNonCriticalSteps.isNotEmpty()) {
                view.renderMissedNonCriticalSteps(missedNonCriticalSteps)
            } else {
                view.showNoNonCriticalStepsMissedText()
            }

            view.renderProvider(observationViewModel.provider)
            view.renderAction1(observationViewModel.action1)
            view.renderAction2(observationViewModel.action2)
            view.renderAction3(observationViewModel.action3)
        }
    }

    fun providerChanged(provider: String) {
        if (provider != observationViewModel.provider) {
            observationViewModel.provider = provider
            saveObservation()
        }
    }

    private fun saveObservation() {
        val observation =
            ObservationMapper.mapToObservation(observationViewModel, serverMetadata)

        try {
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

            if (view != null) {
                view!!.changeToReadOnlyMode()

                updateStatus()
            }
        } else {
            if (view != null) {
                view!!.showInvalidObservationErrorMessage()
            }
        }
    }

    private fun updateStatus() {
        if (view != null) {
            view!!.updateStatusView(observationViewModel.status)
        }

        when (observationViewModel.status) {
            ObservationStatus.COMPLETED, ObservationStatus.SENT -> {
                view!!.enableShareButton()
                view!!.changeToReadOnlyMode()
            }

            ObservationStatus.IN_PROGRESS -> view!!.disableShareButton()
        }
    }

    fun shareObsActionPlan() {
        if (view != null) {

            if (survey.status != Constants.SURVEY_SENT) {
                view!!.shareNotSent(context.getString(R.string.feedback_not_sent))
            } else {
                view!!.shareByText(
                    observationViewModel,
                    survey,
                    missedCriticalSteps,
                    missedNonCriticalSteps
                )
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
        val observation: Observation
        try {
            observation = getObservationBySurveyUidUseCase.execute(surveyUid)

            observationViewModel = ObservationMapper.mapToViewModel(observation, serverMetadata)

            updateStatus()
        } catch (e: Exception) {
            println("An error has occur refreshing observation: " + e.message)
        }
    }

    interface View {
        fun changeToReadOnlyMode()

        fun renderProvider(provider: String)

        fun renderMissedCriticalSteps(missedCriticalSteps: List<@JvmSuppressWildcards MissedStepViewModel>)

        fun renderMissedNonCriticalSteps(missedNonCriticalSteps: List<@JvmSuppressWildcards MissedStepViewModel>)

        fun renderHeaderInfo(
            orgUnitName: String,
            mainScore: Float?,
            completionDate: String,
            nextDate: String?,
            classification: CompetencyScoreClassification
        )

        fun updateStatusView(status: ObservationStatus)

        fun shareByText(
            observationViewModel: ObservationViewModel?,
            survey: SurveyDB,
            missedCriticalStepViewModels: List<@JvmSuppressWildcards MissedStepViewModel>?,
            missedNonCriticalStepViewModels: List<@JvmSuppressWildcards MissedStepViewModel>?
        )

        fun shareNotSent(surveyNoSentMessage: String)

        fun enableShareButton()

        fun disableShareButton()

        fun renderAction1(action1: ActionViewModel)
        fun renderAction2(action2: ActionViewModel)
        fun renderAction3(action3: ActionViewModel)

        fun showInvalidObservationErrorMessage()
        fun showInvalidServerMetadataErrorMessage()

        fun showNoCriticalStepsMissedText()

        fun showNoNonCriticalStepsMissedText()
    }
}
