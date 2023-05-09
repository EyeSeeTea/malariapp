package org.eyeseetea.malariacare.data.database.datasources;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB_Table;
import org.eyeseetea.malariacare.data.database.model.OrgUnitLevelDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.domain.entity.OrgUnitLevel;

import java.util.ArrayList;
import java.util.List;

public class OrgUnitLevelLocalDataSource
        implements IMetadataLocalDataSource<OrgUnitLevel> {

    List<OrgUnitLevelDB> orgUnitLevelsDB;
    List<OrgUnitProgramRelationDB> orgUnitProgramRelationsDB;
    List<ProgramDB> programsDB;

    @Override
    public List<OrgUnitLevel> getAll() {
        List<OrgUnitLevelDB> orgUnitLevelsDB = new Select().from(OrgUnitLevelDB.class).queryList();

        return mapToDomain(orgUnitLevelsDB);
    }

    @Override
    public OrgUnitLevel getByUid(String uid) {
        OrgUnitLevelDB orgUnitLevelDB = new Select().from(OrgUnitLevelDB.class)
                .where(OrgUnitDB_Table.uid_org_unit.eq(uid)).querySingle();

        return new OrgUnitLevel(orgUnitLevelDB.getUid(), orgUnitLevelDB.getName());
    }

    @NonNull
    private List<OrgUnitLevel> mapToDomain(List<OrgUnitLevelDB> orgUnitLevelsDB) {

        List<OrgUnitLevel> orgUnitLevels = new ArrayList<>();

        for (OrgUnitLevelDB orgUnitLevelDB : orgUnitLevelsDB) {
            OrgUnitLevel orgUnitLevel = new OrgUnitLevel(orgUnitLevelDB.getUid(), orgUnitLevelDB.getName());

            orgUnitLevels.add(orgUnitLevel);
        }

        return orgUnitLevels;
    }
}