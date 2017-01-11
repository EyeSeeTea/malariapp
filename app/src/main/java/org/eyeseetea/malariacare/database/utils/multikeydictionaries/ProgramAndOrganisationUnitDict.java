package org.eyeseetea.malariacare.database.utils.multikeydictionaries;

/**
 * Created by idelcano on 11/01/17.
 */

import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;



/**
 * This class is a dictionary for program and organisationunits
 */
public class ProgramAndOrganisationUnitDict {
    org.hisp.dhis.client.sdk.models.program.Program program;
    OrganisationUnit organisationUnit;

    public ProgramAndOrganisationUnitDict(org.hisp.dhis.client.sdk.models.program.Program program,
            OrganisationUnit organisationUnit) {
        this.program = program;
        this.organisationUnit = organisationUnit;
    }

    public org.hisp.dhis.client.sdk.models.program.Program getProgram() {
        return program;
    }

    public OrganisationUnit getOrganisationUnit() {
        return organisationUnit;
    }
}

