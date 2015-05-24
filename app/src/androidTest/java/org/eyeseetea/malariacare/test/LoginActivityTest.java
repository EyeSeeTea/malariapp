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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Path;
import android.support.v7.internal.widget.ViewUtils;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.orm.SugarApp;
import com.orm.SugarCursorFactory;
import com.orm.SugarDb;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositiveScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.database.utils.Session;

import java.io.File;

/**
 * Created by arrizabalaga on 19/05/15.
 */

public class LoginActivityTest extends MalariaInstrumentationTestCase<LoginActivity> {

    private static final long TIMEOUT_IN_MS=10000;

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

        setActivityInitialTouchMode(true);
        loginActivity = getActivity();
        userView =(TextView) loginActivity.findViewById(R.id.user);
        passView =(TextView) loginActivity.findViewById(R.id.password);
        signInButton =(Button) loginActivity.findViewById(R.id.email_sign_in_button);
        res = getInstrumentation().getTargetContext().getResources();
    }

    @Override
    protected void tearDown() throws Exception{
        super.tearDown();
        cleanDB();
    }

    public void test_preconditions() {
        assertNotNull("userView is null", userView);
        assertNotNull("passView is null", passView);
    }

    public void test_form_initial_empty() {
        assertEquals("", userView.getText().toString());
        assertEquals("", passView.getText().toString());
    }

    public void test_nologin_with_bad_credentials(){
        //GIVEN
        setText(userView, "bad");
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
        TouchUtils.clickView(this, signInButton);

        //THEN
        DashboardActivity dashboardActivity = (DashboardActivity) receiverActivityMonitor.waitForActivityWithTimeout(TIMEOUT_IN_MS);
        assertNotNull("DashboardActivity is not started", dashboardActivity);

        //CLEANUP
        cleanDB();
        dashboardActivity.finish();
    }

}
