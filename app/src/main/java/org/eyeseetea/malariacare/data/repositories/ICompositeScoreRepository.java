package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.domain.entity.CompositeScore;

import java.util.List;

public interface ICompositeScoreRepository {

    List<CompositeScore> getCompositesScoreByProgram(String programUid);

}
