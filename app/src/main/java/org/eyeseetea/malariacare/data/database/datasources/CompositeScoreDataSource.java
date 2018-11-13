package org.eyeseetea.malariacare.data.database.datasources;

import android.support.annotation.NonNull;

import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.repositories.ICompositeScoreRepository;
import org.eyeseetea.malariacare.domain.entity.CompositeScore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CompositeScoreDataSource implements ICompositeScoreRepository {
    @Override
    public List<CompositeScore> getByProgram(String programUid) {
        ProgramDB programDB = ProgramDB.getProgram(programUid);
        List<CompositeScoreDB> compositeScoreDBS = CompositeScoreDB.listByProgram(programDB);

        List<CompositeScore> compositeScores = map(compositeScoreDBS);

        return compositeScores;
    }

    @Override
    public List<CompositeScore> getAll() {
        List<CompositeScoreDB> compositeScoreDBS = CompositeScoreDB.list();

        List<CompositeScore> compositeScores = map(compositeScoreDBS);

        return compositeScores;
    }

    @NonNull
    private List<CompositeScore> map(List<CompositeScoreDB> compositeScoreDBS) {
        List<CompositeScore> compositeScores = new ArrayList<>();
        HashMap<String, CompositeScore> compositeScoresMap = new HashMap<>();

        LinkedHashMap<Long, CompositeScoreDB> compositeScoresDBMap =
                createCompositeScoresDBMap(compositeScoreDBS);

        for (Map.Entry<Long, CompositeScoreDB> entry : compositeScoresDBMap.entrySet()){
            CompositeScoreDB compositeScoreDB = entry.getValue();

            String parentUid = null;

            if (compositeScoreDB.getId_composite_score_parent() != null) {
                CompositeScoreDB compositeScoreDBParent =
                        compositeScoresDBMap.get(compositeScoreDB.getId_composite_score_parent());

                parentUid = compositeScoreDBParent.getUid();
            }

            CompositeScore compositeScore = new CompositeScore(parentUid, compositeScoreDB.getUid(),
                    compositeScoreDB.getLabel(), compositeScoreDB.getHierarchical_code(),
                    compositeScoreDB.getOrder_pos());

            compositeScoresMap.put(compositeScore.getUid(),compositeScore);

            if (parentUid != null){
                compositeScoresMap.get(parentUid).addChild(compositeScore);
            }
        }


        for (Map.Entry<String, CompositeScore> entry : compositeScoresMap.entrySet()) {
            if (entry.getValue().getHierarchicalCode().equals("0"))
                compositeScores.add(entry.getValue());
        }

        return compositeScores;
    }

    private LinkedHashMap<Long, CompositeScoreDB> createCompositeScoresDBMap(
            List<CompositeScoreDB> compositeScoreDBS) {

        LinkedHashMap<Long, CompositeScoreDB> compositeScoresDBMap = new LinkedHashMap<>();

        Collections.sort(compositeScoreDBS, new Comparator<CompositeScoreDB>() {
            public int compare(CompositeScoreDB o1, CompositeScoreDB o2) {
                return o1.getHierarchical_code().compareTo(
                        o2.getHierarchical_code());
            }
        });

        for (CompositeScoreDB compositeScoreDB : compositeScoreDBS) {
            if (!compositeScoresDBMap.containsKey(compositeScoreDB.getId_composite_score())) {
                compositeScoresDBMap.put(compositeScoreDB.getId_composite_score(), compositeScoreDB);
            }
        }

        return compositeScoresDBMap;
    }
}
