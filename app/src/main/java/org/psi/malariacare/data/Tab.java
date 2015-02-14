package org.psi.malariacare.data;

import com.orm.SugarRecord;

/**
 * Created by adrian on 14/02/15.
 */
public class Tab extends SugarRecord<Tab> {

    String name;
    Integer order_tab;

    public Tab() {
    }

    public Tab(String name, Integer order_tab) {
        this.name = name;
        this.order_tab = order_tab;
    }
}
