package org.eyeseetea.malariacare.data.database.datasources;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB_Table;
import org.eyeseetea.malariacare.data.database.model.OrgUnitLevelDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrgUnitLocalDataSource
        implements IMetadataLocalDataSource<OrgUnit> {

    List<OrgUnitLevelDB> orgUnitLevelsDB;
    List<OrgUnitProgramRelationDB> orgUnitProgramRelationsDB;
    List<ProgramDB> programsDB;

    @Override
    public List<OrgUnit> getAll() {
        List<OrgUnitDB> orgUnitsDB = new Select().from(OrgUnitDB.class).queryList();

        loadDependants();

        return mapToDomain(orgUnitsDB);
    }

    @Override
    public OrgUnit getByUid(String uid) {
        OrgUnitDB orgUnitDB = new Select().from(OrgUnitDB.class)
                .where(OrgUnitDB_Table.uid_org_unit.eq(uid)).querySingle();

        loadDependants();

        return mapOrgUnit(orgUnitDB);
    }

    private void loadDependants() {
        if (orgUnitLevelsDB == null) {
            orgUnitLevelsDB = new Select().from(OrgUnitLevelDB.class).queryList();
        }

        if (orgUnitProgramRelationsDB == null) {
            orgUnitProgramRelationsDB =
                    new Select().from(OrgUnitProgramRelationDB.class).queryList();
        }

        if (programsDB == null) {
            programsDB = new Select().from(ProgramDB.class).queryList();
        }
    }

    @NonNull
    private List<OrgUnit> mapToDomain(List<OrgUnitDB> orgUnitsDB) {

        List<OrgUnit> orgUnits = new ArrayList<>();

        String orgUnitLevelUid;
        Map<String, Integer> productivityByProgram;

        for (OrgUnitDB orgUnitDB : orgUnitsDB) {
            OrgUnit orgUnit = mapOrgUnit(orgUnitDB);

            orgUnits.add(orgUnit);
        }

        return orgUnits;
    }

    @NotNull
    private OrgUnit mapOrgUnit(OrgUnitDB orgUnitDB) {
        String orgUnitLevelUid;
        Map<String, Integer> productivityByProgram;
        orgUnitLevelUid = getOrgUnitLevelUid(orgUnitDB.getId_org_unit_level_fk());

        productivityByProgram = getProductivityByProgram(orgUnitDB);

        return new OrgUnit(orgUnitDB.getUid(), orgUnitDB.getName(),
                orgUnitLevelUid, productivityByProgram);
    }

    private String getOrgUnitLevelUid(Long orgUnitLevelId) {
        String orgUnitLevelUid = null;

        for (OrgUnitLevelDB orgUnitLevelDB : orgUnitLevelsDB) {
            if (orgUnitLevelId.equals(orgUnitLevelDB.getId_org_unit_level())) {
                orgUnitLevelUid = orgUnitLevelDB.getUid();
                break;
            }
        }

        return orgUnitLevelUid;
    }

    private String getProgramUid(Long id_program_fk) {
        String programUid = null;

        for (ProgramDB programDB : programsDB) {
            if (programDB.getId_program().equals(id_program_fk)) {
                programUid = programDB.getUid();
                break;
            }
        }

        return programUid;
    }

    private Map<String, Integer> getProductivityByProgram(OrgUnitDB orgUnitDB) {
        Map<String, Integer> productivityByProgram = new HashMap<>();

        for (OrgUnitProgramRelationDB orgUnitProgramRelationDB : orgUnitProgramRelationsDB) {
            if (orgUnitProgramRelationDB.getId_org_unit_fk().equals(
                    orgUnitDB.getId_org_unit())) {
                String programUid = getProgramUid(orgUnitProgramRelationDB.getId_program_fk());
                productivityByProgram.put(programUid,
                        orgUnitProgramRelationDB.getProductivity());
            }
        }

        return productivityByProgram;
    }
}