package org.eyeseetea.malariacare.database.utils;

import com.orm.query.Select;

import org.eyeseetea.malariacare.database.model.Tab;

import java.util.List;

public class Persistence {

    public static List<Tab> getTabs(){
        return Select.from(Tab.class).orderBy("orderpos").list();
    }
}
