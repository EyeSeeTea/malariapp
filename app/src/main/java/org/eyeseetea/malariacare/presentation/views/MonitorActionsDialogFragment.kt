package org.eyeseetea.malariacare.presentation.views

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import kotlinx.android.synthetic.main.dialog_actions_monitoring.view.*
import org.eyeseetea.malariacare.R

class MonitorActionsDialogFragment : DialogFragment() {

    private lateinit var surveyId: String

    override fun onStart() {
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        surveyId = arguments!!.getString(SURVEY_ID)!!

        return inflater.inflate(R.layout.dialog_actions_monitoring, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeOKCancelButtons(view)
    }

    private fun initializeOKCancelButtons(rootView: View) {
        rootView.ok_button.setOnClickListener {

            // presenter.Save

            dismiss()
        }

        rootView.cancel_button.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        private const val SURVEY_ID = "surveyId"

        @JvmStatic
        fun newInstance(surveyIds: String): MonitorActionsDialogFragment {
            val fragment = MonitorActionsDialogFragment()

            val args = Bundle()
            args.putString(SURVEY_ID, surveyIds)
            fragment.arguments = args

            return fragment
        }
    }
}