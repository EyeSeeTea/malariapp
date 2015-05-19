/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.internal.widget.ViewUtils;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.R;

/**
 * Created by arrizabalaga on 19/05/15.
 */
public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    private static final long TIMEOUT_IN_MS=3000;

    private LoginActivity loginActivity;

    private TextView userView;

    private TextView passView;

    private Button signInButton;

    private Resources res;

    public LoginActivityTest(){
        super(LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cleanDB();
        loginActivity = getActivity();
        userView =(TextView) loginActivity.findViewById(R.id.user);
        passView =(TextView) loginActivity.findViewById(R.id.password);
        signInButton =(Button) loginActivity.findViewById(R.id.email_sign_in_button);
        res = getInstrumentation().getTargetContext().getResources();

    }

    private void cleanDB(){
        Context context=getInstrumentation().getTargetContext().getApplicationContext();
        context.deleteDatabase("malariacare.db");
    }

    public void testPreconditions() {
        assertNotNull("userView is null", userView);
        assertNotNull("passView is null", passView);
    }

    public void testFields_initial_empty() {
        final String expected = "";
        final String actualUser = userView.getText().toString();
        final String actualPass = passView.getText().toString();
        assertEquals(expected, actualUser);
        assertEquals(expected, actualPass);
    }

    public void test_nologin_with_bad_credentials(){
        //GIVEN
        setText(userView,"bad");
        setText(passView, "bad");

        //WHEN
        TouchUtils.clickView(this, signInButton);


        //THEN
        String error=userView.getError().toString();
        assertEquals(res.getString(R.string.login_error_bad_credentials), error);
    }

    public void test_login_with_good_credentials(){
        //GIVEN
        Instrumentation.ActivityMonitor receiverActivityMonitor =getInstrumentation().addMonitor(DashboardActivity.class.getName(),null, false);
        setText(userView, "user");
        setText(passView, "user");

        //WHEN
        TouchUtils.clickView(this,signInButton);

        //THEN
        DashboardActivity dashboardActivity = (DashboardActivity) receiverActivityMonitor.waitForActivityWithTimeout(TIMEOUT_IN_MS);
        assertNotNull("DashboardActivity is started", dashboardActivity);
        dashboardActivity.finish();
    }

    private void setText(final View v,String text){
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                v.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();
        getInstrumentation().sendStringSync(text);
        getInstrumentation().waitForIdleSync();
    }


}
