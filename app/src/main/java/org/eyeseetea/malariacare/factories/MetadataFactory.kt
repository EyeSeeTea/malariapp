package org.eyeseetea.malariacare.factories

import android.content.Context
import org.eyeseetea.malariacare.data.database.datasources.OrgUnitLocalDataSource
import org.eyeseetea.malariacare.data.database.datasources.ProgramLocalDataSource
import org.eyeseetea.malariacare.data.repositories.OrgUnitRepository
import org.eyeseetea.malariacare.data.repositories.ProgramRepository
import org.eyeseetea.malariacare.data.repositories.ServerMetadataRepository
import org.eyeseetea.malariacare.data.repositories.UserAccountRepository
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitRepository
import org.eyeseetea.malariacare.domain.boundary.repositories.IProgramRepository
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository
import org.eyeseetea.malariacare.domain.boundary.repositories.IUserAccountRepository
import org.eyeseetea.malariacare.domain.usecase.GetOrgUnitByUidUseCase
import org.eyeseetea.malariacare.domain.usecase.GetOrgUnitsUseCase
import org.eyeseetea.malariacare.domain.usecase.GetProgramByUidUseCase
import org.eyeseetea.malariacare.domain.usecase.GetProgramsUseCase
import org.eyeseetea.malariacare.domain.usecase.GetServerMetadataUseCase

object MetadataFactory {

    fun provideGetProgramByUidUseCase(): GetProgramByUidUseCase =
        GetProgramByUidUseCase(provideProgramRepository())

    fun provideGetOrgUnitByUidUseCase(): GetOrgUnitByUidUseCase =
        GetOrgUnitByUidUseCase(provideOrgUnitRepository())

    fun provideGetProgramsUseCase(): GetProgramsUseCase =
        GetProgramsUseCase(provideProgramRepository())

    fun provideGetOrgUnitsUseCase(): GetOrgUnitsUseCase =
        GetOrgUnitsUseCase(provideOrgUnitRepository())

    fun provideServerMetadataUseCase(context: Context): GetServerMetadataUseCase =
        GetServerMetadataUseCase(provideServerMetadataRepository(context))

    private fun provideServerMetadataRepository(context: Context): IServerMetadataRepository =
        ServerMetadataRepository(context)

    private fun provideOrgUnitRepository(): IOrgUnitRepository =
        OrgUnitRepository(provideOrgUnitLocalDataSource())

    private fun provideOrgUnitLocalDataSource(): OrgUnitLocalDataSource =
        OrgUnitLocalDataSource()

    fun provideUserAccountRepository(context: Context): IUserAccountRepository =
        UserAccountRepository(context)

    private fun provideProgramRepository(): IProgramRepository =
        ProgramRepository(ProgramLocalDataSource())
}