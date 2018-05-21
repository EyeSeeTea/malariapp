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

package org.eyeseetea.malariacare.data.remote.sdk.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.ConvertFromSDKVisitor;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models
        .OrganisationUnitExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.ProgramExtended;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.remote.sdk.SdkQueries;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.usecase.pull.PullFilters;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.core.event.EventFilters;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramType;

import java.util.ArrayList;
import java.util.List;

import rx.Scheduler;
import rx.schedulers.Schedulers;


public class SurveyDhisDataSource implements ISurveyDataSource {
    private final String TAG = ".SurveyDhisDataSource";

    public SurveyDhisDataSource() {
    }

    @Override
    public List<Survey> getSurveys(PullFilters filters) throws Exception {
        boolean isNetworkAvailable = isNetworkAvailable();

        if (isNetworkAvailable) {
            pullEvents(filters);

            List<Survey> surveys = convertToSurveys();

            return surveys;

        } else {
            throw new NetworkException();
        }
    }


    @Override
    public void Save(List<Survey> surveys) throws Exception {
        //Here push surveys code
    }

    private boolean isNetworkAvailable() {
        //TODO: extract this to another class to avoid duplicate code
        ConnectivityManager cm =
                (ConnectivityManager) PreferencesState.getInstance().getContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private List<Event> pullEvents(PullFilters filters) {
        List<Event> allPulledEvents = new ArrayList<>();

        Scheduler listThread = Schedulers.newThread();
        List<Program> sdkPrograms = D2.me().programs().list().subscribeOn(listThread)
                .observeOn(listThread).toBlocking().single();
        List<OrganisationUnit> sdkOrganisationUnits =
                D2.me().organisationUnits().list().subscribeOn(listThread)
                        .observeOn(listThread).toBlocking().single();
        for (Program program : sdkPrograms) {
            for (OrganisationUnit organisationUnit : sdkOrganisationUnits) {
                for (Program orgunitProgram : organisationUnit.getPrograms()) {
                    if (orgunitProgram.getUId().equals(program.getUId())) {

                        EventFilters eventFilters = new EventFilters();
                        eventFilters.setProgramUId(program.getUId());
                        eventFilters.setOrganisationUnitUId(organisationUnit.getUId());
                        eventFilters.setStartDate(filters.getStartDate());
                        eventFilters.setEndDate(filters.getEndDate());
                        eventFilters.setMaxEvents(filters.getMaxEvents());

                        Scheduler pullEventsThread = Schedulers.newThread();
                        List<Event> eventByOrgUnitAndProgram = D2.events().pull(eventFilters)
                                .subscribeOn(pullEventsThread)
                                .observeOn(pullEventsThread).toBlocking().single();

                        allPulledEvents.addAll(eventByOrgUnitAndProgram);
                    }
                }
            }
        }

        return allPulledEvents;
    }

    private List<Survey> convertToSurveys() {
        ConvertFromSDKVisitor converter = new ConvertFromSDKVisitor();

        for (OrganisationUnitExtended organisationUnit : OrganisationUnitExtended.getExtendedList(
                SdkQueries.getAssignedOrganisationUnits())) {
            for (ProgramExtended program : ProgramExtended.getExtendedList(
                    SdkQueries.getProgramsForOrganisationUnit(organisationUnit.getId(),
                            PreferencesState.getInstance().getContext().getString(
                                    R.string.pull_program_code),
                            ProgramType.WITHOUT_REGISTRATION))) {
                converter.actualProgram = program;
                List<EventExtended> events = EventExtended.getExtendedList(
                        SdkQueries.getEvents(organisationUnit.getId(), program.getUid()));
                System.out.printf("Converting surveys and values for orgUnit: %s | program: %s",
                        organisationUnit.getLabel(), program.getDisplayName());
                for (EventExtended event : events) {
                    if (event.getEventDate() == null
                            || event.getEventDate().equals("")) {
                        Log.d(TAG, "Alert, ignoring event without eventdate, event uid:"
                                + event.getUid());
                        continue;
                    }
                    event.accept(converter);
                }
            }
        }

        return new ArrayList<>();
    }






}
