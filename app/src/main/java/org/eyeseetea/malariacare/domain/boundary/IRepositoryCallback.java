package org.eyeseetea.malariacare.domain.boundary;

public interface IRepositoryCallback<T>{
    void onSuccess(T result);
    void onError(Throwable throwable);
}
