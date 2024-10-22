package com.developeek.circleon.data.di

import com.developeek.circleon.BuildConfig
import com.developeek.circleon.data.source.remote.interceptor.ErrorInterceptor
import com.developeek.circleon.data.source.remote.interceptor.TokenAuthenticator
import com.developeek.circleon.data.source.remote.retrofit.service.LoginService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RemoteSourceModule {
    private const val TIMEOUT_LIMIT: Long = 5

    @Provides
    @Singleton
    fun provideConverter(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    @Provides
    @Singleton
    fun provideClient(
        errorInterceptor: ErrorInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient.Builder {
        return OkHttpClient().newBuilder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(errorInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(TIMEOUT_LIMIT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_LIMIT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_LIMIT, TimeUnit.SECONDS)
    }

    @Provides
    @Singleton
    fun provideLoginService(
        converter: GsonConverterFactory,
        clientBuilder: OkHttpClient.Builder,
    ): LoginService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.SERVICE_API_URL)
            .addConverterFactory(converter)
            .client(clientBuilder.build())
            .build()
            .create(LoginService::class.java)
    }
}
