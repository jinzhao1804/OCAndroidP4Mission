package com.aura.ui.login

import androidx.lifecycle.ViewModel
import com.aura.data.model.ApiService
import com.aura.data.model.LoginRequest
import com.aura.data.model.LoginResponse
import com.aura.data.model.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {

    // StateFlow to represent whether the login button should be enabled or disabled
    private val _isFormValid = MutableStateFlow(false)
    val isFormValid: StateFlow<Boolean> get() = _isFormValid

    // Function to verify the form state
    fun verifyFormState(id: String, pwd: String) {
        // Check if both fields are not empty
        _isFormValid.value = id.isNotEmpty() && pwd.isNotEmpty()
    }

    // Function to call the API and perform login
    suspend fun login(identifier: String, password: String): LoginResponse {
        val loginRequest = LoginRequest(identifier, password)
        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        return apiService.login(loginRequest)
    }
}
