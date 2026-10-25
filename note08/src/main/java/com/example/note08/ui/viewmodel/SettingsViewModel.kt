package com.example.note08.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.note08.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository
) : ViewModel() {

    val darkMode = repo.darkModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val notification = repo.notificationFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun toggleDarkMode(v: Boolean) {
        viewModelScope.launch { repo.setDarkMode(v) }
    }

    fun toggleNotification(v: Boolean) {
        viewModelScope.launch { repo.setNotification(v) }
    }
}