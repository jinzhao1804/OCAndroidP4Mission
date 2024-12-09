package com.aura.ui.transfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.data.model.ApiService
import com.aura.data.model.RetrofitClient
import com.aura.data.model.transfer.TransferRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class TransferViewModel : ViewModel() {

    private val _transferResult = MutableLiveData<Boolean>()
    val transferResult: LiveData<Boolean> get() = _transferResult

    private val _updatedBalance = MutableLiveData<String>()
    val updatedBalance: LiveData<String> get() = _updatedBalance

    private val _isButtonEnabled = MutableLiveData<Boolean>()
    val isButtonEnabled: LiveData<Boolean> get() = _isButtonEnabled

    fun validateFields(recipient: String, amount: String) {
        _isButtonEnabled.value = recipient.isNotEmpty() && amount.isNotEmpty()
    }

    fun transfer(currentUser: String, recipient: String, amount: Double) {
        viewModelScope.launch {
            try {
                val transferRequest = TransferRequest(currentUser, recipient, amount)

                val response = RetrofitClient.instance.create(ApiService::class.java).transfer(transferRequest)

                if (response.result) {
                    _transferResult.value = true
                    fetchUpdatedBalance(currentUser) // Fetch balance after the transfer
                } else {
                    _transferResult.value = false
                }

            } catch (e: HttpException) {
                _transferResult.value = false
            } catch (e: Exception) {
                _transferResult.value = false
            }
        }
    }

    private suspend fun fetchUpdatedBalance(currentUser: String) {
        try {
            val accountResponse = RetrofitClient.instance.create(ApiService::class.java).getAccount(currentUser)
            val updatedBalance = accountResponse[0].balance
            _updatedBalance.value = updatedBalance.toString() // Store the updated balance
        } catch (e: Exception) {
            _updatedBalance.value = "Error fetching updated balance."
        }
    }
}
