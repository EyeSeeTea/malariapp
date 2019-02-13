package org.eyeseetea.malariacare.data.remote.sdk.dataSources;

import android.content.Context;

import org.eyeseetea.dhis2.lightsdk.D2Response;
import org.eyeseetea.dhis2.lightsdk.programs.ProgramStage;
import org.eyeseetea.dhis2.lightsdk.programs.ProgramStageSection;
import org.eyeseetea.malariacare.data.boundaries.IMetadataRemoteDataSource;
import org.eyeseetea.malariacare.data.remote.sdk.DhisFilter;
import org.eyeseetea.malariacare.domain.entity.OptionSet;
import org.eyeseetea.malariacare.domain.entity.Program;
import org.eyeseetea.malariacare.domain.entity.Tab;

import java.util.ArrayList;
import java.util.List;

public class ProgramD2LightSDKDataSource
        extends D2LightSDKDataSource
        implements IMetadataRemoteDataSource<Program> {


    public ProgramD2LightSDKDataSource(Context context) {
        super(context);
    }

    @Override
    public List<Program> getAll(DhisFilter filter) throws Exception {

        D2Response<List<org.eyeseetea.dhis2.lightsdk.programs.Program>> programsResponse =
                getD2Api().programs().getAll(filter.getUIds()).execute();

        if (programsResponse.isSuccess()) {
            D2Response.Success<List<org.eyeseetea.dhis2.lightsdk.programs.Program>> success =
                    (D2Response.Success<List<org.eyeseetea.dhis2.lightsdk.programs.Program>>)
                            programsResponse;

            return mapToDomain(success.getValue());
        } else {
            D2Response.Error errorResponse = (D2Response.Error) programsResponse;

            handleError(errorResponse);
        }

        return null;
    }

    private List<Program> mapToDomain(
            List<org.eyeseetea.dhis2.lightsdk.programs.Program> dhisPrograms) {
        List<Program> programs = new ArrayList<>();

        for (org.eyeseetea.dhis2.lightsdk.programs.Program dhisProgram : dhisPrograms) {

            String programStageUid = "";

            List<Tab> tabs = new ArrayList<>();

            for (ProgramStage programStage : dhisProgram.getProgramStages()) {

                programStageUid = programStage.getId();

                for (ProgramStageSection programStageSection : programStage
                        .getProgramStageSections()) {
                    Tab tab = new Tab(
                            programStageSection.getId(),
                            programStageSection.getName(),
                            programStageSection.getSortOrder());

                    tabs.add(tab);
                }
            }
            programs.add(new Program(
                    dhisProgram.getId(), dhisProgram.getDisplayName(), programStageUid, tabs));
        }

        return programs;
    }
}