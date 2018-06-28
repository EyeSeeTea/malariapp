package org.eyeseetea.malariacare.domain.boundary;

import org.eyeseetea.malariacare.domain.entity.UserAttributes;
import org.eyeseetea.malariacare.domain.exception.PullUserAttributesException;

public interface IUserAttributesRemoteDataSource {
    UserAttributes getUser(String userUId) throws PullUserAttributesException;
}
