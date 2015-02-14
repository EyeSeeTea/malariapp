package org.psi.malariacare.database;

import android.database.Cursor;

/**
 * Created by adrian on 14/02/15.
 */
public interface IMalacariaDBService {

    public Cursor getQuestions();

    public Cursor getQuestionsByTag(String tagId);



   // public Cursor putValue(Value value);


    public void close();


}
