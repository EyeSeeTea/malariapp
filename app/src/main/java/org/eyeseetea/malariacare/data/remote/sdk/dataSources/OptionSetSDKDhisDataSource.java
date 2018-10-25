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

package org.eyeseetea.malariacare.data.remote.sdk.dataSources;

import org.eyeseetea.malariacare.data.boundaries.IMetadataRemoteDataSource;
import org.eyeseetea.malariacare.domain.entity.OptionSet;

import java.util.ArrayList;
import java.util.List;

public class OptionSetSDKDhisDataSource implements IMetadataRemoteDataSource<OptionSet> {
    @Override
    public List<OptionSet> getAll() throws Exception {
        List<org.hisp.dhis.client.sdk.models.optionset.OptionSet> dhisOptionSets =
                new ArrayList<>();
        //=D2.optionSets()..pull().toBlocking().single();

        return mapToDomain(dhisOptionSets);
    }

    private List<OptionSet> mapToDomain(
            List<org.hisp.dhis.client.sdk.models.optionset.OptionSet> dhisOptionSets) {
        List<OptionSet> optionSets = new ArrayList<>();

        for (org.hisp.dhis.client.sdk.models.optionset.OptionSet dhisOptionSet : dhisOptionSets) {
            optionSets.add(new OptionSet(dhisOptionSet.getUId(), dhisOptionSet.getDisplayName()));
        }

        return optionSets;
    }
}
