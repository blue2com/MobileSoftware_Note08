package com.example.note08.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.note08.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    repo: SettingsRepository
) : ViewModel() {
    val isDark = repo.darkModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}