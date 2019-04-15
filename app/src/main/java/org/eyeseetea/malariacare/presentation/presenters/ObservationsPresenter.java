package org.eyeseetea.malariacare.presentation.presenters;

import static org.eyeseetea.malariacare.domain.entity.ObservationStatus.*;

import android.content.Context;
import android.support.annotation.NonNull;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.domain.usecase.GetObservationBySurveyUidUseCase;
import org.eyeseetea.malariacare.domain.usecase.GetServerMetadataUseCase;
import org.eyeseetea.malariacare.domain.usecase.SaveObservationUseCase;
import org.eyeseetea.malariacare.observables.ObservablePush;
import org.eyeseetea.malariacare.presentation.mapper.ObservationMapper;
import org.eyeseetea.malariacare.presentation.viewmodels.ObservationsViewModel;
import org.eyeseetea.malariacare.utils.DateParser;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ObservationsPresenter {
    private final GetObservationBySurveyUidUseCase mGetObservationBySurveyUidUseCase;
    private final GetServerMetadataUseCase mGetServerMetadataUseCase;
    private final SaveObservationUseCase mSaveObservationUseCase;

    private final Context mContext;
    private View mView;
    private String[] mActions;
    private String[] mSubActions;
    private SurveyDB mSurvey;

    private ServerMetadata mServerMetadata;
    private String mSurveyUid;

    private ObservationsViewModel mObservationsViewModel;

    public ObservationsPresenter(Context context,
            GetObservationBySurveyUidUseCase getObservationBySurveyUidUseCase,
            GetServerMetadataUseCase getServerMetadataUseCase,
            SaveObservationUseCase saveObservationUseCase) {
        this.mContext = context;
        this.mGetObservationBySurveyUidUseCase = getObservationBySurveyUidUseCase;
        this.mGetServerMetadataUseCase = getServerMetadataUseCase;
        this.mSaveObservationUseCase = saveObservationUseCase;

        ObservablePush.getInstance().addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object o) {
                if (mView != null) {
                    refreshObservation();
                }
            }
        });
    }

    public void attachView(View view, String surveyUid) {
        this.mView = view;
        this.mSurveyUid = surveyUid;


        LoadData();
    }

    public void detachView() {
        this.mView = null;
    }

    private void LoadData() {
        mGetServerMetadataUseCase.execute(new GetServerMetadataUseCase.Callback() {
            @Override
            public void onSuccess(ServerMetadata serverMetadata) {
                ObservationsPresenter.this.mServerMetadata = serverMetadata;
                loadObservation();
            }

            @Override
            public void onError(Exception e) {
                System.out.println(
                        "An error has occur retrieving server metadata: " + e.getMessage());
            }
        });
    }

    private void loadObservation() {
        mGetObservationBySurveyUidUseCase.execute(mSurveyUid,
                new GetObservationBySurveyUidUseCase.Callback() {
                    @Override
                    public void onSuccess(Observation observation) {
                        mObservationsViewModel =
                                ObservationMapper.mapToViewModel(observation, mServerMetadata);

                        if (mView != null) {
                            if (!mObservationsViewModel.getStatus().equals(
                                    IN_PROGRESS)) {
                                mView.changeToReadOnlyMode();
                            }

                            loadSurvey();
                            loadActions();
                            updateStatus();
                            showObservations();
                        }
                    }

                    @Override
                    public void onObservationNotFound() {
                        mObservationsViewModel = new ObservationsViewModel(mSurveyUid);
                        saveObservation();
                    }

                    @Override
                    public void onError(Exception e) {
                        System.out.println(
                                "An error has occur retrieving observation: " + e.getMessage());
                    }
                });
    }


    private void loadSurvey() {
        mSurvey = SurveyDB.getSurveyByUId(mSurveyUid);

        if (mView != null) {
            DateParser dateParser = new DateParser();
            String formattedCompletionDate = "NaN";
            if (mSurvey.getCompletionDate() != null) {
                formattedCompletionDate = dateParser.format(mSurvey.getCompletionDate(),
                        DateParser.EUROPEAN_DATE_FORMAT);
            }

            String formattedNextDate = "NaN";
            if (mSurvey != null) {
                formattedNextDate = dateParser.format(
                        SurveyPlanner.getInstance().findScheduledDateBySurvey(mSurvey),
                        DateParser.EUROPEAN_DATE_FORMAT);
            }

            CompetencyScoreClassification classification =
                    CompetencyScoreClassification.get(
                            mSurvey.getCompetencyScoreClassification());

            mView.renderHeaderInfo(mSurvey.getOrgUnit().getName(), mSurvey.getMainScore(),
                    formattedCompletionDate, formattedNextDate, classification);
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

    private void showObservations() {
        if (mView != null) {
            mView.renderBasicObservations(mObservationsViewModel.getProvider(),
                    mObservationsViewModel.getGaps(), mObservationsViewModel.getPlanAction());

            if (mObservationsViewModel.getAction1() != null) {
                for (int i = 0; i < mActions.length; i++) {
                    if (mObservationsViewModel.getAction1().equals(mActions[i])) {
                        mView.selectAction(i);
                        break;
                    }
                }
            }
        }

        if (mObservationsViewModel.getAction2() != null
                && mObservationsViewModel.getAction1() != null) {
            if (mObservationsViewModel.getAction1().equals(mActions[1])) {
                for (int i = 0; i < mSubActions.length; i++) {
                    if (mObservationsViewModel.getAction2().equals(mSubActions[i])) {
                        mView.selectSubAction(i);
                        break;
                    }
                }
            } else if (mObservationsViewModel.getAction1().equals(mActions[5])) {
                mView.renderOtherSubAction(mObservationsViewModel.getAction2());
            }
        }
    }

    public void onActionSelected(String selectedAction) {
        if (selectedAction.equals(mActions[0])) {
            mObservationsViewModel.setAction1(null);
        } else {
            mObservationsViewModel.setAction1(selectedAction);
        }

        mObservationsViewModel.setAction2(null);

        saveObservation();

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

    private void saveObservation() {
        Observation observation =
                ObservationMapper.mapToObservation(mObservationsViewModel, mServerMetadata);

        mSaveObservationUseCase.execute(observation, new SaveObservationUseCase.Callback() {
            @Override
            public void onSuccess() {
                System.out.println("Observation saved successfully");
            }

            @Override
            public void onError(Exception e) {
                System.out.println(
                        "An error has occur saving Observation: " + e.getMessage());
            }
        });
    }

    public void onSubActionSelected(String selectedSubAction) {
        if (selectedSubAction.equals(mSubActions[0])) {
            mObservationsViewModel.setAction2(null);
        } else {
            mObservationsViewModel.setAction2(selectedSubAction);
        }

        saveObservation();
    }

    public void gaspChanged(String gasp) {
        mObservationsViewModel.setGaps(gasp);
        saveObservation();
    }

    public void actionPlanChanged(String actionPlan) {
        mObservationsViewModel.setPlanAction(actionPlan);
        saveObservation();
    }

    public void providerChanged(String provider) {
        mObservationsViewModel.setProvider(provider);
        saveObservation();
    }


    public void subActionOtherChanged(String subActionOther) {
        mObservationsViewModel.setAction2(subActionOther);
        saveObservation();
    }

    public void completeObservation() {
        mObservationsViewModel.setStatus(COMPLETED);
        saveObservation();

        if (mView != null) {
            mView.changeToReadOnlyMode();

            updateStatus();
        }
    }

    private void updateStatus() {
        if (mView != null) {
            mView.updateStatusView(mObservationsViewModel.getStatus());
        }


        switch (mObservationsViewModel.getStatus()) {
            case COMPLETED:
            case SENT:
                mView.enableShareButton();
                break;

            case IN_PROGRESS:
                mView.disableShareButton();
                break;
        }
    }

    public void shareObsActionPlan() {
        List<QuestionDB> criticalQuestions = QuestionDB.getCriticalFailedQuestions(
                mSurvey.getId_survey());

        List<CompositeScoreDB> compositeScoresTree = getValidTreeOfCompositeScores();
        if (mView != null) {

            if (mSurvey.getStatus() != Constants.SURVEY_SENT) {
                mView.shareNotSent(mContext.getString(R.string.feedback_not_sent));
            } else {
                mView.shareByText(mObservationsViewModel, mSurvey, criticalQuestions, compositeScoresTree);
            }
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

    private void refreshObservation() {
        mGetObservationBySurveyUidUseCase.execute(mSurveyUid,
                new GetObservationBySurveyUidUseCase.Callback() {
                    @Override
                    public void onSuccess(Observation observation) {
                        mObservationsViewModel =
                                ObservationMapper.mapToViewModel(observation, mServerMetadata);

                        updateStatus();
                    }

                    @Override
                    public void onObservationNotFound() {
                        System.out.println(
                                "Observation not found by surveyUid: " + mSurveyUid);
                    }

                    @Override
                    public void onError(Exception e) {
                        System.out.println(
                                "An error has occur retrieving observation: " + e.getMessage());
                    }
                });
    }

    public interface View {
        void loadActions(String[] actions);

        void loadSubActions(String[] subActions);

        void changeToReadOnlyMode();

        void renderBasicObservations(String provider, String gasp, String actionPlan);

        void renderHeaderInfo(String orgUnitName, Float mainScore, String completionDate,
                String nextDate, CompetencyScoreClassification classification);

        void renderOtherSubAction(String action2);

        void selectAction(int index);

        void selectSubAction(int index);

        void showSubActionOptionsView();

        void showSubActionOtherView();

        void hideSubActionOptionsView();

        void hideSubActionOtherView();

        void updateStatusView(ObservationStatus status);

        void shareByText(ObservationsViewModel observationsViewModel, SurveyDB survey,
                List<QuestionDB> criticalQuestions, List<CompositeScoreDB> compositeScoresTree);

        void shareNotSent(String surveyNoSentMessage);

        void enableShareButton();

        void disableShareButton();

    }
}
