package org.eyeseetea.malariacare.adapters;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.eyeseetea.malariacare.R;

/**
 * Created by Jose on 03/03/2015.
 */
public class ManualIQAAdapter {

    private final Activity context;
    private final int[] ids_combos;

    public ManualIQAAdapter(Activity context)
    {
        this.context = context;
        ids_combos=new int[20];

        Spinner view = (Spinner) context.findViewById(R.id.act1);

        ids_combos[0]= R.id.testresult1;
        ids_combos[1]= R.id.testresult2;
        ids_combos[2]= R.id.testresult3;
        ids_combos[3]= R.id.testresult4;
        ids_combos[4]= R.id.testresult5;
        ids_combos[5]= R.id.testresult6;
        ids_combos[6]= R.id.testresult7;
        ids_combos[7]= R.id.testresult8;
        ids_combos[8]= R.id.testresult9;
        ids_combos[9]= R.id.testresult10;
        ids_combos[10]= R.id.testresult11;
        ids_combos[11]= R.id.testresult12;
        ids_combos[12]= R.id.testresult13;
        ids_combos[13]= R.id.testresult14;
        ids_combos[14]= R.id.testresult15;
        ids_combos[15]= R.id.testresult16;
        ids_combos[16]= R.id.testresult17;
        ids_combos[17]= R.id.testresult18;
        ids_combos[18]= R.id.testresult19;
        ids_combos[19]= R.id.testresult20;

        for (int i=0;i<20;i++) {
            ArrayAdapter adapter = ArrayAdapter.createFromResource(context, R.array.iqa_testresult, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ((Spinner) context.findViewById(ids_combos[i])).setAdapter(adapter);

        }

    }



}
