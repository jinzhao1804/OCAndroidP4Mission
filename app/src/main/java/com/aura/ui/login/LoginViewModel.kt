package com.aura.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.data.model.ApiService
import com.aura.data.model.login.LoginRequest
import com.aura.data.model.login.LoginResponse
import com.aura.data.model.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginViewModel : ViewModel() {

    // StateFlow to represent whether the login button should be enabled or disabled
    private val _isFormValid = MutableStateFlow(false)
    val isFormValid: StateFlow<Boolean> get() = _isFormValid

    // StateFlow to manage the loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    // StateFlow to notify when the login is successful
    private val _navigateToHome = MutableStateFlow<String?>(null)  // Will contain the identifier if login is successful
    val navigateToHome: StateFlow<String?> get() = _navigateToHome

    // Function to verify the form state
    fun verifyFormState(id: String, pwd: String) {
        // Check if both fields are not empty
        _isFormValid.value = id.isNotEmpty() && pwd.isNotEmpty()
    }

    // Function to call the API and perform login
    suspend fun login(identifier: String, password: String) {
        _isLoading.value = true
        try {
            val loginRequest = LoginRequest(identifier, password)
            val apiService = RetrofitClient.instance.create(ApiService::class.java)

            val response = withContext(Dispatchers.IO) {
                apiService.login(loginRequest)
            }

            _isLoading.value = false

            if (response.granted) {
                // If login is successful, notify the activity to navigate to HomeActivity
                _navigateToHome.value = identifier  // Pass the identifier to the activity
            } else {
                // Handle login failure (e.g., show an error message as a toast)
                throw Exception("Login failed: $response")
            }
        } catch (e: Exception) {
            _isLoading.value = false
            throw e
        }
    }
}
