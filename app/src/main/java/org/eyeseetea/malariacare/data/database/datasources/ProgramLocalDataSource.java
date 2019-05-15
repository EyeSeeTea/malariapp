package org.eyeseetea.malariacare.data.database.datasources;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.domain.entity.Program;

import java.util.ArrayList;
import java.util.List;

public class ProgramLocalDataSource
        implements IMetadataLocalDataSource<Program> {

    @Override
    public List<Program> getAll() {
        List<ProgramDB> programDBS = new Select().from(ProgramDB.class).queryList();

        return mapToDomain(programDBS);
    }

    private List<Program> mapToDomain(List<ProgramDB> programDBs) {
        List<Program> programs = new ArrayList<>();

        for (ProgramDB programDB:programDBs) {

            programs.add(
                    new Program(programDB.getUid(),programDB.getName(),
                            programDB.getStageUid()));
        }

        return programs;
    }
}