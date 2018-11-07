package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.Option;

import java.util.List;

public interface IOptionRepository {
    void saveOption(Option option);

    Option getOptionByUId(String uId);

    List<Option> getAll();
}
