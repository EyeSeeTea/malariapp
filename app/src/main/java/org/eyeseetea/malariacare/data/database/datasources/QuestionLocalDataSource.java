package org.eyeseetea.malariacare.data.database.datasources;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.domain.boundary.repositories.IQuestionRepository;
import org.eyeseetea.malariacare.domain.entity.Question;

import java.util.ArrayList;
import java.util.List;

public class QuestionLocalDataSource implements IQuestionRepository{

    @Override
    public List<Question> getAll() {
        List<Question> questions = new ArrayList<>();

        List<QuestionDB> questionsDB = new Select().from(QuestionDB.class).queryList();

        for (QuestionDB questionDB:questionsDB) {
            questions.add(mapQuestion(questionDB));
        }

        return questions;
    }

    @Override
    public Question getQuestionByUId(String uid) {
        QuestionDB questionDB = QuestionDB.getQuestionByUid(uid);

        return mapQuestion(questionDB);
    }

    private Question mapQuestion(QuestionDB questionDB) {
        String answerName = null;

        if (questionDB.getAnswer() != null)
            answerName = questionDB.getAnswer().getName();

        return new Question(questionDB.getUid(), questionDB.getOutput(), questionDB.getCompulsory(),answerName);
    }

}
