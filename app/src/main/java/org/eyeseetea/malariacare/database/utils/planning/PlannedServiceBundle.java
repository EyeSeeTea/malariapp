package org.eyeseetea.malariacare.database.utils.planning;

import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by idelcano on 08/08/2016.
 */
public class PlannedServiceBundle {
    private List<PlannedItem> plannedItems = new ArrayList<>();
    private List<OrgUnit> orgUnits;
    private List<Program> programs;

    public PlannedServiceBundle(){
    }
    public List<PlannedItem> getPlannedItems(){return plannedItems;}
    public void setPlannedItems(List<PlannedItem> plannedItems){this.plannedItems=plannedItems;}
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
