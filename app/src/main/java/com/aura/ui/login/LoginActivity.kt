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

    setupObservers()
    setupTextWatchers()
    setupLoginButton()
  }

  // Function to setup all observers
  private fun setupObservers() {
    observeFormState()
    observeLoadingState()
    observeNavigateToHome()
  }

  // Function to observe form state (enable/disable login button)
  private fun observeFormState() {
    lifecycleScope.launch {
      viewModel.isFormValid.collect { isValid ->
        binding.login.isEnabled = isValid
      }
    }
  }

  // Function to observe loading state and show/hide the loading indicator
  private fun observeLoadingState() {
    lifecycleScope.launch {
      viewModel.isLoading.collect { isLoading ->
        binding.loading.visibility = if (isLoading) View.VISIBLE else View.GONE
      }
    }
  }

  // Function to observe the navigateToHome LiveData and handle navigation
  private fun observeNavigateToHome() {
    lifecycleScope.launch {
      viewModel.navigateToHome.collect { identifier ->
        identifier?.let {
          navigateToHomeActivity(it)
        }
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

  // Function to set up the login button click listener
  private fun setupLoginButton() {
    binding.login.setOnClickListener {
      val identifier = binding.identifier.text.toString()
      val password = binding.password.text.toString()

      // Perform login operation
      loginUser(identifier, password)
    }
  }

  // Function to perform the login operation
  private fun loginUser(identifier: String, password: String) {
    lifecycleScope.launch {
      try {
        viewModel.login(identifier, password)
      } catch (e: Exception) {
        // Show error if login fails
        Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
      }
    }
  }

  // Function to navigate to HomeActivity with the identifier
  private fun navigateToHomeActivity(identifier: String) {
    val intent = Intent(this, HomeActivity::class.java)
    intent.putExtra("currentUser", identifier)
    startActivity(intent)
    finish()  // Close the LoginActivity
  }
}
