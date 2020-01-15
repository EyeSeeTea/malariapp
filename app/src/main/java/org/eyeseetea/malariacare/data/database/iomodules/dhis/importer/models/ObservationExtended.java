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

package org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.VisitableFromSDK;
import org.eyeseetea.malariacare.data.remote.sdk.SdkQueries;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.TrackedEntityDataValueFlow;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;

import java.util.ArrayList;
import java.util.List;

public class ObservationExtended implements VisitableFromSDK {

    private final static String TAG = ".ObservationExtended";

    EventFlow event;

    public ObservationExtended(EventFlow event) {
        this.event = event;
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }

    public EventFlow getEvent() {
        return event;
    }

    public List<ObservationValueExtended> getObservationValues(
            ServerMetadata serverMetadata) {
        Event eventModel = SdkQueries.getEvent(event.getUId());
        List<TrackedEntityDataValue> trackedEntityDataValues = eventModel.getDataValues();
        List<ObservationValueExtended> observationValues = new ArrayList<>();
        for (TrackedEntityDataValue trackedEntityDataValue : trackedEntityDataValues) {
            if (serverMetadata.getObservationsDataElementUids()
                    .contains(trackedEntityDataValue.getDataElement())) {
                {
                    observationValues.add(new ObservationValueExtended(
                            TrackedEntityDataValueFlow.MAPPER.mapToDatabaseEntity(
                                    trackedEntityDataValue)));
                }
            }
        }

        return observationValues;
    }
}
