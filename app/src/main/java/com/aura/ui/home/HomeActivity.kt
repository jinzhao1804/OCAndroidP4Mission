package com.aura.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aura.R
import com.aura.data.model.ApiService
import com.aura.data.model.RetrofitClient
import com.aura.databinding.ActivityHomeBinding
import com.aura.ui.login.LoginActivity
import com.aura.ui.transfer.TransferActivity
import kotlinx.coroutines.launch

/**
 * The home activity for the app.
 */
class HomeActivity : AppCompatActivity()
{

  /**
   * The binding for the home layout.
   */
  private lateinit var binding: ActivityHomeBinding

  /**
   * A callback for the result of starting the TransferActivity.
   */
  private val startTransferActivityForResult =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        val updatedBalance = result.data?.getStringExtra("updatedBalance")
        if (updatedBalance != null) {
          // Update the balance UI
          binding.balance.text = updatedBalance
        }
      }
    }
  override fun onCreate(savedInstanceState: Bundle?)
  {
    super.onCreate(savedInstanceState)

    binding = ActivityHomeBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val balance = binding.balance
    val transfer = binding.transfer

    //balance.text = "2654,54€"

    val extras = intent.extras
    if (extras != null) {
      val value = extras.getString("currentUser")
      //The key argument here must match that used in the other activity

      fetchAccountData(value.toString())
    }

    transfer.setOnClickListener {


      startTransferActivityForResult.launch(
        Intent(this@HomeActivity, TransferActivity::class.java).putExtra("currentUser",
          extras?.getString("currentUser").toString())
      )
    }
  }

  private fun fetchAccountData(userId: String) {
    lifecycleScope.launch {
      try {
        // Make the API call to fetch account data
        val accountResponseList = RetrofitClient.instance.create(ApiService::class.java)
          .getAccount(userId)

        // Check if the accountResponseList is not empty
        if (accountResponseList.isNotEmpty()) {
          val accountResponse = accountResponseList[0]  // Get the first account in the list

          // Log the response to verify its structure
          Log.d("fetchAccountData", "Account Response: $accountResponse")

          // Update the UI with the account details
          binding.balance.text = "${accountResponse.balance}€"  // Display the balance
          Toast.makeText(this@HomeActivity, "Amount fetched", Toast.LENGTH_LONG).show()
        } else {
          Toast.makeText(this@HomeActivity, "No account found", Toast.LENGTH_LONG).show()
        }
      } catch (e: Exception) {
        // Handle errors (e.g., network errors)
        Toast.makeText(
          this@HomeActivity,
          "Error fetching account data: ${e.message}",
          Toast.LENGTH_LONG
        ).show()
        Log.e("fetchAccountData", "Error: ${e.message}")
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean
  {
    menuInflater.inflate(R.menu.home_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean
  {
    return when (item.itemId)
    {
      R.id.disconnect ->
      {
        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
        finish()
        true
      }
      else            -> super.onOptionsItemSelected(item)
    }
  }

}
