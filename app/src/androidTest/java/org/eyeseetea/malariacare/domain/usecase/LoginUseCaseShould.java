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

import org.eyeseetea.malariacare.data.remote.api.ServerInfoDataSource;
import org.eyeseetea.malariacare.data.repositories.ServerInfoRepository;
import org.eyeseetea.malariacare.data.repositories.UserAccountRepository;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.rules.MockWebServerRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.fail;

public class LoginUseCaseShould {

    private static final String SYSTEM_INFO_VERSION_24 = "system_info_24.json";
    private static final String SYSTEM_INFO_VERSION_25 = "system_info_25.json";
    private static final String SYSTEM_INFO_VERSION_26 = "system_info_26.json";
    private static final String AUTH = "auth.json";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Rule
    public MockWebServerRule mockWebServerRule = new MockWebServerRule();

    @Test
    public void return_on_login_success_with_demo_credentials() {
        Credentials credentials = Credentials.createDemoCredentials();
        LoginUseCase loginUseCase = givenLoginUseCase(credentials);

        int maxCompatibleVersion = 25;

        loginUseCase.execute(credentials, maxCompatibleVersion, new LoginUseCase.Callback() {

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
            public void onServerVersionError() {
                fail("onServerVersionError");
            }
        });
    }

    @Test
    public void return_on_login_success_when_server_version_is_equals_to_max_compatible_version()
            throws Exception {
        Credentials credentials = new Credentials(
                mockWebServerRule.getMockServer().getBaseEndpoint(), "user", "password");
        LoginUseCase loginUseCase = givenLoginUseCase(credentials);

        int maxCompatibleVersion = 25;

        mockWebServerRule.getMockServer().enqueueMockResponseFileName(200, SYSTEM_INFO_VERSION_25);
        mockWebServerRule.getMockServer().enqueueMockResponseFileName(200, AUTH);
        loginUseCase.execute(credentials, maxCompatibleVersion, new LoginUseCase.Callback() {

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
            public void onServerVersionError() {
                fail("onServerVersionError");
            }
        });
    }

    @Test
    public void return_on_server_version_error_when_server_version_is_greater_than_max_compatible_version()
            throws Exception {
        Credentials credentials = new Credentials(
                mockWebServerRule.getMockServer().getBaseEndpoint(), "user", "password");
        LoginUseCase loginUseCase = givenLoginUseCase(credentials);

        int maxCompatibleVersion = 25;

        mockWebServerRule.getMockServer().enqueueMockResponseFileName(200, SYSTEM_INFO_VERSION_26);
        mockWebServerRule.getMockServer().enqueueMockResponseFileName(200, AUTH);
        loginUseCase.execute(credentials, maxCompatibleVersion, new LoginUseCase.Callback() {

            @Override
            public void onLoginSuccess() {
                fail("onLoginSuccess");
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
            public void onServerVersionError() {
                Assert.assertTrue(true);
            }
        });
    }

    @Test
    public void return_on_login_success_when_server_version_is_lower_than_max_compatible_version()
            throws Exception {
        Credentials credentials = new Credentials(
                mockWebServerRule.getMockServer().getBaseEndpoint(), "user", "password");
        LoginUseCase loginUseCase = givenLoginUseCase(credentials);

        int maxCompatibleVersion = 25;

        mockWebServerRule.getMockServer().enqueueMockResponseFileName(200, SYSTEM_INFO_VERSION_24);
        mockWebServerRule.getMockServer().enqueueMockResponseFileName(200, AUTH);
        loginUseCase.execute(credentials, maxCompatibleVersion, new LoginUseCase.Callback() {

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
            public void onServerVersionError() {
                fail("onServerVersionError");
            }
        });
    }

    private LoginUseCase givenLoginUseCase(Credentials credentials) {
        IMainExecutor mainExecutor = new UIThreadExecutor();
        IAsyncExecutor asyncExecutor = new IAsyncExecutor() {
            @Override
            public void run(Runnable runnable) {
                runnable.run();
            }
        };
        return new LoginUseCase(
                new UserAccountRepository(InstrumentationRegistry.getTargetContext()),
                new ServerInfoRepository(new ServerInfoDataSource(credentials)), mainExecutor,
                asyncExecutor);
    }
}
