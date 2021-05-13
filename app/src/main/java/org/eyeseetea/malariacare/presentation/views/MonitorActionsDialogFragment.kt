package org.eyeseetea.malariacare.presentation.views

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import kotlinx.android.synthetic.main.dialog_actions_monitoring.view.*
import org.eyeseetea.malariacare.DashboardActivity
import org.eyeseetea.malariacare.R
import org.eyeseetea.malariacare.factories.DataFactory
import org.eyeseetea.malariacare.factories.MetadataFactory
import org.eyeseetea.malariacare.presentation.executors.WrapperExecutor
import org.eyeseetea.malariacare.presentation.presenters.monitoring.MonitorActionsDialogPresenter
import org.eyeseetea.malariacare.presentation.viewmodels.observations.ActionViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class MonitorActionsDialogFragment : DialogFragment(), MonitorActionsDialogPresenter.View {
    private lateinit var rootView: View
    private lateinit var surveyId: String
    private lateinit var presenter: MonitorActionsDialogPresenter

    var onActionsSaved: (() -> Unit)? = null

    override fun onStart() {
        dialog?.window?.setLayout(
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

        initializeButtons(view)
        initializePresenter()
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    override fun showLoading() {
        rootView.progress_view.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        rootView.progress_view.visibility = View.GONE
    }

    override fun showLoadErrorMessage() {
        Toast.makeText(
            activity as Activity,
            getString(R.string.load_error_message),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun showSaveErrorMessage() {
        Toast.makeText(
            activity as Activity,
            getString(R.string.save_error_message),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun showOrgUnitAndProgram(orgUnit: String, program: String) {
        rootView.program_view.text = program
        rootView.org_unit_view.text = orgUnit
    }

    override fun showSaveConfirmMessage() {
        val builder = AlertDialog.Builder(activity)
        builder
            .setMessage(R.string.action_monitor_save_confirm_message)
            .setPositiveButton(R.string.ok) { _, _ ->
                presenter.confirmSave()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
            }

        // Create the AlertDialog object and return it
        val dialog = builder.create()

        dialog.show()
    }

    override fun exit() {
        dismiss()
    }

    override fun navigateToFeedback(surveyUid: String) {
        DashboardActivity.dashboardActivity.openFeedback(surveyUid, false)
    }

    override fun showActions(
        action1: ActionViewModel,
        action2: ActionViewModel,
        action3: ActionViewModel
    ) {
        if (!action1.description.isBlank()) {
            rootView.action1_container.visibility = View.VISIBLE
            rootView.action1_view.text = getTextToShowInAction(action1)
            rootView.action1_conducted_view.isChecked = action1.isCompleted
        } else {
            rootView.action1_container.visibility = View.GONE
        }

        if (!action2.description.isBlank()) {
            rootView.action2_container.visibility = View.VISIBLE
            rootView.action2_view.text = getTextToShowInAction(action2)
            rootView.action2_conducted_view.isChecked = action2.isCompleted
        } else {
            rootView.action2_container.visibility = View.GONE
        }

        if (!action3.description.isBlank()) {
            rootView.action3_container.visibility = View.VISIBLE
            rootView.action3_view.text = getTextToShowInAction(action3)
            rootView.action3_conducted_view.isChecked = action3.isCompleted
        } else {
            rootView.action3_container.visibility = View.GONE
        }
    }

    override fun notifyOnSave() {
        onActionsSaved?.invoke()
        dismiss()
    }

    override fun changeToReadOnlyMode() {
        rootView.action1_conducted_view.isEnabled = false
        rootView.action2_conducted_view.isEnabled = false
        rootView.action3_conducted_view.isEnabled = false
        rootView.ok_button.isEnabled = false
    }

    private fun initializeButtons(rootView: View) {
        rootView.ok_button.setOnClickListener {

            presenter.save(
                rootView.action1_conducted_view.isChecked,
                rootView.action2_conducted_view.isChecked,
                rootView.action3_conducted_view.isChecked
            )
        }

        rootView.cancel_button.setOnClickListener {
            presenter.cancel()
        }

        rootView.feedback_button.setOnClickListener {
            presenter.feedback()
        }
    }

    private fun initializePresenter() {
        presenter = MonitorActionsDialogPresenter(
            WrapperExecutor(),
            MetadataFactory.provideGetProgramByUidUseCase(),
            MetadataFactory.provideGetOrgUnitByUidUseCase(),
            MetadataFactory.provideServerMetadataUseCase(activity as Activity),
            DataFactory.provideGetSurveyByUidUseCase(),
            DataFactory.provideGetObservationBySurveyUidUseCase(),
            DataFactory.provideSaveObservationUseCase()
        )

        presenter.attachView(this, surveyId)
    }

    private fun getTextToShowInAction(action: ActionViewModel): String {
        val responsibleLabel =
            activity?.resources?.getString(R.string.observation_action_responsible)
        val dueDateLabel = activity?.resources?.getString(R.string.observation_action_due_date)

        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val date = dateFormatter.format(action.dueDate)
        val maxLength = 75
        val description =
            if (action.description.length > maxLength)
                "${action.description.substring(0, maxLength)} ..."
            else action.description

        return "$description \n$responsibleLabel ${action.responsible} \n$dueDateLabel $date"
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