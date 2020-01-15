package org.eyeseetea.malariacare.data.remote.poeditor

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface PoEditorApi {
    @FormUrlEncoded
    @POST("v2/terms/list")
    fun getTerms(
        @Field("api_token") token: String,
        @Field("id") id: String,
        @Field("language") language: String?
    ): Call<PoEditorResponse<TermsResult>>
}