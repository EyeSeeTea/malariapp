package org.eyeseetea.malariacare.data.d2api

import org.eyeseetea.malariacare.domain.entity.User
import retrofit2.Call
import retrofit2.http.GET

interface UserD2Api {
    @GET("api/30/me?fields=id,name,authorities")
    fun getMe(): Call<User>
}