package org.eyeseetea.malariacare.factories

import android.content.Context
import org.eyeseetea.malariacare.data.database.datasources.OrgUnitLevelLocalDataSource
import org.eyeseetea.malariacare.data.database.datasources.OrgUnitLocalDataSource
import org.eyeseetea.malariacare.data.database.datasources.ProgramLocalDataSource
import org.eyeseetea.malariacare.data.repositories.*
import org.eyeseetea.malariacare.domain.boundary.repositories.*
import org.eyeseetea.malariacare.domain.usecase.*

object MetadataFactory {

    fun provideGetProgramByUidUseCase(): GetProgramByUidUseCase =
        GetProgramByUidUseCase(provideProgramRepository())

    fun provideGetOrgUnitByUidUseCase(): GetOrgUnitByUidUseCase =
        GetOrgUnitByUidUseCase(provideOrgUnitRepository())

    fun provideGetProgramsUseCase(): GetProgramsUseCase =
        GetProgramsUseCase(provideProgramRepository())

    fun provideGetOrgUnitsUseCase(): GetOrgUnitsUseCase =
        GetOrgUnitsUseCase(provideOrgUnitRepository())

    fun provideGetOrgUnitLevelsUseCase(): GetOrgUnitLevelsUseCase =
        GetOrgUnitLevelsUseCase(provideOrgUnitLevelRepository())

    fun provideServerMetadataUseCase(context: Context): GetServerMetadataUseCase =
        GetServerMetadataUseCase(provideServerMetadataRepository(context))

    private fun provideServerMetadataRepository(context: Context): IServerMetadataRepository =
        ServerMetadataRepository(context)

    private fun provideOrgUnitRepository(): IOrgUnitRepository =
        OrgUnitRepository(provideOrgUnitLocalDataSource())

    private fun provideOrgUnitLevelRepository(): IOrgUnitLevelRepository =
        OrgUnitLevelRepository(provideOrgUnitLevelLocalDataSource())

    private fun provideOrgUnitLocalDataSource(): OrgUnitLocalDataSource =
        OrgUnitLocalDataSource()

    private fun provideOrgUnitLevelLocalDataSource(): OrgUnitLevelLocalDataSource =
        OrgUnitLevelLocalDataSource()

    fun provideUserAccountRepository(context: Context): IUserAccountRepository =
        UserAccountRepository(context)

    private fun provideProgramRepository(): IProgramRepository =
        ProgramRepository(ProgramLocalDataSource())
}