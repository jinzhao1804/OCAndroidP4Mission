package com.aura.ui.transfer

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aura.data.model.ApiService
import com.aura.data.model.RetrofitClient
import com.aura.data.model.transfer.TransferRequest
import com.aura.databinding.ActivityTransferBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * The transfer activity for the app.
 */
class TransferActivity : AppCompatActivity() {

  /**
   * The binding for the transfer layout.
   */
  private lateinit var binding: ActivityTransferBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityTransferBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val extras = intent.extras
    val currentUser = extras?.getString("currentUser") ?: ""

    val recipientEditText = binding.recipient
    //val recipient = recipientEditText.text.toString()
  //  val amountString = binding.amount.text.toString()
   // val amount = if (amountString.isNotEmpty()) amountString.toDouble() else 0.0

    val transferButton = binding.transfer
    val loadingIndicator = binding.loading

    transferButton.setOnClickListener {
      val recipient = recipientEditText.text.toString().trim()
      val amountString = binding.amount.text.toString().trim()

      // Check if recipient or amount is empty
      if (recipient.isEmpty() || amountString.isEmpty()) {
        Toast.makeText(this@TransferActivity, "Please enter both recipient and amount", Toast.LENGTH_LONG).show()
        return@setOnClickListener
      }

      // Try to parse the amount string into a double
      val amount = amountString.toDoubleOrNull()

      if (amount == null || amount <= 0.0) {
        Toast.makeText(this@TransferActivity, "Please enter a valid amount", Toast.LENGTH_LONG).show()
        return@setOnClickListener
      }

      val transferRequest = TransferRequest(currentUser, recipient, amount)
      Log.e("TransferRequest", "Sending request: $transferRequest")


      lifecycleScope.launch {
        try {
          loadingIndicator.visibility = View.VISIBLE

          // Send the transfer request to the server
          val response = RetrofitClient.instance.create(ApiService::class.java).transfer(transferRequest)

          // Handle server response
          if (response.result) {
            Toast.makeText(this@TransferActivity, "Transfer successful", Toast.LENGTH_LONG).show()
          } else {
            Toast.makeText(this@TransferActivity, "Transfer failed", Toast.LENGTH_LONG).show()
          }

        } catch (e: HttpException) {
          // Log detailed error information
          val errorBody = e.response()?.errorBody()?.string()
          Log.e("TransferActivity", "HTTP 500 Internal Server Error: ${e.message()}")
          Log.e("TransferActivity", "Error Body: $errorBody")
          Toast.makeText(this@TransferActivity, "Server error occurred. Please try again later.", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
          // Handle other exceptions
          Toast.makeText(this@TransferActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
          Log.e("TransferActivity", "General Error: ${e.message}", e)
        } finally {
          loadingIndicator.visibility = View.GONE
          setResult(Activity.RESULT_OK)
          finish()
        }
      }

    }

  }
}
