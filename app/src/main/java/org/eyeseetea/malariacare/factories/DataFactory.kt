package org.eyeseetea.malariacare.factories

import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource
import org.eyeseetea.malariacare.data.database.datasources.ObservationLocalDataSource
import org.eyeseetea.malariacare.data.database.datasources.SurveyLocalDataSource
import org.eyeseetea.malariacare.data.repositories.ObservationRepository
import org.eyeseetea.malariacare.data.repositories.SurveyRepository
import org.eyeseetea.malariacare.domain.boundary.repositories.IObservationRepository
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository
import org.eyeseetea.malariacare.domain.usecase.GetObservationBySurveyUidUseCase
import org.eyeseetea.malariacare.domain.usecase.GetSentObservationsUseCase
import org.eyeseetea.malariacare.domain.usecase.GetSurveyByUidUseCase
import org.eyeseetea.malariacare.domain.usecase.GetSurveysUseCase

object DataFactory {

    fun provideGetSurveyByUidUseCase(): GetSurveyByUidUseCase =
        GetSurveyByUidUseCase(provideSurveyRepository())

    fun provideSurveysUseCase(): GetSurveysUseCase = GetSurveysUseCase(provideSurveyRepository())

    fun provideObservationBySurveyUidUseCase(): GetObservationBySurveyUidUseCase =
        GetObservationBySurveyUidUseCase(provideObservationRepository())

    fun provideSentObservationsUseCase(): GetSentObservationsUseCase =
        GetSentObservationsUseCase(provideObservationRepository())

    private fun provideSurveyRepository(): ISurveyRepository =
        SurveyRepository(provideSurveyLocalDataSource())

    private fun provideObservationRepository(): IObservationRepository =
        ObservationRepository(provideObservationLocalDataSource())

    private fun provideSurveyLocalDataSource(): ISurveyDataSource = SurveyLocalDataSource()

    private fun provideObservationLocalDataSource(): ObservationLocalDataSource =
        ObservationLocalDataSource()
}