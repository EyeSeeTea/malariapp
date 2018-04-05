package org.eyeseetea.malariacare.presentation.presenters;

import android.content.Context;
import android.support.annotation.NonNull;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.ObsActionPlanDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.observables.ObservablePush;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ObsActionPlanPresenter {
    private final Context mContext;
    private View mView;
    private ObsActionPlanDB mObsActionPlan;
    private String[] mActions;
    private String[] mSubActions;
    SurveyDB mSurvey;

    public ObsActionPlanPresenter(Context context) {
        this.mContext = context;

        ObservablePush.getInstance().addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object o) {
                if (mView != null){
                    refreshStatusFromDB();
                }
            }
        });
    }

    public void attachView(View view, long surveyId) {
        this.mView = view;

        loadActions();
        loadSurvey(surveyId);
        loadObsActionPlan(surveyId);
    }

    private void loadSurvey(long surveyId) {
        mSurvey = SurveyDB.getSurveyById(surveyId);

        if (mView != null) {
            String formattedCompletionDate = "NaN";
            if (mSurvey.getCompletionDate() != null) {
                formattedCompletionDate = EventExtended.format(mSurvey.getCompletionDate(),
                        EventExtended.EUROPEAN_DATE_FORMAT);
            }

            String formattedNextDate = "NaN";
            if (mSurvey.getScheduledDate() != null) {
                formattedNextDate = EventExtended.format(mSurvey.getScheduledDate(),
                        EventExtended.EUROPEAN_DATE_FORMAT);
            }

            mView.renderHeaderInfo(mSurvey.getOrgUnit().getName(), mSurvey.getMainScore(),
                    formattedCompletionDate, formattedNextDate);
        }
    }

    private void loadActions() {
        mActions =
                mContext.getResources().getStringArray(R.array.plan_action_dropdown_options);

        mSubActions =
                mContext.getResources().getStringArray(R.array.plan_action_dropdown_suboptions);

        if (mView != null) {
            mView.loadActions(mActions);
            mView.loadSubActions(mSubActions);
        }
    }

    public void detachView() {
        this.mView = null;
    }

    private void loadObsActionPlan(long surveyId) {
        mObsActionPlan = ObsActionPlanDB.findObsActionPlanBySurvey(surveyId);

        if (mObsActionPlan == null) {
            mObsActionPlan = new ObsActionPlanDB(surveyId);
            mObsActionPlan.save();
        }

        if (mView != null) {
            if (!mObsActionPlan.getStatus().equals(Constants.SURVEY_IN_PROGRESS)) {
                mView.changeToReadOnlyMode();
            }

            updateStatus();

            showPlanInfo();
        }
    }

    private void showPlanInfo() {
        if (mView != null) {
            mView.renderBasicPlanInfo(mObsActionPlan.getProvider(), mObsActionPlan.getGaps(), mObsActionPlan.getAction_plan());

            if (mObsActionPlan.getAction1() != null) {
                for (int i = 0; i < mActions.length; i++) {
                    if (mObsActionPlan.getAction1().equals(mActions[i])) {
                        mView.selectAction(i);
                        break;
                    }
                }
            }
        }

        if (mObsActionPlan.getAction2() != null && mObsActionPlan.getAction1() != null) {
            if (mObsActionPlan.getAction1().equals(mActions[1])) {
                for (int i = 0; i < mSubActions.length; i++) {
                    if (mObsActionPlan.getAction2().equals(mSubActions[i])) {
                        mView.selectSubAction(i);
                        break;
                    }
                }
            } else if (mObsActionPlan.getAction1().equals(mActions[5])) {
                mView.renderOtherSubAction(mObsActionPlan.getAction2());
            }
        }
    }

    public void onActionSelected(String selectedAction) {
        if (selectedAction.equals(mActions[0])) {
            mObsActionPlan.setAction1(null);
        } else {
            mObsActionPlan.setAction1(selectedAction);
        }

        mObsActionPlan.setAction2(null);

        mObsActionPlan.save();

        if (selectedAction.equals(mActions[1])) {
            mView.showSubActionOptionsView();
            mView.hideSubActionOtherView();
        } else if (selectedAction.equals(mActions[5])) {
            mView.hideSubActionOptionsView();
            mView.showSubActionOtherView();
        } else {
            mView.hideSubActionOptionsView();
            mView.hideSubActionOtherView();
        }

    }

    public void onSubActionSelected(String selectedSubAction) {
        if (selectedSubAction.equals(mSubActions[0])) {
            mObsActionPlan.setAction2(null);
        } else {
            mObsActionPlan.setAction2(selectedSubAction);
        }

        mObsActionPlan.save();
    }

    public void gaspChanged(String gasp) {
        mObsActionPlan.setGaps(gasp);
        mObsActionPlan.save();
    }

    public void actionPlanChanged(String actionPlan) {
        mObsActionPlan.setAction_plan(actionPlan);
        mObsActionPlan.save();
    }

    public void providerChanged(String provider) {
        mObsActionPlan.setProvider(provider);
        mObsActionPlan.save();
    }


    public void subActionOtherChanged(String subActionOther) {
        mObsActionPlan.setAction2(subActionOther);
        mObsActionPlan.save();
    }

    public void completePlan() {
        mObsActionPlan.setStatus(Constants.SURVEY_COMPLETED);
        mObsActionPlan.save();

        if (mView != null) {
            mView.changeToReadOnlyMode();

            updateStatus();
        }
    }

    private void updateStatus() {
        mView.updateStatusView(mObsActionPlan.getStatus());

        if (mObsActionPlan.getStatus().equals(Constants.SURVEY_COMPLETED)) {
            mView.showShareButton();
        }else {
            mView.hideShareButton();
        }
    }

    public void shareObsActionPlan() {
        List<QuestionDB> criticalQuestions = QuestionDB.getCriticalFailedQuestions(
                mSurvey.getId_survey());

        List<CompositeScoreDB> compositeScoresTree = getValidTreeOfCompositeScores();

        if (mView != null) {
            mView.shareByText(mObsActionPlan, mSurvey, criticalQuestions, compositeScoresTree);
        }
    }

    @NonNull
    private List<CompositeScoreDB> getValidTreeOfCompositeScores() {
        List<CompositeScoreDB> compositeScoreList = QuestionDB.getCSOfriticalFailedQuestions(
                mSurvey.getId_survey());

        List<CompositeScoreDB> compositeScoresTree = new ArrayList<>();
        for (CompositeScoreDB compositeScore : compositeScoreList) {
            buildCompositeScoreTree(compositeScore, compositeScoresTree);
        }

        //Order composite scores
        Collections.sort(compositeScoresTree, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {

                CompositeScoreDB cs1 = (CompositeScoreDB) o1;
                CompositeScoreDB cs2 = (CompositeScoreDB) o2;

                return new Integer(cs1.getOrder_pos().compareTo(cs2.getOrder_pos()));
            }
        });
        return compositeScoresTree;
    }

    //Recursive compositescore parent builder
    private void buildCompositeScoreTree(CompositeScoreDB compositeScore,
            List<CompositeScoreDB> compositeScoresTree) {
        if (compositeScore.getHierarchical_code().equals("0")) {
            //ignore composite score root
            return;
        }
        if (!compositeScoresTree.contains(compositeScore)) {
            compositeScoresTree.add(compositeScore);
        }
        if (compositeScore.hasParent()) {
            buildCompositeScoreTree(compositeScore.getComposite_score(), compositeScoresTree);
        }
    }

    private void refreshStatusFromDB() {
        mObsActionPlan = ObsActionPlanDB.findById(mObsActionPlan.getId_obs_action_plan());

        updateStatus();
    }

    public interface View {
        void loadActions(String[] actions);

        void loadSubActions(String[] subActions);

        void changeToReadOnlyMode();

        void renderBasicPlanInfo(String provider, String gasp, String actionPlan);

        void renderHeaderInfo(String orgUnitName, Float mainScore, String completionDate,
                String nextDate);

        void renderOtherSubAction(String action2);

        void selectAction(int index);

        void selectSubAction(int index);

        void showSubActionOptionsView();

        void showSubActionOtherView();

        void hideSubActionOptionsView();

        void hideSubActionOtherView();

        void updateStatusView(Integer status);

        void shareByText(ObsActionPlanDB obsActionPlan,SurveyDB survey, List<QuestionDB> criticalQuestions,
                List<CompositeScoreDB> compositeScoresTree);

        void showShareButton();

        void hideShareButton();

    }
}
