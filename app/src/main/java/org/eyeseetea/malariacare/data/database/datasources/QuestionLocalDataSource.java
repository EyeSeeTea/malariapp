package org.eyeseetea.malariacare.data.database.datasources;

import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.domain.boundary.repositories.IQuestionRepository;
import org.eyeseetea.malariacare.domain.entity.Question;

import java.util.ArrayList;
import java.util.List;

public class QuestionLocalDataSource implements IQuestionRepository{
    @Override
    public Question getQuestionByUId(String uid) {
        QuestionDB questionDB = QuestionDB.getQuestionByUid(uid);

        List<String> options = new ArrayList<>();
        for(OptionDB option : questionDB.getAnswer().getOptions()){
            options.add(option.getUid());
        }
        return new Question(questionDB.getUid(), options, questionDB.getOutput(), questionDB.getCompulsory());
    }
}
