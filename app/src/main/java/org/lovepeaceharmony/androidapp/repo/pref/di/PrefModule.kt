package org.lovepeaceharmony.androidapp.repo.pref.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.lovepeaceharmony.androidapp.repo.pref.DataStoreManager
import javax.inject.Singleton

/*
@Module
@InstallIn(SingletonComponent::class)
object PrefModule {

    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context) = DataStoreManager(context)
}*/
