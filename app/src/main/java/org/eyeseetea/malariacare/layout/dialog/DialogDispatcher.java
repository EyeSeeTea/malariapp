package org.eyeseetea.malariacare.layout.dialog;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;

import java.io.InputStream;
import java.util.List;

public class DialogDispatcher extends Fragment {

    private static View contextView;

//    public static final int CLEAR_DIALOG = 1;
    public static final int ERROR_DIALOG = 2;
    public static final int LICENSE_DIALOG = 3;
    public static final int DELETE_SURVEY_DIALOG = 4;
    public static final int ABOUT_DIALOG = 5;

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

//            case CLEAR_DIALOG:
//
//                DialogFragment dialogFrag = NoticeDialogFragment.newInstance(R.string.clearTitle, R.string.clearMessage);
//                dialogFrag.setTargetFragment(this, CLEAR_DIALOG);
//                dialogFrag.show(fragmentManager.beginTransaction(), "dialog");
//
//                break;

            case DELETE_SURVEY_DIALOG:

                DialogFragment dialogSFrag = NoticeDialogFragment.newInstance(R.string.deleteTitle, R.string.deleteMessage);
                dialogSFrag.setTargetFragment(this, DELETE_SURVEY_DIALOG);
                dialogSFrag.show(fragmentManager.beginTransaction(), "dialog");

                break;

            case ERROR_DIALOG:
                DialogFragment errorFrag = ErrorDialogFragment.newInstance(R.string.errorTitle, R.string.errorDenominator);
                errorFrag.setTargetFragment(this, ERROR_DIALOG);
                errorFrag.show(fragmentManager.beginTransaction(), "dialog");

                break;

            case LICENSE_DIALOG:
                InputStream is = contextView.getContext().getResources().openRawResource(R.raw.gpl);
                DialogFragment licenseFrag = LargeTextDialogFragment.newInstance(R.string.licenseTitle, is);
                licenseFrag.setTargetFragment(this, LICENSE_DIALOG);
                licenseFrag.show(fragmentManager.beginTransaction(), "dialog");

                break;

            case ABOUT_DIALOG:
                InputStream isa = contextView.getContext().getResources().openRawResource(R.raw.about);
                DialogFragment aboutFrag = LargeTextDialogFragment.newInstance(R.string.aboutTitle, isa);
                aboutFrag.setTargetFragment(this, ABOUT_DIALOG);
                aboutFrag.show(fragmentManager.beginTransaction(), "dialog");

                break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            //FIXME: AQP
//            case CLEAR_DIALOG:
//
//                if (resultCode == Activity.RESULT_OK) {
//                    // After Ok code.
//                    Log.i("FragmentAlertDialog", "Positive click!");
//                    ViewGroup root = (LinearLayout) LayoutUtils.findParentRecursively(contextView, R.id.Grid);
//                    List<View> viewsToClear = LayoutUtils.getChildrenByTag(root, R.id.QuestionTypeTag, null);
//                    for (View viewToClear: viewsToClear){
//                        LayoutUtils.resetComponent(viewToClear);
//                        // After resetting the component, if it is a question we need also to recalculate its num/dem
//                        if ((viewToClear.getId() == R.id.ddl) && viewToClear.getTag(R.id.QuestionTag) != null){
//                            Question triggeredQuestion = (Question) viewToClear.getTag(R.id.QuestionTag);
//                        }
//                    }
//
//                    // Reset Score tab
//                    LayoutUtils.resetScores(root);
//                }
//
//                break;

            case DELETE_SURVEY_DIALOG:

                if (resultCode == Activity.RESULT_OK) {
                    Log.i("FragmentAlertDialog", "Positive click!");
                    Session.getSurvey().delete();

                    //myActivity.finish();
                    Intent intent = new Intent(contextView.getContext(), DashboardActivity.class);
                    contextView.getContext().startActivity(intent);
                }
                Session.setSurvey(null);

                break;
        }
    }
}
