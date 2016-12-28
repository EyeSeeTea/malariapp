/*
 * Copyright (c) 2016.
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

package org.eyeseetea.malariacare;

/**
 * Created by idelcano on 26/12/16.
 */

import android.support.test.runner.AndroidJUnit4;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.config.FlowManager;

import junit.framework.Assert;

import org.apache.commons.jexl2.UnifiedJEXL;
import org.eyeseetea.malariacare.EyeSeeTeaApplication;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class TestDb extends ApplicationTestCase<EyeSeeTeaApplication> {

    public TestDb() {
        super(EyeSeeTeaApplication.class);
        FlowManager.init(getContext());
    }

    @Override
    public void setUp() throws Exception {
        createApplication();
        try {
            PopulateDB.populateDB(getContext().getAssets());
        }catch (Exception c){}
        testProgramIsPopulated();
        testProgramIsPopulated2();
    }

    @MediumTest
    public void testProgramIsPopulated() throws Exception {
        Assert.assertEquals(true,Program.list().size()>0);

        System.out.println("TESTPUEBW"+ Program.list().size()+"");
        Log.d("TESTPUEBW", Program.list().size()+"");
    }

    @MediumTest
    public void testProgramIsPopulated2() throws Exception {
        Assert.assertEquals(true,Program.list().size()==0);

        System.out.println("TESTPUEBW"+ Program.list().size()+"");
        Log.d("TESTPUEBW", Program.list().size()+"");
    }

    @Override
    public void tearDown() throws Exception {
    }

}