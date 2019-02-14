package org.eyeseetea.malariacare.data.database.datasources;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitLevelDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;

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

    @Override
    public void clearAndSave(List<OrgUnit> orgUnits) throws Exception {
        Delete.table(OrgUnitDB.class);
        Delete.table(OrgUnitProgramRelationDB.class);

        loadDependants();

        List<OrgUnitDB> orgUnitsDB = mapToDB(orgUnits);

        for (OrgUnitDB orgUnitDB : orgUnitsDB) {
            orgUnitDB.save();

            for(OrgUnitProgramRelationDB orgUnitProgramRelationDB:
                    orgUnitDB.getOrgUnitProgramRelationDBS()){
                orgUnitProgramRelationDB.setOrgUnit(orgUnitDB);

                orgUnitProgramRelationDB.save();
            }
        }
    }

    @NonNull
    private List<OrgUnit> mapToDomain(List<OrgUnitDB> orgUnitsDB) {

        List<OrgUnit> orgUnits = new ArrayList<>();

        String orgUnitLevelUid;
        Map<String, Integer> productivityByProgram;

        for (OrgUnitDB orgUnitDB : orgUnitsDB) {
            orgUnitLevelUid = getOrgUnitLevelUid(orgUnitDB.getId_org_unit_level_fk());

            productivityByProgram = getProductivityByProgram(orgUnitDB);

            orgUnits.add(new OrgUnit(orgUnitDB.getUid(), orgUnitDB.getName(),
                    orgUnitLevelUid, productivityByProgram));
        }

        return orgUnits;
    }

    private List<OrgUnitDB> mapToDB(List<OrgUnit> orgUnits) {
        List<OrgUnitDB> orgUnitsDB = new ArrayList<>();

        for (OrgUnit orgUnit : orgUnits) {
            OrgUnitDB orgUnitDB = new OrgUnitDB();

            orgUnitDB.setName(orgUnit.getName());

            Long orgUnitLevelId = getOrgUnitLevelId(orgUnit.getOrgUnitLevelUid());

            orgUnitDB.setOrgUnitLevel(orgUnitLevelId);

            orgUnitDB.setOrgUnitProgramRelationDBS(mapProductivityByProgramToDB(orgUnit));

            orgUnitsDB.add(orgUnitDB);
        }

        return orgUnitsDB;
    }

    private List<OrgUnitProgramRelationDB> mapProductivityByProgramToDB(OrgUnit orgUnit) {
        List<OrgUnitProgramRelationDB> programRelationDBS = new ArrayList<>();

        for (String programUid: orgUnit.getRelatedPrograms()) {
            Integer productivity = orgUnit.getProductivity(programUid);

            if (productivity != null) {
                Long programId = getProgramId(programUid);

                if (programId != null) {
                    OrgUnitProgramRelationDB orgUnitProgramRelationDB =
                            new OrgUnitProgramRelationDB();
                    orgUnitProgramRelationDB.setProgram(programId);
                    orgUnitProgramRelationDB.setProductivity(productivity);

                    programRelationDBS.add(orgUnitProgramRelationDB);
                }
            }
        }

        return programRelationDBS;
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

    private Long getOrgUnitLevelId(String orgUnitLevelUId) {
        Long orgUnitLevelId = null;

        for (OrgUnitLevelDB orgUnitLevelDB : orgUnitLevelsDB) {
            if (orgUnitLevelUId.equals(orgUnitLevelDB.getUid())) {
                orgUnitLevelId = orgUnitLevelDB.getId_org_unit_level();
                break;
            }
        }

        return orgUnitLevelId;
    }

    private Long getProgramId(String programUId) {
        Long programId = null;

        for (ProgramDB programDB : programsDB) {
            if (programUId.equals(programDB.getUid())) {
                programId = programDB.getId_program();
                break;
            }
        }

        return programId;
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
