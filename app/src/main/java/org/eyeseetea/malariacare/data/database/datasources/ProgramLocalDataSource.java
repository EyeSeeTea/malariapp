package org.eyeseetea.malariacare.data.database.datasources;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB_Table;
import org.eyeseetea.malariacare.domain.entity.Program;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ProgramLocalDataSource
        implements IMetadataLocalDataSource<Program> {

    @Override
    public List<Program> getAll() {
        List<ProgramDB> programDBS = new Select().from(ProgramDB.class).queryList();

        return mapToDomain(programDBS);
    }

    @Override
    public Program getByUid(String uid) {
        ProgramDB programDB =
                new Select().from(ProgramDB.class)
                        .where(ProgramDB_Table.uid_program.eq(uid))
                        .querySingle();

        return mapProgram(programDB);
    }

    private List<Program> mapToDomain(List<ProgramDB> programDBs) {
        List<Program> programs = new ArrayList<>();

        for (ProgramDB programDB:programDBs) {

            programs.add(mapProgram(programDB));
        }

        return programs;
    }

    @NotNull
    private Program mapProgram(ProgramDB programDB) {
        return new Program(programDB.getUid(),programDB.getName(),
                programDB.getStageUid());
    }
}