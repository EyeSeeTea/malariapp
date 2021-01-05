package org.eyeseetea.malariacare.domain.boundary.repositories

import org.eyeseetea.malariacare.domain.common.Either
import org.eyeseetea.malariacare.domain.entity.User

sealed class UserFailure {
    object UnexpectedError : UserFailure()
    object NetworkFailure : UserFailure()
}

interface UserRepository {
    fun getCurrent(): Either<UserFailure, User>
}