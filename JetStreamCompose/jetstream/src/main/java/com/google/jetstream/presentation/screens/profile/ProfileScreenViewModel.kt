package com.google.jetstream.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import co.touchlab.kermit.Logger
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.data.entities.User
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.network.TokenForCustomerResponse
import com.google.jetstream.data.repositories.CatalogRepository
import com.google.jetstream.data.repositories.GenreRepository
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.data.repositories.StreamingProvidersRepository
import com.google.jetstream.data.repositories.UserRepository
import com.google.jetstream.data.pagingsources.movie.MoviesPagingSources
import com.google.jetstream.data.pagingsources.movie.MoviesHeroSectionPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject


@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    init {
        getUser()
    }

    private val _uiState = MutableStateFlow(ProfileScreenUiState.Ready())
    val uiState: StateFlow<ProfileScreenUiState> = _uiState.asStateFlow()

    fun getUser() {

        viewModelScope.launch {
            ProfileScreenUiState.Loading
            try {
                val user = userRepository.getUser()
                _uiState.update { it.copy(user = user) }
            } catch (e: Exception) {
                ProfileScreenUiState.Error
            }
        }
    }
}

sealed interface ProfileScreenUiState {
    data object Loading : ProfileScreenUiState
    data object Error : ProfileScreenUiState
    data class Ready(
        val user: User? = null,
    ) : ProfileScreenUiState
}
