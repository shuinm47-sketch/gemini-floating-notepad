package com.gemini.floatingnotepad

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("NotepadPrefs", Context.MODE_PRIVATE)
        
        val switchAnchor = findViewById<Switch>(R.id.switch_anchor)
        val seekOpacity = findViewById<SeekBar>(R.id.seek_opacity)
        val seekSize = findViewById<SeekBar>(R.id.seek_size)
        val btnSave = findViewById<Button>(R.id.btn_save_settings)

        // Load saved values
        switchAnchor.isChecked = prefs.getBoolean("is_anchored", false)
        seekOpacity.progress = prefs.getInt("opacity", 100)
        seekSize.progress = prefs.getInt("size", 100)

        btnSave.setOnClickListener {
            prefs.edit().apply {
                putBoolean("is_anchored", switchAnchor.isChecked)
                putInt("opacity", seekOpacity.progress)
                putInt("size", seekSize.progress)
                apply()
            }
            Toast.makeText(this, "Settings Saved!", Toast.LENGTH_SHORT).show()
            finish() // Go back to main screen
        }
    }
}
