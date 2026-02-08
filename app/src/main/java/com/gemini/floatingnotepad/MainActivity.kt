package com.gemini.floatingnotepad

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find our new buttons
        val btnStart = findViewById<Button>(R.id.btn_start_service)
        val btnStop = findViewById<Button>(R.id.btn_stop_service)
        val btnSettings = findViewById<Button>(R.id.btn_settings)

        // 1. Start Service Logic
        btnStart.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                // If we don't have permission, ask for it
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, 0)
            } else {
                // If we have permission, start the floating widget
                startService(Intent(this, FloatingWidgetService::class.java))
                // Minimize the app so the user sees the widget
                moveTaskToBack(true)
            }
        }

        // 2. Stop Service Logic
        btnStop.setOnClickListener {
            stopService(Intent(this, FloatingWidgetService::class.java))
            Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show()
        }

        // 3. Settings Logic (Placeholder for now)
        btnSettings.setOnClickListener {
            Toast.makeText(this, "Settings coming in Phase 2!", Toast.LENGTH_SHORT).show()
            // We will create the SettingsActivity next!
        }
    }
}
