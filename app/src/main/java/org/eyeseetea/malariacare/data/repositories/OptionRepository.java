package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.database.model.AnswerDB;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOptionRepository;
import org.eyeseetea.malariacare.domain.entity.Option;


public class OptionRepository implements IOptionRepository {

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

    @Override
    public Option getOptionByUId(String uId) {
        OptionDB optionDB = OptionDB.getByUId(uId);
        if(optionDB==null){
            return null;
        }
        return new Option(optionDB.getUid(), optionDB.getCode(), optionDB.getName(), optionDB.getFactor(), optionDB.getAnswer().getName());
    }
}
