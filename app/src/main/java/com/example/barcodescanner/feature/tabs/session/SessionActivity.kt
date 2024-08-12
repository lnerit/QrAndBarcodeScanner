package com.example.barcodescanner.feature.tabs.session

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.barcodescanner.R
import com.example.barcodescanner.extension.applySystemWindowInsets
import com.example.barcodescanner.feature.BaseActivity
import com.example.barcodescanner.feature.tabs.settings.device.DeviceIdActivity
import com.example.barcodescanner.feature.tabs.settings.device.DeviceIdProvider
import kotlinx.android.synthetic.main.activity_device_id.root_view
import kotlinx.android.synthetic.main.activity_device_id.toolbar

class SessionActivity : BaseActivity() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SessionFragment::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)
       /* val button1: Button = findViewById(R.id.buttonOk)
        button1.setOnClickListener {
            // Action to perform when the button is clicked
            // For example, navigate to the home screen or show a message
        }*/
        // Assigning values to view elements
        //val nameTextView = findViewById<TextView>(R.id.session_text)
       // nameTextView.text = DeviceIdProvider.getDeviceId().uppercase()
        root_view.applySystemWindowInsets(applyTop = true, applyBottom = true)
        toolbar.setNavigationOnClickListener { finish() }
    }

}