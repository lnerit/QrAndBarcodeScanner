package com.example.barcodescanner.feature.tabs.settings.device

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.example.barcodescanner.R
import com.example.barcodescanner.extension.applySystemWindowInsets
import com.example.barcodescanner.feature.BaseActivity
import kotlinx.android.synthetic.main.activity_device_id.root_view
import kotlinx.android.synthetic.main.activity_device_id.toolbar

class DeviceIdActivity:BaseActivity() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, DeviceIdActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_id)
        // Assigning values to view elements
        val nameTextView = findViewById<TextView>(R.id.device_id_text)
        nameTextView.text = DeviceIdProvider.getDeviceId().uppercase()
        root_view.applySystemWindowInsets(applyTop = true, applyBottom = true)
        toolbar.setNavigationOnClickListener { finish() }
    }
}