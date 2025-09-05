package com.google.wiltv.data.network

import android.content.Context
import androidx.room.Room
import coil.ImageLoader
import com.google.wiltv.AppDatabase
import com.google.wiltv.MIGRATION_1_2
import com.google.wiltv.MIGRATION_2_3
import com.google.wiltv.MIGRATION_3_4
import com.google.wiltv.data.dao.MovieRemoteKeyDao
import com.google.wiltv.data.dao.MoviesDao
import com.google.wiltv.data.dao.WatchlistDao
import com.google.wiltv.data.dao.WatchProgressDao
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.wiltv.data.models.SearchContent
import com.google.wiltv.data.models.SearchContentDeserializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedImageLoader

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    private val BASE_URL = "https://api.nortv.xyz"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(SearchContent::class.java, SearchContentDeserializer())
            .create()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Singleton
    @Provides
    fun provideMovieDatabase(@ApplicationContext context: Context): AppDatabase =
        Room
            .databaseBuilder(context, AppDatabase::class.java, "app_database")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()

    @Singleton
    @Provides
    fun provideMoviesDao(moviesDatabase: AppDatabase): MoviesDao = moviesDatabase.getMoviesDao()

    @Singleton
    @Provides
    fun provideRemoteKeysDao(moviesDatabase: AppDatabase): MovieRemoteKeyDao =
        moviesDatabase.getMovieRemoteKeyDao()

    @Singleton
    @Provides
    fun provideWatchlistDao(moviesDatabase: AppDatabase): WatchlistDao =
        moviesDatabase.getWatchlistDao()

    @Singleton
    @Provides
    fun provideWatchProgressDao(moviesDatabase: AppDatabase): WatchProgressDao =
        moviesDatabase.getWatchProgressDao()


    @Provides
    @Singleton
    fun userService(retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }

    @Provides
    @Singleton
    fun loginRequestService(retrofit: Retrofit): LoginRequestService {
        return retrofit.create(LoginRequestService::class.java)
    }

    @Provides
    @Singleton
    fun tokenService(retrofit: Retrofit): TokenService {
        return retrofit.create(TokenService::class.java)
    }


    @Provides
    @Singleton
    fun MovieService(retrofit: Retrofit): MovieService {
        return retrofit.create(MovieService::class.java)
    }

    @Provides
    @Singleton
    fun TvShowsService(retrofit: Retrofit): TvShowsService {
        return retrofit.create(TvShowsService::class.java)
    }

    @Provides
    @Singleton
    fun TvShowSeasonsService(retrofit: Retrofit): TvShowSeasonsService {
        return retrofit.create(TvShowSeasonsService::class.java)
    }

    @Provides
    @Singleton
    fun TvChannelsService(retrofit: Retrofit): TvChannelService {
        return retrofit.create(TvChannelService::class.java)
    }


    @Provides
    @Singleton
    fun StreamingProviderService(retrofit: Retrofit): StreamingProviderService {
        return retrofit.create(StreamingProviderService::class.java)
    }

    @Provides
    @Singleton
    fun CatalogService(retrofit: Retrofit): CatalogService {
        return retrofit.create(CatalogService::class.java)
    }

    @Provides
    @Singleton
    fun genreService(retrofit: Retrofit): GenreService {
        return retrofit.create(GenreService::class.java)
    }

    @Provides
    @Singleton
    fun SearchService(retrofit: Retrofit): SearchService {
        return retrofit.create(SearchService::class.java)
    }

    @Provides
    @Singleton
    fun BroadcastingService(retrofit: Retrofit): BroadcastingService {
        return retrofit.create(BroadcastingService::class.java)
    }

    @Provides
    @Singleton
    fun SportsService(retrofit: Retrofit): SportsService {
        return retrofit.create(SportsService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(userTokenProvider: UserTokenProvider): AuthInterceptor {
        return AuthInterceptor(userTokenProvider)
    }

    @Provides
    @Singleton
    @AuthenticatedImageLoader
    fun provideAuthenticatedImageLoader(
        @ApplicationContext context: Context,
        authInterceptor: AuthInterceptor
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .build()
            }
            .build()
    }
}