package com.developeek.circleon.data.di

import com.developeek.circleon.BuildConfig
import com.developeek.circleon.data.source.remote.interceptor.ErrorInterceptor
import com.developeek.circleon.data.source.remote.interceptor.HeaderInterceptor
import com.developeek.circleon.data.source.remote.interceptor.TokenAuthenticator
import com.developeek.circleon.data.source.remote.retrofit.service.CircleService
import com.developeek.circleon.data.source.remote.retrofit.service.LoginService
import com.developeek.circleon.data.source.remote.retrofit.service.TokenService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RemoteSourceModule {
    @Qualifier
    annotation class LoginClient

    annotation class ServiceClient

    @Provides
    @Singleton
    fun provideConverter(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    @LoginClient
    @Provides
    @Singleton
    fun provideLoginClient(errorInterceptor: ErrorInterceptor): Builder {
        return OkHttpClient().newBuilder()
            .addInterceptor(errorInterceptor)
    }

    @ServiceClient
    @Provides
    @Singleton
    fun provideServiceClient(
        errorInterceptor: ErrorInterceptor,
        headerInterceptor: HeaderInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): Builder {
        return OkHttpClient().newBuilder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(errorInterceptor)
            .authenticator(tokenAuthenticator)
    }

    @Provides
    @Singleton
    fun provideTokenService(converter: GsonConverterFactory): TokenService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.SERVICE_API_URL)
            .addConverterFactory(converter)
            .build()
            .create(TokenService::class.java)
    }

    @Provides
    @Singleton
    fun provideLoginService(
        converter: GsonConverterFactory,
        @LoginClient clientBuilder: Builder,
    ): LoginService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.SERVICE_API_URL)
            .addConverterFactory(converter)
            .client(clientBuilder.build())
            .build()
            .create(LoginService::class.java)
    }

    @Provides
    @Singleton
    fun provideCircleService(
        converter: GsonConverterFactory,
        @ServiceClient clientBuilder: Builder,
    ): CircleService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.SERVICE_API_URL)
            .addConverterFactory(converter)
            .client(clientBuilder.build())
            .build()
            .create(CircleService::class.java)
    }
}
