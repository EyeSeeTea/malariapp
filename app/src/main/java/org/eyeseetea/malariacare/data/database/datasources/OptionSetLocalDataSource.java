package org.eyeseetea.malariacare.data.database.datasources;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.data.database.model.AnswerDB;
import org.eyeseetea.malariacare.domain.entity.OptionSet;

import java.util.ArrayList;
import java.util.List;

public class OptionSetLocalDataSource
        implements IMetadataLocalDataSource<OptionSet> {
    @Override
    public List<OptionSet> getAll() {
        List<AnswerDB> answersDB = new Select().from(AnswerDB.class).queryList();

        return mapToDomain(answersDB);
    }

    @Override
    public void clearAndSave(List<OptionSet> optionSets) throws Exception {
        Delete.table(AnswerDB.class);

        List<AnswerDB> answersDB = mapToDB(optionSets);

        for (AnswerDB answerDB:answersDB) {
            answerDB.save();
        }
    }

    private List<OptionSet> mapToDomain(List<AnswerDB> answersDB) {
        List<OptionSet> optionSets = new ArrayList<>();

        for (AnswerDB answerDB:answersDB) {
            optionSets.add(new OptionSet(answerDB.getUid_option_set(),answerDB.getName()));
        }

        return optionSets;
    }

    private List<AnswerDB> mapToDB(List<OptionSet> optionSets) {
        List<AnswerDB> answersDB = new ArrayList<>();

        for (OptionSet optionSet:optionSets) {
            AnswerDB answerDB = new AnswerDB();
            answerDB.setUid_option_set(optionSet.getUid());
            answerDB.setName(optionSet.getName());

            answersDB.add(answerDB);
        }

        return answersDB;
    }
}
