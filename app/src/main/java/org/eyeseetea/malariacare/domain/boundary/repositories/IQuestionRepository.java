package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.Question;

import java.util.List;

public interface IQuestionRepository {

    Question getQuestionByUId(String uid);

    List<Question> getAll();
}
