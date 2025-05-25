/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jetstream

import android.app.Application
import android.content.Context
import com.google.jetstream.data.repositories.CatalogRepository
import com.google.jetstream.data.repositories.CatalogRepositoryImpl
import com.google.jetstream.data.repositories.CustomerRepository
import com.google.jetstream.data.repositories.CustomerRepositoryImpl
import com.google.jetstream.data.repositories.GenreRepository
import com.google.jetstream.data.repositories.GenreRepositoryImpl
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.data.repositories.MovieRepositoryImpl
import com.google.jetstream.data.repositories.StreamingProvidersRepository
import com.google.jetstream.data.repositories.StreamingProvidersRepositoryImpl
import com.google.jetstream.data.repositories.TvShowsRepository
import com.google.jetstream.data.repositories.TvShowsRepositoryImpl
import com.google.jetstream.data.repositories.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@HiltAndroidApp
class JetStreamApplication : Application()

@InstallIn(SingletonComponent::class)
@Module
abstract class MovieRepositoryModule {

    @Binds
    abstract fun bindMovieRepository(
        movieRepositoryImpl: MovieRepositoryImpl
    ): MovieRepository
}

@InstallIn(SingletonComponent::class)
@Module
abstract class TvShowsRepositoryModule {

    @Binds
    abstract fun bindTvShowsRepository(
        tvShowsRepositoryImpl: TvShowsRepositoryImpl
    ): TvShowsRepository
}

@InstallIn(SingletonComponent::class)
@Module
abstract class CustomerRepositoryModule {

    @Binds
    abstract fun bindCustomerRepository(
        customerRepositoryImpl: CustomerRepositoryImpl
    ): CustomerRepository
}

@InstallIn(SingletonComponent::class)
@Module
abstract class GenreRepositoryModule {

    @Binds
    abstract fun bindGenreRepository(
        genreRepositoryImpl: GenreRepositoryImpl
    ): GenreRepository
}

@InstallIn(SingletonComponent::class)
@Module
abstract class StreamingProvidersRepositoryModule {

    @Binds
    abstract fun bindStreamingProvidersRepository(
        streamingProvidersRepositoryImpl: StreamingProvidersRepositoryImpl
    ): StreamingProvidersRepository
}

@InstallIn(SingletonComponent::class)
@Module
abstract class CatalogRepositoryModule {

    @Binds
    abstract fun bindCatalogRepository(
        catalogRepositoryImpl: CatalogRepositoryImpl
    ): CatalogRepository
}


@Module
@InstallIn(SingletonComponent::class)
object UserRepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        @ApplicationContext context: Context
    ): UserRepository {
        return UserRepository(context)
    }
}

