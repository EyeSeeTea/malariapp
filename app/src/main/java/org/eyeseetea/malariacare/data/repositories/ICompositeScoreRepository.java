package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.domain.entity.CompositeScore;

import java.util.List;

public interface ICompositeScoreRepository {

    List<CompositeScore> getByProgram(String programUid);

    List<CompositeScore> getAll();
}
