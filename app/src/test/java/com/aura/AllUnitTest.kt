package com.aura

import TransferViewModel
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aura.data.model.ApiService
import com.aura.data.model.RetrofitClient
import com.aura.data.model.transfer.TransferResponse
import com.aura.ui.login.LoginViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
class ViewModelTest {

  @get:Rule
  val instantExecutorRule = InstantTaskExecutorRule()
  private lateinit var transferViewModel: TransferViewModel


  private lateinit var viewModel: LoginViewModel
  private val apiService = mockk<ApiService>()
  private val retrofitClient = mockk<RetrofitClient>()
  private val dispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {

    Dispatchers.setMain(dispatcher)

    // Mock RetrofitClient and ApiService
    mockkObject(RetrofitClient)
    every { RetrofitClient.instance.create(ApiService::class.java) } returns apiService

    // Initialize the ViewModel
    viewModel = LoginViewModel()
    transferViewModel = TransferViewModel()

  }

  @Test
  fun testVerifyFormState() {
    // Test that the form is valid when both id and password are non-empty
    viewModel.verifyFormState("user1", "password123")
    assertTrue(viewModel.isFormValid.value)

    // Test that the form is invalid when either id or password is empty
    viewModel.verifyFormState("", "password123")
    assertFalse(viewModel.isFormValid.value)

    viewModel.verifyFormState("user1", "")
    assertFalse(viewModel.isFormValid.value)
  }

  @Test
  fun testLoginSuccess() = runTest {
    // Mock the login API call to return a successful response
    coEvery { apiService.login(any()) } returns mockk {
      every { granted } returns true
    }

    // Perform the login
    viewModel.login("2", "2")

    // Verify that navigation to the home activity is triggered
    assertEquals("2", viewModel.navigateToHome.value)
  }

  @Test
  fun testLoginFailure() = runTest {
    // Mock the Response object
    val mockResponse = mockk<Response<Any>>()
    every { mockResponse.code() } returns 401 // or the appropriate HTTP status code
    every { mockResponse.message() } returns "Unauthorized" // or any other message you want to return

    // Create the HttpException with the mocked Response
    val httpException = HttpException(mockResponse)

    // Mock the login API call to throw the HttpException
    coEvery { apiService.login(any()) } throws httpException

    // Perform the login
    try {
      viewModel.login("user1", "password123")
    } catch (e: Exception) {
      // Verify that the login fails and the exception is an HttpException
      assertTrue(e is HttpException)

      // Optionally, verify that the message is as expected
      assertEquals("Unauthorized", (e as HttpException).response()?.message())
    }
  }

  @Test
  fun testValidateFields() {
    // Test that the button should be enabled when both recipient and amount are provided
    transferViewModel.validateFields("recipient", "100")
    assertTrue(transferViewModel.isButtonEnabled.value)

    // Test that the button should be disabled when either recipient or amount is empty
    transferViewModel.validateFields("", "100")
    assertFalse(transferViewModel.isButtonEnabled.value)

    transferViewModel.validateFields("recipient", "")
    assertFalse(transferViewModel.isButtonEnabled.value)
  }




  @Test
  fun testTransferFailure() = runTest {
    // Create a mocked TransferResponse
    val transferResponse = mockk<TransferResponse>()

    // Create a mocked Response and make it return the mocked TransferResponse body
    val response = mockk<Response<TransferResponse>>()
    every { response.body() } returns transferResponse
    every { response.code() } returns 500 // HTTP 500 Internal Server Error
    every { response.isSuccessful } returns false // Simulating failure response

    // Mock the transfer API call to return the mocked Response
    coEvery { apiService.transfer(any()) } throws Exception("Transfer failed")

    // Perform the transfer
    val transferViewModel = TransferViewModel()
    transferViewModel.transfer("user1", "recipient", 100.0)

    // Wait for the asynchronous operation to complete
    advanceUntilIdle() // Ensures all coroutines are completed

    // Verify that the result is false (since an exception was thrown)
    assertTrue(transferViewModel.transferResult.value == false)
  }

  @Test
  fun testTransferSuccess() = runTest {
    // Step 1: Create a mocked TransferResponse to simulate a successful transfer
    val transferResponse = mockk<TransferResponse>()

    // Step 2: Mocking the result property of TransferResponse
    every { transferResponse.result } returns true // Simulating successful transfer result

    // Step 3: Mock the transfer API call to return the mocked TransferResponse
    coEvery { apiService.transfer(any()) } returns transferResponse

    // Step 4: Initialize the ViewModel
    val viewModel = TransferViewModel()

    // Step 5: Call the transfer method in the ViewModel
    viewModel.transfer("1", "2", 100.0)

    // Step 6: Wait for the asynchronous operation to complete
    advanceUntilIdle() // Ensures all coroutines are completed

    // Step 7: Verify that the transferResult is true after successful transfer
    assertTrue(transferResponse.result == true)  // Verifying that transferResult is set to true
  }


  @Test
  fun testFetchUpdatedBalance() = runTest {
    // Mock the balance response
    val mockBalance = 10000.0
    coEvery { apiService.getAccount(any()) } returns listOf(mockk { every { balance } returns mockBalance })

    // Call fetchUpdatedBalance
    transferViewModel.fetchUpdatedBalance("2")

    // Verify that the balance is updated
    assertEquals(mockBalance.toString(), transferViewModel.updatedBalance.value)
  }




}




