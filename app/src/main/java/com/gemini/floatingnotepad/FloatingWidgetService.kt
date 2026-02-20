package com.gemini.floatingnotepad
    
    import android.app.Service
    import android.content.ClipboardManager
    import android.content.Context
    import android.content.Intent
    import android.graphics.PixelFormat
    import android.os.IBinder
    import android.view.*
    import android.widget.*
    import java.io.File
    
    class FloatingWidgetService : Service() {
    
        private lateinit var windowManager: WindowManager
        private lateinit var floatingView: View
        
        private lateinit var layoutNote: View
        private lateinit var layoutMenu: View
        private lateinit var etContent: EditText
        private lateinit var tvTitle: TextView
        
        private var currentNoteFile: File? = null
        private val notesDir by lazy { File(getExternalFilesDir(null), "GeminiNotepad") }
        private val trashDir by lazy { File(notesDir, "Trash") }
    
        override fun onBind(intent: Intent?): IBinder? = null
    
        override fun onCreate() {
            super.onCreate()
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            
            if (!notesDir.exists()) notesDir.mkdirs()
            if (!trashDir.exists()) trashDir.mkdirs()
            
            if (notesDir.listFiles { f -> f.isFile }?.isEmpty() == true) {
                File(notesDir, "Note 1.txt").writeText("")
            }
            currentNoteFile = notesDir.listFiles { f -> f.isFile }?.firstOrNull()
    
            floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null)
            layoutNote = floatingView.findViewById(R.id.view_note_interface)
            layoutMenu = floatingView.findViewById(R.id.view_menu_overlay)
            etContent = floatingView.findViewById(R.id.et_note_content)
            tvTitle = floatingView.findViewById(R.id.tv_note_title)
    
            loadCurrentNote()
    
            floatingView.findViewById<View>(R.id.btn_menu_open).setOnClickListener {
                saveCurrentNote()
                layoutMenu.visibility = View.VISIBLE
                layoutNote.visibility = View.GONE
            }
    
            floatingView.findViewById<View>(R.id.btn_menu_close).setOnClickListener {
                layoutMenu.visibility = View.GONE
                layoutNote.visibility = View.VISIBLE
            }
    
            floatingView.findViewById<View>(R.id.btn_action_copy).setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = android.content.ClipData.newPlainText("Note", etContent.text.toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show()
            }
    
            floatingView.findViewById<View>(R.id.btn_action_delete).setOnClickListener {
                currentNoteFile?.let { file ->
                    val trashFile = File(trashDir, file.name)
                    file.renameTo(trashFile)
                    Toast.makeText(this, "Moved to Trash", Toast.LENGTH_SHORT).show()
                    
                    currentNoteFile = notesDir.listFiles { f -> f.isFile }?.firstOrNull()
                    if (currentNoteFile == null) {
                        currentNoteFile = File(notesDir, "New Note.txt")
                        currentNoteFile?.writeText("")
                    }
                    loadCurrentNote()
                    
                    layoutMenu.visibility = View.GONE
                    layoutNote.visibility = View.VISIBLE
                }
            }
    
            setupDragging()
    
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.TOP or Gravity.START
            params.x = 100
            params.y = 100
            
            windowManager.addView(floatingView, params)
        }
    
        private fun loadCurrentNote() {
            currentNoteFile?.let {
                tvTitle.text = it.nameWithoutExtension
                etContent.setText(it.readText())
            }
        }
    
        private fun saveCurrentNote() {
            currentNoteFile?.writeText(etContent.text.toString())
        }
    
        private fun setupDragging() {
            val header = floatingView.findViewById<View>(R.id.header_layout)
            val params = floatingView.layoutParams as WindowManager.LayoutParams
            
            header.setOnTouchListener(object : View.OnTouchListener {
                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f
    
                override fun onTouch(v: View, event: MotionEvent): Boolean {
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
        }
    
        override fun onDestroy() {
            saveCurrentNote()
            if (::floatingView.isInitialized) windowManager.removeView(floatingView)
            super.onDestroy()
        }
    }
    
