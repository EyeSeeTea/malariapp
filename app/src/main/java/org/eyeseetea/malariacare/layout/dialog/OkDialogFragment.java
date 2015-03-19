package org.eyeseetea.malariacare.layout.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class OkDialogFragment extends DialogFragment{

    public static Integer ICON;

    public static DialogFragment newInstance(int title, int message) {
        ErrorDialogFragment frag = new ErrorDialogFragment();
        setIcon(null);
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putInt("message", message);
        frag.setArguments(args);
        return frag;
    }

    public static void setIcon(Integer icon){
        ICON = icon;
    }

    public static Integer getIcon(){
        return ICON;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getInt("title"));
        if (getArguments().get("message") instanceof String)
            builder.setMessage(getArguments().getString("message"));
        else
            builder.setMessage(getArguments().getInt("message"));
        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Send the positive button event back to the host activity
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
            }
        });
        if (ICON != null)
            builder.setIcon(ICON);
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
