package org.eyeseetea.malariacare.adapters;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.eyeseetea.malariacare.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jose on 03/03/2015.
 */
public class ManualAdapterIQA {

    private final Activity context;
    private final List<Integer> idsCombos;

    // This adapter is for filling the dropdown lists in the Adherence tab with 2 options: [Yes, No]
    // FIXME: This class can be merged with ManualAdapterAdherence. i.e. creating an abstract class implemented by both ManualIQA and ManualAdherence
    public ManualAdapterIQA(Activity context)
    {
        this.context = context;
        this.idsCombos = new ArrayList<Integer>();

        Spinner view = (Spinner) context.findViewById(R.id.act1);

        // We hard-code the ids that contribute to this score calculus
        idsCombos.add(R.id.testresult1);
        idsCombos.add(R.id.testresult2);
        idsCombos.add(R.id.testresult3);
        idsCombos.add(R.id.testresult4);
        idsCombos.add(R.id.testresult5);
        idsCombos.add(R.id.testresult6);
        idsCombos.add(R.id.testresult7);
        idsCombos.add(R.id.testresult8);
        idsCombos.add(R.id.testresult9);
        idsCombos.add(R.id.testresult10);
        idsCombos.add(R.id.testresult11);
        idsCombos.add(R.id.testresult12);
        idsCombos.add(R.id.testresult13);
        idsCombos.add(R.id.testresult14);
        idsCombos.add(R.id.testresult15);
        idsCombos.add(R.id.testresult16);
        idsCombos.add(R.id.testresult17);
        idsCombos.add(R.id.testresult18);
        idsCombos.add(R.id.testresult19);
        idsCombos.add(R.id.testresult20);

        // For each dropdown list we add its content (in every dropdown there will be 2 options, yes/no).
        for (Integer idCombo: idsCombos){
            ArrayAdapter adapter = ArrayAdapter.createFromResource(context, R.array.iqa_testresult, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ((Spinner) context.findViewById(idCombo)).setAdapter(adapter);

        }

    }



}
