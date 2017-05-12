package org.eyeseetea.malariacare.data;

public interface IPullSourceCallback {
    void onComplete();

    void onError(Throwable throwable);
}
