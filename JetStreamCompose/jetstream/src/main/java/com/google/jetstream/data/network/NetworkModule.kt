package com.google.jetstream.data.network

import android.content.Context
import androidx.room.Room
import com.google.jetstream.AppDatabase
import com.google.jetstream.data.dao.MovieRemoteKeyDao
import com.google.jetstream.data.dao.MoviesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideMovieDatabase(@ApplicationContext context: Context): AppDatabase =
        Room
            .databaseBuilder(context, AppDatabase::class.java, "app_database")
            .build()

    @Singleton
    @Provides
    fun provideMoviesDao(moviesDatabase: AppDatabase): MoviesDao = moviesDatabase.getMoviesDao()

    @Singleton
    @Provides
    fun provideRemoteKeysDao(moviesDatabase: AppDatabase): MovieRemoteKeyDao =
        moviesDatabase.getMovieRemoteKeyDao()


    @Provides
    @Singleton
    fun customerService(retrofit: Retrofit): CustomerService {
        return retrofit.create(CustomerService::class.java)
    }

    @Provides
    @Singleton
    fun userService(retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
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
    fun GenreService(retrofit: Retrofit): GenreService {
        return retrofit.create(GenreService::class.java)
    }

    @Provides
    @Singleton
    fun SearchService(retrofit: Retrofit): SearchService {
        return retrofit.create(SearchService::class.java)
    }
}