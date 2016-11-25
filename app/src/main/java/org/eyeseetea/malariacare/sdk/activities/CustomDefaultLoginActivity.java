package org.eyeseetea.malariacare.sdk.activities;

import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.widget.Toast;

import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.core.common.network.Configuration;
import org.hisp.dhis.client.sdk.ui.activities.AbsLoginActivity;
import org.hisp.dhis.client.sdk.ui.bindings.commons.Inject;
import org.hisp.dhis.client.sdk.ui.bindings.commons.NavigationHandler;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.LoginPresenter;
import org.hisp.dhis.client.sdk.ui.bindings.views.LoginView;

/**
 * Created by idelcano on 25/11/2016.
 */


public class CustomDefaultLoginActivity extends AbsLoginActivity implements LoginView {
    private LoginPresenter loginPresenter;
    private AlertDialog alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializate();
        // getServerUrl().setText(BuildConfig.SERVER_URL);
        // getUsername().setText(BuildConfig.USERNAME);
        // getPassword().setText(BuildConfig.PASSWORD);
    }

    private void initializate() {
        loginPresenter = defaultUserModule
                .providesLoginPresenter(currentUserInteractor, apiExceptionHandler, logger);
        this.setLoginPresenter(loginPresenter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //loginPresenter.attachView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to avoid leaks on configuration changes:
        if (alertDialog != null) {
            alertDialog.dismiss();
        }

        loginPresenter.detachView();
    }

    @Override
    protected void onLoginButtonClicked(Editable server, Editable username, Editable password) {
        try {
            String authority = getString(org.hisp.dhis.client.sdk.ui.bindings.R.string.authority);
            String accountType = getString(org.hisp.dhis.client.sdk.ui.bindings.R.string.account_type);
            String serverUrl=server.toString();
            if (!isEmpty(serverUrl)) {
                // configure D2
                Configuration configuration = new Configuration(serverUrl);
                D2.configure(configuration).toBlocking().first();
            }
        } catch (ApiException e) {
            loginPresenter.handleError(e);
            return;
        }

        // since we have re-instantiated LoginPresenter, we
        // also have to re-attach view to it
        loginPresenter.attachView(this);

        loginPresenter.validateCredentials(
                server.toString(), username.toString(), password.toString());
    }

    @Override
    public void showProgress() {
        onStartLoading();
    }

    @Override
    public void hideProgress(final OnProgressFinishedListener listener) {
        onFinishLoading(new OnAnimationFinishListener() {
            @Override
            public void onFinish() {
                if (listener != null) {
                    listener.onProgressFinished();
                }
            }
        });
    }

    @Override
    public void showServerError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        getServerUrl().setError(message);
    }

    @Override
    public void showInvalidCredentialsError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        getUsername().setError(message);
        getPassword().setError(message);
    }

    @Override
    public void showUnexpectedError(String message) {
        showErrorDialog(getString(org.hisp.dhis.client.sdk.ui.bindings.R.string.title_error_unexpected), message);
    }

    @Override
    public void navigateToHome() {
        navigateTo(NavigationHandler.homeActivity());
    }

    private void showErrorDialog(String title, String message) {
        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(this)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        }

        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.show();
    }

    public void setLoginPresenter(LoginPresenter loginPresenter) {
        this.loginPresenter = loginPresenter;
    }
}