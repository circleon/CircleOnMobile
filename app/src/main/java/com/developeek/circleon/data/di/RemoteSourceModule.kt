package com.developeek.circleon.data.di

import com.developeek.circleon.BuildConfig
import com.developeek.circleon.data.source.remote.interceptor.ErrorInterceptor
import com.developeek.circleon.data.source.remote.interceptor.HeaderInterceptor
import com.developeek.circleon.data.source.remote.interceptor.TokenAuthenticator
import com.developeek.circleon.data.source.remote.retrofit.service.AuthService
import com.developeek.circleon.data.source.remote.retrofit.service.CircleService
import com.developeek.circleon.data.source.remote.retrofit.service.TokenService
import com.developeek.circleon.data.source.remote.retrofit.service.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RemoteSourceModule {
    @Qualifier
    annotation class AuthClient

    annotation class ServiceClient

    @Provides
    @Singleton
    fun provideConverter(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    @AuthClient
    @Provides
    @Singleton
    fun provideAuthClient(errorInterceptor: ErrorInterceptor): Builder {
        return OkHttpClient().newBuilder()
            .addInterceptor(errorInterceptor)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
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
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
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
    fun provideAuthService(
        converter: GsonConverterFactory,
        @AuthClient clientBuilder: Builder,
    ): AuthService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.SERVICE_API_URL)
            .addConverterFactory(converter)
            .client(clientBuilder.build())
            .build()
            .create(AuthService::class.java)
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

    @Provides
    @Singleton
    fun provideUserService(
        converter: GsonConverterFactory,
        @ServiceClient clientBuilder: Builder,
    ): UserService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.SERVICE_API_URL)
            .addConverterFactory(converter)
            .client(clientBuilder.build())
            .build()
            .create(UserService::class.java)
    }
}
