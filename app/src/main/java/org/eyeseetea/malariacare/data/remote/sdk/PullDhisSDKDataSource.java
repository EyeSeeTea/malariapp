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

package org.eyeseetea.malariacare.data.remote.sdk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.eyeseetea.malariacare.data.IPullSourceCallback;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.CompositeScoreBuilder;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.PullDataController;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.remote.IPullDataSource;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.usecase.pull.PullFilters;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.AttributeFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.AttributeValueFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.DataElementFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.FailedItemFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ModelLinkFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OptionFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OptionSetFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitLevelFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitToProgramRelationFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageDataElementFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageSectionFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.StateFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.TrackedEntityDataValueFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.UserAccountFlow;
import org.hisp.dhis.client.sdk.core.common.controllers.SyncStrategy;
import org.hisp.dhis.client.sdk.core.event.EventFilters;
import org.hisp.dhis.client.sdk.core.program.ProgramFields;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;


public class PullDhisSDKDataSource implements IPullDataSource {
    private final String TAG = ".PullDhisSDKDataSource";

    public PullDhisSDKDataSource() {
    }

    public void pullMetadata(final IPullSourceCallback callback) {
        boolean isNetworkAvailable = isNetworkAvailable();

        if (!isNetworkAvailable) {
            callback.onError(new NetworkException());
        } else {
            Set<ProgramType> programTypes = new HashSet<>();
            programTypes.add(ProgramType.WITHOUT_REGISTRATION);
            Scheduler pullThread = Schedulers.newThread();
            D2.organisationUnitLevels().pull().subscribeOn(pullThread)
                    .observeOn(pullThread).toBlocking().single();
            D2.attributes().pull().subscribeOn(pullThread)
                    .observeOn(pullThread).toBlocking().single();
            Observable.zip(D2.me().organisationUnits().pull(SyncStrategy.NO_DELETE),
                    D2.me().programs().pull(SyncStrategy.NO_DELETE, ProgramFields.DESCENDANTS,
                            programTypes),
                    new Func2<List<OrganisationUnit>, List<Program>, List<Program>>() {
                        @Override
                        public List<Program> call(List<OrganisationUnit> organisationUnits,
                                List<Program> programs) {
                            return programs;
                        }
                    })
                    .subscribeOn(pullThread).
                    observeOn(pullThread)
                    .subscribe(new Action1<List<Program>>() {
                        @Override
                        public void call(List<Program> programs) {
                            callback.onComplete();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            callback.onError(throwable);
                        }
                    });
        }

    }

    public void pullData(PullFilters filters, IPullSourceCallback callback) {
        boolean isNetworkAvailable = isNetworkAvailable();

        if (!isNetworkAvailable) {
            callback.onError(new NetworkException());
        } else {
            try {
                pullEvents(filters, callback);
                callback.onComplete();
            } catch (Exception e) {
                callback.onError(e);
            }
        }
    }

    private void pullEvents(PullFilters filters, IPullSourceCallback callback) {
        Scheduler listThread = Schedulers.newThread();
        List<Program> sdkPrograms = D2.me().programs().list().subscribeOn(listThread)
                .observeOn(listThread).toBlocking().single();
        List<OrganisationUnit> sdkOrganisationUnits =
                D2.me().organisationUnits().list().subscribeOn(listThread)
                        .observeOn(listThread).toBlocking().single();

        if (!PullDataController.PULL_IS_ACTIVE) return;
        for (Program program : sdkPrograms) {
            for (OrganisationUnit organisationUnit : sdkOrganisationUnits) {
                for (Program orgunitProgram : organisationUnit.getPrograms()) {
                    if (orgunitProgram.getUId().equals(program.getUId())) {
                        if (!PullDataController.PULL_IS_ACTIVE) return;

                        EventFilters eventFilters = new EventFilters();
                        eventFilters.setProgramUId(program.getUId());
                        eventFilters.setOrganisationUnitUId(organisationUnit.getUId());
                        eventFilters.setStartDate(filters.getStartDate());
                        eventFilters.setEndDate(filters.getEndDate());
                        eventFilters.setMaxEvents(filters.getMaxEvents());

                        Scheduler pullEventsThread = Schedulers.newThread();
                        D2.events().pull(eventFilters)
                                .subscribeOn(pullEventsThread)
                                .observeOn(pullEventsThread).toBlocking().single();
                    }
                }
            }
        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) PreferencesState.getInstance().getContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void wipeDataBase() {

        Delete.tables(
                EventFlow.class,
                TrackedEntityDataValueFlow.class,
                DataElementFlow.class,
                ProgramFlow.class,
                AttributeFlow.class,
                AttributeValueFlow.class,
                ModelLinkFlow.class,
                OptionSetFlow.class,
                OptionFlow.class,
                OrganisationUnitFlow.class,
                OrganisationUnitLevelFlow.class,
                OrganisationUnitToProgramRelationFlow.class,
                ProgramStageDataElementFlow.class,
                ProgramStageSectionFlow.class,
                ProgramStageFlow.class,
                FailedItemFlow.class,
                StateFlow.class,
                FailedItemFlow.class
        );
    }
}
