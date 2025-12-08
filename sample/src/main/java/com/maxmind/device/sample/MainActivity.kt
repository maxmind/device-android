package com.maxmind.device.sample

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.maxmind.device.DeviceTracker
import com.maxmind.device.config.SdkConfig
import com.maxmind.device.sample.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.full.memberProperties

/**
 * Main activity demonstrating the MaxMind Device Tracker usage.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var logText = StringBuilder()
    private val json = Json { prettyPrint = true }

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

        binding.btnCollect.setOnClickListener {
            collectDeviceData()
        }

        binding.btnSend.setOnClickListener {
            sendDeviceData()
        }

        binding.btnClearLog.setOnClickListener {
            clearLog()
        }

        // Disable collect and send buttons until SDK is initialized
        binding.btnCollect.isEnabled = false
        binding.btnSend.isEnabled = false
    }

    private fun initializeSdk() {
        try {
            if (DeviceTracker.isInitialized()) {
                appendLog("⚠️ SDK already initialized")
                showMessage("SDK already initialized")
                return
            }

            // Create SDK configuration
            // Note: Replace with your actual MaxMind account ID
            val config = SdkConfig.Builder(123456)  // Demo account ID - replace with real one
                .enableLogging(true)
                .build()

            // Initialize SDK
            DeviceTracker.initialize(this, config)

            appendLog("✓ SDK initialized successfully")
            binding.btnCollect.isEnabled = true
            binding.btnSend.isEnabled = true
            showMessage("SDK initialized successfully")
        } catch (e: Exception) {
            val errorMsg = "Failed to initialize SDK: ${e.message}"
            appendLog("✗ $errorMsg")
            Log.e(TAG, errorMsg, e)
            showMessage(errorMsg)
        }
    }

    private fun collectDeviceData() {
        try {
            val sdk = DeviceTracker.getInstance()
            val deviceData = sdk.collectDeviceData()

            appendLog("📱 Device Data Collected:")
            appendLog("  Manufacturer: ${deviceData.build.manufacturer}")
            appendLog("  Model: ${deviceData.build.model}")
            appendLog("  Brand: ${deviceData.build.brand}")
            appendLog("  OS Version: ${deviceData.build.osVersion}")
            appendLog("  SDK Version: ${deviceData.build.sdkVersion}")
            appendLog("  Screen: ${deviceData.display.widthPixels}x${deviceData.display.heightPixels} (${deviceData.display.densityDpi}dpi)")
            appendLog("  Timestamp: ${deviceData.deviceTime}")
            appendLog("")

            // Dynamically add collapsible sections for each property
            deviceData::class.memberProperties.forEach { prop ->
                val value = prop.getter.call(deviceData)
                if (value != null) {
                    val content = try {
                        json.encodeToString(serializer(prop.returnType), value)
                    } catch (e: Exception) {
                        value.toString()
                    }
                    addCollapsibleSection(prop.name, content)
                }
            }

            // Scroll to top to show summary first
            binding.scrollView.post {
                binding.scrollView.fullScroll(android.view.View.FOCUS_UP)
            }

            showMessage("Device data collected")
        } catch (e: Exception) {
            val errorMsg = "Failed to collect data: ${e.message}"
            appendLog("✗ $errorMsg")
            Log.e(TAG, errorMsg, e)
            showMessage(errorMsg)
        }
    }

    private fun addCollapsibleSection(title: String, content: String) {
        val header = TextView(this).apply {
            text = "▶ $title"
            setTypeface(typeface, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(0, dpToPx(12), 0, dpToPx(4))
            setTextColor(getColor(R.color.section_header))
        }

        val contentView = TextView(this).apply {
            text = content
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            typeface = Typeface.MONOSPACE
            setPadding(dpToPx(16), dpToPx(4), 0, dpToPx(12))
            setTextColor(getColor(R.color.section_content))
            setBackgroundColor(getColor(R.color.surface))
            visibility = View.GONE
        }

        header.setOnClickListener {
            if (contentView.visibility == View.GONE) {
                contentView.visibility = View.VISIBLE
                header.text = "▼ $title"
            } else {
                contentView.visibility = View.GONE
                header.text = "▶ $title"
            }
        }

        binding.logContainer.addView(header)
        binding.logContainer.addView(contentView)
    }

    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()

    private fun sendDeviceData() {
        try {
            val sdk = DeviceTracker.getInstance()

            appendLog("📤 Sending device data...")

            lifecycleScope.launch {
                sdk.collectAndSend().fold(
                    onSuccess = {
                        appendLog("✓ Data sent successfully!")
                        showMessage("Data sent successfully")
                    },
                    onFailure = { error ->
                        val errorMsg = "Failed to send data: ${error.message}"
                        appendLog("✗ $errorMsg")
                        Log.e(TAG, errorMsg, error)
                        showMessage(errorMsg)
                    }
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
        // Remove all views except the tvLog TextView
        val childCount = binding.logContainer.childCount
        if (childCount > 1) {
            binding.logContainer.removeViews(1, childCount - 1)
        }
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
