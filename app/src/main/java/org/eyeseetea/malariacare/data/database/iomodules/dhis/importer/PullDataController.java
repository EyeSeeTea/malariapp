/*
 * Copyright (c) 2015.
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

package org.eyeseetea.malariacare.data.database.iomodules.dhis.importer;

import android.util.Log;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.IPullSourceCallback;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models
        .OrganisationUnitExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.ProgramExtended;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.data.remote.sdk.PullDhisSDKDataSource;
import org.eyeseetea.malariacare.data.remote.sdk.SdkQueries;
import org.eyeseetea.malariacare.domain.boundary.IPullDataController;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.domain.usecase.pull.PullFilters;
import org.eyeseetea.malariacare.domain.usecase.pull.PullStep;
import org.hisp.dhis.client.sdk.models.program.ProgramType;

import java.util.List;

public class PullDataController implements IPullDataController {
    /**
     * Used for control new steps
     */
    private final String TAG = ".PullDataController";
    PullDhisSDKDataSource pullRemoteDataSource;
    IPullDataController.Callback callback;

    ConvertFromSDKVisitor converter;

    public PullDataController() {
        converter = new ConvertFromSDKVisitor();
        pullRemoteDataSource = new PullDhisSDKDataSource();
    }

    @Override
    public void pullData(final PullFilters filters, final IPullDataController.Callback callback) {
        this.callback = callback;

        pullRemoteDataSource.pullData(filters, new IPullSourceCallback() {
            @Override
            public void onComplete() {
                try {
                    if (!pullRemoteDataSource
                            .mandatoryMetadataTablesNotEmpty()) {
                        callback.onError(new
                                ConversionException());
                        return;
                    }
                    try {
                        convertDataValues();

                        validateCS();
                    } catch (Exception e) {
                        callback.onError(new
                                ConversionException(e));
                        return;
                    }

                    callback.onComplete();
                } catch (NullPointerException e) {
                    callback.onError(new
                            ConversionException(e));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                callback.onError(throwable);
            }

        });
    }

    /**
     * Turns events and datavalues into
     */
    private void convertDataValues() {
        callback.onStep(PullStep.PREPARING_SURVEYS);
        //XXX This is the right place to apply additional filters to data conversion (only
        // predefined orgunit for instance)
        //For each unit
        for (OrganisationUnitExtended organisationUnit : OrganisationUnitExtended.getExtendedList(
                SdkQueries.getAssignedOrganisationUnits())) {
            //Each assigned program
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

        //Plan surveys for the future
        SurveyPlanner.getInstance().buildNext();
    }

    private void validateCS() {
        Log.d(TAG, "Validate Composite scores");
        callback.onStep(PullStep.VALIDATE_COMPOSITE_SCORES);
        List<CompositeScoreDB> compositeScores = CompositeScoreDB.list();
        for (CompositeScoreDB compositeScore : compositeScores) {
            if (!compositeScore.hasChildren() && (compositeScore.getQuestions() == null
                    || compositeScore.getQuestions().size() == 0)) {
                Log.d(TAG,
                        "CompositeScoreDB without children and without questions will be removed: "
                                + compositeScore.toString());
                compositeScore.delete();
                continue;
            }
            if (compositeScore.getHierarchical_code() == null) {
                Log.d(TAG, "CompositeScoreDB without hierarchical code will be removed: "
                        + compositeScore.toString());
                compositeScore.delete();
                continue;
            }
            if (compositeScore.getComposite_score() == null
                    && !compositeScore.getHierarchical_code().equals(
                    CompositeScoreBuilder.ROOT_NODE_CODE)) {
                Log.d(TAG, "CompositeScoreDB not root and not parent should be fixed: "
                        + compositeScore.toString());
                continue;
            }
        }
    }

}
