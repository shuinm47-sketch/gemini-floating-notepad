package com.gemini.floatingnotepad
    
    import android.content.Intent
    import android.net.Uri
    import android.os.Build
    import android.os.Bundle
    import android.provider.Settings
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    
    class MainActivity : AppCompatActivity() {
        private val DRAW_OVER_OTHER_APPS_PERMISSION_REQUEST_CODE = 1234
    
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            
            // This app primarily runs as a service. 
            // We check for permissions here and start the service.
            if (checkPermission()) {
                startFloatingService()
            } else {
                requestPermission()
            }
        }
    
        private fun startFloatingService() {
            startService(Intent(this, FloatingWidgetService::class.java))
            finish()
        }
    
        private fun checkPermission(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(this)
            } else {
                true
            }
        }
    
        private fun requestPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, DRAW_OVER_OTHER_APPS_PERMISSION_REQUEST_CODE)
            }
        }
    
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == DRAW_OVER_OTHER_APPS_PERMISSION_REQUEST_CODE) {
                if (checkPermission()) {
                    startFloatingService()
                } else {
                    Toast.makeText(this, "Permission denied! Need 'Draw over other apps' for the floating notepad.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
