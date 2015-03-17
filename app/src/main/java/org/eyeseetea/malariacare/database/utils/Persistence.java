package org.eyeseetea.malariacare.database.utils;

import com.orm.query.Select;

import org.eyeseetea.malariacare.database.model.Tab;

import java.util.List;

/**
 * Created by adrian on 16/03/15.
 */
public class Persistence {

    public static List<Tab> getTabs(){
        return Select.from(Tab.class).orderBy("orderpos").list();
    }
}
