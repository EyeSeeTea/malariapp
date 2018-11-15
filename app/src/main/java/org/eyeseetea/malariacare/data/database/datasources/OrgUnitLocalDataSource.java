package org.eyeseetea.malariacare.data.database.datasources;

import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB_Table;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB_Table;
import org.eyeseetea.malariacare.domain.boundary.IOrgUnitRepository;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eyeseetea.malariacare.data.database.AppDatabase.orgUnitProgramRelationAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.orgUnitProgramRelationName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programName;

public class OrgUnitLocalDataSource implements IOrgUnitRepository {
    private static final Integer DEFAULT_PRODUCTIVITY = 0;

    @Override
    public List<OrgUnit> getAll() {
        List<OrgUnit> orgUnits = new ArrayList<>();

        for(OrgUnitDB orgUnitDB : new Select().from(OrgUnitDB.class).queryList()){
            List<String> programs = getAllRelatedPrograms(orgUnitDB);
            Map<String,Integer> productivityByOrgUnit = getProductivityByProgram(orgUnitDB);
            OrgUnit orgUnit = map(orgUnitDB, programs, productivityByOrgUnit);
            orgUnits.add(orgUnit);
        }
        return orgUnits;
    }

    private List<String> getAllRelatedPrograms(OrgUnitDB orgUnitDb) {
        List<String> uids = new ArrayList<>();
        for(ProgramDB programDB : getPrograms(orgUnitDb)){
            uids.add(programDB.getUid());
        }
        return uids;
    }

    private static Map<String,Integer> getProductivityByProgram(OrgUnitDB orgUnitDB){
        Map<String,Integer> productivityByOrgUnit = new HashMap<>();
        for(ProgramDB program : getPrograms(orgUnitDB)) {
            OrgUnitProgramRelationDB
                    orgUnitProgramRelation = new Select().from(OrgUnitProgramRelationDB.class)
                    .where(OrgUnitProgramRelationDB_Table.id_org_unit_fk.eq(orgUnitDB.getId_org_unit()))
                    .and(OrgUnitProgramRelationDB_Table.id_program_fk.eq(program.getId_program())).querySingle();
            int productivity = DEFAULT_PRODUCTIVITY;
            if(orgUnitProgramRelation != null){
                productivity = orgUnitProgramRelation.getProductivity();
            }
            productivityByOrgUnit.put(program.getUid(), productivity);
        }
        return productivityByOrgUnit;
    }

    private static List<ProgramDB> getPrograms(OrgUnitDB orgUnitDB){
        return new Select().from(ProgramDB.class).as(programName)
                .join(OrgUnitProgramRelationDB.class, Join.JoinType.LEFT_OUTER).as(orgUnitProgramRelationName)
                .on(ProgramDB_Table.id_program.withTable(programAlias)
                        .eq(OrgUnitProgramRelationDB_Table.id_program_fk.withTable(orgUnitProgramRelationAlias))
                ).where(OrgUnitProgramRelationDB_Table.id_org_unit_fk.withTable(orgUnitProgramRelationAlias).eq(orgUnitDB.getId_org_unit()))
                .orderBy(ProgramDB_Table.name.withTable(programAlias), true)
                .queryList();
    }

    private OrgUnit map(OrgUnitDB orgUnitDB, List<String> programs, Map<String,Integer> productivityByOrgUnit) {
        String orgUnitLevel = null;
        if(orgUnitDB.getOrgUnitLevel()!=null){
            orgUnitLevel = orgUnitDB.getOrgUnitLevel().getUid();
        }
        OrgUnit orgUnit = new OrgUnit(orgUnitDB.getUid(), orgUnitDB.getName(), orgUnitLevel, productivityByOrgUnit);

        orgUnit.addRelatedPrograms(programs);

        return orgUnit;
    }
}
