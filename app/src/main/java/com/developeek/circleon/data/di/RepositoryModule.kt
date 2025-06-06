package com.developeek.circleon.data.di

import com.developeek.circleon.data.repository.AuthRepository
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.repository.UserRepository
import com.developeek.circleon.data.repositoryimpl.AuthRepositoryImpl
import com.developeek.circleon.data.repositoryimpl.CircleRepositoryImpl
import com.developeek.circleon.data.repositoryimpl.UserRepositoryImpl
import com.developeek.circleon.data.source.manager.TokenManager
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.data.source.remote.retrofit.service.AuthService
import com.developeek.circleon.data.source.remote.retrofit.service.CircleService
import com.developeek.circleon.data.source.remote.retrofit.service.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        service: AuthService,
        tokenManager: TokenManager,
        userManager: UserManager,
    ): AuthRepository {
        return AuthRepositoryImpl(service, tokenManager, userManager)
    }

    @Provides
    @Singleton
    fun provideCircleRepository(service: CircleService): CircleRepository {
        return CircleRepositoryImpl(service)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        service: UserService,
        tokenManager: TokenManager,
        userManager: UserManager,
    ): UserRepository {
        return UserRepositoryImpl(service, tokenManager, userManager)
    }
}
