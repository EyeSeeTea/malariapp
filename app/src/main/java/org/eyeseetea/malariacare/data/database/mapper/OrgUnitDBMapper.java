package org.eyeseetea.malariacare.data.database.mapper;

import android.util.Log;

import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;

import java.util.ArrayList;
import java.util.List;

public class OrgUnitDBMapper {
    private final static String TAG = ".OrgUnitDBMapper";

    public List<OrgUnit> mapOrgUnits(List<OrgUnitDB> orgUnitsDB) {

        List<OrgUnit> orgUnits = new ArrayList<>();

        for (OrgUnitDB orgUnitDB : orgUnitsDB) {
            try {
                OrgUnit orgUnit = map(orgUnitDB);

                orgUnits.add(orgUnit);
            } catch (Exception e) {
                Log.e(TAG, "An error occurred converting OrgUnit " + orgUnitDB.getUid() +
                        " to orgUnitDB:" + e.getMessage());
            }
        }

        return orgUnits;
    }

    private OrgUnit map(OrgUnitDB orgUnitDB) {
        String orgUnitLevel = null;
        if(orgUnitDB.getOrgUnitLevel()!=null){
            orgUnitLevel = orgUnitDB.getOrgUnitLevel().getUid();
        }
        OrgUnit orgUnit = new OrgUnit(orgUnitDB.getUid(), orgUnitDB.getName(), orgUnitLevel);

        orgUnit.addRelatedPrograms(orgUnitDB.getAllRelatedPrograms());

        return orgUnit;
    }
}
