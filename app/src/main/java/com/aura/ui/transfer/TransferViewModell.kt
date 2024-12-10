import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.data.model.ApiService
import com.aura.data.model.RetrofitClient
import com.aura.data.model.transfer.TransferRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class TransferViewModel : ViewModel() {

    private val _transferResult = MutableStateFlow<Boolean?>(null)
    val transferResult: StateFlow<Boolean?> get() = _transferResult

    private val _updatedBalance = MutableStateFlow<String?>(null)
    val updatedBalance: StateFlow<String?> get() = _updatedBalance

    private val _isButtonEnabled = MutableStateFlow(false)
    val isButtonEnabled: StateFlow<Boolean> get() = _isButtonEnabled

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun validateFields(recipient: String, amount: String) {
        _isButtonEnabled.value = recipient.isNotEmpty() && amount.isNotEmpty()
    }

    fun transfer(currentUser: String, recipient: String, amount: Double) {
        viewModelScope.launch {
            _isLoading.value = true // Start loading

            try {
                val transferRequest = TransferRequest(currentUser, recipient, amount)
                val response = RetrofitClient.instance.create(ApiService::class.java).transfer(transferRequest)

                if (response.result) {
                    _transferResult.value = true
                    Log.d("TransferViewModelTest", "Transfer Result: ${transferResult.value}")

                } else {
                    _transferResult.value = false
                    Log.d("TransferViewModelTest", "Transfer Result: ${transferResult.value}")

                }

            } catch (e: HttpException) {
                _transferResult.value = false
            } catch (e: Exception) {
                _transferResult.value = false
            } finally {
                _isLoading.value = false // Stop loading
            }
        }
    }

    suspend fun fetchUpdatedBalance(currentUser: String) {
        try {
            val accountResponse = RetrofitClient.instance.create(ApiService::class.java).getAccount(currentUser)
            val updatedBalance = accountResponse[0].balance
            _updatedBalance.value = updatedBalance.toString() // Store the updated balance
        } catch (e: Exception) {
            _updatedBalance.value = "Error fetching updated balance."
        }
    }
}
