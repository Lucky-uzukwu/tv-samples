package com.google.wiltv

import android.app.Application
import android.content.Context
import com.google.wiltv.data.network.CatalogService
import com.google.wiltv.data.network.CustomerService
import com.google.wiltv.data.network.GenreService
import com.google.wiltv.data.network.MovieService
import com.google.wiltv.data.network.SearchService
import com.google.wiltv.data.network.StreamingProviderService
import com.google.wiltv.data.network.TokenService
import com.google.wiltv.data.network.TvShowsService
import com.google.wiltv.data.network.UserService
import com.google.wiltv.data.repositories.CatalogRepository
import com.google.wiltv.data.repositories.CatalogRepositoryImpl
import com.google.wiltv.data.repositories.AuthRepository
import com.google.wiltv.data.repositories.AuthRepositoryImpl
import com.google.wiltv.data.repositories.GenreRepository
import com.google.wiltv.data.repositories.GenreRepositoryImpl
import com.google.wiltv.data.repositories.MockCatalogRepositoryImpl
import com.google.wiltv.data.repositories.MockAuthRepositoryImpl
import com.google.wiltv.data.repositories.MockGenreRepositoryImpl
import com.google.wiltv.data.repositories.MockMovieRepositoryImpl
import com.google.wiltv.data.repositories.MockSearchRepositoryImpl
import com.google.wiltv.data.repositories.MockStreamingProvidersRepositoryImpl
import com.google.wiltv.data.repositories.MockTvShowsRepositoryImpl
import com.google.wiltv.data.repositories.MovieCategoryDataSource
import com.google.wiltv.data.repositories.MovieDataSource
import com.google.wiltv.data.repositories.MovieRepository
import com.google.wiltv.data.repositories.MovieRepositoryImpl
import com.google.wiltv.data.repositories.SearchRepository
import com.google.wiltv.data.repositories.SearchRepositoryImpl
import com.google.wiltv.data.repositories.StreamingProvidersRepository
import com.google.wiltv.data.repositories.StreamingProvidersRepositoryImpl
import com.google.wiltv.data.repositories.TvShowsRepository
import com.google.wiltv.data.repositories.TvShowsRepositoryImpl
import com.google.wiltv.data.repositories.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@HiltAndroidApp
class WilTvApplication : Application()


@InstallIn(SingletonComponent::class)
@Module
object MovieRepositoryModule {
    @Provides
    @Singleton
    fun provideMovieRepository(
        movieDataSource: MovieDataSource,
        movieCategoryDataSource: MovieCategoryDataSource,
        movieService: MovieService,
        authRepository: AuthRepository,
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
                authRepository,
                userRepository
            )
        }
    }

    @Provides
    @Named("isMock")
    fun provideIsMock(): Boolean {
        // Enable mock mode for debug builds or offline scenarios
        return BuildConfig.DEBUG == false // Or check network status
    }
}

@InstallIn(SingletonComponent::class)
@Module
object TvShowsRepositoryModule {
    @Provides
    @Singleton
    fun provideMovieRepository(
        tvShowService: TvShowsService,
        authRepository: AuthRepository,
        userRepository: UserRepository,
        @Named("isMock") isMock: Boolean
    ): TvShowsRepository {
        return if (isMock) {
            MockTvShowsRepositoryImpl()
        } else {
            TvShowsRepositoryImpl(
                tvShowService,
                authRepository,
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
        userService: UserService,
        tokenService: TokenService,
        @Named("isMock") isMock: Boolean
    ): AuthRepository {
        return if (isMock) {
            MockAuthRepositoryImpl()
        } else {
            AuthRepositoryImpl(
                customerService,
                userService,
                tokenService
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
        authRepository: AuthRepository,
        userRepository: UserRepository,
        @Named("isMock") isMock: Boolean
    ): GenreRepository {
        return if (isMock) {
            MockGenreRepositoryImpl()
        } else {
            GenreRepositoryImpl(
                genreService,
                authRepository,
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
        authRepository: AuthRepository,
        userRepository: UserRepository,
        streamingProviderService: StreamingProviderService,
        @Named("isMock") isMock: Boolean
    ): StreamingProvidersRepository {
        return if (isMock) {
            MockStreamingProvidersRepositoryImpl()
        } else {
            StreamingProvidersRepositoryImpl(
                authRepository,
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
        authRepository: AuthRepository,
        userRepository: UserRepository,
        @Named("isMock") isMock: Boolean
    ): CatalogRepository {
        return if (isMock) {
            MockCatalogRepositoryImpl()
        } else {
            CatalogRepositoryImpl(
                catalogService,
                authRepository,
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
        authRepository: AuthRepository,
        userRepository: UserRepository,
        searchService: SearchService,
        @Named("isMock") isMock: Boolean
    ): SearchRepository {
        return if (isMock) {
            MockSearchRepositoryImpl()
        } else {
            SearchRepositoryImpl(
                authRepository,
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

