package org.eyeseetea.malariacare.data.database.datasources;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.data.database.model.OrgUnitLevelDB;
import org.eyeseetea.malariacare.domain.entity.OrgUnitLevel;

import java.util.ArrayList;
import java.util.List;

public class OrgUnitLevelLocalDataSource
        implements IMetadataLocalDataSource<OrgUnitLevel> {
    @Override
    public List<OrgUnitLevel> getAll() {
        List<OrgUnitLevelDB> orgUnitLevelsDB = new Select().from(OrgUnitLevelDB.class).queryList();

        return mapToDomain(orgUnitLevelsDB);
    }

    @Override
    public void clearAndSave(List<OrgUnitLevel> orgUnitLevels) throws Exception {
        Delete.table(OrgUnitLevelDB.class);

        List<OrgUnitLevelDB> orgUnitLevelsDB = mapToDB(orgUnitLevels);

        for (OrgUnitLevelDB orgUnitLevelDB:orgUnitLevelsDB) {
            orgUnitLevelDB.save();
        }
    }

    private List<OrgUnitLevel> mapToDomain(List<OrgUnitLevelDB> orgUnitLevelsDB) {
        List<OrgUnitLevel> orgUnitLevels = new ArrayList<>();

        for (OrgUnitLevelDB orgUnitLevelDB:orgUnitLevelsDB) {
            orgUnitLevels.add(new OrgUnitLevel(orgUnitLevelDB.getUid(),orgUnitLevelDB.getName()));
        }

        return orgUnitLevels;
    }

    private List<OrgUnitLevelDB> mapToDB(List<OrgUnitLevel> orgUnitLevels) {
        List<OrgUnitLevelDB> orgUnitLevelsDB = new ArrayList<>();

        for (OrgUnitLevel orgUnitLevel:orgUnitLevels) {
            orgUnitLevelsDB.add(new OrgUnitLevelDB(orgUnitLevel.getUid(),orgUnitLevel.getName()));
        }

        return orgUnitLevelsDB;
    }
}
