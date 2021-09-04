package org.lovepeaceharmony.androidapp.repo.remote.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.lovepeaceharmony.androidapp.repo.pref.UserPreferencesRepository
import org.lovepeaceharmony.androidapp.repo.remote.LPHServiceRetro
import org.lovepeaceharmony.androidapp.utility.http.LPHUrl
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(LPHUrl.BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideLphService(retrofit: Retrofit): LPHServiceRetro = retrofit.create(
        LPHServiceRetro::class.java
    )

}