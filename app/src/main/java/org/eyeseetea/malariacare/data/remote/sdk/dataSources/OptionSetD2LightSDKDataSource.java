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

import android.content.Context;

import org.eyeseetea.dhis2.lightsdk.D2Response;
import org.eyeseetea.malariacare.data.boundaries.IMetadataRemoteDataSource;
import org.eyeseetea.malariacare.data.remote.sdk.DhisFilter;
import org.eyeseetea.malariacare.domain.entity.OptionSet;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.exception.ServerException;

import java.util.ArrayList;
import java.util.List;

public class OptionSetD2LightSDKDataSource
        extends D2LightSDKDataSource
        implements IMetadataRemoteDataSource<OptionSet> {


    public OptionSetD2LightSDKDataSource(Context context) {
        super(context);
    }

    @Override
    public List<OptionSet> getAll(DhisFilter filter) throws Exception {

        D2Response<List<org.eyeseetea.dhis2.lightsdk.optionsets.OptionSet>> optionSetsResponse =
                getD2Api().optionSets().getAll().execute();

        if (optionSetsResponse.isSuccess()) {
            D2Response.Success<List<org.eyeseetea.dhis2.lightsdk.optionsets.OptionSet>> success =
                    (D2Response.Success<List<org.eyeseetea.dhis2.lightsdk.optionsets.OptionSet>>)
                            optionSetsResponse;

            return mapToDomain(success.getValue());
        } else {
            D2Response.Error errorResponse = (D2Response.Error) optionSetsResponse;

            handleError(errorResponse);
        }

        return null;
    }

    private List<OptionSet> mapToDomain(
            List<org.eyeseetea.dhis2.lightsdk.optionsets.OptionSet> dhisOptionSets) {
        List<OptionSet> optionSets = new ArrayList<>();

        for (org.eyeseetea.dhis2.lightsdk.optionsets.OptionSet dhisOptionSet : dhisOptionSets) {
            optionSets.add(new OptionSet(dhisOptionSet.getId(), dhisOptionSet.getDisplayName()));
        }

        return optionSets;
    }
}