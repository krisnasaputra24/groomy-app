package com.krisna.groomy.ui.diagnosis

data class DiagnosisUiState(
    val textInput: String = "",
    val prediction: String = "",
    val confidence: Float = 0f,
    val recommendation: String = "",
    val isLoading: Boolean = false,
    val isResultVisible: Boolean = false
)
