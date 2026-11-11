package com.example.batterysticker

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat

class BatteryOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private lateinit var prefs: StickerPreferences
    private var batteryReceiver: BroadcastReceiver? = null

    companion object {
        const val ACTION_UPDATE = "com.example.batterysticker.UPDATE"
        private const val CHANNEL_ID = "battery_sticker_overlay_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        prefs = StickerPreferences(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        startInForeground()
        initOverlay()
        registerBatteryReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_UPDATE) {
            updateOverlayLayout()
        }
        return START_STICKY
    }

    private fun startInForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Shows when the battery pet overlay is active"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_pet_oggy_cat)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    @SuppressLint("InflateParams")
    private fun initOverlay() {
        if (overlayView != null) return

        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.overlay_battery_sticker, null)

        val sizePx = dpToPx(prefs.sizeDp)
        val xPx = dpToPx(prefs.offsetX)
        val yPx = dpToPx(prefs.offsetY)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = xPx
            y = yPx
        }

        applyStickerImage()

        try {
            windowManager?.addView(overlayView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyStickerImage() {
        val ivSticker = overlayView?.findViewById<ImageView>(R.id.ivSticker) ?: return
        val resId = when (prefs.character) {
            StickerPreferences.CHAR_FROG -> R.drawable.ic_pet_frog
            StickerPreferences.CHAR_BEAR -> R.drawable.ic_pet_bear
            else -> R.drawable.ic_pet_oggy_cat
        }
        ivSticker.setImageResource(resId)
    }

    private fun updateOverlayLayout() {
        if (overlayView == null || layoutParams == null) return

        val sizePx = dpToPx(prefs.sizeDp)
        layoutParams?.width = sizePx
        layoutParams?.height = sizePx
        layoutParams?.x = dpToPx(prefs.offsetX)
        layoutParams?.y = dpToPx(prefs.offsetY)

        applyStickerImage()

        try {
            windowManager?.updateViewLayout(overlayView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun registerBatteryReceiver() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL

                    val ivBolt = overlayView?.findViewById<ImageView>(R.id.ivChargingBolt)
                    ivBolt?.visibility = if (isCharging) View.VISIBLE else View.GONE
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (batteryReceiver != null) {
            try {
                unregisterReceiver(batteryReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            batteryReceiver = null
        }
        if (overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
