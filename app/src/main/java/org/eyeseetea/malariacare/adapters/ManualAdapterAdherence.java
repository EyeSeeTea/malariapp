package org.eyeseetea.malariacare.adapters;

import android.app.Activity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.eyeseetea.malariacare.R;

/**
 * Created by Jose on 03/03/2015.
 */
public class ManualAdapterAdherence {

    private final Activity context;
    private final int[] ids_combos;

    public ManualAdapterAdherence(Activity context)
    {
        this.context = context;
        ids_combos=new int[20];

        Spinner view = (Spinner) context.findViewById(R.id.act1);

        ids_combos[0]= R.id.act1;
        ids_combos[1]= R.id.act2;
        ids_combos[2]= R.id.act3;
        ids_combos[3]= R.id.act4;
        ids_combos[4]= R.id.act5;
        ids_combos[5]= R.id.act6;
        ids_combos[6]= R.id.act7;
        ids_combos[7]= R.id.act8;
        ids_combos[8]= R.id.act9;
        ids_combos[9]= R.id.act10;
        ids_combos[10]= R.id.act11;
        ids_combos[11]= R.id.act12;
        ids_combos[12]= R.id.act13;
        ids_combos[13]= R.id.act14;
        ids_combos[14]= R.id.act15;
        ids_combos[15]= R.id.act16;
        ids_combos[16]= R.id.act17;
        ids_combos[17]= R.id.act18;
        ids_combos[18]= R.id.act19;
        ids_combos[19]= R.id.act20;

        for (int i=0;i<20;i++) {
            ArrayAdapter adapter = ArrayAdapter.createFromResource(context, R.array.yesno, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ((Spinner) context.findViewById(ids_combos[i])).setAdapter(adapter);

        }

    }


}
