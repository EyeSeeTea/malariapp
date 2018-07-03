package org.eyeseetea.malariacare.data.database.datasources;

import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.repositories.ICompositeScoreRepository;
import org.eyeseetea.malariacare.domain.entity.CompositeScore;

import java.util.ArrayList;
import java.util.List;

public class CompositeScoreDataSource implements ICompositeScoreRepository {
    @Override
    public ArrayList<CompositeScore> getCompositesScoreByProgram(String programUid) {
        ArrayList<CompositeScore> compositeScores = new ArrayList<>();
        ProgramDB programDB = ProgramDB.getProgram(programUid);
        List<CompositeScoreDB> compositeScoreDBS = CompositeScoreDB.listByProgram(programDB);
        for (CompositeScoreDB compositeScore : compositeScoreDBS) {
            if (compositeScore.getComposite_score() == null) {
                compositeScores.add(
                        convertCompositeScoreChildren(compositeScore, compositeScoreDBS));
            }
        }
        return compositeScores;
    }

    private CompositeScore convertCompositeScoreChildren(CompositeScoreDB compositeScoreDB,
            List<CompositeScoreDB> compositeScoreDBS) {
        CompositeScore compositeScore = convertFromDBToDomain(compositeScoreDB, null,
                compositeScoreDBS);
        if (compositeScoreDB.getCompositeScoreChildren() != null
                && !compositeScoreDB.getCompositeScoreChildren().isEmpty()) {
            for (CompositeScoreDB compositeScoreChild : compositeScoreDB
                    .getCompositeScoreChildren()) {
                compositeScore.addChild(convertFromDBToDomain(compositeScoreChild, compositeScore,
                        compositeScoreDBS));
            }
        }
        return compositeScore;
    }

    private CompositeScore convertFromDBToDomain(CompositeScoreDB compositeScoreDB,
            CompositeScore parent, List<CompositeScoreDB> compositeScoreDBS) {
        ArrayList<CompositeScore> compositeScoreChildren = new ArrayList<>();

        CompositeScore compositeScore = new CompositeScore(compositeScoreDB.getUid(),
                compositeScoreDB.getLabel(),
                compositeScoreDB.getHierarchical_code(), compositeScoreDB.getOrder_pos());
        if (compositeScoreDB.getComposite_score() != null) {
            compositeScore.addParent(parent);
        }
        List<CompositeScoreDB> compositeScoreChildrenDB = getCompositeScoreChildrenDBFromList(
                compositeScoreDB, compositeScoreDBS);
        if (compositeScoreChildrenDB != null) {

            for (CompositeScoreDB compositeScoreChild : compositeScoreChildrenDB) {
                compositeScoreChildren.add(
                        convertFromDBToDomain(compositeScoreChild, compositeScore,
                                compositeScoreDBS));
            }
        }
        compositeScore.addChildren(compositeScoreChildren);
        return compositeScore;
    }


    private List<CompositeScoreDB> getCompositeScoreChildrenDBFromList(CompositeScoreDB parent,
            List<CompositeScoreDB> compositeScoreDBS) {
        List<CompositeScoreDB> compositeScoreChildren = new ArrayList<>();
        for (CompositeScoreDB compositeScoreDB : compositeScoreDBS) {
            if (compositeScoreDB.getComposite_score() != null
                    && compositeScoreDB.getComposite_score().equals(parent)) {
                compositeScoreChildren.add(compositeScoreDB);
            }
        }
        return compositeScoreChildren;
    }
}
