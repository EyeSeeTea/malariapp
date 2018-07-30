package org.eyeseetea.malariacare.data.repositories;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.database.model.AnswerDB;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOptionRepository;
import org.eyeseetea.malariacare.domain.entity.Option;

import java.util.ArrayList;
import java.util.List;


public class OptionRepository implements IOptionRepository {

    @Override
    public List<Option> getAll() {

        List<Option> options = new ArrayList<>();

        List<OptionDB> optionsDB = new Select().from(OptionDB.class).queryList();

        for (OptionDB optionDB:optionsDB) {
            options.add(mapOption(optionDB));
        }

        return options;
    }

    @Override
    public Option getOptionByUId(String uId) {
        OptionDB optionDB = OptionDB.getByUId(uId);
        if(optionDB==null){
            return null;
        }
        return mapOption(optionDB);
    }

    @NonNull
    private Option mapOption(OptionDB optionDB) {
        return new Option(optionDB.getUid(), optionDB.getCode(), optionDB.getName(), optionDB.getFactor(), optionDB.getAnswer().getName());
    }

    @Override
    public void saveOption(Option option) {
        OptionDB optionDB = OptionDB.getByUId(option.getUId());
        AnswerDB answerDB = AnswerDB.getByName(option.getAnswerName());
        if(optionDB == null){
            optionDB = new OptionDB(option.getUId(), option.getName(), option.getCode(), option.getFactor(), answerDB);
        }else{
            optionDB.setCode(option.getCode());
            optionDB.setName(option.getName());
            optionDB.setFactor(option.getFactor());
            optionDB.setUid(option.getUId());
            optionDB.setAnswer(answerDB.getId_answer());
        }
        optionDB.save();
    }


}
