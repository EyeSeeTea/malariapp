package org.eyeseetea.malariacare.data.database.datasources;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.TabDB;
import org.eyeseetea.malariacare.domain.entity.Program;
import org.eyeseetea.malariacare.domain.entity.Tab;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ProgramLocalDataSource
        implements IMetadataLocalDataSource<Program> {

    @Override
    public List<Program> getAll() {
        List<ProgramDB> programDBS = new Select().from(ProgramDB.class).queryList();
        List<TabDB> tabDBS = new Select().from(TabDB.class).queryList();

        return mapToDomain(programDBS, tabDBS);
    }

    @Override
    public void clearAndSave(List<Program> programs) throws Exception {
        Delete.table(ProgramDB.class);
        Delete.table(TabDB.class);

        List<ProgramDB> programDBS = mapToDB(programs);

        for (ProgramDB programDB:programDBS) {
            programDB.save();

            for (TabDB tabDB:programDB.getTabs()) {
                tabDB.setProgram(programDB);
                tabDB.save();
            }
        }
    }

    private List<Program> mapToDomain(List<ProgramDB> programDBs, List<TabDB> tabDBS) {
        List<Program> programs = new ArrayList<>();

        for (ProgramDB programDB:programDBs) {
            List<Tab> tabsByProgram = getTabsByProgram(programDB,tabDBS);

            programs.add(
                    new Program(programDB.getUid(),programDB.getName(),
                            programDB.getStageUid(),tabsByProgram));
        }

        return programs;
    }

    private List<ProgramDB> mapToDB(List<Program> programs) {
        List<ProgramDB> programDBS = new ArrayList<>();

        for (Program program:programs) {
            ProgramDB programDB = new ProgramDB();
            programDB.setUid(program.getUid());
            programDB.setName(program.getName());
            programDB.setStageUid(program.getProgramStageUid());

            List<TabDB> tabDBS = new ArrayList<>();

            for (Tab tab:program.getTabs()) {
                TabDB tabDB = new TabDB();
                tabDB.setUid(tab.getUid());
                tabDB.setName(tab.getName());
                tabDB.setOrder_pos(tab.getOrderPosition());
                tabDB.setType(Constants.TAB_AUTOMATIC);
                tabDBS.add(tabDB);
            }

            programDB.setTabs(tabDBS);
            programDBS.add(programDB);
        }

        return programDBS;
    }

    private List<Tab> getTabsByProgram(ProgramDB programDB, List<TabDB> tabDBS) {
        List<Tab> tabs = new ArrayList<>();

        for (TabDB tabDB:tabDBS) {
            if (tabDB.getId_program_fk().equals(programDB.getId_program())) {
                Tab tab = new Tab(tabDB.getUid(), tabDB.getName(), tabDB.getOrder_pos());

                tabs.add(tab);
            }
        }

        return tabs;
    }
}
