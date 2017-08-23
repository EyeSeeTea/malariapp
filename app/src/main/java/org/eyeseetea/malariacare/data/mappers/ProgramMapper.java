package org.eyeseetea.malariacare.data.mappers;


import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.domain.entity.Program;

import java.util.ArrayList;
import java.util.List;

public class ProgramMapper {
    public static List<Program> mapFromDbToDomain(List<ProgramDB> programDBs) {
        List<Program> programs = new ArrayList<>();
        for (ProgramDB programDB : programDBs) {
            programs.add(mapFromDbToDomain(programDB));
        }
        return programs;
    }

    public static Program mapFromDbToDomain(ProgramDB programDB) {
        return new Program(programDB.getName(), programDB.getUid(), programDB.getId_program());
    }
}
