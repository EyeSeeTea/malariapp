package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.domain.entity.CompositeScore;

import java.util.ArrayList;

public interface ICompositeScoreRepository {

    ArrayList<CompositeScore> getCompositesScoreByProgram(String programUid);

}
