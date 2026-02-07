package com.gemini.floatingnotepad
    
    import android.app.Service
    import android.content.Context
    import android.content.Intent
    import android.graphics.PixelFormat
    import android.os.Build
    import android.os.IBinder
    import android.text.Editable
    import android.text.TextWatcher
    import android.view.*
    import android.widget.*
    import java.util.*
    
    class FloatingWidgetService : Service() {
    
        private lateinit var windowManager: WindowManager
        private lateinit var floatingView: View
        private lateinit var collapsedView: View
        private lateinit var expandedView: View
        private lateinit var editText: EditText
        private lateinit var noteTitle: TextView
        
        private var currentNoteIndex = 0
        private val notes = mutableListOf<String>()
        private val prefs by lazy { getSharedPreferences("notes_prefs", Context.MODE_PRIVATE) }
    
        override fun onBind(intent: Intent?): IBinder? = null
    
        override fun onCreate() {
            super.onCreate()
            loadNotes()
    
            floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null)
    
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
    
            params.gravity = Gravity.TOP or Gravity.START
            params.x = 0
            params.y = 100
    
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.addView(floatingView, params)
    
            collapsedView = floatingView.findViewById(R.id.collapsed_view)
            expandedView = floatingView.findViewById(R.id.expanded_view)
            editText = floatingView.findViewById(R.id.note_edit_text)
            noteTitle = floatingView.findViewById(R.id.note_title)
    
            floatingView.findViewById<Button>(R.id.close_btn).setOnClickListener { stopSelf() }
    
            collapsedView.setOnClickListener {
                collapsedView.visibility = View.GONE
                expandedView.visibility = View.VISIBLE
                updateParams(true)
            }
    
            floatingView.findViewById<Button>(R.id.minimize_btn).setOnClickListener {
                collapsedView.visibility = View.VISIBLE
                expandedView.visibility = View.GONE
                updateParams(false)
            }
    
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (currentNoteIndex < notes.size) {
                        notes[currentNoteIndex] = s.toString()
                        saveNotes()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
    
            floatingView.findViewById<Button>(R.id.prev_note).setOnClickListener {
                if (currentNoteIndex > 0) {
                    currentNoteIndex--
                    displayNote()
                }
            }
    
            floatingView.findViewById<Button>(R.id.next_note).setOnClickListener {
                if (currentNoteIndex < notes.size - 1) {
                    currentNoteIndex++
                    displayNote()
                } else if (notes[currentNoteIndex].isNotEmpty()) {
                    notes.add("")
                    currentNoteIndex++
                    displayNote()
                }
            }
    
            floatingView.setOnTouchListener(object : View.OnTouchListener {
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
            displayNote()
        }
    
        private fun updateParams(focusable: Boolean) {
            val params = floatingView.layoutParams as WindowManager.LayoutParams
            if (focusable) params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
            else params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowManager.updateViewLayout(floatingView, params)
        }
    
        private fun displayNote() {
            noteTitle.text = "Note ${currentNoteIndex + 1}"
            if (currentNoteIndex < notes.size) editText.setText(notes[currentNoteIndex])
        }
    
        private fun loadNotes() {
            val count = prefs.getInt("note_count", 1)
            for (i in 0 until count) notes.add(prefs.getString("note_$i", "") ?: "")
            if (notes.isEmpty()) notes.add("")
        }
    
        private fun saveNotes() {
            val editor = prefs.edit()
            editor.putInt("note_count", notes.size)
            for (i in notes.indices) editor.putString("note_$i", notes[i])
            editor.apply()
        }
    
        override fun onDestroy() {
            super.onDestroy()
            if (::floatingView.isInitialized) windowManager.removeView(floatingView)
        }
    }
    
