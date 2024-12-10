package com.aura

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aura.data.model.ApiService
import com.aura.data.model.RetrofitClient
import com.aura.data.model.login.LoginRequest
import com.aura.ui.login.LoginViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: LoginViewModel
    private val apiService = mockk<ApiService>()
    private val retrofitClient = mockk<RetrofitClient>()

    @Before
    fun setUp() {
        // Mock RetrofitClient and ApiService
        mockkObject(RetrofitClient)
        every { RetrofitClient.instance.create(ApiService::class.java) } returns apiService

        // Initialize the ViewModel
        viewModel = LoginViewModel()
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

}
