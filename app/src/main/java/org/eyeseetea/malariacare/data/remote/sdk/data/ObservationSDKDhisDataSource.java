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

import org.eyeseetea.malariacare.data.boundaries.IObservationDataSource;
import org.eyeseetea.malariacare.domain.entity.Observation;

import java.util.List;

public class ObservationSDKDhisDataSource implements IObservationDataSource {


    @Override
    public Observation getObservation(String surveyUId) throws Exception {
        // Not used for the moment
        // The app not realize pull of observations from Dhis2
        return null;
    }

    @Override
    public List<Observation> getObservations(
            ObservationsToRetrieve observationsToRetrieve) {
        // Not used for the moment
        // The app not realize pull of observations from Dhis2
        return null;
    }

    @Override
    public void save(Observation observation) {

    }

    @Override
    public void save(List<Observation> observations) {

    }
}
