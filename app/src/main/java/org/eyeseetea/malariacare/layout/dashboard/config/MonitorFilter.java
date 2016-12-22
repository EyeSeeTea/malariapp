package org.eyeseetea.malariacare.layout.dashboard.config;

/**
 * * Represents the origin of info (csv, dhis server, ..)
 * Created by arrizabalaga on 16/03/16.
 */
public enum MonitorFilter {
    ALL("all"),
    ORG_UNIT("orgunit"),
    PROGRAM("program");

    private final String id;

    MonitorFilter(final String id){
            this.id=id;
        }

    public String toString(){
            return id;
        }

    public static MonitorFilter fromId(final String id){
        if(id==null){
            return null;
        }

        for(MonitorFilter monitorFilter: MonitorFilter.values()){
            if(id.equalsIgnoreCase(monitorFilter.id)){
                return monitorFilter;
            }
        }

        return null;
    }
}
