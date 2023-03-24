package org.eyeseetea.malariacare.factories

import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource
import org.eyeseetea.malariacare.data.database.datasources.ObservationLocalDataSource
import org.eyeseetea.malariacare.data.database.datasources.SurveyLocalDataSource
import org.eyeseetea.malariacare.data.repositories.ObservationRepository
import org.eyeseetea.malariacare.data.repositories.SurveyRepository
import org.eyeseetea.malariacare.domain.boundary.repositories.IObservationRepository
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository
import org.eyeseetea.malariacare.domain.usecase.*
import org.eyeseetea.malariacare.presentation.executors.WrapperExecutor
import org.eyeseetea.malariacare.presentation.presenters.monitoring.MonitorPresenter
import org.eyeseetea.malariacare.presentation.presenters.surveys.*

object DataFactory {

    private var surveyLocalDataSource: ISurveyDataSource? = null
    private var observationLocalDataSource: ObservationLocalDataSource? = null

    fun provideCreateSurveyPresenter(): CreateSurveyPresenter =
        CreateSurveyPresenter(
            WrapperExecutor(),
            MetadataFactory.provideGetProgramsUseCase(),
            MetadataFactory.provideGetOrgUnitsUseCase(),
            MetadataFactory.provideGetOrgUnitLevelsUseCase()
        )

    fun provideMonitorPresenter(): MonitorPresenter =
        MonitorPresenter(
            WrapperExecutor(),
            this.provideSurveysUseCase(),
            MetadataFactory.provideGetProgramsUseCase(),
            MetadataFactory.provideGetOrgUnitsUseCase()
        )

    fun provideSurveysPresenter(): SurveysPresenter =
        SurveysPresenter(
            WrapperExecutor(),
            this.provideSurveysUseCase(),
            MetadataFactory.provideGetProgramsUseCase(),
            MetadataFactory.provideGetOrgUnitsUseCase()
        )

    fun providePlannedSurveysPresenter(): PlannedSurveysPresenter =
        PlannedSurveysPresenter(WrapperExecutor())

    fun provideSurveyPresenter(): SurveyPresenter =
        SurveyPresenter(WrapperExecutor())

    fun provideFeedbackPresenter(): FeedbackPresenter =
        FeedbackPresenter(WrapperExecutor())

    fun provideGetSurveyByUidUseCase(): GetSurveyByUidUseCase =
        GetSurveyByUidUseCase(provideSurveyRepository())

    fun provideSurveysByUIdsUseCase(): GetSurveysByUIdsUseCase =
        GetSurveysByUIdsUseCase(provideSurveyRepository())

    private fun provideSurveysUseCase(): GetSurveysUseCase =
        GetSurveysUseCase(provideSurveyRepository())

    fun provideGetObservationBySurveyUidUseCase(): GetObservationBySurveyUidUseCase =
        GetObservationBySurveyUidUseCase(provideObservationRepository())

    fun provideSaveObservationUseCase(): SaveObservationUseCase =
        SaveObservationUseCase(provideObservationRepository())

    fun provideSentObservationsUseCase(): GetSentObservationsUseCase =
        GetSentObservationsUseCase(provideObservationRepository())

    private fun provideSurveyRepository(): ISurveyRepository =
        SurveyRepository(provideSurveyLocalDataSource())

    fun reset() {
        // Reset data sources with cached metadata
        surveyLocalDataSource = null
        observationLocalDataSource = null
    }

    private fun provideObservationRepository(): IObservationRepository =
        ObservationRepository(provideObservationLocalDataSource())

    private fun provideSurveyLocalDataSource(): ISurveyDataSource {
        if (surveyLocalDataSource == null) {
            surveyLocalDataSource = SurveyLocalDataSource()
        }

        return surveyLocalDataSource as ISurveyDataSource
    }

    private fun provideObservationLocalDataSource(): ObservationLocalDataSource {
        if (observationLocalDataSource == null) {
            observationLocalDataSource = ObservationLocalDataSource()
        }

        return observationLocalDataSource!!
    }
}