package com.maxmind.device.sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.maxmind.device.DeviceTracker
import com.maxmind.device.config.SdkConfig
import com.maxmind.device.sample.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

/**
 * Main activity demonstrating the MaxMind Device Tracker usage.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var logText = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        appendLog("App started. SDK not initialized.")
    }

    private fun setupViews() {
        binding.btnInitialize.setOnClickListener {
            initializeSdk()
        }

        binding.btnSend.setOnClickListener {
            sendDeviceData()
        }

        binding.btnClearLog.setOnClickListener {
            clearLog()
        }

        // Disable send button until SDK is initialized
        binding.btnSend.isEnabled = false
    }

    @Suppress("TooGenericExceptionCaught")
    private fun initializeSdk() {
        try {
            if (DeviceTracker.isInitialized()) {
                appendLog("⚠️ SDK already initialized")
                showMessage("SDK already initialized")
                return
            }

            // Check that account ID is configured
            if (BuildConfig.MAXMIND_ACCOUNT_ID == 0) {
                val errorMsg = "Account ID not configured. Add maxmind.account.id to local.properties"
                appendLog("✗ $errorMsg")
                showMessage(errorMsg)
                return
            }

            // Create SDK configuration
            val configBuilder =
                SdkConfig
                    .Builder(BuildConfig.MAXMIND_ACCOUNT_ID)
                    .enableLogging(true)

            // Use debug server URL if configured in local.properties
            if (BuildConfig.DEBUG_SERVER_URL.isNotEmpty()) {
                configBuilder.serverUrl(BuildConfig.DEBUG_SERVER_URL)
            }

            val config = configBuilder.build()

            // Initialize SDK
            DeviceTracker.initialize(this, config)

            appendLog("✓ SDK initialized successfully")
            binding.btnSend.isEnabled = true
            showMessage("SDK initialized successfully")
        } catch (e: Exception) {
            val errorMsg = "Failed to initialize SDK: ${e.message}"
            appendLog("✗ $errorMsg")
            Log.e(TAG, errorMsg, e)
            showMessage(errorMsg)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun sendDeviceData() {
        try {
            val sdk = DeviceTracker.getInstance()

            appendLog("📤 Sending device data...")

            lifecycleScope.launch {
                sdk.collectAndSend().fold(
                    onSuccess = { response ->
                        appendLog("✓ Data sent! Tracking token: ${response.trackingToken}")
                        showMessage("Data sent successfully")
                    },
                    onFailure = { error ->
                        val errorMsg = "Failed to send data: ${error.message}"
                        appendLog("✗ $errorMsg")
                        Log.e(TAG, errorMsg, error)
                        showMessage(errorMsg)
                    },
                )
            }
        } catch (e: Exception) {
            val errorMsg = "Error: ${e.message}"
            appendLog("✗ $errorMsg")
            Log.e(TAG, errorMsg, e)
            showMessage(errorMsg)
        }
    }

    private fun appendLog(message: String) {
        logText.append(message).append("\n")
        binding.tvLog.text = logText.toString()

        // Scroll to bottom
        binding.scrollView.post {
            binding.scrollView.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }

    private fun clearLog() {
        logText.clear()
        binding.tvLog.text = ""
        appendLog("Log cleared.")
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Optionally shut down SDK when activity is destroyed
        // In a real app, you might want to do this in Application.onTerminate()
        // Example: if (DeviceTracker.isInitialized()) { DeviceTracker.getInstance().shutdown() }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
