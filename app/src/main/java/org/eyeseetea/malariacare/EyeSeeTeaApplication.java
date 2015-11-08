/*
 * Copyright (c) 2015.
 *
 * This file is part of Health Network QIS App.
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

import android.app.Activity;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.eyeseetea.malariacare.database.utils.LocationMemory;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;

import io.fabric.sdk.android.Fabric;

/**
 * Created by nacho on 04/08/15.
 */
public class EyeSeeTeaApplication extends Dhis2Application  {

    public Class<? extends Activity> getMainActivity() {
        return new DashboardActivity().getClass();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        PreferencesState.getInstance().init(getApplicationContext());
        LocationMemory.getInstance().init(getApplicationContext());
        FlowManager.init(this, "_EyeSeeTeaDB");
        //dummyData();
        //convertFromSDK();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        FlowManager.destroy();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

//    public void dummyData(){
//        ProgramStageSection tab = new ProgramStageSection();
//        tab.setSortOrder(1);
//        tab.setExternalAccess(true);
//        tab.setName("dummyTab");
//        tab.save();
//
//        OrganisationUnit organisationUnit = new OrganisationUnit();
//        organisationUnit.setLabel("dummyOrgUnit");
//        organisationUnit.save();
//    }

//    public void convertFromSDK(){
//        ConvertFromSDKVisitor converter = new ConvertFromSDKVisitor();
//        List<ProgramStageSection> tabs = new Select().all().from(ProgramStageSection.class).queryList();
//        final VisitableFromSDK<ProgramStageSection> visitableFromSDKTabs = new ProgramStageSectionVisitableFromSDK<>(tabs);
//        visitableFromSDKTabs.accept(converter);
//
//        List<OrganisationUnit> orgUnits = new Select().all().from(OrganisationUnit.class).queryList();
//        final VisitableFromSDK<OrganisationUnit> visitableFromSDKOrgUnits = new OrganisationUnitExtended<>(orgUnits);
//        visitableFromSDKOrgUnits.accept(converter);
//    }
}
