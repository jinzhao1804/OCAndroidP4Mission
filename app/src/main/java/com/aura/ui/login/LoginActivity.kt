package com.aura.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels  // For Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.aura.databinding.ActivityLoginBinding
import com.aura.ui.home.HomeActivity
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * The login activity for the app.
 */
class LoginActivity : AppCompatActivity()
{

  /**
   * The binding for the login layout.
   */
  private lateinit var binding: ActivityLoginBinding
  private val viewModel: LoginViewModel by viewModels()


  override fun onCreate(savedInstanceState: Bundle?)
  {
    super.onCreate(savedInstanceState)

    binding = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val login = binding.login
    val loading = binding.loading

    // Collect the form validation state from the StateFlow
    collectFormState()

    // Set up listeners for the form fields
    binding.identifier.addTextChangedListener {
      checkFormState()
    }
    binding.password.addTextChangedListener {
      checkFormState()
    }



// Inside the login setOnClickListener in LoginActivity
    login.setOnClickListener {
      loading.visibility = View.VISIBLE
      val identifier = binding.identifier.text.toString()
      val password = binding.password.text.toString()

      // Call the login function
      lifecycleScope.launch {
        try {
          val response = viewModel.login(identifier, password)
          loading.visibility = View.GONE

          if (response.granted) {
            // If login is successful, navigate to the HomeActivity
            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            intent.putExtra("currentUser",identifier);
            startActivity(intent)
            finish()
          } else {
            // Handle login failure (e.g., show an error message as a toast)
            Toast.makeText(this@LoginActivity, "Login failed: ${response}", Toast.LENGTH_LONG).show()
            Log.e("error0","$response")

          }
        } catch (e: HttpException) {
          loading.visibility = View.GONE
          Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
          Log.e("error1","$e")
          Log.e("error1", e.message())

        } catch (e: IOException) {
          loading.visibility = View.GONE
          Toast.makeText(this@LoginActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
          Log.e("error2","$e")
        }
      }
    }


  }

  // Function to collect the form state and enable/disable the login button
  private fun collectFormState() {
    lifecycleScope.launch {
      viewModel.isFormValid.collect { isValid ->
        // Enable or disable the login button based on the form validity
        binding.login.isEnabled = isValid
      }
    }
  }
  // Function to verify the form state whenever the text changes
  private fun checkFormState() {
    val id = binding.identifier.text.toString()
    val pwd = binding.password.text.toString()

    // Verify the form state in the ViewModel
    viewModel.verifyFormState(id, pwd)
  }

}
