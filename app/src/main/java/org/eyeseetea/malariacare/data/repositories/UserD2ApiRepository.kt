package org.eyeseetea.malariacare.data.repositories

import okhttp3.OkHttpClient
import org.eyeseetea.malariacare.data.d2api.BasicAuthInterceptor
import org.eyeseetea.malariacare.data.d2api.UserD2Api
import org.eyeseetea.malariacare.data.database.utils.PreferencesState
import org.eyeseetea.malariacare.domain.boundary.repositories.UserFailure
import org.eyeseetea.malariacare.domain.boundary.repositories.UserRepository
import org.eyeseetea.malariacare.domain.common.Either
import org.eyeseetea.malariacare.domain.entity.User
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserD2ApiRepository : UserRepository {

    private var userD2Api: UserD2Api? = null

    init {
        initializeApi()
    }

    override fun getCurrent(): Either<UserFailure, User> {
        return try {
            if (userD2Api == null) {
                initializeApi()
            }

            val userResponse = userD2Api?.getMe()?.execute()

            if (userResponse!!.isSuccessful && userResponse.body() != null) {
                val user = userResponse.body()

                Either.Right(user!!)
            } else {
                Either.Left(UserFailure.UnexpectedError)
            }
        } catch (e: Exception) {
            Either.Left(UserFailure.NetworkFailure)
        }
    }

    private fun initializeApi() {
        val credentials = PreferencesState.getInstance().creedentials

        if (credentials != null) {
            val authInterceptor = BasicAuthInterceptor(credentials.username, credentials.password)

            val retrofit: Retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(authInterceptor).build()
                )
                .baseUrl(credentials.serverURL)
                .build()

            userD2Api = retrofit.create(UserD2Api::class.java)
        }
    }
}
