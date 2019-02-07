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
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.remote.sdk.DhisFilter;
import org.eyeseetea.malariacare.domain.entity.OrgUnitLevel;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnitLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrgUnitLevelSDKDhisDataSource implements IMetadataRemoteDataSource<OrgUnitLevel> {
    @Override
    public List<OrgUnitLevel> getAll(DhisFilter filter) throws Exception {
        List<OrganisationUnitLevel> organisationUnitLevels =
                D2.organisationUnitLevels().pull().toBlocking().single();

        return mapToDomain(organisationUnitLevels);
    }

    private List<OrgUnitLevel> mapToDomain(List<OrganisationUnitLevel> organisationUnitLevels) {
        List<OrgUnitLevel> orgUnitLevels = new ArrayList<>();

        sortByLevel(organisationUnitLevels);

        for (OrganisationUnitLevel organisationUnitLevel:organisationUnitLevels) {
            orgUnitLevels.add(new OrgUnitLevel(organisationUnitLevel.getUId(),
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
