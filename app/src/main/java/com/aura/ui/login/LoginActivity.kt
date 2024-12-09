package com.aura.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.aura.databinding.ActivityLoginBinding
import com.aura.ui.home.HomeActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

  private lateinit var binding: ActivityLoginBinding
  private val viewModel: LoginViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // Collect the form state and loading state from the ViewModel
    collectFormState()
    collectLoadingState()

    // Set up listeners for the form fields
    setupTextWatchers()

    // Observe the ViewModel's state to navigate to HomeActivity when login is successful
    observeNavigateToHome()

    // Set up the login button click listener
    binding.login.setOnClickListener {
      val identifier = binding.identifier.text.toString()
      val password = binding.password.text.toString()

      // Perform login operation
      lifecycleScope.launch {
        try {
          viewModel.login(identifier, password)
        } catch (e: Exception) {
          // Show error if login fails
          Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
      }
    }
  }

  // Function to observe the form state (enable/disable login button)
  private fun collectFormState() {
    lifecycleScope.launch {
      viewModel.isFormValid.collect { isValid ->
        binding.login.isEnabled = isValid
      }
    }
  }

  // Function to observe loading state and show/hide the loading indicator
  private fun collectLoadingState() {
    lifecycleScope.launch {
      viewModel.isLoading.collect { isLoading ->
        binding.loading.visibility = if (isLoading) View.VISIBLE else View.GONE
      }
    }
  }

  // Function to set up listeners for the form fields
  private fun setupTextWatchers() {
    binding.identifier.addTextChangedListener {
      checkFormState()
    }

    binding.password.addTextChangedListener {
      checkFormState()
    }
  }

  // Function to verify the form state whenever the text changes
  private fun checkFormState() {
    val id = binding.identifier.text.toString()
    val pwd = binding.password.text.toString()

    viewModel.verifyFormState(id, pwd)
  }

  // Function to observe the navigateToHome LiveData and handle navigation
  private fun observeNavigateToHome() {
    lifecycleScope.launch {
      viewModel.navigateToHome.collect { identifier ->
        if (identifier != null) {
          navigateToHomeActivity(identifier)  // Navigate to HomeActivity with the identifier
        }
      }
    }
  }

  // Function to navigate to HomeActivity
  private fun navigateToHomeActivity(identifier: String) {
    val intent = Intent(this, HomeActivity::class.java)
    intent.putExtra("currentUser", identifier)
    startActivity(intent)
    finish()  // Close the LoginActivity
  }
}
