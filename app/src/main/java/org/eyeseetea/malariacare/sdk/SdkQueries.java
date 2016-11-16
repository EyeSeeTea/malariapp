package org.eyeseetea.malariacare.sdk;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.ConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.model.Media;
import org.eyeseetea.malariacare.database.model.OrganisationUnitLevelFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.DataElementFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OptionSetFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.UserAccountFlow;
import org.hisp.dhis.client.sdk.models.program.ProgramType;

import java.util.List;

/**
 * Created by idelcano on 15/11/2016.
 */

public class SdkQueries {

    public static List<String> getAssignedPrograms() {
        return null;
        //return MetaDataController.getAssignedPrograms();
    }

    public static ProgramFlow getProgram(String assignedProgramID) {
        return null;
        //return MetaDataController.getProgram(assignedProgramID);
    }

    public static List<OptionSetFlow> getOptionSets() {
        return null;
        //MetaDataController.getOptionSets();
    }

    public static UserAccountFlow getUserAccount() {
        return null;
        //return etaDataController.getUserAccount();
    }

    public static DataElementFlow getDataElement(DataElementFlow dataElement) {
        return null;
        //return MetaDataController.getDataElement(dataElement.getId());
    }

    public static DataElementFlow getDataElement(String UId) {
        return null;
        //return MetaDataController.getDataElement(UId);
    }

    public static List<OrganisationUnitLevelFlow> getOrganisationUnitLevels() {
        return null;
        //return MetaDataController.getOrganisationUnitLevels();
    }

    public static List<OrganisationUnitFlow> getAssignedOrganisationUnits() {
        return null;
        //return MetaDataController.getAssignedOrganisationUnits();
    }

    public static List<ProgramFlow> getProgramsForOrganisationUnit(String UId, ProgramType programType) {
        return null;
        //return MetaDataController.getProgramsForOrganisationUnit(UId, programType);
    }

    public static List<EventFlow> getEvents(String organisationUnitUId, String ProgramUId) {
        return null;
        //return TrackerController.getEvents(organisationUnitUId, ProgramUId);
    }

    public static ProgramStageFlow getProgramStage(ProgramStageFlow programStage) {
        return null;
        //return MetaDataController.getProgramStage(programStage);
    }

    public static List<ProgramStageFlow> getProgramStages(ProgramFlow program) {
        return null;
    }

    //ConvertFromSDKVisitor
    public static void saveBatch() {
        /*
        //Save questions in batch
        new SaveModelTransaction<>(ProcessModelInfo.withModels(ConvertFromSDKVisitor.questions)).onExecute();

        //Refresh media references
        List<Media> medias = ConvertFromSDKVisitor.questionBuilder.getListMedia();
        for(Media media: medias){
            media.updateQuestion();
        }
        //Save media in batch
        new SaveModelTransaction<>(ProcessModelInfo.withModels(medias)).onExecute();
        */
    }
}
