package org.eyeseetea.malariacare.factories;

import android.content.Context;
import android.support.annotation.NonNull;

import org.eyeseetea.malariacare.data.database.datasources.QuestionLocalDataSource;
import org.eyeseetea.malariacare.data.repositories.OptionRepository;
import org.eyeseetea.malariacare.data.repositories.OrgUnitRepository;
import org.eyeseetea.malariacare.data.repositories.ServerMetadataRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOptionRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IQuestionRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;

public class MetadataFactory {
    @NonNull
    public IServerMetadataRepository getServerMetadataRepository(Context context) {
        return new ServerMetadataRepository(context);
    }

    @NonNull
    public IOptionRepository getOptionRepository() {
        return new OptionRepository();
    }

    public IQuestionRepository getQuestionLocalDataSource() {
        return new QuestionLocalDataSource();
    }

    public IOrgUnitRepository getOrgUnitRepository() {
        return new OrgUnitRepository();
    }
}
