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
import androidx.core.widget.addTextChangedListener
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

    setupUI()
    observeViewModel()
    // The login button is disabled if the recipient field or the amount field is empty.
    binding.transfer.isEnabled = false
  }

  // Setup UI components and event listeners
  private fun setupUI() {
    setupTextWatchers()
    setupTransferButton()

  }

  // Observing ViewModel states
  private fun observeViewModel() {
    observeButtonState()
    observeLoadingState()
    observeTransferResult()
    observeUpdatedBalance()
  }

  // Observing button enabling state
  private fun observeButtonState() {
    lifecycleScope.launch {
      transferViewModel.isButtonEnabled.collect { isEnabled ->
        binding.transfer.isEnabled = isEnabled
      }
    }
  }

  // Observing loading state to show/hide the loading indicator
  // The transfer button is disabled while the transfer is being checked
  private fun observeLoadingState() {
    lifecycleScope.launch {
      transferViewModel.isLoading.collect { isLoading ->
        binding.loading.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        binding.transfer.isEnabled = if (isLoading) false else true
      }
    }
  }

  // Observing transfer result (success or failure)
  private fun observeTransferResult() {
    lifecycleScope.launch {
      transferViewModel.transferResult.collect { isSuccess ->
        if (isSuccess != null) {
          showToast(if (isSuccess) "Transfer successful" else "Transfer failed")
          if (isSuccess) fetchUpdatedBalance()
        }
      }
    }
  }

  // Observing updated balance
  private fun observeUpdatedBalance() {
    lifecycleScope.launch {
      transferViewModel.updatedBalance.collect { updatedBalance ->
        updatedBalance?.let {
          sendUpdatedBalanceBack(it)
        }
      }
    }
  }

  // Setup TextWatchers for recipient and amount fields
  private fun setupTextWatchers() {



    binding.recipient.addTextChangedListener {
      validateFields()
    }
    binding.amount.addTextChangedListener {
      validateFields()
    }
  }

  // Validate recipient and amount fields to enable/disable the transfer button
  // The login button is disabled if the recipient field or the amount field is empty.
  private fun validateFields() {
    val recipient = binding.recipient.text.toString()
    val amount = binding.amount.text.toString()
    transferViewModel.validateFields(recipient, amount)
  }

  // Setup transfer button click listener
  private fun setupTransferButton() {
    binding.transfer.setOnClickListener {
      val recipient = binding.recipient.text.toString().trim()
      val amount = binding.amount.text.toString().trim()

      if (validateTransferInput(recipient, amount)) {
        transferViewModel.transfer(
          currentUser = intent.extras?.getString("currentUser") ?: "",
          recipient = recipient,
          amount = amount.toDouble()
        )
      }
    }
  }

  // Validate transfer input before initiating transfer
  private fun validateTransferInput(recipient: String, amount: String): Boolean {
    when {
      recipient.isEmpty() || amount.isEmpty() -> {
        binding.transfer.isEnabled = false
        showToast("Please enter both recipient and amount")
        return false
      }
      amount.toDoubleOrNull() == null || amount.toDouble() <= 0.0 -> {
        showToast("Please enter a valid amount")
        return false
      }
      intent.extras?.getString("currentUser").isNullOrEmpty() -> {
        showToast("User ID is missing")
        return false
      }
    }
    return true
  }

  // Fetch updated balance after successful transfer
  private suspend fun fetchUpdatedBalance() {
    transferViewModel.fetchUpdatedBalance(
      intent.extras?.getString("currentUser") ?: ""
    )
  }

  // Send updated balance back to the previous activity
  private fun sendUpdatedBalanceBack(updatedBalance: String) {
    val resultIntent = Intent()
    resultIntent.putExtra("updatedBalance", updatedBalance)
    setResult(Activity.RESULT_OK, resultIntent)
    finish() // Finish and go back to the previous activity
  }

  // Helper function to show toast messages
  private fun showToast(text: String) {
    Toast.makeText(this@TransferActivity, text, Toast.LENGTH_LONG).show()
  }
}
