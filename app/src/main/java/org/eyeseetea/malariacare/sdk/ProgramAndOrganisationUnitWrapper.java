package org.eyeseetea.malariacare.sdk;

import android.util.Log;

import org.eyeseetea.malariacare.database.utils.multikeydictionaries.ProgramAndOrganisationUnitDict;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by idelcano on 11/01/17.
 */

public class ProgramAndOrganisationUnitWrapper {

    private HashMap<Program, List<OrganisationUnit>>
            programsAndOrganisationUnits;
    private List<Program> sdkPrograms;

    public ProgramAndOrganisationUnitWrapper() {
        programsAndOrganisationUnits = new HashMap<>();
        sdkPrograms = new ArrayList<>();
    }


    /**
     * This method gets a organisation unit and program for each program(with organisation units)
     * and removes it(it removes the organisation unit and the program without organisation units)
     */
    public void buildProgramAndOrganisationUnit(List
            <OrganisationUnit> organisationUnits, List<Program> programs) {
        sdkPrograms = programs;
        addPrograms(sdkPrograms);
        for (Program program : programs) {
            for (OrganisationUnit organisationUnit : organisationUnits) {
                for (Program orgUnitProgram : organisationUnit.getPrograms()) {
                    Log.d("programs", "program org unit"+ orgUnitProgram.getUId() + " program sdk "+ program.getUId() +"org unit" +organisationUnit.getUId() );
                    if (orgUnitProgram.getUId().equals(program.getUId())) {
                        Log.d("programs", "program org unit"+ orgUnitProgram.getUId() + " program sdk "+ program.getUId() +"org unit" +organisationUnit.getUId() );
                        addOrganisationUnit(program, organisationUnit);
                        continue;
                    }
                }
            }
        }
    }

    private void addPrograms(List<Program> programs) {
        for(Program program:programs) {
            List<OrganisationUnit> organisationUnitsTemp = new ArrayList<>();
            programsAndOrganisationUnits.put(program, organisationUnitsTemp);
        }
    }

    private void addOrganisationUnit(Program program, OrganisationUnit organisationUnit) {
        if (programsAndOrganisationUnits.containsKey(
                program)) {
            programsAndOrganisationUnits.get(program).add(organisationUnit);
        } else {
        }
    }

    /**
     * This method gets the next organisation unit and program and removes it from the wrapper
     */
    public ProgramAndOrganisationUnitDict popNextProgramAndOrganisationUnit() {
        ProgramAndOrganisationUnitDict programAndOrganisationUnitDict = getNextProgramAndOrganisationUnit();
        if(programAndOrganisationUnitDict!=null){
            removeNextProgramAndOrganisationUnit();
        }
        return programAndOrganisationUnitDict;
    }

    public ProgramAndOrganisationUnitDict getNextProgramAndOrganisationUnit() {
        if (sdkPrograms == null || sdkPrograms.size() == 0 || programsAndOrganisationUnits == null
                || programsAndOrganisationUnits.size() == 0) {
            return null;
        }

        //get always the first program organisationUnit list
        List<OrganisationUnit> organisationUnits = programsAndOrganisationUnits.get(
                sdkPrograms.get(0));
        Program program = sdkPrograms.get(0);

        OrganisationUnit organisationUnit = organisationUnits.get(0);
        return new ProgramAndOrganisationUnitDict(program, organisationUnit);
    }

    private void removeNextProgramAndOrganisationUnit() {
        List <OrganisationUnit> nextOrganisationUnit=programsAndOrganisationUnits.get(sdkPrograms.get(0));
        if(nextOrganisationUnit==null || nextOrganisationUnit.size()==0) {
            removeNextProgram();
        }
        else{
            nextOrganisationUnit.remove(0);
            if(nextOrganisationUnit.size()==0){
                removeNextProgram();
            }
            if(sdkPrograms.size()>0) {
                programsAndOrganisationUnits.put(sdkPrograms.get(0), nextOrganisationUnit);
            }
        }
    }

    private void removeNextProgram() {
        programsAndOrganisationUnits.remove(sdkPrograms.get(0));
        sdkPrograms.remove(0);
    }

}
