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
        for (CompositeScoreDB compositeScoreChild : compositeScoreDBS) {
            convertCompositeScoreChildren(compositeScoreChild);
        }
        return compositeScores;
    }

    private CompositeScore convertCompositeScoreChildren(CompositeScoreDB compositeScoreDB) {
        CompositeScore compositeScore = convertFromDBToDomain(compositeScoreDB, null);
        if (compositeScoreDB.getCompositeScoreChildren() != null
                && !compositeScoreDB.getCompositeScoreChildren().isEmpty()) {
            for (CompositeScoreDB compositeScoreChild : compositeScoreDB
                    .getCompositeScoreChildren()) {
                compositeScore.addChild(convertFromDBToDomain(compositeScoreChild, compositeScore));
            }
        }
        return compositeScore;
    }


    private CompositeScore convertFromDBToDomain(CompositeScoreDB compositeScoreDB,
            CompositeScore parent) {
        ArrayList<CompositeScore> compositeScoreChildren = new ArrayList<>();

        CompositeScore compositeScore = new CompositeScore(compositeScoreDB.getUid(),
                compositeScoreDB.getLabel(),
                compositeScoreDB.getHierarchical_code(), compositeScoreDB.getOrder_pos());
        if (compositeScoreDB.getComposite_score() != null) {
            compositeScore.addParent(parent);
        }
        if (compositeScoreDB.getCompositeScoreChildren() != null) {

            for (CompositeScoreDB compositeScoreChild : compositeScoreDB
                    .getCompositeScoreChildren()) {
                compositeScoreChildren.add(
                        convertFromDBToDomain(compositeScoreChild, compositeScore));
            }
        }
        compositeScore.addChildren(compositeScoreChildren);
        return compositeScore;
    }
}
