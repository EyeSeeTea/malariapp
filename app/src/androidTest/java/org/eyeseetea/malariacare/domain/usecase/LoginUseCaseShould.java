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

import android.support.test.InstrumentationRegistry;

import org.eyeseetea.malariacare.data.database.datasources.ServerInfoLocalDataSource;
import org.eyeseetea.malariacare.data.file.AssetsFileReader;
import org.eyeseetea.malariacare.data.remote.api.ServerInfoRemoteDataSource;
import org.eyeseetea.malariacare.data.repositories.ServerInfoRepository;
import org.eyeseetea.malariacare.data.repositories.ServerRepository;
import org.eyeseetea.malariacare.data.repositories.UserAccountRepository;
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

public class LoginUseCaseShould {

    private static final String SYSTEM_INFO_VERSION_30 = "system_info_30.json";
    private static final String AUTH = "auth.json";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Rule
    public MockWebServerRule mockWebServerRule = new MockWebServerRule(new AssetsFileReader());
    @Mock
    ServerRepository serverRepository;
    @Mock
    ServerInfoLocalDataSource mServerLocalDataSource;
    @Test
    public void return_on_login_success_callback_when_server_do_login_with_demo_credentials() {
        Credentials credentials = Credentials.createDemoCredentials();
        int actualVersion = 30;
        LoginUseCase loginUseCase = givenLoginUseCase(credentials, actualVersion);

        loginUseCase.execute(credentials, new LoginUseCase.Callback() {

            @Override
            public void onLoginSuccess() {
                Assert.assertTrue(true);
            }

            @Override
            public void onServerURLNotValid() {
                fail("onServerURLNotValid");
            }

            @Override
            public void onInvalidCredentials() {
                fail("onInvalidCredentials");
            }

            @Override
            public void onNetworkError() {
                fail("onNetworkError");
            }

            @Override
            public void onUnsupportedServerVersion() {
                fail("onServerVersionError");
            }
        });
    }

    @Test
    public void return_on_login_success_callback_when_server_do_login() throws Exception {
        Credentials credentials = new Credentials(mockWebServerRule.getMockServer().getBaseEndpoint(), "user", "password");
        int actualVersion = -1;
        LoginUseCase loginUseCase = givenLoginUseCase(credentials, actualVersion);

        mockWebServerRule.getMockServer().enqueueMockResponseFileName(200, SYSTEM_INFO_VERSION_30);
        mockWebServerRule.getMockServer().enqueueMockResponseFileName(200, AUTH);
        loginUseCase.execute(credentials, new LoginUseCase.Callback() {

            @Override
            public void onLoginSuccess() {
                Assert.assertTrue(true);
            }

            @Override
            public void onServerURLNotValid() {
                fail("onServerURLNotValid");
            }

            @Override
            public void onInvalidCredentials() {
                fail("onInvalidCredentials");
            }

            @Override
            public void onNetworkError() {
                fail("onNetworkError");
            }

            @Override
            public void onUnsupportedServerVersion() {
                fail("onUnsupportedServerVersion");
            }
        });
    }

    private LoginUseCase givenLoginUseCase(Credentials credentials, int serverVersion) {
        IMainExecutor mainExecutor = new UIThreadExecutor();
        IAsyncExecutor asyncExecutor = new IAsyncExecutor() {
            @Override
            public void run(Runnable runnable) {
                runnable.run();
            }
        };
        when(mServerLocalDataSource.get()).thenReturn(new ServerInfo(serverVersion));
        ServerInfoRemoteDataSource mServerRemoteDataSource = new ServerInfoRemoteDataSource(InstrumentationRegistry.getTargetContext());
        ServerInfoRepository serverInfoRepository = new ServerInfoRepository(mServerLocalDataSource, mServerRemoteDataSource);
        return new LoginUseCase(
                new UserAccountRepository(InstrumentationRegistry.getTargetContext()),
                serverRepository,
                serverInfoRepository,
                mainExecutor,
                asyncExecutor);
    }
}
