package org.psi.malariacare.data;

import com.orm.SugarRecord;

/**
 * Created by adrian on 14/02/15.
 */
public class Value extends SugarRecord<Tab> {

    Option option;
    Question question;

    public Value() {
    }

    public Value(Option option, Question question) {
        this.option = option;
        this.question = question;
    }
}
