package org.eyeseetea.malariacare.domain.boundary;

import org.eyeseetea.malariacare.domain.entity.PullStep;

public interface IPullControllerCallback {
    void onComplete();

    void onStep(PullStep step);

    void onError(Throwable throwable);
}
