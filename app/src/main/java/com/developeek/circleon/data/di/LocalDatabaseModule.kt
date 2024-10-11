package com.developeek.circleon.data.di

import android.content.Context
import android.content.SharedPreferences
import com.developeek.circleon.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Qualifier
annotation class TokenSharedPreferences

@InstallIn(SingletonComponent::class)
@Module
object LocalDatabaseModule {
    @TokenSharedPreferences
    @Provides
    fun provideSharedPreferences(
        @ApplicationContext appContext: Context,
    ): SharedPreferences {
        return appContext.getSharedPreferences(
            BuildConfig.TOKEN_PREFERENCE_KEY,
            Context.MODE_PRIVATE,
        )
    }
}
