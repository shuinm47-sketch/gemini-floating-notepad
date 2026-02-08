package com.gemini.floatingnotepad

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class FloatingWidgetService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        val inflater = LayoutInflater.from(this)
        floatingView = inflater.inflate(R.layout.layout_floating_widget, null)

        // Load Settings
        val prefs = getSharedPreferences("NotepadPrefs", Context.MODE_PRIVATE)
        val isAnchored = prefs.getBoolean("is_anchored", false)
        val opacity = prefs.getInt("opacity", 100) / 100f
        val sizeMultiplier = prefs.getInt("size", 100) / 100f

        // Apply Settings to View
        floatingView.alpha = opacity
        
        val params = WindowManager.LayoutParams(
            (250 * resources.displayMetrics.density * sizeMultiplier).toInt(),
            (200 * resources.displayMetrics.density * sizeMultiplier).toInt(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 100

        // Handle Dragging (with Anchor check and Delta calculation for smoothness)
        floatingView.findViewById<View>(R.id.header_layout).setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (isAnchored) return false // Prevent dragging if locked

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })

        // Settings Button (Gear) - Opens the app settings
        floatingView.findViewById<Button>(R.id.btn_widget_settings).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        // Editable Title Logic
        val titleView = floatingView.findViewById<TextView>(R.id.note_title)
        titleView.setOnClickListener {
            Toast.makeText(this, "Title editing coming in the next update!", Toast.LENGTH_SHORT).show()
        }

        windowManager.addView(floatingView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }
}
