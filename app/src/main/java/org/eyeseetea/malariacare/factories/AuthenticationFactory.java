package org.eyeseetea.malariacare.factories;

import android.content.Context;

import org.eyeseetea.malariacare.data.repositories.AuthenticationManager;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IAuthenticationManager;
import org.eyeseetea.malariacare.domain.usecase.LoginUseCase;
import org.eyeseetea.malariacare.domain.usecase.LogoutUseCase;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;

public class AuthenticationFactory {
    private IMainExecutor mMainExecutor = new UIThreadExecutor();
    private IAsyncExecutor mAsyncExecutor = new AsyncExecutor();

    public LoginUseCase getLoginUseCase(Context context) {
        IAuthenticationManager authenticationManager = new AuthenticationManager(context);
        LoginUseCase loginUseCase = new LoginUseCase(authenticationManager, mMainExecutor,
                mAsyncExecutor);
        return loginUseCase;
    }

    public LogoutUseCase getLogoutUseCase(Context context) {
        IAuthenticationManager authenticationManager = new AuthenticationManager(context);
        LogoutUseCase logoutUseCase = new LogoutUseCase(authenticationManager, mMainExecutor,
                mAsyncExecutor);
        return logoutUseCase;
    }


}
