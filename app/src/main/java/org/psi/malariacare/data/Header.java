package org.psi.malariacare.data;

import com.orm.SugarRecord;

/**
 * Created by adrian on 14/02/15.
 */
public class Header extends SugarRecord<Tab> {

    String short_name;
    String name;
    Integer order_header;
    Integer master;
    Tab tag;

    public Header() {
    }

    public Header(String short_name, String name, Integer order_header, Integer master, Tab tag) {
        this.short_name = short_name;
        this.name = name;
        this.order_header = order_header;
        this.master = master;
        this.tag = tag;
    }
}
