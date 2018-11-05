package org.eyeseetea.malariacare.factories;

import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;

public class AFactory {
    protected IAsyncExecutor asyncExecutor = new AsyncExecutor();
    protected IMainExecutor mainExecutor = new UIThreadExecutor();
}
