package org.eyeseetea.malariacare.data;

public interface IDhisPullSourceCallback {
    void onComplete();

    void onError(Throwable throwable);
}
