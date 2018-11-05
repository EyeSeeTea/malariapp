package org.eyeseetea.malariacare.data.repositories;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitLevelDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitRepository;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OrgUnitRepository implements IOrgUnitRepository{

    @Override
    public List<OrgUnit> getAll() {

        List<OrgUnit> orgUnits = new ArrayList<>();

        List<OrgUnitDB> orgUnitsDB = new Select().from(OrgUnitDB.class).queryList();
        List<OrgUnitLevelDB> orgUnitLevelsDB = new Select().from(OrgUnitLevelDB.class).queryList();
        List<OrgUnitProgramRelationDB> orgUnitProgramRelationsDB =
                new Select().from(OrgUnitProgramRelationDB.class).queryList();
        List<ProgramDB> programsDB = new Select().from(ProgramDB.class).queryList();

        for (OrgUnitDB orgUnitDB:orgUnitsDB) {
            orgUnits.add(
                    mapOrgUnit(orgUnitDB, orgUnitLevelsDB,
                            orgUnitProgramRelationsDB, programsDB));
        }

        return orgUnits;
    }


    @NonNull
    private OrgUnit mapOrgUnit(OrgUnitDB orgUnitDB, List<OrgUnitLevelDB> orgUnitLevelsDB,
            List<OrgUnitProgramRelationDB> orgUnitProgramRelationsDB, List<ProgramDB> programsDB) {

        String orgUnitLevelUid = null;
        Map<String, Integer> productivityByProgram = new HashMap<>();

        for (OrgUnitLevelDB orgUnitLevelDB:orgUnitLevelsDB ) {
            if (orgUnitDB.getId_org_unit_level_fk().equals(orgUnitLevelDB.getId_org_unit_level())){
                orgUnitLevelUid = orgUnitLevelDB.getUid();
                break;
            }
        }

        for (OrgUnitProgramRelationDB orgUnitProgramRelationDB:orgUnitProgramRelationsDB ) {
            if (orgUnitProgramRelationDB.getId_org_unit_fk().equals(orgUnitDB.getId_org_unit())){
                String programUid = getProgramUid (programsDB,
                        orgUnitProgramRelationDB.getId_program_fk());
                productivityByProgram.put(programUid, orgUnitProgramRelationDB.getProductivity());
            }
        }

        return new OrgUnit(orgUnitDB.getUid(), orgUnitDB.getName(),
                orgUnitLevelUid, productivityByProgram);
    }

    private String getProgramUid(List<ProgramDB> programsDB, Long id_program_fk) {
        String programUid = null;

        for (ProgramDB programDB:programsDB ) {
            if (programDB.getId_program().equals(id_program_fk)){
                programUid = programDB.getUid();
                break;
            }
        }

        return programUid;
    }

}
