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
import org.eyeseetea.dhis2.lightsdk.organisationunits.OrganisationUnitLevel;
import org.eyeseetea.malariacare.data.boundaries.IMetadataRemoteDataSource;
import org.eyeseetea.malariacare.data.remote.sdk.DhisFilter;
import org.eyeseetea.malariacare.domain.entity.OrgUnitLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrgUnitLevelD2LightSDKDataSource
        extends D2LightSDKDataSource
        implements IMetadataRemoteDataSource<OrgUnitLevel> {

    public OrgUnitLevelD2LightSDKDataSource(Context context) {
        super(context);
    }

    @Override
    public List<OrgUnitLevel> getAll(DhisFilter filter) throws Exception {
        D2Response<List<OrganisationUnitLevel>> response =
                getD2Api().organisationUnitLevels().getAll().execute();

        if (response.isSuccess()) {
            D2Response.Success<List<OrganisationUnitLevel>> success =
                    (D2Response.Success<List<OrganisationUnitLevel>>) response;

            return mapToDomain(success.getValue());
        } else {
            D2Response.Error errorResponse = (D2Response.Error) response;

            handleError(errorResponse);
        }

        return null;
    }

    private List<OrgUnitLevel> mapToDomain(List<OrganisationUnitLevel> organisationUnitLevels) {
        List<OrgUnitLevel> orgUnitLevels = new ArrayList<>();

        sortByLevel(organisationUnitLevels);

        for (OrganisationUnitLevel organisationUnitLevel:organisationUnitLevels) {
            orgUnitLevels.add(new OrgUnitLevel(organisationUnitLevel.getId(),
                            organisationUnitLevel.getDisplayName()));
        }

        return orgUnitLevels;
    }

    private void sortByLevel(List<OrganisationUnitLevel> organisationUnitLevels) {
        Collections.sort(organisationUnitLevels, new Comparator<OrganisationUnitLevel>() {
            @Override
            public int compare(
                    OrganisationUnitLevel orgUnitLevel1,
                    OrganisationUnitLevel orgUnitLevel2) {
                return  new Integer(orgUnitLevel1.getLevel())
                        .compareTo(new Integer(orgUnitLevel2.getLevel()));
            }
        });
    }
}
