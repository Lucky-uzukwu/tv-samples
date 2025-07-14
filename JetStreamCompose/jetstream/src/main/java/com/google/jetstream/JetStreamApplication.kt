package com.google.jetstream

import android.app.Application
import android.content.Context
import com.google.jetstream.data.network.CatalogService
import com.google.jetstream.data.network.CustomerService
import com.google.jetstream.data.network.GenreService
import com.google.jetstream.data.network.MovieService
import com.google.jetstream.data.network.SearchService
import com.google.jetstream.data.network.StreamingProviderService
import com.google.jetstream.data.network.TvShowsService
import com.google.jetstream.data.repositories.CatalogRepository
import com.google.jetstream.data.repositories.CatalogRepositoryImpl
import com.google.jetstream.data.repositories.CustomerRepository
import com.google.jetstream.data.repositories.CustomerRepositoryImpl
import com.google.jetstream.data.repositories.GenreRepository
import com.google.jetstream.data.repositories.GenreRepositoryImpl
import com.google.jetstream.data.repositories.MockCatalogRepositoryImpl
import com.google.jetstream.data.repositories.MockCustomerRepositoryImpl
import com.google.jetstream.data.repositories.MockGenreRepositoryImpl
import com.google.jetstream.data.repositories.MockMovieRepositoryImpl
import com.google.jetstream.data.repositories.MockSearchRepositoryImpl
import com.google.jetstream.data.repositories.MockStreamingProvidersRepositoryImpl
import com.google.jetstream.data.repositories.MockTvShowsRepositoryImpl
import com.google.jetstream.data.repositories.MovieCategoryDataSource
import com.google.jetstream.data.repositories.MovieDataSource
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.data.repositories.MovieRepositoryImpl
import com.google.jetstream.data.repositories.SearchRepository
import com.google.jetstream.data.repositories.SearchRepositoryImpl
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
import javax.inject.Named
import javax.inject.Singleton

@HiltAndroidApp
class JetStreamApplication : Application()


@InstallIn(SingletonComponent::class)
@Module
object MovieRepositoryModule {
    @Provides
    @Singleton
    fun provideMovieRepository(
        movieDataSource: MovieDataSource,
        movieCategoryDataSource: MovieCategoryDataSource,
        movieService: MovieService,
        customerRepository: CustomerRepository,
        userRepository: UserRepository,
        @Named("isMock") isMock: Boolean
    ): MovieRepository {
        return if (isMock) {
            MockMovieRepositoryImpl(
                movieDataSource,
                movieCategoryDataSource,
            )
        } else {
            MovieRepositoryImpl(
                movieDataSource,
                movieCategoryDataSource,
                movieService,
                customerRepository,
                userRepository
            )
        }
    }

    @Provides
    @Named("isMock")
    fun provideIsMock(): Boolean {
        // Enable mock mode for debug builds or offline scenarios
        return BuildConfig.DEBUG // Or check network status
    }
}

@InstallIn(SingletonComponent::class)
@Module
object TvShowsRepositoryModule {
    @Provides
    @Singleton
    fun provideMovieRepository(
        tvShowService: TvShowsService,
        customerRepository: CustomerRepository,
        userRepository: UserRepository,
        @Named("isMock") isMock: Boolean
    ): TvShowsRepository {
        return if (isMock) {
            MockTvShowsRepositoryImpl()
        } else {
            TvShowsRepositoryImpl(
                tvShowService,
                customerRepository,
                userRepository
            )
        }
    }
}

@InstallIn(SingletonComponent::class)
@Module
object CustomerRepositoryModule {
    @Provides
    @Singleton
    fun provideMovieRepository(
        customerService: CustomerService,
        @Named("isMock") isMock: Boolean
    ): CustomerRepository {
        return if (isMock) {
            MockCustomerRepositoryImpl()
        } else {
            CustomerRepositoryImpl(
                customerService
            )
        }
    }
}

@InstallIn(SingletonComponent::class)
@Module
object GenreRepositoryModule {
    @Provides
    @Singleton
    fun provideMovieRepository(
        genreService: GenreService,
        customerRepository: CustomerRepository,
        userRepository: UserRepository,
        @Named("isMock") isMock: Boolean
    ): GenreRepository {
        return if (isMock) {
            MockGenreRepositoryImpl()
        } else {
            GenreRepositoryImpl(
                genreService,
                customerRepository,
                userRepository
            )
        }
    }
}

@InstallIn(SingletonComponent::class)
@Module
object StreamingProvidersRepositoryModule {
    @Provides
    @Singleton
    fun provideMovieRepository(
        customerRepository: CustomerRepository,
        userRepository: UserRepository,
        streamingProviderService: StreamingProviderService,
        @Named("isMock") isMock: Boolean
    ): StreamingProvidersRepository {
        return if (isMock) {
            MockStreamingProvidersRepositoryImpl()
        } else {
            StreamingProvidersRepositoryImpl(
                customerRepository,
                userRepository,
                streamingProviderService
            )
        }
    }
}


@InstallIn(SingletonComponent::class)
@Module
object CatalogRepositoryModule {
    @Provides
    @Singleton
    fun provideMovieRepository(
        catalogService: CatalogService,
        customerRepository: CustomerRepository,
        userRepository: UserRepository,
        @Named("isMock") isMock: Boolean
    ): CatalogRepository {
        return if (isMock) {
            MockCatalogRepositoryImpl()
        } else {
            CatalogRepositoryImpl(
                catalogService,
                customerRepository,
                userRepository
            )
        }
    }
}

@InstallIn(SingletonComponent::class)
@Module
object SearchRepositoryModule {
    @Provides
    @Singleton
    fun provideMovieRepository(
        customerRepository: CustomerRepository,
        userRepository: UserRepository,
        searchService: SearchService,
        @Named("isMock") isMock: Boolean
    ): SearchRepository {
        return if (isMock) {
            MockSearchRepositoryImpl()
        } else {
            SearchRepositoryImpl(
                customerRepository,
                userRepository,
                searchService
            )
        }
    }
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

