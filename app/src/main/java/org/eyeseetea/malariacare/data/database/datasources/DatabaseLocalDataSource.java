package org.eyeseetea.malariacare.data.database.datasources;

import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.CompositeScoreBuilder;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.AttributeFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.AttributeValueFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.DataElementFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OptionFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OptionSetFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitToProgramRelationFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageDataElementFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageSectionFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.UserAccountFlow;

import java.util.List;

public class DatabaseLocalDataSource {
    public final static String TAG = "DatabaseLocalDataSource";


    public final static Class[] MANDATORY_METADATA_TABLES = {
            AttributeFlow.class,
            DataElementFlow.class,
            AttributeValueFlow.class,
            OptionFlow.class,
            OptionSetFlow.class,
            UserAccountFlow.class,
            OrganisationUnitFlow.class,
            OrganisationUnitToProgramRelationFlow.class,
            ProgramStageFlow.class,
            ProgramStageDataElementFlow.class,
            ProgramStageSectionFlow.class
    };


    public boolean mandatoryMetadataTablesNotEmpty() {

        int elementsInTable = 0;
        for (Class table : MANDATORY_METADATA_TABLES) {
            elementsInTable = (int) new SQLite().selectCountOf()
                    .from(table).count();
            if (elementsInTable == 0) {
                Log.d(TAG, "Error empty table: " + table.getName());
                return false;
            }
        }
        return true;
    }

    public boolean validateCS() {
        boolean wrongCS = false;
        List<CompositeScoreDB> compositeScores = CompositeScoreDB.list();
        for (CompositeScoreDB compositeScore : compositeScores) {
            if (!compositeScore.hasChildren() && (compositeScore.getQuestions() == null
                    || compositeScore.getQuestions().size() == 0)) {
                Log.d(TAG,
                        "CompositeScoreDB without children and without questions will be removed: "
                                + compositeScore.toString());
                wrongCS=true;
                compositeScore.delete();
                continue;
            }
            if (compositeScore.getHierarchical_code() == null) {
                Log.d(TAG, "CompositeScoreDB without hierarchical code will be removed: "
                        + compositeScore.toString());
                wrongCS=true;
                compositeScore.delete();
                continue;
            }
            if (compositeScore.getComposite_score() == null
                    && !compositeScore.getHierarchical_code().equals(
                    CompositeScoreBuilder.ROOT_NODE_CODE)) {
                Log.d(TAG, "CompositeScoreDB not root and not parent should be fixed: "
                        + compositeScore.toString());
                wrongCS=true;
                continue;
            }
        }
        return !wrongCS;
    }
}
