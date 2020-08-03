/*
 * Copyright (c) 2017.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.data.database.datasources.ServerInfoLocalDataSource;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.PushDataController;
import org.eyeseetea.malariacare.data.file.AssetsFileReader;
import org.eyeseetea.malariacare.data.remote.api.ServerInfoRemoteDataSource;
import org.eyeseetea.malariacare.data.repositories.ServerInfoRepository;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.ServerInfo;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.rules.MockWebServerRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import android.support.test.InstrumentationRegistry;

public class PushUseCaseShould {

    private static final String SYSTEM_INFO_VERSION_30 = "system_info_30.json";
    private static final String SYSTEM_INFO_VERSION_31 = "system_info_31.json";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Rule
    public MockWebServerRule mockWebServerRule = new MockWebServerRule(new AssetsFileReader());
    @Mock
    ServerInfoLocalDataSource mServerLocalDataSource;

    @Test
    public void return_on_complete_with_demo_credentials() throws Exception {
        Credentials credentials = Credentials.createDemoCredentials();
        int actualVersion = 30;
        PushUseCase loginUseCase = givenPushUseCase(credentials, actualVersion);

        mockWebServerRule.getMockServer().enqueueMockResponseFileName(200, SYSTEM_INFO_VERSION_31);
        loginUseCase.execute(credentials, new PushUseCase.Callback() {

            @Override
            public void onComplete(PushDataController.Kind kind) { Assert.assertTrue(true); }

            @Override
            public void onPushError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onPushInProgressError() { fail("onLoginSuccess"); }

            @Override
            public void onSurveysNotFoundError() { fail("onSurveysNotFoundError"); }

            @Override
            public void onInformativeError(String message) {
                fail("onLoginSuccess");
            }

            @Override
            public void onConversionError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onNetworkError() {
                fail("onNetworkError");
            }

            @Override
            public void onServerVersionError() {
                fail("onLoginSuccess");
            }
        });
    }

    @Test
    public void return_on_complete_when_server_version_is_equals_to_persisted_version() throws Exception {
        Credentials credentials = new Credentials(mockWebServerRule.getMockServer().getBaseEndpoint(), "user", "password");
        int actualVersion = 30;
        PushUseCase loginUseCase = givenPushUseCase(credentials, actualVersion);

        mockWebServerRule.getMockServer().enqueueMockResponseFileName(200, SYSTEM_INFO_VERSION_30);
        loginUseCase.execute(credentials, new PushUseCase.Callback() {

            @Override
            public void onComplete(PushDataController.Kind kind) {
                Assert.assertTrue(true); }

            @Override
            public void onPushError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onPushInProgressError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onSurveysNotFoundError() { fail("onSurveysNotFoundError"); }

            @Override
            public void onInformativeError(String message) {
                fail("onLoginSuccess");
            }

            @Override
            public void onConversionError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onNetworkError() {
                fail("onNetworkError");
            }

            @Override
            public void onServerVersionError() {
                fail("onLoginSuccess");
            }
        });
    }

    @Test
    public void return_on_server_version_error_when_server_version_is_greater_than_saved_version() throws Exception {
        Credentials credentials = new Credentials(mockWebServerRule.getMockServer().getBaseEndpoint(), "user", "password");
        int actualVersion = 30;
        PushUseCase loginUseCase = givenPushUseCase(credentials, actualVersion);

        mockWebServerRule.getMockServer().enqueueMockResponseFileName(200, SYSTEM_INFO_VERSION_31);
        loginUseCase.execute(credentials, new PushUseCase.Callback() {

            @Override
            public void onComplete(PushDataController.Kind kind) {
                fail("onLoginSuccess");
            }

            @Override
            public void onPushError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onPushInProgressError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onSurveysNotFoundError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onInformativeError(String message) {
                fail("onLoginSuccess");
            }

            @Override
            public void onConversionError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onNetworkError() {
                fail("onNetworkError");
            }

            @Override
            public void onServerVersionError() {
                Assert.assertTrue(true);
            }
        });
    }

    @Test
    public void return_on_server_version_error_when_server_version_is_lower_than_saved_version() throws Exception {
        Credentials credentials = new Credentials(mockWebServerRule.getMockServer().getBaseEndpoint(), "user", "password");
        int actualVersion = 31;
        PushUseCase loginUseCase = givenPushUseCase(credentials, actualVersion);

        mockWebServerRule.getMockServer().enqueueMockResponseFileName(200, SYSTEM_INFO_VERSION_30);
        loginUseCase.execute(credentials, new PushUseCase.Callback() {

            @Override
            public void onComplete(PushDataController.Kind kind) {
                fail("onLoginSuccess");
            }

            @Override
            public void onPushError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onPushInProgressError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onSurveysNotFoundError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onInformativeError(String message) {
                fail("onLoginSuccess");
            }

            @Override
            public void onConversionError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onNetworkError() {
                fail("onNetworkError");
            }

            @Override
            public void onServerVersionError() {
                Assert.assertTrue(true);
            }
        });
    }

    @Test
    public void return_on_complete_when_server_version_is_not_persisted_on_push() throws Exception {
        Credentials credentials = new Credentials(mockWebServerRule.getMockServer().getBaseEndpoint(), "user", "password");
        int actualVersion = -1;
        PushUseCase loginUseCase = givenPushUseCase(credentials, actualVersion);

        mockWebServerRule.getMockServer().enqueueMockResponseFileName(200, SYSTEM_INFO_VERSION_30);
        mockWebServerRule.getMockServer().enqueueMockResponseFileName(200, SYSTEM_INFO_VERSION_30);
        loginUseCase.execute(credentials, new PushUseCase.Callback() {

            @Override
            public void onComplete(PushDataController.Kind kind) {
                Assert.assertTrue(true);
            }

            @Override
            public void onPushError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onPushInProgressError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onSurveysNotFoundError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onInformativeError(String message) {
                fail("onLoginSuccess");
            }

            @Override
            public void onConversionError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onNetworkError() {
                fail("onNetworkError");
            }

            @Override
            public void onServerVersionError() {
                fail("onServerVersionError");
            }
        });
    }
    private PushUseCase givenPushUseCase(Credentials credentials, int serverVersion) {
        IMainExecutor mainExecutor = new UIThreadExecutor();
        IAsyncExecutor asyncExecutor = new IAsyncExecutor() {
            @Override
            public void run(Runnable runnable) {
                runnable.run();
            }
        };
        IPushController pushController = new IPushController() {
            @Override
            public void push(IPushControllerCallback callback) {
                callback.onComplete(PushDataController.Kind.EVENTS);
            }

            @Override
            public boolean isPushInProgress() {
                return false;
            }

            @Override
            public void changePushInProgress(boolean inProgress) {

            }
        };
        when(mServerLocalDataSource.get()).thenReturn(new ServerInfo(serverVersion));
        ServerInfoRemoteDataSource serverInfoRemoteDataSource = new ServerInfoRemoteDataSource(
                InstrumentationRegistry.getTargetContext());
        return new PushUseCase(pushController, mainExecutor, asyncExecutor, new ServerInfoRepository(mServerLocalDataSource, serverInfoRemoteDataSource));
    }
}
