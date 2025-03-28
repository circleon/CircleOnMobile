package com.developeek.circleon.data.di

import android.util.Log
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
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Handshake
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
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
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
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
            .eventListener(
                object : EventListener() {
                    override fun dnsStart(
                        call: Call,
                        domainName: String,
                    ) {
                        super.dnsStart(call, domainName)
                        Log.d("connect", "dns Start")
                    }

                    override fun dnsEnd(
                        call: Call,
                        domainName: String,
                        inetAddressList: MutableList<InetAddress>,
                    ) {
                        super.dnsEnd(call, domainName, inetAddressList)
                        Log.d("connect", "dns End")
                    }

                    override fun connectStart(
                        call: Call,
                        inetSocketAddress: InetSocketAddress,
                        proxy: Proxy,
                    ) {
                        super.connectStart(call, inetSocketAddress, proxy)
                        Log.d("connect", "connectStart")
                    }

                    override fun secureConnectStart(call: Call) {
                        super.secureConnectStart(call)

                        Log.d("connect", "secureConnectStart")
                    }

                    override fun secureConnectEnd(
                        call: Call,
                        handshake: Handshake?,
                    ) {
                        super.secureConnectEnd(call, handshake)
                        Log.d("connect", "secure Connect End")
                    }

                    override fun connectEnd(
                        call: Call,
                        inetSocketAddress: InetSocketAddress,
                        proxy: Proxy,
                        protocol: Protocol?,
                    ) {
                        super.connectEnd(call, inetSocketAddress, proxy, protocol)
                        Log.d("connect", "connect End")
                    }
                },
            )
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
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
