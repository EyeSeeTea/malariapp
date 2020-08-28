package org.eyeseetea.malariacare.data.remote.poeditor

import okhttp3.OkHttpClient
import org.eyeseetea.malariacare.domain.common.Either
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

sealed class PoEditorApiClientFailure {
    data class ApiError(val message: String) : PoEditorApiClientFailure()
    object NetworkConnectionFailure : PoEditorApiClientFailure()
}

class PoEditorApiClient(
    private val projectID: String,
    private val apiToken: String,
    baseUrl: String = "https://api.poeditor.com/"
) {
    private lateinit var poEditorApi: PoEditorApi

    fun getTerms(language: String?): Either<PoEditorApiClientFailure, List<Term>> {
        return try {
            val response = poEditorApi.getTerms(apiToken, projectID, language).execute()

            if (response!!.isSuccessful) {
                val terms = response.body()?.result?.terms ?: listOf()

                Either.Right(terms)
            } else {
                val error = response.errorBody()!!.string()
                Either.Left(PoEditorApiClientFailure.ApiError(error))
            }
        } catch (e: IOException) {
            Either.Left(PoEditorApiClientFailure.NetworkConnectionFailure)
        }
    }

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .baseUrl(baseUrl)
            .build()
        poEditorApi = retrofit.create(PoEditorApi::class.java)
    }
}