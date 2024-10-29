package com.developeek.circleon.data.di

import com.developeek.circleon.data.repository.LoginRepository
import com.developeek.circleon.data.repositoryimpl.LoginRepositoryImpl
import com.developeek.circleon.data.source.remote.interceptor.TokenManager
import com.developeek.circleon.data.source.remote.retrofit.service.LoginService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {
    @Provides
    @Singleton
    fun provideLoginRepository(
        service: LoginService,
        tokenManager: TokenManager,
    ): LoginRepository {
        return LoginRepositoryImpl(service, tokenManager, Dispatchers.IO)
    }
}
