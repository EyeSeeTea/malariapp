package org.eyeseetea.malariacare.data;

import org.eyeseetea.malariacare.domain.usecase.pull.PullStep;

public interface IDhisPullSourceCallback {
    void onComplete();

    void onError(Throwable throwable);

    void onStep(PullStep pullStep);
}
