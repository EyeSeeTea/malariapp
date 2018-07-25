package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.entity.Option;
import org.eyeseetea.malariacare.domain.entity.Question;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.domain.usecase.ISurveyAnsweredRatioCallback;

import java.util.List;

public interface IOptionRepository {
    void saveOption(Option option);

    Option getOptionByUId(String uId);

    List<Option> getAll();
}
