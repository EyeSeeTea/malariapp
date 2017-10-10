package org.eyeseetea.malariacare.domain.boundary.executors;

public interface IAsyncExecutor {
    void run(final Runnable runnable);
}