package org.eyeseetea.malariacare.database.utils.feedback;

import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by idelcano on 07/07/2016.
 */
public class DashboardSentBundle {
    List<Survey> surveys = new ArrayList<>();
    List<OrgUnit> orgUnits;
    List<Program> programs;

    public DashboardSentBundle(){
        surveys = new ArrayList<>();
        orgUnits = new ArrayList<>();
        programs = new ArrayList<>();
    }
    public List<Survey> getSentSurveys(){return surveys;}
    public void setSentSurveys(List<Survey> surveys){this.surveys=surveys;}
    public List<OrgUnit> getOrgUnits() {
        return orgUnits;
    }

    public void setOrgUnits(List<OrgUnit> orgUnits) {
        this.orgUnits = orgUnits;
    }

    public List<Program> getPrograms() {
        return programs;
    }

    public void setPrograms(List<Program> programs) {
        this.programs = programs;
    }
}
