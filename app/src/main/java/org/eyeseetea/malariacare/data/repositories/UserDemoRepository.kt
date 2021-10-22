package org.eyeseetea.malariacare.data.repositories

import org.eyeseetea.malariacare.domain.boundary.repositories.UserFailure
import org.eyeseetea.malariacare.domain.boundary.repositories.UserRepository
import org.eyeseetea.malariacare.domain.common.Either
import org.eyeseetea.malariacare.domain.entity.Credentials
import org.eyeseetea.malariacare.domain.entity.REQUIRED_AUTHORITY
import org.eyeseetea.malariacare.domain.entity.User

class UserDemoRepository : UserRepository {

    override fun getCurrent(): Either<UserFailure, User> {
        val demoCredentials = Credentials.createDemoCredentials()
        return Either.Right(
            User(
                demoCredentials.username,
                demoCredentials.username,
                listOf(REQUIRED_AUTHORITY)
            )
        )
    }
}
