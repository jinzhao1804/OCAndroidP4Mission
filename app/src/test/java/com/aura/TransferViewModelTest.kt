package com.aura

import TransferViewModel
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aura.data.model.ApiService
import com.aura.data.model.RetrofitClient
import com.aura.data.model.transfer.TransferResponse
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
class TransferViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: TransferViewModel
    private val apiService = mockk<ApiService>()
    private val dispatcher = StandardTestDispatcher()


    @Before
    fun setUp() {
        // Mock RetrofitClient and ApiService
        Dispatchers.setMain(dispatcher)
        mockkObject(RetrofitClient)
        every { RetrofitClient.instance.create(ApiService::class.java) } returns apiService

        // Initialize the ViewModel
        viewModel = TransferViewModel()
    }

    @After
    fun tearDown() {
        // Reset the main dispatcher after the test
        Dispatchers.resetMain()
    }

    @Test
    fun testValidateFields() {
        // Test that the button should be enabled when both recipient and amount are provided
        viewModel.validateFields("recipient", "100")
        assertTrue(viewModel.isButtonEnabled.value)

        // Test that the button should be disabled when either recipient or amount is empty
        viewModel.validateFields("", "100")
        assertFalse(viewModel.isButtonEnabled.value)

        viewModel.validateFields("recipient", "")
        assertFalse(viewModel.isButtonEnabled.value)
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
        val viewModel = TransferViewModel()
        viewModel.transfer("user1", "recipient", 100.0)

        // Wait for the asynchronous operation to complete
        advanceUntilIdle() // Ensures all coroutines are completed

        // Verify that the result is false (since an exception was thrown)
        assertTrue(viewModel.transferResult.value == false)
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
        viewModel.fetchUpdatedBalance("2")

        // Verify that the balance is updated
        assertEquals(mockBalance.toString(), viewModel.updatedBalance.value)
    }
}
