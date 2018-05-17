package org.eyeseetea.malariacare.data.database;


import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.eyeseetea.malariacare.data.database.model.AnswerDB;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.HeaderDB;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.ServerMetadataDB;
import org.eyeseetea.malariacare.domain.boundary.IMetadataValidator;

public class MetadataValidator implements IMetadataValidator {

    @Override
    public boolean isValid() {
        return mandatoryMetadataTablesNotEmpty();
    }

    public final static Class[] MANDATORY_METADATA_TABLES = {
            AnswerDB.class,
            CompositeScoreDB.class,
            HeaderDB.class,
            OptionDB.class,
            OrgUnitDB.class,
            ProgramDB.class,
            QuestionDB.class,
            ServerMetadataDB.class
    };


    public boolean mandatoryMetadataTablesNotEmpty() {

        int elementsInTable = 0;
        for (Class table : MANDATORY_METADATA_TABLES) {
            elementsInTable = (int) new SQLite().selectCountOf()
                    .from(table).count();
            if (elementsInTable == 0) {
                Log.d(getClass().getName(), "Error empty table: " + table.getName());
                return false;
            }
        }
        return true;
    }
}
