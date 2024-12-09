package com.aura.ui.transfer


import TransferViewModel
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aura.databinding.ActivityTransferBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TransferActivity : AppCompatActivity() {

  private lateinit var binding: ActivityTransferBinding
  private val transferViewModel: TransferViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityTransferBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val recipientEditText = binding.recipient
    val amountEditText = binding.amount
    val transferButton = binding.transfer
    val loadingIndicator = binding.loading

    // Observe the ViewModel for button enabling
    lifecycleScope.launch {
      transferViewModel.isButtonEnabled.collect { isEnabled ->
        transferButton.isEnabled = isEnabled
      }
    }

    // Observe the ViewModel for the loading state
    lifecycleScope.launch {
      transferViewModel.isLoading.collect { isLoading ->
        loadingIndicator.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
      }
    }

    // Observe the ViewModel for transfer result (success or failure)
    lifecycleScope.launch {
      transferViewModel.transferResult.collect { isSuccess ->
        if (isSuccess != null) {
          if (isSuccess) {
            // Transfer successful
            Toast.makeText(this@TransferActivity, "Transfer successful", Toast.LENGTH_LONG).show()

            // Fetch updated balance after successful transfer
            transferViewModel.fetchUpdatedBalance(
              intent.extras?.getString("currentUser") ?: ""
            )
          } else {
            // Transfer failed
            Toast.makeText(this@TransferActivity, "Transfer failed", Toast.LENGTH_LONG).show()
          }
        }
      }
    }

    // Observe the updated balance from ViewModel
    lifecycleScope.launch {
      transferViewModel.updatedBalance.collect { updatedBalance ->
        updatedBalance?.let {
          val resultIntent = Intent()
          resultIntent.putExtra("updatedBalance", it)
          setResult(Activity.RESULT_OK, resultIntent) // Pass updated balance to HomeActivity
          finish() // Finish the activity and go back to HomeActivity
        }
      }
    }

    // Add TextWatchers to recipient and amount fields
    recipientEditText.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
        transferViewModel.validateFields(recipientEditText.text.toString(), amountEditText.text.toString())
      }
      override fun afterTextChanged(editable: Editable?) {}
    })

    amountEditText.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
        transferViewModel.validateFields(recipientEditText.text.toString(), amountEditText.text.toString())
      }
      override fun afterTextChanged(editable: Editable?) {}
    })

    // Set the button listener
    transferButton.setOnClickListener {
      val recipient = recipientEditText.text.toString().trim()
      val amountString = binding.amount.text.toString().trim()

      // Validate fields before making the transfer
      if (recipient.isEmpty() || amountString.isEmpty()) {
        Toast.makeText(this@TransferActivity, "Please enter both recipient and amount", Toast.LENGTH_LONG).show()
        return@setOnClickListener
      }

      val amount = amountString.toDoubleOrNull()
      if (amount == null || amount <= 0.0) {
        Toast.makeText(this@TransferActivity, "Please enter a valid amount", Toast.LENGTH_LONG).show()
        return@setOnClickListener
      }

      val currentUser = intent.extras?.getString("currentUser") ?: ""
      if (currentUser.isEmpty()) {
        Toast.makeText(this@TransferActivity, "User ID is missing", Toast.LENGTH_LONG).show()
        return@setOnClickListener
      }

      // Call the ViewModel's transfer function
      transferViewModel.transfer(currentUser, recipient, amount)
    }
  }
}
