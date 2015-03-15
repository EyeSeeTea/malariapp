package org.eyeseetea.malariacare.layout.components;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.layout.LayoutUtils;

import java.util.List;

/**
 * Created by adrian on 14/03/15.
 */
public class DialogDispatcher extends Fragment {

    private static View contextView;

    public static final int DIALOG_FRAGMENT = 1;

    public static DialogDispatcher newInstance(View view) {
        contextView = view;
        return new DialogDispatcher();
    }


    public void showDialog(FragmentManager fragmentManager, int type) {

        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        switch (type) {

            case DIALOG_FRAGMENT:

                DialogFragment dialogFrag = NoticeDialogFragment.newInstance(R.string.clearTitle, R.string.clearMessage);
                dialogFrag.setTargetFragment(this, DIALOG_FRAGMENT);
                dialogFrag.show(fragmentManager.beginTransaction(), "dialog");

                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case DIALOG_FRAGMENT:

                if (resultCode == Activity.RESULT_OK) {
                    // After Ok code.
                    Log.i("FragmentAlertDialog", "Positive click!");
                    ViewGroup root = (LinearLayout) LayoutUtils.findParentRecursively(contextView, R.id.Grid);
                    List<View> viewsToClear = LayoutUtils.getChildrenByTag(root, R.id.QuestionTypeTag, null);
                    for (View viewToClear: viewsToClear){
                        LayoutUtils.resetComponent(viewToClear);
                    }
                }

                break;
        }
    }
}
