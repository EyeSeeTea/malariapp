package org.eyeseetea.malariacare.presentation.views

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import kotlinx.android.synthetic.main.dialog_actions_monitoring.view.*
import org.eyeseetea.malariacare.R
import org.eyeseetea.malariacare.factories.DataFactory
import org.eyeseetea.malariacare.factories.MetadataFactory
import org.eyeseetea.malariacare.presentation.executors.WrapperExecutor
import org.eyeseetea.malariacare.presentation.presenters.monitoring.MonitorActionsDialogPresenter
import org.eyeseetea.malariacare.presentation.viewmodels.observations.ActionViewModel

class MonitorActionsDialogFragment : DialogFragment(), MonitorActionsDialogPresenter.View {

    private lateinit var rootView: View
    private lateinit var surveyId: String
    private lateinit var presenter: MonitorActionsDialogPresenter

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

        rootView = view

        initializeOKCancelButtons(view)
        initializePresenter()
    }

    override fun showLoading() {
        rootView.progress_view.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        rootView.progress_view.visibility = View.GONE
    }

    override fun showLoadErrorMessage() {
        Toast.makeText(activity, getString(R.string.load_error_message), Toast.LENGTH_LONG).show()
    }

    override fun showOrgUnitAndProgram(orgUnit: String, program: String) {
        rootView.program_view.text = program
        rootView.org_unit_view.text = orgUnit
    }

    override fun showActions(
        action1: ActionViewModel,
        action2: ActionViewModel,
        action3: ActionViewModel
    ) {
        if (!action1.description.isBlank()) {
            rootView.action1_container.visibility = View.VISIBLE
            rootView.action1_view.text = action1.description
        } else {
            rootView.action1_container.visibility = View.GONE
        }

        if (!action2.description.isBlank()) {
            rootView.action2_container.visibility = View.VISIBLE
            rootView.action2_view.text = action2.description
        } else {
            rootView.action2_container.visibility = View.GONE
        }

        if (!action3.description.isBlank()) {
            rootView.action3_container.visibility = View.VISIBLE
            rootView.action3_view.text = action3.description
        } else {
            rootView.action3_container.visibility = View.GONE
        }
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

    private fun initializePresenter() {
        presenter = MonitorActionsDialogPresenter(
            WrapperExecutor(),
            MetadataFactory.provideGetProgramByUidUseCase(),
            MetadataFactory.provideGetOrgUnitByUidUseCase(),
            MetadataFactory.provideServerMetadataUseCase(activity as Activity),
            DataFactory.provideGetSurveyByUidUseCase(),
            DataFactory.provideObservationBySurveyUidUseCase()
        )

        presenter.attachView(this, surveyId)
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