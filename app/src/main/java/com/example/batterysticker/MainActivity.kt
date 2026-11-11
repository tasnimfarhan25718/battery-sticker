package com.example.batterysticker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.batterysticker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: StickerPreferences

    private var batteryReceiver: BroadcastReceiver? = null

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkOverlayPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = StickerPreferences(this)

        setupUI()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        checkOverlayPermission()
        registerBatteryMonitor()
    }

    override fun onPause() {
        super.onPause()
        batteryReceiver?.let {
            unregisterReceiver(it)
            batteryReceiver = null
        }
    }

    private fun checkOverlayPermission(): Boolean {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }

        binding.cardPermission.visibility = if (hasPermission) View.GONE else View.VISIBLE
        binding.switchEnable.isEnabled = hasPermission

        if (!hasPermission && prefs.isEnabled) {
            stopStickerService()
            prefs.isEnabled = false
            binding.switchEnable.isChecked = false
        }

        return hasPermission
    }

    private fun setupUI() {
        // Load initial switch state
        binding.switchEnable.isChecked = prefs.isEnabled && checkOverlayPermission()

        // Sliders
        binding.sliderX.value = prefs.offsetX.toFloat()
        binding.tvLabelX.text = "Horizontal Offset (X): ${prefs.offsetX} dp"

        binding.sliderY.value = prefs.offsetY.toFloat()
        binding.tvLabelY.text = "Vertical Offset (Y): ${prefs.offsetY} dp"

        binding.sliderSize.value = prefs.sizeDp.toFloat()
        binding.tvLabelSize.text = "Sticker Size: ${prefs.sizeDp} dp"

        updateCharacterSelectionUI()
    }

    private fun setupListeners() {
        // Grant Permission Button
        binding.btnGrantPermission.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                overlayPermissionLauncher.launch(intent)
            }
        }

        // Enable / Disable Switch
        binding.switchEnable.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (checkOverlayPermission()) {
                    prefs.isEnabled = true
                    startStickerService()
                } else {
                    binding.switchEnable.isChecked = false
                    Toast.makeText(this, "Please grant overlay permission first", Toast.LENGTH_SHORT).show()
                }
            } else {
                prefs.isEnabled = false
                stopStickerService()
            }
        }

        // Character Selectors
        binding.btnCharOggy.setOnClickListener {
            selectCharacter(StickerPreferences.CHAR_OGGY)
        }
        binding.btnCharFrog.setOnClickListener {
            selectCharacter(StickerPreferences.CHAR_FROG)
        }
        binding.btnCharBear.setOnClickListener {
            selectCharacter(StickerPreferences.CHAR_BEAR)
        }

        // Sliders
        binding.sliderX.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val intVal = value.toInt()
                prefs.offsetX = intVal
                binding.tvLabelX.text = "Horizontal Offset (X): $intVal dp"
                notifyServiceUpdate()
            }
        }

        binding.sliderY.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val intVal = value.toInt()
                prefs.offsetY = intVal
                binding.tvLabelY.text = "Vertical Offset (Y): $intVal dp"
                notifyServiceUpdate()
            }
        }

        binding.sliderSize.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val intVal = value.toInt()
                prefs.sizeDp = intVal
                binding.tvLabelSize.text = "Sticker Size: $intVal dp"
                notifyServiceUpdate()
            }
        }
    }

    private fun selectCharacter(character: String) {
        prefs.character = character
        updateCharacterSelectionUI()
        notifyServiceUpdate()
    }

    private fun updateCharacterSelectionUI() {
        val selectedChar = prefs.character
        binding.btnCharOggy.setBackgroundResource(
            if (selectedChar == StickerPreferences.CHAR_OGGY) R.drawable.bg_char_selected else R.drawable.bg_char_unselected
        )
        binding.btnCharFrog.setBackgroundResource(
            if (selectedChar == StickerPreferences.CHAR_FROG) R.drawable.bg_char_selected else R.drawable.bg_char_unselected
        )
        binding.btnCharBear.setBackgroundResource(
            if (selectedChar == StickerPreferences.CHAR_BEAR) R.drawable.bg_char_selected else R.drawable.bg_char_unselected
        )
    }

    private fun startStickerService() {
        val intent = Intent(this, BatteryOverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopStickerService() {
        val intent = Intent(this, BatteryOverlayService::class.java)
        stopService(intent)
    }

    private fun notifyServiceUpdate() {
        if (prefs.isEnabled) {
            val intent = Intent(this, BatteryOverlayService::class.java).apply {
                action = BatteryOverlayService.ACTION_UPDATE
            }
            startService(intent)
        }
    }

    private fun registerBatteryMonitor() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                    val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 0
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL

                    val statusText = if (isCharging) "Battery: $batteryPct% ⚡ (Charging)" else "Battery: $batteryPct%"
                    binding.tvBatteryStatus.text = statusText
                }
            }
        }
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }
}
