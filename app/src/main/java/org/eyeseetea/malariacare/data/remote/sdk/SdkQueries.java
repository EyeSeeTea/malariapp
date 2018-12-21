package org.eyeseetea.malariacare.data.remote.sdk;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.ProgramExtended;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.AttributeValueFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.DataElementFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.DataElementFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OptionSetFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitLevelFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitLevelFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitToProgramRelationFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow
        .OrganisationUnitToProgramRelationFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageDataElementFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageDataElementFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageSectionFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageSectionFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.UserAccountFlow;
import org.hisp.dhis.client.sdk.models.program.ProgramType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SdkQueries {

    public static List<String> getAssignedProgramUids(String pullProgramCode) {
        List<String> uids = new ArrayList<>();
        List<ProgramExtended> programExtendeds = ProgramExtended.getAllPrograms(pullProgramCode);
        for (ProgramExtended programExtended : programExtendeds) {
            uids.add(programExtended.getUid());
        }
        return uids;
    }

    public static ProgramFlow getProgram(String assignedProgramID) {
        return new Select().from(ProgramFlow.class).where(
                ProgramFlow_Table.uId.eq(assignedProgramID)).querySingle();
    }

    public static List<OptionSetFlow> getOptionSets() {
        return new Select().from(OptionSetFlow.class).queryList();
    }

    public static UserAccountFlow getUserAccount() {
        return new Select().from(UserAccountFlow.class).querySingle();
    }

    public static DataElementFlow getDataElement(String UId) {
        return new Select().from(DataElementFlow.class).where(DataElementFlow_Table.uId.
                is(UId)).querySingle();
    }

    public static List<OrganisationUnitLevelFlow> getOrganisationUnitLevels() {
        return new Select().from(OrganisationUnitLevelFlow.class)
                .orderBy(OrganisationUnitLevelFlow_Table.level, true)
                .queryList();
    }

    public static List<OrganisationUnitFlow> getAssignedOrganisationUnits() {
        return new Select().from(OrganisationUnitFlow.class)
                .queryList();
    }

    public static List<ProgramFlow> getProgramsForOrganisationUnit(String UId, String programAttribute,
            ProgramType... programType) {

        List<OrganisationUnitToProgramRelationFlow> organisationUnitProgramRelationships =
                new Select().from(OrganisationUnitToProgramRelationFlow.class).where(
                        OrganisationUnitToProgramRelationFlow_Table.organisationUnit.
                                is(UId)).queryList();

        List<String> assignedProgramUids = SdkQueries.getAssignedProgramUids(programAttribute);
        List<ProgramFlow> programs = new ArrayList<ProgramFlow>();
        for (OrganisationUnitToProgramRelationFlow oupr : organisationUnitProgramRelationships) {
            addProgramsWithValidProgramTypes(assignedProgramUids, programs, oupr, programType);
        }
        return programs;
    }

    private static void addProgramsWithValidProgramTypes(List<String> assignedProgramUids, List<ProgramFlow> programs, OrganisationUnitToProgramRelationFlow oupr, ProgramType[] programType) {
        if (checkNulls(assignedProgramUids, oupr, programType)) {
            for(ProgramType programtypename : programType){
                if(oupr.getProgram().getProgramType().equals(programtypename)) {
                    programs.add(oupr.getProgram());
                }
            }
        }
    }

    private static boolean checkNulls(List<String> assignedProgramUids, OrganisationUnitToProgramRelationFlow oupr, ProgramType[] programType) {
        return oupr.getProgram() != null
                && assignedProgramUids.contains(oupr.getProgram().getUId())
                && programType != null;
    }

    public static List<EventFlow> getEvents() {
        return new Select().from(EventFlow.class).queryList();
    }

    public static ProgramStageFlow getProgramStage(ProgramStageFlow programStage) {
        return new Select().from(ProgramStageFlow.class).where(
                ProgramStageFlow_Table.uId.is(programStage.getUId())).querySingle();
    }

    public static List<ProgramStageFlow> getProgramStages(ProgramFlow program) {
        return new Select().from(ProgramStageFlow.class).where(
                ProgramStageFlow_Table.program.is(program.getUId()))
                .orderBy(OrderBy.fromProperty(ProgramStageFlow_Table.sortOrder)).queryList();
    }

    //ConvertFromSDKVisitor
    public static void saveBatch(final List<Model> insertModels) {
        //Save questions in batch

        DatabaseDefinition databaseDefinition =
                FlowManager.getDatabase(AppDatabase.class);
        databaseDefinition.executeTransaction(new ITransaction() {
            @Override
            public void execute(DatabaseWrapper databaseWrapper) {
                for (Model model : insertModels) {
                    model.insert();
                }
            }
        });
    }

    public static List<ProgramStageSectionFlow> getProgramStageSectionFromProgramStage(String uId) {
        List<ProgramStageSectionFlow> programStageSections = new Select().from(
                ProgramStageSectionFlow.class)
                .where(ProgramStageSectionFlow_Table.programStage
                        .eq(uId))
                .queryList();
        return programStageSections;
    }

    public static List<ProgramStageDataElementFlow> getProgramStageDataElementFromProgramStage(
            String uId) {
        List<ProgramStageDataElementFlow> programStageDataElements = new Select().from(
                ProgramStageDataElementFlow.class)
                .where(ProgramStageDataElementFlow_Table.programStage
                        .eq(uId))
                .queryList();
        return programStageDataElements;
    }


    public static HashMap<String, HashMap<String, AttributeValueFlow>> createHashMapCodeAndReferenceForAttributeValues() {
        HashMap<String, HashMap<String, AttributeValueFlow>> attributes = new HashMap<>();
        List<AttributeValueFlow> attributeValueFlows = new Select().from(AttributeValueFlow.class).queryList();
        for (AttributeValueFlow attributeValueFlow : attributeValueFlows){
            HashMap<String, AttributeValueFlow> attributesValues = new HashMap<>();
            if(attributes.containsKey(attributeValueFlow.getAttribute().getCode())){
                attributesValues = attributes.get(attributeValueFlow.getAttribute().getCode());
            }
            attributesValues.put(attributeValueFlow.getReference(), attributeValueFlow);
            attributes.put(attributeValueFlow.getAttribute().getCode(), attributesValues);
        }
        return attributes;
    }
}
