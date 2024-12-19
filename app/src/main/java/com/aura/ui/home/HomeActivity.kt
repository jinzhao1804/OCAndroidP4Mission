package com.aura.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aura.R
import com.aura.data.model.ApiService
import com.aura.data.model.RetrofitClient
import com.aura.data.model.account.AccountResponse
import com.aura.databinding.ActivityHomeBinding
import com.aura.ui.login.LoginActivity
import com.aura.ui.transfer.TransferActivity
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

  private lateinit var binding: ActivityHomeBinding

  private val startTransferActivityForResult =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      handleTransferResult(result)
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityHomeBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupUI()
    handleIntentExtras()
  }

  // Setup UI components and their event listeners
  private fun setupUI() {
    setupReloadButton()
    setupTransferButton()
  }

  // Setup the reload button
  private fun setupReloadButton() {
    binding.reloadButton.setOnClickListener {
      reloadActivity()
    }
  }

  // Reload the activity (start LoginActivity)
  private fun reloadActivity() {
    startActivity(Intent(this, LoginActivity::class.java))
    finish()
  }

  // Setup the transfer button
  private fun setupTransferButton() {
    binding.transfer.setOnClickListener {
      startTransferActivityForResult.launch(
        Intent(this@HomeActivity, TransferActivity::class.java).apply {
          putExtra("currentUser", intent.extras?.getString("currentUser"))
        }
      )
    }
  }

  // Handle the intent extras to fetch the user ID and account data
  private fun handleIntentExtras() {
    val extras = intent.extras
    val userId = extras?.getString("currentUser") ?: return
    fetchAccountData(userId)
  }

  // Fetch the account data for a given user
  private fun fetchAccountData(userId: String) {
    lifecycleScope.launch {
      try {
        val accountResponseList = RetrofitClient.instance.create(ApiService::class.java)
          .getAccount(userId)

        if (accountResponseList.isNotEmpty()) {
          handleAccountResponse(accountResponseList[0])
        } else {
          showToast("No account found")
          showReloadButton()
        }
      } catch (e: Exception) {
        handleErrorFetchingData(e)
      }
    }
  }

  // Handle the API response when account data is fetched successfully
  private fun handleAccountResponse(accountResponse: AccountResponse) {
    Log.d("fetchAccountData", "Account Response: $accountResponse")
    binding.balance.text = "${accountResponse.balance}€"
    showToast("Amount fetched")
    hideReloadButton()
  }

  // Show the reload button
  private fun showReloadButton() {
    binding.reloadButton.visibility = View.VISIBLE
  }

  // Hide the reload button
  private fun hideReloadButton() {
    binding.reloadButton.visibility = View.GONE
  }

  // Show a toast message
  private fun showToast(message: String) {
    Toast.makeText(this@HomeActivity, message, Toast.LENGTH_LONG).show()
  }

  // Handle errors when fetching account data
  private fun handleErrorFetchingData(exception: Exception) {
    Log.e("fetchAccountData", "Error: ${exception.message}")
    showToast("Error fetching account data: ${exception.message}")
    showReloadButton()
  }

  // Handle the result from the TransferActivity
  private fun handleTransferResult(result: ActivityResult) {
    if (result.resultCode == Activity.RESULT_OK) {
      val updatedBalance = result.data?.getStringExtra("updatedBalance")
      updatedBalance?.let {
        binding.balance.text = it
      }
    }
  }

  // Inflate the options menu
  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.home_menu, menu)
    return true
  }

  // Handle options menu item selections
  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.disconnect -> {
        reloadActivity()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }
}
