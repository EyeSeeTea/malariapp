package org.eyeseetea.malariacare.data.remote;

import static org.eyeseetea.malariacare.data.database.AppDatabase.programStageDataElementFlowAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programStageDataElementFlowName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programStageFlowAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programStageFlowName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programStageSectionFlowAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programStageSectionFlowName;
import static org.hisp.dhis.client.sdk.android.api.persistence.DbFlowOperation.insert;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.ConvertFromSDKVisitor;
import org.eyeseetea.malariacare.data.database.model.Question;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.DataElementFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.DataElementFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OptionSetFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitFlow_Table;
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
import org.hisp.dhis.client.sdk.core.common.persistence.DbOperation;
import org.hisp.dhis.client.sdk.core.common.persistence.DbOperationImpl;
import org.hisp.dhis.client.sdk.core.common.persistence.TransactionManager;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.models.program.ProgramType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by idelcano on 15/11/2016.
 */

public class SdkQueries {

    public static List<String> getAssignedPrograms() {
        //return MetaDataController.getAssignedPrograms();
        List<String> uids = new ArrayList<>();
        List<ProgramFlow> programsFlow = new Select().from(ProgramFlow.class).queryList();
        for (ProgramFlow programFlow : programsFlow) {
            uids.add(programFlow.getUId());
        }
        return uids;
    }


    private static OrganisationUnitFlow getOrganisationUnit(String organisationUnitUId) {
        return new Select().from(OrganisationUnitFlow.class).where(
                OrganisationUnitFlow_Table.uId.eq(organisationUnitUId)).querySingle();

    }

    public static ProgramFlow getProgram(String assignedProgramID) {
        //return MetaDataController.getProgram(assignedProgramID);
        return new Select().from(ProgramFlow.class).where(
                ProgramFlow_Table.uId.eq(assignedProgramID)).querySingle();
    }

    public static List<OptionSetFlow> getOptionSets() {
        return new Select().from(OptionSetFlow.class).queryList();
        //MetaDataController.getOptionSets();
    }

    public static UserAccountFlow getUserAccount() {
        return new Select().from(UserAccountFlow.class).querySingle();
        //return MetaDataController.getUserAccount();
    }

    public static DataElementFlow getDataElement(DataElementFlow dataElement) {
        //return MetaDataController.getDataElement(dataElement.getId());
        return new Select().from(DataElementFlow.class).where(DataElementFlow_Table.uId.
                is(dataElement.getUId())).querySingle();
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
        //return MetaDataController.getAssignedOrganisationUnits();
    }

    public static List<ProgramFlow> getProgramsForOrganisationUnit(String UId,
            ProgramType... programType) {

        List<OrganisationUnitToProgramRelationFlow> organisationUnitProgramRelationships =
                new Select().from(OrganisationUnitToProgramRelationFlow.class).where(
                        OrganisationUnitToProgramRelationFlow_Table.organisationUnit.
                                is(UId)).queryList();

        List<ProgramFlow> programs = new ArrayList<ProgramFlow>();
        for (OrganisationUnitToProgramRelationFlow oupr : organisationUnitProgramRelationships) {
            if (programType != null) {
                for (ProgramType kind : programType) {
                    if(oupr.getProgram()==null)
                        continue;
                    List<ProgramFlow> plist = new Select().from(ProgramFlow.class).where(
                            ProgramFlow_Table.id.is(oupr.getProgram().getId()))
                            .and(
                                    ProgramFlow_Table.programType.is(kind)).queryList();
                    programs.addAll(plist);
                }
            }
        }
        return programs;
    }

    public static List<EventFlow> getEvents(String organisationUnitUId, String programUId) {
        //Observer form server
        /*
        ProgramFlow programFlow=getProgram(programUId);
        OrganisationUnitFlow organisationUnitFlow = getOrganisationUnit(organisationUnitUId);
        OrganisationUnit organisationUnit = OrganisationUnitFlow.MAPPER.mapToModel
        (organisationUnitFlow);

        Program program = ProgramFlow.MAPPER.mapToModel(programFlow);
        Observable<List<Event>> eventListObservable = D2.events().list(
                organisationUnit,
                program);
        eventListObservable.
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Event>>() {
                    @Override
                    public void call(List<Event> events) {
                        //// FIXME: 22/11/2016 return event
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                });
        */
        return new Select().from(EventFlow.class).where(
                EventFlow_Table.orgUnit.eq(organisationUnitUId))
                .and(EventFlow_Table.program.eq(programUId)).queryList();
    }

    public static List<EventFlow> getEvents() {
        return new Select().from(EventFlow.class).queryList();
    }

    public static ProgramStageFlow getProgramStage(ProgramStageFlow programStage) {
        return new Select().from(ProgramStageFlow.class).where(
                ProgramStageFlow_Table.id.is(programStage.getId())).querySingle();
        //return MetaDataController.getProgramStage(programStage);
    }

    public static List<ProgramStageFlow> getProgramStages(ProgramFlow program) {
        return new Select().from(ProgramStageFlow.class).where(
                ProgramStageFlow_Table.program.is(program.getUId()))
                .orderBy(OrderBy.fromProperty(ProgramStageFlow_Table.sortOrder)).queryList();
    }

    //ConvertFromSDKVisitor
    public static void saveBatch() {
        //Save questions in batch

        DatabaseDefinition databaseDefinition =
                FlowManager.getDatabase(AppDatabase.class);
        databaseDefinition.executeTransaction(new ITransaction() {
            @Override
            public void execute(DatabaseWrapper databaseWrapper) {
                for (Question question : ConvertFromSDKVisitor.questions) {
                    question.insert();
                }
            }
        });
    }

    public static Event getEvent(String uId) {
        return D2.events().get(uId).toBlocking().first();
    }

    public static List<ProgramStageSectionFlow> getProgramStageSectionFromProgramStage(String uId) {
        List<ProgramStageSectionFlow> programStageSections = new Select().from(
                ProgramStageSectionFlow.class).as(
                programStageSectionFlowName)
                .join(ProgramStageFlow.class, Join.JoinType.LEFT_OUTER).as(
                        programStageFlowName)
                .on(ProgramStageFlow_Table.uId.withTable(programStageFlowAlias)
                        .eq(ProgramStageSectionFlow_Table.programStage.withTable(
                                programStageSectionFlowAlias)))
                .where(ProgramStageFlow_Table.uId.withTable(programStageFlowAlias)
                        .eq(uId))
                .queryList();
        return programStageSections;
    }

    public static List<ProgramStageDataElementFlow> getProgramStageDataElementFromProgramStage(String uId) {
        List<ProgramStageDataElementFlow> programStageDataElements = new Select().from(
                ProgramStageDataElementFlow.class).as(
                programStageDataElementFlowName)
                .join(ProgramStageFlow.class, Join.JoinType.LEFT_OUTER).as(
                        programStageFlowName)
                .on(ProgramStageFlow_Table.uId.withTable(programStageFlowAlias)
                        .eq(ProgramStageDataElementFlow_Table.programStage.withTable(
                                programStageDataElementFlowAlias)))
                .where(ProgramStageFlow_Table.uId.withTable(programStageFlowAlias)
                        .eq(uId))
                .queryList();
        return programStageDataElements;
    }
}
