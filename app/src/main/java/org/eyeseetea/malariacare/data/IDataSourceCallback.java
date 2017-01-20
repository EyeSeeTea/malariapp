package org.eyeseetea.malariacare.data;

public interface IDataSourceCallback <T>{
    void onSuccess(T result);
    void onError(Throwable throwable);
}
