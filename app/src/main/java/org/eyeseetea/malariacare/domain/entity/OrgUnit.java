package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.common.RequiredChecker.required;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OrgUnit implements IMetadata {

    private final String uid;
    private final String name;
    private final String orgUnitLevelUid;
    private final List<String> relatedPrograms;
    private final Map<String,Integer> productivityByOrgUnit;

    public OrgUnit(String uid, String name, String orgUnitLevelUid,
            Map<String,Integer> productivityByProgram){
        required(uid,"uid is required");
        required(name,"name is required");
        required(orgUnitLevelUid,"orgUnitLevelUid is required");
        required(productivityByProgram,"productivityByProgram is required");

        this.uid = uid;
        this.name = name;
        this.orgUnitLevelUid = orgUnitLevelUid;
        this.relatedPrograms = new ArrayList<>();
        this.productivityByOrgUnit = productivityByProgram;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getOrgUnitLevelUid() {
        return orgUnitLevelUid;
    }

    public void addRelatedPrograms(List<String> relatedPrograms) {
        this.relatedPrograms.addAll(relatedPrograms);
    }

    public List<String> getRelatedPrograms() {
        return Collections.unmodifiableList(relatedPrograms);
    }

    public Integer getProductivity(String programUId){
        if (productivityByOrgUnit.containsKey(programUId)){
            return productivityByOrgUnit.get(programUId);
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrgUnit orgUnit = (OrgUnit) o;

        if (!uid.equals(orgUnit.uid)) return false;
        if (!name.equals(orgUnit.name)) return false;
        if (!orgUnitLevelUid.equals(orgUnit.orgUnitLevelUid)) return false;
        return relatedPrograms.equals(orgUnit.relatedPrograms);
    }

    @Override
    public int hashCode() {
        int result = uid.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + orgUnitLevelUid.hashCode();
        result = 31 * result + relatedPrograms.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "OrgUnit{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", orgUnitLevelUid='" + orgUnitLevelUid + '\'' +
                ", relatedPrograms=" + relatedPrograms +
                '}';
    }
}